package com.poorest.client.ui.font;

import com.poorest.client.ui.render.GLRenderer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.system.MemoryUtil;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class PoorestFont {

    public static final class Glyph {

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int advance;

        private Glyph(
                int x,
                int y,
                int width,
                int height,
                int advance
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.advance = advance;
        }
    }

    private static final int ATLAS_SIZE = 2048;
    private static final int FONT_SIZE = 80;

    private static final Map<Character, Glyph> GLYPHS =
            new HashMap<>();

    private static int texture;

    private static boolean initialized;

    private static int ascent;

    private PoorestFont() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        Font font;
        try (InputStream input = PoorestFont.class.getResourceAsStream(
                "/assets/poorest_client/fonts/Inter-Regular.otf")) {
            if (input == null) {
                throw new IllegalStateException("Inter-Regular.otf is missing");
            }
            font = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(Font.PLAIN, (float) FONT_SIZE);
        } catch (Exception ignored) {
            // Keep the client usable if the bundled font cannot be loaded.
            font = new Font("SansSerif", Font.PLAIN, FONT_SIZE);
        }

        BufferedImage image =
                new BufferedImage(
                        ATLAS_SIZE,
                        ATLAS_SIZE,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D graphics =
                image.createGraphics();

        graphics.setFont(
                font
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        ascent =
                graphics
                        .getFontMetrics()
                        .getAscent();

        int x = 4;
        int y = 4;
        int rowHeight = 92;

        for (int code = 32;
             code <= 126;
             code++) {

            char character =
                    (char) code;

            x =
                    placeGlyph(
                            graphics,
                            image,
                            character,
                            x,
                            y,
                            rowHeight
                    ).x;

            y =
                    placeY;
        }

        /* Latin-1 and Latin Extended-A cover European translations. */
        for (int code = 0x00A0; code <= 0x017F; code++) {
            char character = (char) code;
            Placement placement = placeGlyph(graphics, image, character, x, y, rowHeight);
            x = placement.x;
            y = placement.y;
        }

        /*
         * Cyrillic block.
         */

        int startX = x;
        int startY = y;

        x = startX;
        y = startY;

        for (int code = 0x0400;
             code <= 0x04FF;
             code++) {

            char character =
                    (char) code;

            Placement placement =
                    placeGlyph(
                            graphics,
                            image,
                            character,
                            x,
                            y,
                            rowHeight
                    );

            x = placement.x;
            y = placement.y;
        }

        graphics.dispose();

        ByteBuffer buffer =
                MemoryUtil.memAlloc(
                        ATLAS_SIZE *
                                ATLAS_SIZE *
                                4
                );

        int[] pixels =
                image.getRGB(
                        0,
                        0,
                        ATLAS_SIZE,
                        ATLAS_SIZE,
                        null,
                        0,
                        ATLAS_SIZE
                );

        for (int pixel : pixels) {
            buffer.put(
                    (byte) 255
            );

            buffer.put(
                    (byte) 255
            );

            buffer.put(
                    (byte) 255
            );

            buffer.put(
                    (byte) (
                            (pixel >>> 24)
                                    & 0xFF
                    )
            );
        }

        buffer.flip();

        texture =
                GL11C.glGenTextures();

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                texture
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_MIN_FILTER,
                GL11C.GL_LINEAR
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_MAG_FILTER,
                GL11C.GL_LINEAR
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_WRAP_S,
                GL12C.GL_CLAMP_TO_EDGE
        );

        GL11C.glTexParameteri(
                GL11C.GL_TEXTURE_2D,
                GL11C.GL_TEXTURE_WRAP_T,
                GL12C.GL_CLAMP_TO_EDGE
        );

        GL11C.glPixelStorei(
                GL11C.GL_UNPACK_ALIGNMENT,
                1
        );

        GL11C.glTexImage2D(
                GL11C.GL_TEXTURE_2D,
                0,
                GL11C.GL_RGBA8,
                ATLAS_SIZE,
                ATLAS_SIZE,
                0,
                GL11C.GL_RGBA,
                GL11C.GL_UNSIGNED_BYTE,
                buffer
        );

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                0
        );

        MemoryUtil.memFree(
                buffer
        );
    }

    /*
     * Temporary placement state used only while
     * generating the atlas.
     */
    private static int placeY;

    private static Placement placeGlyph(
            Graphics2D graphics,
            BufferedImage image,
            char character,
            int x,
            int y,
            int rowHeight
    ) {
        var metrics =
                graphics.getFontMetrics();

        int advance =
                metrics.charWidth(
                        character
                );

        int width =
                Math.max(
                        advance,
                        1
                );

        if (x + width + 8 >= ATLAS_SIZE) {
            x = 4;
            y += rowHeight;
        }

        if (character != ' ') {
            graphics.drawString(
                    String.valueOf(
                            character
                    ),
                    x,
                    y + ascent
            );

            GLYPHS.put(
                    character,
                    new Glyph(
                            x,
                            y,
                            width,
                            rowHeight,
                            advance
                    )
            );
        } else {
            GLYPHS.put(
                    character,
                    new Glyph(
                            x,
                            y,
                            1,
                            1,
                            advance
                    )
            );
        }

        placeY = y;

        return new Placement(
                x + width + 4,
                y
        );
    }

    private record Placement(
            int x,
            int y
    ) {
    }

    public static void drawString(
            String text,
            float x,
            float y,
            float size,
            int color,
            float screenWidth,
            float screenHeight
    ) {
        if (!initialized ||
                text == null ||
                text.isEmpty()) {
            return;
        }

        float scale =
                size /
                        FONT_SIZE;

        float cursor =
                x;

        for (int i = 0;
             i < text.length();
             i++) {

            char character =
                    text.charAt(i);

            Glyph glyph =
                    GLYPHS.get(
                            character
                    );

            if (glyph == null) {
                glyph =
                        GLYPHS.get('?');
            }

            if (glyph == null) {
                continue;
            }

            if (character != ' ') {
                float width =
                        glyph.width *
                                scale;

                float height =
                        glyph.height *
                                scale;

                float u1 =
                        glyph.x /
                                (float) ATLAS_SIZE;

                float v1 =
                        glyph.y /
                                (float) ATLAS_SIZE;

                float u2 =
                        (glyph.x + glyph.width) /
                                (float) ATLAS_SIZE;

                float v2 =
                        (glyph.y + glyph.height) /
                                (float) ATLAS_SIZE;

                GLRenderer.drawGlyph(
                        cursor,
                        y,
                        width,
                        height,
                        u1,
                        v1,
                        u2,
                        v2,
                        color,
                        texture,
                        screenWidth,
                        screenHeight
                );
            }

            cursor +=
                    glyph.advance *
                            scale;
        }
    }

    public static int width(
            String text,
            float size
    ) {
        if (text == null ||
                text.isEmpty()) {
            return 0;
        }

        Glyph question =
                GLYPHS.get('?');

        float scale =
                size /
                        FONT_SIZE;

        float width = 0.0F;

        for (int i = 0;
             i < text.length();
             i++) {

            Glyph glyph =
                    GLYPHS.get(
                            text.charAt(i)
                    );

            if (glyph == null) {
                glyph = question;
            }

            if (glyph != null) {
                width +=
                        glyph.advance *
                                scale;
            }
        }

        return Math.round(
                width
        );
    }
}
