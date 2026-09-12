package com.poorest.client.ui.render;

import com.poorest.client.ui.font.PoorestFont;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Small standalone OpenGL renderer used by Poorest UI.
 * It does not use Minecraft GuiGraphics or Minecraft's UI renderer.
 */
public final class GLRenderer {

    private static final int MAX_RECT_VERTICES = 220_000;
    private static final int MAX_TEXT_VERTICES = 48_000;

    private static final String COLOR_VERTEX = """
            #version 150
            in vec2 aPos;
            in vec4 aColor;
            uniform vec2 uScreen;
            out vec4 vColor;
            void main() {
                float x = aPos.x / uScreen.x * 2.0 - 1.0;
                float y = 1.0 - aPos.y / uScreen.y * 2.0;
                gl_Position = vec4(x, y, 0.0, 1.0);
                vColor = aColor;
            }
            """;

    private static final String COLOR_FRAGMENT = """
            #version 150
            in vec4 vColor;
            out vec4 fragColor;
            void main() {
                fragColor = vColor;
            }
            """;

    private static final String TEXT_VERTEX = """
            #version 150
            in vec2 aPos;
            in vec2 aUV;
            in vec4 aColor;
            uniform vec2 uScreen;
            out vec2 vUV;
            out vec4 vColor;
            void main() {
                float x = aPos.x / uScreen.x * 2.0 - 1.0;
                float y = 1.0 - aPos.y / uScreen.y * 2.0;
                gl_Position = vec4(x, y, 0.0, 1.0);
                vUV = aUV;
                vColor = aColor;
            }
            """;

    private static final String TEXT_FRAGMENT = """
            #version 150
            in vec2 vUV;
            in vec4 vColor;
            uniform sampler2D uTexture;
            out vec4 fragColor;
            void main() {
                float a = texture(uTexture, vUV).a;
                fragColor = vec4(vColor.rgb, vColor.a * a);
            }
            """;

    private static int colorProgram;
    private static int textProgram;
    private static int colorVao;
    private static int colorVbo;
    private static int textVao;
    private static int textVbo;

    private static FloatBuffer colorBuffer;
    private static FloatBuffer textBuffer;
    private static float[] colorData;
    private static float[] textData;

    private static int colorFloatCount;
    private static int textFloatCount;
    private static int textTexture;

    private static int screenWidth;
    private static int screenHeight;

    private static int colorScreenUniform;
    private static int textScreenUniform;
    private static int textTextureUniform;

    private static int previousProgram;
    private static int previousVao;
    private static int previousTexture;

    private static boolean previousBlend;
    private static boolean previousDepth;
    private static boolean previousCull;
    private static boolean previousScissor;
    private static boolean previousDepthMask;

    private static boolean initialized;
    private static boolean drawing;

    private GLRenderer() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        colorData = new float[MAX_RECT_VERTICES * 6];
        textData = new float[MAX_TEXT_VERTICES * 8];

        colorBuffer = MemoryUtil.memAllocFloat(colorData.length);
        textBuffer = MemoryUtil.memAllocFloat(textData.length);

        colorProgram = createProgram(
                COLOR_VERTEX,
                COLOR_FRAGMENT,
                false
        );

        textProgram = createProgram(
                TEXT_VERTEX,
                TEXT_FRAGMENT,
                true
        );

        colorScreenUniform = GL20C.glGetUniformLocation(
                colorProgram,
                "uScreen"
        );

        textScreenUniform = GL20C.glGetUniformLocation(
                textProgram,
                "uScreen"
        );

        textTextureUniform = GL20C.glGetUniformLocation(
                textProgram,
                "uTexture"
        );

        createColorBuffer();
        createTextBuffer();

        PoorestFont.init();
    }

    private static void createColorBuffer() {
        colorVao = GL30C.glGenVertexArrays();
        colorVbo = GL15C.glGenBuffers();

        GL30C.glBindVertexArray(colorVao);
        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                colorVbo
        );

        GL15C.glBufferData(
                GL15C.GL_ARRAY_BUFFER,
                (long) colorData.length * Float.BYTES,
                GL15C.GL_STREAM_DRAW
        );

        GL20C.glEnableVertexAttribArray(0);
        GL20C.glVertexAttribPointer(
                0,
                2,
                GL11C.GL_FLOAT,
                false,
                24,
                0
        );

        GL20C.glEnableVertexAttribArray(1);
        GL20C.glVertexAttribPointer(
                1,
                4,
                GL11C.GL_FLOAT,
                false,
                24,
                8
        );

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                0
        );

        GL30C.glBindVertexArray(0);
    }

    private static void createTextBuffer() {
        textVao = GL30C.glGenVertexArrays();
        textVbo = GL15C.glGenBuffers();

        GL30C.glBindVertexArray(textVao);
        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                textVbo
        );

        GL15C.glBufferData(
                GL15C.GL_ARRAY_BUFFER,
                (long) textData.length * Float.BYTES,
                GL15C.GL_STREAM_DRAW
        );

        GL20C.glEnableVertexAttribArray(0);
        GL20C.glVertexAttribPointer(
                0,
                2,
                GL11C.GL_FLOAT,
                false,
                32,
                0
        );

        GL20C.glEnableVertexAttribArray(1);
        GL20C.glVertexAttribPointer(
                1,
                2,
                GL11C.GL_FLOAT,
                false,
                32,
                8
        );

        GL20C.glEnableVertexAttribArray(2);
        GL20C.glVertexAttribPointer(
                2,
                4,
                GL11C.GL_FLOAT,
                false,
                32,
                16
        );

        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                0
        );

        GL30C.glBindVertexArray(0);
    }

    public static void begin(
            float width,
            float height
    ) {
        init();

        screenWidth = Math.max(1, Math.round(width));
        screenHeight = Math.max(1, Math.round(height));

        previousProgram = GL11C.glGetInteger(
                GL20C.GL_CURRENT_PROGRAM
        );

        previousVao = GL11C.glGetInteger(
                GL30C.GL_VERTEX_ARRAY_BINDING
        );

        previousTexture = GL11C.glGetInteger(
                GL11C.GL_TEXTURE_BINDING_2D
        );

        previousBlend = GL11C.glIsEnabled(GL11C.GL_BLEND);
        previousDepth = GL11C.glIsEnabled(GL11C.GL_DEPTH_TEST);
        previousCull = GL11C.glIsEnabled(GL11C.GL_CULL_FACE);
        previousScissor = GL11C.glIsEnabled(GL11C.GL_SCISSOR_TEST);
        previousDepthMask = GL11C.glGetBoolean(
                GL11C.GL_DEPTH_WRITEMASK
        );

        GL11C.glDisable(GL11C.GL_DEPTH_TEST);
        GL11C.glDisable(GL11C.GL_CULL_FACE);
        GL11C.glDisable(GL11C.GL_SCISSOR_TEST);
        GL11C.glDepthMask(false);

        GL11C.glEnable(GL11C.GL_BLEND);
        GL11C.glBlendFunc(
                GL11C.GL_SRC_ALPHA,
                GL11C.GL_ONE_MINUS_SRC_ALPHA
        );

        colorFloatCount = 0;
        textFloatCount = 0;
        textTexture = 0;
        drawing = true;
    }

    public static void end() {
        if (!drawing) {
            return;
        }

        flush();

        GL11C.glDepthMask(previousDepthMask);
        restore(GL11C.GL_BLEND, previousBlend);
        restore(GL11C.GL_DEPTH_TEST, previousDepth);
        restore(GL11C.GL_CULL_FACE, previousCull);
        restore(GL11C.GL_SCISSOR_TEST, previousScissor);

        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                previousTexture
        );

        GL30C.glBindVertexArray(previousVao);
        GL20C.glUseProgram(previousProgram);

        drawing = false;
    }

    private static void restore(
            int capability,
            boolean value
    ) {
        if (value) {
            GL11C.glEnable(capability);
        } else {
            GL11C.glDisable(capability);
        }
    }

    public static void roundedRect(
            float x,
            float y,
            float width,
            float height,
            float radius,
            int color,
            float screenWidth,
            float screenHeight
    ) {
        if (!drawing || width <= 0.0F || height <= 0.0F) {
            return;
        }

        if (radius <= 0.5F) {
            quad(x, y, width, height, color);
            return;
        }

        float r = Math.min(
                radius,
                Math.min(width, height) * 0.5F
        );

        /*
         * Nine-piece construction:
         * center + four edge strips + four corner fans.
         * Unlike the previous fan-only implementation this leaves
         * no gaps along the straight sides of the rounded rectangle.
         */
        quad(
                x + r,
                y,
                Math.max(0.0F, width - 2.0F * r),
                height,
                color
        );

        quad(
                x,
                y + r,
                r,
                Math.max(0.0F, height - 2.0F * r),
                color
        );

        quad(
                x + width - r,
                y + r,
                r,
                Math.max(0.0F, height - 2.0F * r),
                color
        );

        quad(
                x + r,
                y,
                Math.max(0.0F, width - 2.0F * r),
                r,
                color
        );

        quad(
                x + r,
                y + height - r,
                Math.max(0.0F, width - 2.0F * r),
                r,
                color
        );

        drawCorner(
                x + r,
                y + r,
                r,
                (float) Math.PI,
                (float) (Math.PI * 1.5),
                color
        );

        drawCorner(
                x + width - r,
                y + r,
                r,
                (float) (Math.PI * 1.5),
                (float) (Math.PI * 2.0),
                color
        );

        drawCorner(
                x + width - r,
                y + height - r,
                r,
                0.0F,
                (float) (Math.PI * 0.5),
                color
        );

        drawCorner(
                x + r,
                y + height - r,
                r,
                (float) (Math.PI * 0.5),
                (float) Math.PI,
                color
        );
    }

    private static void drawCorner(
            float centerX,
            float centerY,
            float radius,
            float startAngle,
            float endAngle,
            int color
    ) {
        int segments = 8;

        float px = centerX + (float) Math.cos(startAngle) * radius;
        float py = centerY + (float) Math.sin(startAngle) * radius;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float angle = startAngle + (endAngle - startAngle) * t;

            float nx = centerX + (float) Math.cos(angle) * radius;
            float ny = centerY + (float) Math.sin(angle) * radius;

            triangle(
                    centerX,
                    centerY,
                    px,
                    py,
                    nx,
                    ny,
                    color
            );

            px = nx;
            py = ny;
        }
    }

    public static void outline(
            float x,
            float y,
            float width,
            float height,
            float radius,
            float thickness,
            int color,
            float screenWidth,
            float screenHeight
    ) {
        if (!drawing || width <= 0.0F || height <= 0.0F) {
            return;
        }

        float t = Math.max(0.5F, Math.min(thickness, Math.min(width, height) * 0.5F));
        float outerRadius = Math.min(radius, Math.min(width, height) * 0.5F);
        float innerWidth = width - t * 2.0F;
        float innerHeight = height - t * 2.0F;
        float innerRadius = Math.max(0.0F, Math.min(outerRadius - t, Math.min(innerWidth, innerHeight) * 0.5F));

        int segments = 10;
        float[] outerX = new float[segments * 4];
        float[] outerY = new float[segments * 4];
        float[] innerX = new float[segments * 4];
        float[] innerY = new float[segments * 4];

        int index = 0;
        float[] angles = {-90.0F, 0.0F, 90.0F, 180.0F};
        float[] centersX = {x + width - outerRadius, x + width - outerRadius, x + outerRadius, x + outerRadius};
        float[] centersY = {y + outerRadius, y + height - outerRadius, y + height - outerRadius, y + outerRadius};

        for (int corner = 0; corner < 4; corner++) {
            float start = angles[corner];
            float end = start + 90.0F;
            for (int i = 0; i < segments; i++) {
                float a = (float) Math.toRadians(start + (end - start) * i / (segments - 1));
                outerX[index] = centersX[corner] + (float) Math.cos(a) * outerRadius;
                outerY[index] = centersY[corner] + (float) Math.sin(a) * outerRadius;

                float innerCenterX = corner == 0 || corner == 1
                        ? x + width - t - innerRadius
                        : x + t + innerRadius;
                float innerCenterY = corner == 0 || corner == 3
                        ? y + t + innerRadius
                        : y + height - t - innerRadius;
                innerX[index] = innerCenterX + (float) Math.cos(a) * innerRadius;
                innerY[index] = innerCenterY + (float) Math.sin(a) * innerRadius;
                index++;
            }
        }

        for (int i = 0; i < index; i++) {
            int next = (i + 1) % index;
            triangle(outerX[i], outerY[i], outerX[next], outerY[next], innerX[next], innerY[next], color);
            triangle(outerX[i], outerY[i], innerX[next], innerY[next], innerX[i], innerY[i], color);
        }
    }

    public static void shadow(
            float x,
            float y,
            float width,
            float height,
            float radius,
            float blur,
            int color,
            float screenWidth,
            float screenHeight
    ) {
        int alpha = (color >>> 24) & 0xFF;
        int limited = Math.min(100, alpha);
        int shadowColor =
                (limited << 24) | (color & 0x00FFFFFF);

        float spread = Math.min(
                7.0F,
                Math.max(2.0F, blur * 0.20F)
        );

        roundedRect(
                x - spread,
                y - spread,
                width + spread * 2,
                height + spread * 2,
                radius + spread,
                shadowColor,
                screenWidth,
                screenHeight
        );
    }

    public static void drawGlyph(
            float x,
            float y,
            float width,
            float height,
            float u1,
            float v1,
            float u2,
            float v2,
            int color,
            int texture,
            float screenWidth,
            float screenHeight
    ) {
        if (!drawing) {
            return;
        }

        if (textFloatCount + 48 >= textData.length) {
            flushText();
        }

        putText(x, y, u1, v1, color);
        putText(x + width, y, u2, v1, color);
        putText(x + width, y + height, u2, v2, color);
        putText(x, y, u1, v1, color);
        putText(x + width, y + height, u2, v2, color);
        putText(x, y + height, u1, v2, color);

        textTexture = texture;
    }

    private static void quad(
            float x,
            float y,
            float width,
            float height,
            int color
    ) {
        triangle(x, y, x + width, y, x + width, y + height, color);
        triangle(x, y, x + width, y + height, x, y + height, color);
    }

    private static void triangle(
            float x1,
            float y1,
            float x2,
            float y2,
            float x3,
            float y3,
            int color
    ) {
        if (colorFloatCount + 18 >= colorData.length) {
            flushRects();
        }

        putColor(x1, y1, color);
        putColor(x2, y2, color);
        putColor(x3, y3, color);
    }

    private static void putColor(
            float x,
            float y,
            int color
    ) {
        int i = colorFloatCount;
        colorData[i++] = x;
        colorData[i++] = y;
        colorData[i++] = ((color >>> 16) & 0xFF) / 255.0F;
        colorData[i++] = ((color >>> 8) & 0xFF) / 255.0F;
        colorData[i++] = (color & 0xFF) / 255.0F;
        colorData[i++] = ((color >>> 24) & 0xFF) / 255.0F;
        colorFloatCount = i;
    }

    private static void putText(
            float x,
            float y,
            float u,
            float v,
            int color
    ) {
        int i = textFloatCount;
        textData[i++] = x;
        textData[i++] = y;
        textData[i++] = u;
        textData[i++] = v;
        textData[i++] = ((color >>> 16) & 0xFF) / 255.0F;
        textData[i++] = ((color >>> 8) & 0xFF) / 255.0F;
        textData[i++] = (color & 0xFF) / 255.0F;
        textData[i++] = ((color >>> 24) & 0xFF) / 255.0F;
        textFloatCount = i;
    }

    public static void pushScissor(
            float x,
            float y,
            float width,
            float height
    ) {
        flush();

        int sx = Math.max(0, Math.round(x));
        int sy = Math.max(
                0,
                Math.round(screenHeight - y - height)
        );
        int sw = Math.max(1, Math.round(width));
        int sh = Math.max(1, Math.round(height));

        sx = Math.min(sx, Math.max(0, screenWidth - 1));
        sy = Math.min(sy, Math.max(0, screenHeight - 1));
        sw = Math.min(sw, screenWidth - sx);
        sh = Math.min(sh, screenHeight - sy);

        GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
        GL11C.glScissor(sx, sy, sw, sh);
    }

    public static void popScissor() {
        flush();
        GL11C.glDisable(GL11C.GL_SCISSOR_TEST);
    }

    public static void flush() {
        flushRects();
        flushText();
    }

    private static void flushRects() {
        if (colorFloatCount <= 0) {
            return;
        }

        colorBuffer.clear();
        colorBuffer.put(
                colorData,
                0,
                colorFloatCount
        );
        colorBuffer.flip();

        GL20C.glUseProgram(colorProgram);
        GL20C.glUniform2f(
                colorScreenUniform,
                screenWidth,
                screenHeight
        );

        GL30C.glBindVertexArray(colorVao);
        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                colorVbo
        );

        GL15C.glBufferSubData(
                GL15C.GL_ARRAY_BUFFER,
                0,
                colorBuffer
        );

        GL11C.glDrawArrays(
                GL11C.GL_TRIANGLES,
                0,
                colorFloatCount / 6
        );

        colorFloatCount = 0;
    }

    private static void flushText() {
        if (textFloatCount <= 0 || textTexture == 0) {
            return;
        }

        textBuffer.clear();
        textBuffer.put(
                textData,
                0,
                textFloatCount
        );
        textBuffer.flip();

        GL20C.glUseProgram(textProgram);
        GL20C.glUniform2f(
                textScreenUniform,
                screenWidth,
                screenHeight
        );
        GL20C.glUniform1i(
                textTextureUniform,
                0
        );

        GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
        GL11C.glBindTexture(
                GL11C.GL_TEXTURE_2D,
                textTexture
        );

        GL30C.glBindVertexArray(textVao);
        GL15C.glBindBuffer(
                GL15C.GL_ARRAY_BUFFER,
                textVbo
        );

        GL15C.glBufferSubData(
                GL15C.GL_ARRAY_BUFFER,
                0,
                textBuffer
        );

        GL11C.glDrawArrays(
                GL11C.GL_TRIANGLES,
                0,
                textFloatCount / 8
        );

        textFloatCount = 0;
        textTexture = 0;
    }

    private static int createProgram(
            String vertexSource,
            String fragmentSource,
            boolean text
    ) {
        int vertex = compile(
                GL20C.GL_VERTEX_SHADER,
                vertexSource
        );
        int fragment = compile(
                GL20C.GL_FRAGMENT_SHADER,
                fragmentSource
        );

        int result = GL20C.glCreateProgram();
        GL20C.glAttachShader(result, vertex);
        GL20C.glAttachShader(result, fragment);

        GL20C.glBindAttribLocation(result, 0, "aPos");

        if (text) {
            GL20C.glBindAttribLocation(result, 1, "aUV");
            GL20C.glBindAttribLocation(result, 2, "aColor");
        } else {
            GL20C.glBindAttribLocation(result, 1, "aColor");
        }

        GL20C.glLinkProgram(result);

        if (GL20C.glGetProgrami(
                result,
                GL20C.GL_LINK_STATUS
        ) == GL11C.GL_FALSE) {
            throw new IllegalStateException(
                    "Poorest UI shader link failed:\n" +
                    GL20C.glGetProgramInfoLog(result)
            );
        }

        GL20C.glDeleteShader(vertex);
        GL20C.glDeleteShader(fragment);

        return result;
    }

    private static int compile(
            int type,
            String source
    ) {
        int shader = GL20C.glCreateShader(type);
        GL20C.glShaderSource(shader, source);
        GL20C.glCompileShader(shader);

        if (GL20C.glGetShaderi(
                shader,
                GL20C.GL_COMPILE_STATUS
        ) == GL11C.GL_FALSE) {
            throw new IllegalStateException(
                    "Poorest UI shader compile failed:\n" +
                    GL20C.glGetShaderInfoLog(shader)
            );
        }

        return shader;
    }
}
