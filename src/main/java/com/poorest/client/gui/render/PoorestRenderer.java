package com.poorest.client.gui.render;

import net.minecraft.client.gui.GuiGraphics;

public final class PoorestRenderer {

    private PoorestRenderer() {
    }

    public static void rect(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int color
    ) {
        graphics.fill(
                left,
                top,
                right,
                bottom,
                color
        );
    }

    public static void roundedRect(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int radius,
            int color
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

        int width = right - left;
        int height = bottom - top;

        int safeRadius =
                Math.min(
                        radius,
                        Math.min(width, height) / 2
                );

        if (safeRadius <= 0) {
            rect(
                    graphics,
                    left,
                    top,
                    right,
                    bottom,
                    color
            );
            return;
        }

        graphics.fill(
                left + safeRadius,
                top,
                right - safeRadius,
                bottom,
                color
        );

        graphics.fill(
                left,
                top + safeRadius,
                left + safeRadius,
                bottom - safeRadius,
                color
        );

        graphics.fill(
                right - safeRadius,
                top + safeRadius,
                right,
                bottom - safeRadius,
                color
        );

        for (int y = 0; y < safeRadius; y++) {
            for (int x = 0; x < safeRadius; x++) {

                boolean topLeft =
                        x * x + y * y <=
                        safeRadius * safeRadius;

                boolean topRight =
                        (safeRadius - 1 - x) *
                        (safeRadius - 1 - x) +
                        y * y <=
                        safeRadius * safeRadius;

                boolean bottomLeft =
                        x * x +
                        (safeRadius - 1 - y) *
                        (safeRadius - 1 - y) <=
                        safeRadius * safeRadius;

                boolean bottomRight =
                        (safeRadius - 1 - x) *
                        (safeRadius - 1 - x) +
                        (safeRadius - 1 - y) *
                        (safeRadius - 1 - y) <=
                        safeRadius * safeRadius;

                if (topLeft) {
                    graphics.fill(
                            left + x,
                            top + y,
                            left + x + 1,
                            top + y + 1,
                            color
                    );
                }

                if (topRight) {
                    graphics.fill(
                            right - x - 1,
                            top + y,
                            right - x,
                            top + y + 1,
                            color
                    );
                }

                if (bottomLeft) {
                    graphics.fill(
                            left + x,
                            bottom - y - 1,
                            left + x + 1,
                            bottom - y,
                            color
                    );
                }

                if (bottomRight) {
                    graphics.fill(
                            right - x - 1,
                            bottom - y - 1,
                            right - x,
                            bottom - y,
                            color
                    );
                }
            }
        }
    }

    public static void outline(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int color
    ) {
        graphics.fill(
                left,
                top,
                right,
                top + 1,
                color
        );

        graphics.fill(
                left,
                bottom - 1,
                right,
                bottom,
                color
        );

        graphics.fill(
                left,
                top,
                left + 1,
                bottom,
                color
        );

        graphics.fill(
                right - 1,
                top,
                right,
                bottom,
                color
        );
    }

    public static void outline(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int radius,
            int color
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

        int safeRadius =
                Math.min(
                        radius,
                        Math.min(
                                right - left,
                                bottom - top
                        ) / 2
                );

        if (safeRadius <= 0) {
            outline(
                    graphics,
                    left,
                    top,
                    right,
                    bottom,
                    color
            );
            return;
        }

        graphics.fill(
                left + safeRadius,
                top,
                right - safeRadius,
                top + 1,
                color
        );

        graphics.fill(
                left + safeRadius,
                bottom - 1,
                right - safeRadius,
                bottom,
                color
        );

        graphics.fill(
                left,
                top + safeRadius,
                left + 1,
                bottom - safeRadius,
                color
        );

        graphics.fill(
                right - 1,
                top + safeRadius,
                right,
                bottom - safeRadius,
                color
        );

        for (int i = 0; i < safeRadius; i++) {
            graphics.fill(
                    left + i,
                    top + safeRadius - i - 1,
                    left + i + 1,
                    top + safeRadius - i,
                    color
            );

            graphics.fill(
                    right - i - 1,
                    top + safeRadius - i - 1,
                    right - i,
                    top + safeRadius - i,
                    color
            );

            graphics.fill(
                    left + i,
                    bottom - safeRadius + i,
                    left + i + 1,
                    bottom - safeRadius + i + 1,
                    color
            );

            graphics.fill(
                    right - i - 1,
                    bottom - safeRadius + i,
                    right - i,
                    bottom - safeRadius + i + 1,
                    color
            );
        }
    }

    public static void shadow(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        shadow(
                graphics,
                left,
                top,
                right,
                bottom,
                12,
                0x55000000
        );
    }

    public static void shadow(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int blur,
            int color
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

        int layers =
                Math.max(
                        1,
                        Math.min(
                                blur,
                                24
                        )
                );

        int baseAlpha =
                (color >>> 24) & 0xFF;

        int rgb =
                color & 0x00FFFFFF;

        for (int i = layers; i >= 1; i--) {
            float progress =
                    i / (float) layers;

            float strength =
                    1.0F - progress;

            int alpha =
                    (int) (
                            baseAlpha *
                            strength *
                            strength *
                            0.65F
                    );

            if (alpha <= 0) {
                continue;
            }

            int shadowColor =
                    (alpha << 24) | rgb;

            int offset = i;

            graphics.fill(
                    left - offset,
                    top - offset,
                    right + offset,
                    top - offset + 1,
                    shadowColor
            );

            graphics.fill(
                    left - offset,
                    bottom + offset - 1,
                    right + offset,
                    bottom + offset,
                    shadowColor
            );

            graphics.fill(
                    left - offset,
                    top - offset,
                    left - offset + 1,
                    bottom + offset,
                    shadowColor
            );

            graphics.fill(
                    right + offset - 1,
                    top - offset,
                    right + offset,
                    bottom + offset,
                    shadowColor
            );
        }
    }

    public static void verticalGradient(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom,
            int topColor,
            int bottomColor
    ) {
        if (bottom <= top) {
            return;
        }

        int height =
                bottom - top;

        for (int y = 0; y < height; y++) {
            float progress =
                    y / (float) Math.max(
                            1,
                            height - 1
                    );

            int color =
                    lerpColor(
                            topColor,
                            bottomColor,
                            progress
                    );

            graphics.fill(
                    left,
                    top + y,
                    right,
                    top + y + 1,
                    color
            );
        }
    }

    private static int lerpColor(
            int from,
            int to,
            float progress
    ) {
        progress =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                progress
                        )
                );

        int fromA =
                (from >>> 24) & 0xFF;

        int fromR =
                (from >>> 16) & 0xFF;

        int fromG =
                (from >>> 8) & 0xFF;

        int fromB =
                from & 0xFF;

        int toA =
                (to >>> 24) & 0xFF;

        int toR =
                (to >>> 16) & 0xFF;

        int toG =
                (to >>> 8) & 0xFF;

        int toB =
                to & 0xFF;

        int a =
                (int) (
                        fromA +
                        (toA - fromA) *
                        progress
                );

        int r =
                (int) (
                        fromR +
                        (toR - fromR) *
                        progress
                );

        int g =
                (int) (
                        fromG +
                        (toG - fromG) *
                        progress
                );

        int b =
                (int) (
                        fromB +
                        (toB - fromB) *
                        progress
                );

        return
                (a << 24)
                | (r << 16)
                | (g << 8)
                | b;
    }
}
