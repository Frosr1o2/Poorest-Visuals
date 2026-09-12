package com.poorest.client.discord;

import com.poorest.client.PoorestClient;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.net.UnixDomainSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Native Discord Rich Presence using Discord's local IPC protocol.
 *
 * This intentionally does not depend on jDRPC: NeoForge's development/runtime
 * classloader can otherwise leave the library classes outside the mod classpath.
 * It also works with Vesktop/arRPC when the Discord IPC socket is exposed.
 */
public final class DiscordPresence {
    private static final String CLIENT_ID = "1548104533933035601";
    private static final String LARGE_IMAGE = "poorest_logo";
    private static final long START_TIMESTAMP = System.currentTimeMillis();

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;

    private static volatile boolean started;
    private static volatile SocketChannel socket;
    private static volatile boolean connected;

    private DiscordPresence() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }

        started = true;
        Runtime.getRuntime().addShutdownHook(
                new Thread(DiscordPresence::shutdown, "Poorest-Discord-RPC-Shutdown")
        );

        Thread thread = new Thread(() -> {
            while (started) {
                try {
                    connect();
                    update();
                    PoorestClient.LOGGER.info("Discord Rich Presence connected");

                    while (started && connected) {
                        Thread.sleep(5000L);
                        if (started && connected) {
                            update();
                        }
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable throwable) {
                    PoorestClient.LOGGER.debug(
                            "Discord Rich Presence unavailable; retrying",
                            throwable
                    );
                } finally {
                    disconnect();
                }

                if (!started) {
                    break;
                }

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Poorest-Discord-RPC");

        thread.setDaemon(true);
        thread.start();
    }

    private static void connect() throws IOException {
        disconnect();

        Path ipcPath = findIpcSocket();
        if (ipcPath == null) {
            throw new IOException("Discord IPC socket not found");
        }

        SocketChannel channel = SocketChannel.open(
                UnixDomainSocketAddress.of(ipcPath)
        );
        channel.configureBlocking(true);

        socket = channel;

        String handshake = "{\"v\":1,\"client_id\":\"" + CLIENT_ID + "\"}";
        sendFrame(channel, OP_HANDSHAKE, handshake);
        readFrame(channel);

        connected = true;
    }

    public static void update() {
        SocketChannel channel = socket;
        if (!connected || channel == null || !channel.isOpen()) {
            return;
        }

        try {
            long pid = ProcessHandle.current().pid();
            long startSeconds = START_TIMESTAMP / 1000L;
            String nonce = UUID.randomUUID().toString();

            String payload = "{"
                    + "\"cmd\":\"SET_ACTIVITY\","
                    + "\"args\":{"
                    + "\"pid\":" + pid + ","
                    + "\"activity\":{"
                    + "\"type\":0,"
                    + "\"details\":\"Minecraft 1.21.1\","
                    + "\"state\":\"Poorest Visuals\","
                    + "\"timestamps\":{"
                    + "\"start\":" + startSeconds
                    + "},"
                    + "\"assets\":{"
                    + "\"large_image\":\"" + LARGE_IMAGE + "\","
                    + "\"large_text\":\"Poorest Visuals\""
                    + "}"
                    + "}"
                    + "},"
                    + "\"nonce\":\"" + nonce + "\""
                    + "}";

            sendFrame(channel, OP_FRAME, payload);
            readFrame(channel);
        } catch (Throwable throwable) {
            connected = false;
            PoorestClient.LOGGER.debug(
                    "Failed to update Discord Rich Presence",
                    throwable
            );
        }
    }

    private static void sendFrame(
            SocketChannel channel,
            int opcode,
            String payload
    ) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length)
                .order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(opcode);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static String readFrame(SocketChannel channel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN);
        readFully(channel, header);
        header.flip();

        int opcode = header.getInt();
        int length = header.getInt();

        if (length < 0 || length > 4 * 1024 * 1024) {
            throw new IOException("Invalid Discord IPC frame length: " + length);
        }

        ByteBuffer body = ByteBuffer.allocate(length);
        readFully(channel, body);
        body.flip();

        byte[] bytes = new byte[length];
        body.get(bytes);

        if (opcode < 0) {
            throw new IOException("Invalid Discord IPC opcode");
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void readFully(SocketChannel channel, ByteBuffer buffer)
            throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read < 0) {
                throw new EOFException("Discord IPC closed the connection");
            }
        }
    }

    private static Path findIpcSocket() {
        String runtime = System.getenv("XDG_RUNTIME_DIR");
        if (runtime == null || runtime.isBlank()) {
            return null;
        }

        Path runtimeDir = Path.of(runtime);

        // Native Discord, Discord clients, and Vesktop with arRPC commonly use this.
        for (int i = 0; i < 10; i++) {
            Path socketPath = runtimeDir.resolve("discord-ipc-" + i);
            if (Files.exists(socketPath)) {
                return socketPath;
            }
        }

        // Vesktop Flatpak may expose the socket only inside its runtime directory.
        Path flatpakRuntime = runtimeDir
                .resolve(".flatpak")
                .resolve("dev.vencord.Vesktop")
                .resolve("xdg-run");

        for (int i = 0; i < 10; i++) {
            Path socketPath = flatpakRuntime.resolve("discord-ipc-" + i);
            if (Files.exists(socketPath)) {
                return socketPath;
            }
        }

        return null;
    }

    private static synchronized void disconnect() {
        connected = false;

        SocketChannel channel = socket;
        socket = null;

        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static synchronized void shutdown() {
        started = false;
        disconnect();
    }
}
