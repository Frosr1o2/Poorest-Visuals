package com.poorest.client.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.poorest.client.PoorestClient;
import com.poorest.client.core.ClientConfigManager;
import com.poorest.client.core.keybind.KeybindManager;
import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.Setting;
import com.poorest.client.modules.hud.PoorestHudLayout;
import com.poorest.client.ui.font.PoorestFont;
import com.poorest.client.ui.render.GLRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class PoorestUI {

    private static final Path CONFIG = Path.of(
            "config",
            "poorest_client",
            "ui.properties"
    );

    private static final float DEFAULT_WIDTH = 760.0F;
    private static final float DEFAULT_HEIGHT = 430.0F;

    private static final float MIN_WIDTH = 560.0F;
    private static final float MAX_WIDTH = 1050.0F;
    private static final float MIN_HEIGHT = 340.0F;
    private static final float MAX_HEIGHT = 700.0F;

    private static final float HEADER = 64.0F;
    private static final float NAV = 42.0F;

    private static boolean open;
    private static boolean closing;
    private static boolean settings;
    private static boolean searchFocused;
    private static boolean profileFocused;
    private static boolean dragging;
    private static boolean resizing;

    private static float width = DEFAULT_WIDTH;
    private static float height = DEFAULT_HEIGHT;
    private static float x = -1.0F;
    private static float y = -1.0F;

    private static float dragOffsetX;
    private static float dragOffsetY;
    private static float resizeStartX;
    private static float resizeStartY;
    private static float resizeStartWidth;
    private static float resizeStartHeight;

    private static float scroll;
    private static float settingScroll;

    private static String search = "";
    private static String profileName = "default";

    private static ModuleCategory category = ModuleCategory.HUD;
    private static Module selectedModule;

    private static long animationStart;
    private static boolean cursorWasDisabled;

    private static final float[] categoryHover =
            new float[ModuleCategory.values().length];

    private static final float[] moduleHover =
            new float[256];

    private static final int[] ACCENTS = {
            0xFF9B6CFF,
            0xFFB66CFF,
            0xFF7C5CFF,
            0xFF5E8CFF,
            0xFF4AC6FF,
            0xFF32D7C8,
            0xFF56D98A,
            0xFFB8E85A,
            0xFFFFD15C,
            0xFFFFB35E,
            0xFFFF6262,
            0xFFFF62B5,
            0xFFFF4FA3,
            0xFFFFFFFF
    };

    private PoorestUI() {
    }

    public static void open() {
        PoorestTheme.load();
        load();

        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();

        cursorWasDisabled =
                GLFW.glfwGetInputMode(
                        window,
                        GLFW.GLFW_CURSOR
                ) == GLFW.GLFW_CURSOR_DISABLED;

        if (cursorWasDisabled) {
            mc.mouseHandler.releaseMouse();
        } else {
            GLFW.glfwSetInputMode(
                    window,
                    GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_NORMAL
            );
        }

        GLFW.glfwSetCursorPos(
                window,
                mc.getWindow().getWidth() * 0.5,
                mc.getWindow().getHeight() * 0.5
        );

        open = true;
        closing = false;
        settings = false;
        searchFocused = false;
        profileFocused = false;
        dragging = false;
        resizing = false;
        selectedModule = null;
        scroll = 0;
        settingScroll = 0;
        search = "";
        animationStart = System.currentTimeMillis();
    }

    public static void close() {
        if (!open || closing) {
            return;
        }

        save();
        PoorestTheme.save();
        ClientConfigManager.save();

        closing = true;
        searchFocused = false;
        profileFocused = false;
        dragging = false;
        resizing = false;
        animationStart = System.currentTimeMillis();
    }

    public static void toggle() {
        if (open) {
            close();
        } else {
            open();
        }
    }

    public static boolean isOpen() {
        return open;
    }

    public static boolean isClosing() {
        return closing;
    }

    public static void tick() {
        if (!open) {
            return;
        }

        if (closing && getAnimation() <= 0.0F) {
            open = false;
            closing = false;

            Minecraft mc = Minecraft.getInstance();
            long window = mc.getWindow().getWindow();

            if (cursorWasDisabled) {
                mc.mouseHandler.grabMouse();
            } else {
                GLFW.glfwSetInputMode(
                        window,
                        GLFW.GLFW_CURSOR,
                        GLFW.GLFW_CURSOR_NORMAL
                );
            }

            return;
        }

        if (dragging || resizing) {
            pollMouseDrag();
        }
    }

    private static void pollMouseDrag() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();

        if (GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) != GLFW.GLFW_PRESS) {
            dragging = false;
            resizing = false;
            save();
            return;
        }

        double[] mouseX = {0};
        double[] mouseY = {0};

        GLFW.glfwGetCursorPos(
                window,
                mouseX,
                mouseY
        );

        float sx = physicalMouseX(mouseX[0]);
        float sy = physicalMouseY(mouseY[0]);

        if (resizing) {
            width = clamp(
                    resizeStartWidth + sx - resizeStartX,
                    MIN_WIDTH,
                    MAX_WIDTH
            );

            height = clamp(
                    resizeStartHeight + sy - resizeStartY,
                    MIN_HEIGHT,
                    MAX_HEIGHT
            );
        } else if (dragging) {
            x = sx - dragOffsetX;
            y = sy - dragOffsetY;
            keepOnScreen();
        }
    }

    public static void render(GuiGraphics graphics) {
        if (!open) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }

        float screenWidth = mc.getWindow().getWidth();
        float screenHeight = mc.getWindow().getHeight();

        if (x < 0 || y < 0) {
            x = (screenWidth - width) * 0.5F;
            y = (screenHeight - height) * 0.5F;
        }

        keepOnScreen();

        float animation = getAnimation();
        float uiScale = PoorestTheme.uiScale();

        float drawWidth = Math.min(
                width * uiScale,
                screenWidth - 16.0F
        );

        float drawHeight = Math.min(
                height * uiScale,
                screenHeight - 16.0F
        );

        float drawX = x;
        float drawY = y;

        float centerX = drawX + drawWidth * 0.5F;
        float centerY = drawY + drawHeight * 0.5F;
        float animationScale = 0.97F + animation * 0.03F;

        drawX = centerX - drawWidth * animationScale * 0.5F;
        drawY = centerY - drawHeight * animationScale * 0.5F;
        drawWidth *= animationScale;
        drawHeight *= animationScale;

        GLRenderer.begin(screenWidth, screenHeight);

        GLRenderer.roundedRect(
                0,
                0,
                screenWidth,
                screenHeight,
                0,
                colorWithAlpha(
                        0x11141B,
                        (int) (130 * animation)
                ),
                screenWidth,
                screenHeight
        );

        if (PoorestTheme.shadows()) {
            GLRenderer.shadow(
                    drawX - 8,
                    drawY - 8,
                    drawWidth + 16,
                    drawHeight + 16,
                    PoorestTheme.rounding() + 5,
                    14,
                    0x70000000,
                    screenWidth,
                    screenHeight
            );
        }

        GLRenderer.roundedRect(
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                PoorestTheme.rounding(),
                PoorestTheme.panel(),
                screenWidth,
                screenHeight
        );

        renderHeader(
                graphics,
                drawX,
                drawY,
                drawWidth,
                screenWidth,
                screenHeight,
                scaledMouseX(),
                scaledMouseY()
        );

        if (settings) {
            renderSettings(
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight,
                    screenWidth,
                    screenHeight
            );
        } else if (selectedModule != null) {
            renderModuleSettings(
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight,
                    screenWidth,
                    screenHeight
            );
        } else {
            renderCategories(
                    drawX,
                    drawY,
                    drawWidth,
                    screenWidth,
                    screenHeight,
                    scaledMouseX(),
                    scaledMouseY()
            );

            renderModules(
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight,
                    screenWidth,
                    screenHeight,
                    scaledMouseX(),
                    scaledMouseY()
            );
        }

        renderFooter(
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                screenWidth,
                screenHeight
        );

        renderResizeGrip(
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                screenWidth,
                screenHeight
        );

        GLRenderer.end();
    }

    private static void renderHeader(
            GuiGraphics graphics,
            float x,
            float y,
            float width,
            float screenWidth,
            float screenHeight,
            float mouseX,
            float mouseY
    ) {
        GLRenderer.roundedRect(
                x,
                y,
                width,
                HEADER,
                PoorestTheme.rounding(),
                PoorestTheme.header(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                x,
                y + 24,
                width,
                HEADER - 24,
                0,
                PoorestTheme.header(),
                screenWidth,
                screenHeight
        );

        float logoX = x + 18;
        float logoY = y + 12;

        GLRenderer.roundedRect(
                logoX,
                logoY,
                40,
                40,
                12,
                PoorestTheme.accentSurface(),
                screenWidth,
                screenHeight
        );

        drawText(
                "P",
                logoX + 11.0F,
                logoY + 6.0F,
                26,
                PoorestTheme.accentBright(),
                screenWidth,
                screenHeight
        );

        drawText(
                "POOREST",
                logoX + 54,
                logoY + 3,
                16,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        drawText(
                "VISUALS  v" + PoorestClient.VERSION,
                logoX + 54,
                logoY + 24,
                8,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );

        float settingsX = x + width - 26;
        float settingsY = y + 26;

        boolean settingsHover = inside(
                mouseX,
                mouseY,
                settingsX - 18,
                settingsY - 16,
                28,
                28
        );

        drawText(
                "S",
                settingsX - 5,
                settingsY - 7,
                13,
                settings || settingsHover
                        ? PoorestTheme.accentBright()
                        : PoorestTheme.secondary(),
                screenWidth,
                screenHeight
        );

        float searchWidth = Math.min(190, width * 0.29F);
        float searchHeight = 34;
        float searchX = x + width - searchWidth - 48;
        float searchY = y + 15;

        GLRenderer.roundedRect(
                searchX,
                searchY,
                searchWidth,
                searchHeight,
                17,
                searchFocused
                        ? PoorestTheme.accentSurface()
                        : PoorestTheme.surface(),
                screenWidth,
                screenHeight
        );

        String searchText = search.isEmpty()
                ? "Search..."
                : search;

        drawText(
                searchText,
                searchX + 14,
                searchY + 10,
                10,
                search.isEmpty()
                        ? PoorestTheme.muted()
                        : PoorestTheme.secondary(),
                screenWidth,
                screenHeight
        );
    }

    private static void renderCategories(
            float x,
            float y,
            float width,
            float screenWidth,
            float screenHeight,
            float mouseX,
            float mouseY
    ) {
        ModuleCategory[] values =
                ModuleCategory.values();

        float currentX = x + 18;
        float categoryY = y + HEADER + 10;

        for (int i = 0; i < values.length; i++) {
            String name = formatCategory(values[i]);
            float itemWidth = textWidth(name, 10) + 22;

            boolean hovered = inside(
                    mouseX,
                    mouseY,
                    currentX,
                    categoryY,
                    itemWidth,
                    NAV
            );

            boolean selected = category == values[i];

            categoryHover[i] = animate(
                    categoryHover[i],
                    selected || hovered ? 1.0F : 0.0F,
                    0.20F
            );

            if (selected) {
                GLRenderer.roundedRect(
                        currentX,
                        categoryY,
                        itemWidth,
                        NAV,
                        14,
                        PoorestTheme.accentSurface(),
                        screenWidth,
                        screenHeight
                );

                GLRenderer.roundedRect(
                        currentX + 8,
                        categoryY + NAV - 3,
                        itemWidth - 16,
                        2,
                        1,
                        PoorestTheme.accent(),
                        screenWidth,
                        screenHeight
                );
            } else if (categoryHover[i] > 0.01F) {
                GLRenderer.roundedRect(
                        currentX,
                        categoryY,
                        itemWidth,
                        NAV,
                        14,
                        blend(
                                0x151821,
                                PoorestTheme.accent(),
                                categoryHover[i] * 0.12F,
                                0x45
                        ),
                        screenWidth,
                        screenHeight
                );
            }

            drawText(
                    name,
                    currentX + 11,
                    categoryY + 12,
                    10,
                    selected
                            ? PoorestTheme.text()
                            : PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );

            currentX += itemWidth + 2;
        }
    }

    private static void renderModules(
            float x,
            float y,
            float width,
            float height,
            float screenWidth,
            float screenHeight,
            float mouseX,
            float mouseY
    ) {
        float areaX = x + 18;
        float areaY = y + HEADER + NAV + 16;
        float areaWidth = width - 36;
        float areaHeight = height - HEADER - NAV - 58;

        List<Module> modules = filteredModules();

        GLRenderer.roundedRect(
                areaX,
                areaY,
                areaWidth,
                areaHeight,
                15,
                PoorestTheme.surface(),
                screenWidth,
                screenHeight
        );

        GLRenderer.pushScissor(
                areaX,
                areaY,
                areaWidth,
                areaHeight
        );

        if (modules.isEmpty()) {
            drawText(
                    PoorestLang.text("poorest.ui.no_modules", "No modules found"),
                    areaX + 20,
                    areaY + 20,
                    12,
                    PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );
            GLRenderer.popScissor();
            return;
        }

        float gap = 9;
        float cardWidth =
                (areaWidth - 30 - gap) / 2.0F;
        float cardHeight = 68;
        float startY = areaY + 10 - scroll;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            int row = i / 2;
            int column = i % 2;

            float cardX = areaX + 10
                    + column * (cardWidth + gap);
            float cardY = startY
                    + row * (cardHeight + gap);

            if (cardY + cardHeight <= areaY ||
                    cardY >= areaY + areaHeight) {
                continue;
            }

            boolean hovered = inside(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight
            );

            moduleHover[i] = animate(
                    moduleHover[i],
                    hovered ? 1.0F : 0.0F,
                    0.20F
            );

            int cardColor = module.isEnabled()
                    ? PoorestTheme.cardEnabled()
                    : blend(
                            PoorestTheme.card(),
                            PoorestTheme.cardHover(),
                            moduleHover[i],
                            0xFF
                    );

            GLRenderer.roundedRect(
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight,
                    14,
                    cardColor,
                    screenWidth,
                    screenHeight
            );

            GLRenderer.outline(
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight,
                    14,
                    1,
                    module.isEnabled()
                            ? colorWithAlpha(
                                    PoorestTheme.accent(),
                                    110
                            )
                            : PoorestTheme.border(),
                    screenWidth,
                    screenHeight
            );

            if (module.isEnabled()) {
                GLRenderer.roundedRect(
                        cardX,
                        cardY + 12,
                        2,
                        cardHeight - 24,
                        1,
                        PoorestTheme.accent(),
                        screenWidth,
                        screenHeight
                );
            }

            drawText(
                    PoorestLang.module(module),
                    cardX + 14,
                    cardY + 12,
                    12,
                    module.isEnabled()
                            ? PoorestTheme.text()
                            : PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );

            String description = PoorestLang.moduleDescription(module);
            if (description.length() > 45) {
                description = description.substring(0, 42) + "...";
            }

            drawText(
                    description,
                    cardX + 14,
                    cardY + 36,
                    9,
                    PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );

            drawText(
                    ">",
                    cardX + cardWidth - 76,
                    cardY + 27,
                    11,
                    PoorestTheme.muted(),
                    screenWidth,
                    screenHeight
            );

            renderToggle(
                    cardX + cardWidth - 50,
                    cardY + 24,
                    module.isEnabled(),
                    screenWidth,
                    screenHeight
            );
        }

        GLRenderer.popScissor();
    }

    private static void renderModuleSettings(
            float x,
            float y,
            float width,
            float height,
            float screenWidth,
            float screenHeight
    ) {
        float areaX = x + 18;
        float areaY = y + HEADER + 10;
        float areaWidth = width - 36;
        float areaHeight = height - HEADER - 46;

        drawText(
                "<  " + PoorestLang.module(selectedModule),
                areaX,
                areaY + 5,
                13,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.moduleDescription(selectedModule),
                areaX,
                areaY + 26,
                9,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );

        float contentY = areaY + 56 - settingScroll;

        GLRenderer.pushScissor(
                areaX,
                areaY + 48,
                areaWidth,
                areaHeight - 48
        );

        List<Setting<?>> settingsList =
                selectedModule.getSettings();

        if (settingsList.isEmpty()) {
            drawText(
                    PoorestLang.text("poorest.ui.no_settings", "This module has no settings yet."),
                    areaX,
                    contentY,
                    10,
                    PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );
        } else {
            for (Setting<?> setting : settingsList) {
                renderSetting(
                        setting,
                        areaX,
                        contentY,
                        areaWidth,
                        screenWidth,
                        screenHeight
                );
                contentY += 58;
            }
        }

        GLRenderer.popScissor();
    }

    private static void renderSetting(
            Setting<?> setting,
            float x,
            float y,
            float width,
            float screenWidth,
            float screenHeight
    ) {
        drawText(
                PoorestLang.setting(setting),
                x,
                y,
                11,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.settingDescription(setting),
                x,
                y + 18,
                7,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );

        if (setting instanceof BooleanSetting booleanSetting) {
            renderToggle(
                    x + width - 42,
                    y - 3,
                    booleanSetting.isEnabled(),
                    screenWidth,
                    screenHeight
            );
        } else if (setting instanceof NumberSetting numberSetting) {
            double value = numberSetting.getValueAsDouble();
            double min = numberSetting.getMin();
            double max = numberSetting.getMax();
            float normalized = (float) ((value - min) / (max - min));

            float trackX = x + width - 180;
            float trackY = y + 27;
            float trackWidth = 165;

            GLRenderer.roundedRect(
                    trackX,
                    trackY,
                    trackWidth,
                    4,
                    2,
                    PoorestTheme.switchOff(),
                    screenWidth,
                    screenHeight
            );

            GLRenderer.roundedRect(
                    trackX,
                    trackY,
                    trackWidth * normalized,
                    4,
                    2,
                    PoorestTheme.accent(),
                    screenWidth,
                    screenHeight
            );

            GLRenderer.roundedRect(
                    trackX + trackWidth * normalized - 5,
                    trackY - 3,
                    10,
                    10,
                    5,
                    PoorestTheme.accentBright(),
                    screenWidth,
                    screenHeight
            );

            drawText(
                    formatNumber(value),
                    trackX - 44,
                    trackY - 4,
                    8,
                    PoorestTheme.secondary(),
                    screenWidth,
                    screenHeight
            );
        } else if (setting instanceof ModeSetting modeSetting) {
            String text = modeSetting.getValue();
            drawText(
                    text,
                    x + width - textWidth(text, 9) - 8,
                    y + 1,
                    9,
                    PoorestTheme.accentBright(),
                    screenWidth,
                    screenHeight
            );
        }
    }

    private static void renderSettings(
            float x,
            float y,
            float width,
            float height,
            float screenWidth,
            float screenHeight
    ) {
        float areaX = x + 18;
        float areaY = y + HEADER + 10;
        float areaWidth = width - 36;
        float areaHeight = height - HEADER - 46;
        GLRenderer.pushScissor(areaX, areaY, areaWidth, areaHeight);

        drawText(
                PoorestLang.text("poorest.ui.appearance", "Appearance"),
                areaX,
                areaY + 2,
                15,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.text("poorest.ui.client_settings", "Client interface settings"),
                areaX,
                areaY + 24,
                8,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );

        float sectionY = areaY + 54 - settingScroll;

        drawSettingsLabel(
                "Accent Color",
                areaX,
                sectionY,
                screenWidth,
                screenHeight
        );

        for (int i = 0; i < ACCENTS.length; i++) {
            float swatchX = areaX + i * 34;

            GLRenderer.roundedRect(
                    swatchX,
                    sectionY + 22,
                    26,
                    26,
                    8,
                    ACCENTS[i],
                    screenWidth,
                    screenHeight
            );

            if ((PoorestTheme.accent() & 0x00FFFFFF) ==
                    (ACCENTS[i] & 0x00FFFFFF)) {
                GLRenderer.outline(
                        swatchX - 1,
                        sectionY + 21,
                        28,
                        28,
                        9,
                        2,
                        PoorestTheme.text(),
                        screenWidth,
                        screenHeight
                );
            }
        }

        sectionY += 70;

        drawSlider(
                "UI Scale",
                sectionY,
                PoorestTheme.uiScale(),
                0.85F,
                1.15F,
                0.01F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 62;

        drawSlider(
                PoorestLang.text("poorest.ui.menu_text_size", "Menu Text Size"),
                sectionY,
                PoorestTheme.uiTextScale(),
                0.80F,
                1.50F,
                0.05F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 62;

        drawSlider(
                PoorestLang.text("poorest.ui.hud_text_size", "HUD Text Size"),
                sectionY,
                PoorestTheme.hudTextScale(),
                0.75F,
                2.0F,
                0.05F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 62;

        drawSlider(
                "Brightness",
                sectionY,
                PoorestTheme.brightness(),
                0.65F,
                1.25F,
                0.01F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 62;

        drawSlider(
                "Opacity",
                sectionY,
                PoorestTheme.opacity(),
                0.70F,
                1.0F,
                0.01F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 62;

        drawSlider(
                "Rounding",
                sectionY,
                PoorestTheme.rounding(),
                8.0F,
                28.0F,
                1.0F,
                areaX,
                width,
                screenWidth,
                screenHeight
        );

        sectionY += 64;

        drawText(
                "Shadows",
                areaX,
                sectionY,
                10,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        renderToggle(
                areaX + width - 82,
                sectionY - 3,
                PoorestTheme.shadows(),
                screenWidth,
                screenHeight
        );

        sectionY += 46;

        GLRenderer.roundedRect(
                areaX,
                sectionY,
                120,
                34,
                12,
                PoorestTheme.accentSurface(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.text("poorest.ui.reset", "Reset UI"),
                areaX + 31,
                sectionY + 11,
                9,
                PoorestTheme.accentBright(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                areaX + 132,
                sectionY,
                132,
                34,
                12,
                PoorestTheme.accentSurface(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.text("poorest.ui.hud_editor", "HUD Editor  [F8]"),
                areaX + 151,
                sectionY + 11,
                8,
                PoorestTheme.accentBright(),
                screenWidth,
                screenHeight
        );

        drawText(
                PoorestLang.text("poorest.ui.drag_hint", "Drag HUD panels to move them. Use the right handle or wheel to resize."),
                areaX,
                sectionY + 52,
                8,
                PoorestTheme.secondary(),
                screenWidth,
                screenHeight
        );

        sectionY += 78;
        drawText(
                "Config Profiles",
                areaX,
                sectionY,
                11,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(areaX, sectionY + 22, 190, 32, 10, PoorestTheme.surface(), screenWidth, screenHeight);
        drawText(profileName, areaX + 10, sectionY + 32, 9, PoorestTheme.text(), screenWidth, screenHeight);
        GLRenderer.outline(areaX, sectionY + 22, 190, 32, 10, 1, profileFocused ? PoorestTheme.accent() : PoorestTheme.border(), screenWidth, screenHeight);

        GLRenderer.roundedRect(areaX + 198, sectionY + 22, 92, 32, 10, PoorestTheme.accentSurface(), screenWidth, screenHeight);
        drawText("Save CFG", areaX + 217, sectionY + 32, 9, PoorestTheme.accentBright(), screenWidth, screenHeight);

        GLRenderer.roundedRect(areaX + 298, sectionY + 22, 92, 32, 10, PoorestTheme.accentSurface(), screenWidth, screenHeight);
        drawText("Load CFG", areaX + 317, sectionY + 32, 9, PoorestTheme.accentBright(), screenWidth, screenHeight);

        String profiles = String.join(", ", ClientConfigManager.profiles());
        if (profiles.length() > 80) profiles = profiles.substring(0, 77) + "...";
        drawText("Saved: " + (profiles.isBlank() ? "none" : profiles), areaX, sectionY + 72, 8, PoorestTheme.muted(), screenWidth, screenHeight);

        sectionY += 112;
        drawText(PoorestLang.text("poorest.ui.display", "Display"), areaX, sectionY, 11, PoorestTheme.text(), screenWidth, screenHeight);
        drawText(PoorestLang.text("poorest.ui.monitor_modes", "Monitor-supported resolutions and aspect ratios"), areaX, sectionY + 18, 8, PoorestTheme.muted(), screenWidth, screenHeight);

        float fullscreenX = areaX + areaWidth - 110;
        GLRenderer.roundedRect(fullscreenX, sectionY - 7, 92, 28, 10,
                DisplayManager.fullscreen() ? PoorestTheme.accentSurface() : PoorestTheme.surface(), screenWidth, screenHeight);
        drawText(DisplayManager.fullscreen() ? PoorestLang.text("poorest.ui.fullscreen", "Fullscreen") : PoorestLang.text("poorest.ui.windowed", "Windowed"), fullscreenX + 10, sectionY + 2, 8,
                DisplayManager.fullscreen() ? PoorestTheme.accentBright() : PoorestTheme.secondary(), screenWidth, screenHeight);

        drawText(PoorestLang.text("poorest.ui.aspect", "Aspect"), areaX, sectionY + 44, 9, PoorestTheme.secondary(), screenWidth, screenHeight);
        float aspectX = areaX + 58;
        for (DisplayManager.Aspect value : DisplayManager.Aspect.values()) {
            float aw = value == DisplayManager.Aspect.ALL ? 42 : 52;
            boolean active = DisplayManager.aspect() == value;
            GLRenderer.roundedRect(aspectX, sectionY + 36, aw, 26, 9,
                    active ? PoorestTheme.accentSurface() : PoorestTheme.surface(), screenWidth, screenHeight);
            drawText(value.label(), aspectX + (aw - textWidth(value.label(), 8)) * 0.5F, sectionY + 44, 8,
                    active ? PoorestTheme.accentBright() : PoorestTheme.secondary(), screenWidth, screenHeight);
            aspectX += aw + 6;
        }

        drawText(PoorestLang.text("poorest.ui.supported_monitor", "Supported by this monitor"), areaX, sectionY + 76, 9, PoorestTheme.secondary(), screenWidth, screenHeight);
        List<DisplayManager.Mode> displayModes = DisplayManager.supportedModes();
        int visibleModes = displayModes.size();
        for (int i = 0; i < visibleModes; i++) {
            DisplayManager.Mode mode = displayModes.get(i);
            int row = i / 2;
            int col = i % 2;
            float bx = areaX + col * ((areaWidth - 8) / 2.0F);
            float by = sectionY + 98 + row * 34;
            float bw = (areaWidth - 8) / 2.0F;
            DisplayManager.Mode current = DisplayManager.currentMode();
            boolean active = mode.width() == current.width() && mode.height() == current.height();
            GLRenderer.roundedRect(bx, by, bw, 28, 9,
                    active ? PoorestTheme.accentSurface() : PoorestTheme.surface(), screenWidth, screenHeight);
            drawText(mode.fullLabel(), bx + 10, by + 8, 8,
                    active ? PoorestTheme.accentBright() : PoorestTheme.secondary(), screenWidth, screenHeight);
        }
        GLRenderer.popScissor();
    }

    private static void drawSlider(
            String label,
            float y,
            float value,
            float min,
            float max,
            float step,
            float x,
            float width,
            float screenWidth,
            float screenHeight
    ) {
        drawText(
                label,
                x,
                y,
                10,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );

        String valueText =
                formatNumber(value);

        drawText(
                valueText,
                x + width - 85,
                y,
                8,
                PoorestTheme.secondary(),
                screenWidth,
                screenHeight
        );

        float trackX = x;
        float trackY = y + 26;
        float trackWidth = width - 18;

        float normalized =
                clamp(
                        (value - min) / (max - min),
                        0,
                        1
                );

        GLRenderer.roundedRect(
                trackX,
                trackY,
                trackWidth,
                4,
                2,
                PoorestTheme.switchOff(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                trackX,
                trackY,
                trackWidth * normalized,
                4,
                2,
                PoorestTheme.accent(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                trackX + trackWidth * normalized - 5,
                trackY - 3,
                10,
                10,
                5,
                PoorestTheme.accentBright(),
                screenWidth,
                screenHeight
        );
    }

    private static void drawText(
            String text,
            float x,
            float y,
            float size,
            int color,
            float screenWidth,
            float screenHeight
    ) {
        PoorestFont.drawString(text, x, y, size * PoorestTheme.uiTextScale(), color, screenWidth, screenHeight);
    }

    private static float textWidth(String text, float size) {
        return PoorestFont.width(text, size * PoorestTheme.uiTextScale());
    }

    private static void drawSettingsLabel(
            String text,
            float x,
            float y,
            float screenWidth,
            float screenHeight
    ) {
        drawText(
                text,
                x,
                y,
                10,
                PoorestTheme.text(),
                screenWidth,
                screenHeight
        );
    }

    private static void renderToggle(
            float x,
            float y,
            boolean enabled,
            float screenWidth,
            float screenHeight
    ) {
        GLRenderer.roundedRect(
                x,
                y,
                36,
                20,
                10,
                enabled
                        ? PoorestTheme.switchOn()
                        : PoorestTheme.switchOff(),
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                enabled ? x + 18 : x + 3,
                y + 3,
                14,
                14,
                7,
                enabled
                        ? PoorestTheme.switchKnobOn()
                        : PoorestTheme.switchKnobOff(),
                screenWidth,
                screenHeight
        );
    }

    private static void renderFooter(
            float x,
            float y,
            float width,
            float height,
            float screenWidth,
            float screenHeight
    ) {
        int enabled = (int) ModuleManager
                .getModules()
                .stream()
                .filter(Module::isEnabled)
                .count();

        int total = ModuleManager
                .getModules()
                .size();

        drawText(
                enabled + " enabled  •  " + total + " modules",
                x + 18,
                y + height - 20,
                8,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );

        String right = settings
                ? "ESC  " + PoorestLang.text("poorest.ui.back", "Back")
                : selectedModule != null
                ? "ESC  " + PoorestLang.text("poorest.ui.back", "Back")
                : "Right Shift  " + PoorestLang.text("poorest.ui.close", "Close");

        drawText(
                right,
                x + width - textWidth(right, 8) - 18,
                y + height - 20,
                8,
                PoorestTheme.muted(),
                screenWidth,
                screenHeight
        );
    }

    private static void renderResizeGrip(
            float x,
            float y,
            float width,
            float height,
            float screenWidth,
            float screenHeight
    ) {
        float gx = x + width - 20;
        float gy = y + height - 20;

        int color = resizing
                ? PoorestTheme.accentBright()
                : PoorestTheme.muted();

        GLRenderer.roundedRect(
                gx + 9,
                gy + 9,
                3,
                3,
                1,
                color,
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                gx + 4,
                gy + 9,
                3,
                3,
                1,
                color,
                screenWidth,
                screenHeight
        );

        GLRenderer.roundedRect(
                gx + 9,
                gy + 4,
                3,
                3,
                1,
                color,
                screenWidth,
                screenHeight
        );
    }

    public static boolean handleMousePressed(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!open) {
            return false;
        }

        if (button != 0 && button != 1) {
            return true;
        }

        double[] physical = toPhysical(mouseX, mouseY);
        mouseX = physical[0];
        mouseY = physical[1];

        if (closing) {
            return true;
        }

        float panelWidth = width;
        float panelHeight = height;

        if (button == 0 &&
                inside(
                        mouseX,
                        mouseY,
                        x + panelWidth - 30,
                        y + panelHeight - 30,
                        30,
                        30
                )) {
            resizing = true;
            resizeStartX = (float) mouseX;
            resizeStartY = (float) mouseY;
            resizeStartWidth = width;
            resizeStartHeight = height;
            return true;
        }

        if (button == 0 &&
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        panelWidth,
                        HEADER
                )) {
            float settingsX = x + width - 26;

            if (inside(
                    mouseX,
                    mouseY,
                    settingsX - 24,
                    y + 6,
                    42,
                    52
            )) {
                settings = !settings;
                selectedModule = null;
                searchFocused = false;
                return true;
            }

            if (!settings && selectedModule == null) {
                float searchWidth = Math.min(
                        190,
                        width * 0.29F
                );

                float searchX =
                        x + width - searchWidth - 48;

                if (inside(
                        mouseX,
                        mouseY,
                        searchX,
                        y + 15,
                        searchWidth,
                        34
                )) {
                    searchFocused = true;
                    return true;
                }
            }

            dragging = true;
            dragOffsetX = (float) mouseX - x;
            dragOffsetY = (float) mouseY - y;
            return true;
        }

        if (settings) {
            return handleSettingsClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (selectedModule != null) {
            if (button == 0 &&
                    inside(
                            mouseX,
                            mouseY,
                            x + 18,
                            y + HEADER + 10,
                            200,
                            38
                    )) {
                selectedModule = null;
                settingScroll = 0;
                return true;
            }

            return handleModuleSettingsClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (handleCategoryClick(
                mouseX,
                mouseY
        )) {
            return true;
        }

        return handleModuleClick(
                mouseX,
                mouseY,
                button
        );
    }

    private static boolean handleSettingsClick(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return true;
        }

        float contentX = x + 18;
        float contentY = y + HEADER + 10 - settingScroll;

        for (int i = 0; i < ACCENTS.length; i++) {
            float sx = contentX + i * 34;
            float sy = contentY + 54 + 22;

            if (inside(mouseX, mouseY, sx, sy, 26, 26)) {
                PoorestTheme.setAccent(
                        ACCENTS[i]
                );
                return true;
            }
        }

        float hudEditorY = contentY + 54 + 70 + 62 + 62 + 62 + 62 + 62 + 64 + 46;

        if (inside(
                mouseX,
                mouseY,
                contentX + 132,
                hudEditorY,
                132,
                34
        )) {
            PoorestHudLayout.setEditing(
                    !PoorestHudLayout.isEditing()
            );
            return true;
        }

        float sliderY = contentY + 54 + 70;

        if (inside(
                mouseX,
                mouseY,
                contentX,
                sliderY,
                width - 36,
                38
        )) {
            setSliderFromMouse(
                    mouseX,
                    contentX,
                    width - 36,
                    0.85F,
                    1.15F,
                    SettingTarget.UI_SCALE
            );
            return true;
        }

        sliderY += 62;

        if (inside(mouseX, mouseY, contentX, sliderY, width - 36, 38)) {
            setSliderFromMouse(mouseX, contentX, width - 36, 0.80F, 1.50F, SettingTarget.UI_TEXT_SCALE);
            return true;
        }

        sliderY += 62;

        if (inside(mouseX, mouseY, contentX, sliderY, width - 36, 38)) {
            setSliderFromMouse(mouseX, contentX, width - 36, 0.75F, 2.0F, SettingTarget.HUD_TEXT_SCALE);
            return true;
        }

        sliderY += 62;

        if (inside(
                mouseX,
                mouseY,
                contentX,
                sliderY,
                width - 36,
                38
        )) {
            setSliderFromMouse(
                    mouseX,
                    contentX,
                    width - 36,
                    0.65F,
                    1.25F,
                    SettingTarget.BRIGHTNESS
            );
            return true;
        }

        sliderY += 62;

        if (inside(
                mouseX,
                mouseY,
                contentX,
                sliderY,
                width - 36,
                38
        )) {
            setSliderFromMouse(
                    mouseX,
                    contentX,
                    width - 36,
                    0.70F,
                    1.0F,
                    SettingTarget.OPACITY
            );
            return true;
        }

        sliderY += 62;

        if (inside(
                mouseX,
                mouseY,
                contentX,
                sliderY,
                width - 36,
                38
        )) {
            setSliderFromMouse(
                    mouseX,
                    contentX,
                    width - 36,
                    8.0F,
                    28.0F,
                    SettingTarget.ROUNDING
            );
            return true;
        }

        sliderY += 64;

        if (inside(
                mouseX,
                mouseY,
                contentX + width - 100,
                sliderY - 10,
                45,
                32
        )) {
            PoorestTheme.setShadows(
                    !PoorestTheme.shadows()
            );
            return true;
        }

        sliderY += 46;

        if (inside(mouseX, mouseY, contentX, sliderY, 120, 34)) {
            reset();
            return true;
        }

        float profileY = sliderY + 78;
        if (inside(mouseX, mouseY, contentX, profileY, 190, 32)) {
            profileFocused = true;
            searchFocused = false;
            return true;
        }
        if (inside(mouseX, mouseY, contentX + 198, profileY, 92, 32)) {
            ClientConfigManager.saveProfile(profileName);
            return true;
        }
        if (inside(mouseX, mouseY, contentX + 298, profileY, 92, 32)) {
            ClientConfigManager.loadProfile(profileName);
            return true;
        }

        float displayY = profileY + 112;
        if (inside(mouseX, mouseY, contentX + width - 110, displayY - 7, 92, 28)) {
            DisplayManager.toggleFullscreen();
            return true;
        }

        float aspectX = contentX + 58;
        for (DisplayManager.Aspect value : DisplayManager.Aspect.values()) {
            float aw = value == DisplayManager.Aspect.ALL ? 42 : 52;
            if (inside(mouseX, mouseY, aspectX, displayY + 36, aw, 26)) {
                DisplayManager.setAspect(value);
                settingScroll = 0;
                return true;
            }
            aspectX += aw + 6;
        }

        List<DisplayManager.Mode> displayModes = DisplayManager.supportedModes();
        int visibleModes = displayModes.size();
        for (int i = 0; i < visibleModes; i++) {
            int row = i / 2;
            int col = i % 2;
            float bx = contentX + col * (((width - 36) - 8) / 2.0F);
            float by = displayY + 98 + row * 34;
            float bw = ((width - 36) - 8) / 2.0F;
            if (inside(mouseX, mouseY, bx, by, bw, 28)) {
                DisplayManager.apply(displayModes.get(i));
                return true;
            }
        }

        return true;
    }

    private enum SettingTarget {
        UI_SCALE,
        BRIGHTNESS,
        OPACITY,
        ROUNDING,
        HUD_TEXT_SCALE,
        UI_TEXT_SCALE
    }

    private static void setSliderFromMouse(
            double mouseX,
            float x,
            float width,
            float min,
            float max,
            SettingTarget target
    ) {
        float normalized = clamp(
                ((float) mouseX - x) / width,
                0,
                1
        );

        float value = min + (max - min) * normalized;

        switch (target) {
            case UI_SCALE -> PoorestTheme.setUiScale(value);
            case BRIGHTNESS -> PoorestTheme.setBrightness(value);
            case OPACITY -> PoorestTheme.setOpacity(value);
            case ROUNDING -> PoorestTheme.setRounding(value);
            case HUD_TEXT_SCALE -> PoorestTheme.setHudTextScale(value);
            case UI_TEXT_SCALE -> PoorestTheme.setUiTextScale(value);
        }
    }

    private static boolean handleModuleSettingsClick(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return true;
        }

        float areaX = x + 18;
        float areaY = y + HEADER + 10;
        float areaWidth = width - 36;
        float settingY = areaY + 56 - settingScroll;

        for (Setting<?> setting : selectedModule.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                if (inside(
                        mouseX,
                        mouseY,
                        areaX + areaWidth - 60,
                        settingY - 8,
                        60,
                        30
                )) {
                    booleanSetting.toggle();
                    return true;
                }
            } else if (setting instanceof NumberSetting numberSetting) {
                float sliderX = areaX + areaWidth - 180;
                float sliderWidth = 165;
                if (inside(
                        mouseX,
                        mouseY,
                        sliderX,
                        settingY + 20,
                        sliderWidth,
                        20
                )) {
                    float normalized = clamp(
                            ((float) mouseX - sliderX) /
                                    sliderWidth,
                            0,
                            1
                    );
                    double value =
                            numberSetting.getMin()
                            + normalized *
                            (numberSetting.getMax()
                            - numberSetting.getMin());

                    double step = numberSetting.getStep();
                    value = Math.round(value / step) * step;
                    numberSetting.setValue(value);
                    return true;
                }
            } else if (setting instanceof ModeSetting modeSetting) {
                if (inside(
                        mouseX,
                        mouseY,
                        areaX + areaWidth - 130,
                        settingY - 6,
                        130,
                        30
                )) {
                    modeSetting.cycle();
                    return true;
                }
            }

            settingY += 58;
        }

        return true;
    }

    private static boolean handleCategoryClick(
            double mouseX,
            double mouseY
    ) {
        float currentX = x + 18;
        float categoryY = y + HEADER + 10;

        for (ModuleCategory value : ModuleCategory.values()) {
            String text = formatCategory(value);
            float itemWidth =
                    textWidth(text, 10) + 22;

            if (inside(
                    mouseX,
                    mouseY,
                    currentX,
                    categoryY,
                    itemWidth,
                    NAV
            )) {
                category = value;
                selectedModule = null;
                scroll = 0;
                return true;
            }

            currentX += itemWidth + 2;
        }

        return false;
    }

    private static boolean handleModuleClick(
            double mouseX,
            double mouseY,
            int button
    ) {
        float areaX = x + 18;
        float areaY = y + HEADER + NAV + 16;
        float areaWidth = width - 36;
        float areaHeight = height - HEADER - NAV - 58;

        List<Module> modules = filteredModules();

        float gap = 9;
        float cardWidth =
                (areaWidth - 30 - gap) / 2.0F;
        float cardHeight = 68;
        float startY = areaY + 10 - scroll;

        for (int i = 0; i < modules.size(); i++) {
            int row = i / 2;
            int column = i % 2;

            float cardX = areaX + 10
                    + column * (cardWidth + gap);
            float cardY = startY
                    + row * (cardHeight + gap);

            if (!inside(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight
            )) {
                continue;
            }

            Module module = modules.get(i);

            boolean settingsButton = inside(
                    mouseX,
                    mouseY,
                    cardX + cardWidth - 88,
                    cardY + 12,
                    34,
                    44
            );

            if (settingsButton || button == 1) {
                selectedModule = module;
                settingScroll = 0;
            } else if (button == 0) {
                module.toggle();
            }

            return true;
        }

        return true;
    }

    public static boolean handleMouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        dragging = false;
        resizing = false;
        save();
        return open;
    }

    public static boolean handleMouseDragged(
            double mouseX,
            double mouseY,
            int button
    ) {
        return open;
    }

    public static boolean handleScroll(
            double mouseX,
            double mouseY,
            double delta
    ) {
        if (!open) {
            return false;
        }

        double[] physical = toPhysical(mouseX, mouseY);
        mouseX = physical[0];
        mouseY = physical[1];

        if (settings) {
            settingScroll = Math.max(
                    0,
                    settingScroll - (float) delta * 30
            );
            return true;
        }

        if (selectedModule != null) {
            settingScroll = Math.max(
                    0,
                    settingScroll - (float) delta * 28
            );
            return true;
        }

        float areaY = y + HEADER + NAV + 16;
        float areaHeight = height - HEADER - NAV - 58;

        if (inside(
                mouseX,
                mouseY,
                x,
                areaY,
                width,
                areaHeight
        )) {
            List<Module> modules = filteredModules();
            float rows = (modules.size() + 1) / 2.0F;
            float contentHeight = rows * 77.0F;
            float maxScroll = Math.max(
                    0,
                    contentHeight - areaHeight + 8
            );

            scroll = clamp(
                    scroll - (float) delta * 30,
                    0,
                    maxScroll
            );
        }

        return true;
    }

    public static boolean handleKeyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        InputConstants.Key key = InputConstants.getKey(
                keyCode,
                scanCode
        );

        if (KeybindManager.OPEN_MENU.isActiveAndMatches(key)) {
            toggle();
            return true;
        }

        if (!open) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused || profileFocused) {
                searchFocused = false;
                profileFocused = false;
                return true;
            }

            if (settings || selectedModule != null) {
                settings = false;
                selectedModule = null;
                return true;
            }

            close();
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) {
                    search = search.substring(
                            0,
                            search.length() - 1
                    );
                }
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }

            return true;
        }

        if (profileFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!profileName.isEmpty()) profileName = profileName.substring(0, profileName.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                profileFocused = false;
                return true;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (selectedModule != null) {
                settingScroll = Math.max(0, settingScroll - 30);
            } else {
                scroll = Math.max(0, scroll - 30);
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (selectedModule != null) {
                settingScroll += 30;
            } else {
                scroll += 30;
            }
            return true;
        }

        return false;
    }

    public static boolean handleCharTyped(char codePoint) {
        if (!open || (!searchFocused && !profileFocused)) {
            return false;
        }

        if (codePoint < 32) return true;
        if (searchFocused) {
            if (search.length() < 48) search += codePoint;
        } else if (profileFocused) {
            if (profileName.length() < 32) profileName += codePoint;
        }
        return true;
    }

    public static void reset() {
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        x = -1;
        y = -1;
        scroll = 0;
        settingScroll = 0;
        PoorestTheme.reset();
        save();
    }

    private static List<Module> filteredModules() {
        String query = search.toLowerCase(Locale.ROOT);

        return ModuleManager.getModules(category)
                .stream()
                .filter(module -> {
                    if (query.isBlank()) {
                        return true;
                    }

                    return PoorestLang.module(module).toLowerCase(Locale.ROOT).contains(query)
                            || PoorestLang.moduleDescription(module).toLowerCase(Locale.ROOT).contains(query);
                })
                .toList();
    }

    private static double[] toPhysical(
            double mouseX,
            double mouseY
    ) {
        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();
        return new double[]{
                mouseX * scale,
                mouseY * scale
        };
    }

    private static float physicalMouseX(double windowX) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        java.nio.IntBuffer buffer = org.lwjgl.BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(window, buffer, null);
        int windowWidth = Math.max(1, buffer.get(0));
        return (float) (windowX * mc.getWindow().getWidth() / windowWidth);
    }

    private static float physicalMouseY(double windowY) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        java.nio.IntBuffer buffer = org.lwjgl.BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(window, null, buffer);
        int windowHeight = Math.max(1, buffer.get(0));
        return (float) (windowY * mc.getWindow().getHeight() / windowHeight);
    }

    private static float scaledMouseX() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        double[] mx = {0};
        double[] my = {0};
        GLFW.glfwGetCursorPos(window, mx, my);
        return physicalMouseX(mx[0]);
    }

    private static float scaledMouseY() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        double[] mx = {0};
        double[] my = {0};
        GLFW.glfwGetCursorPos(window, mx, my);
        return physicalMouseY(my[0]);
    }

    private static void keepOnScreen() {
        Minecraft mc = Minecraft.getInstance();
        float sw = mc.getWindow().getWidth();
        float sh = mc.getWindow().getHeight();

        x = clamp(x, 8, Math.max(8, sw - width - 8));
        y = clamp(y, 8, Math.max(8, sh - height - 8));
    }

    private static float getAnimation() {
        long elapsed = System.currentTimeMillis() - animationStart;
        float duration = closing ? 170.0F : 220.0F;
        float progress = clamp(
                elapsed / duration,
                0,
                1
        );

        if (closing) {
            return 1.0F - progress * progress * progress;
        }

        float inverse = 1.0F - progress;
        return 1.0F - inverse * inverse * inverse;
    }

    private static float animate(
            float current,
            float target,
            float speed
    ) {
        return current + (target - current) * speed;
    }

    private static float clamp(
            float value,
            float min,
            float max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean inside(
            double mx,
            double my,
            double x,
            double y,
            double width,
            double height
    ) {
        return mx >= x && mx <= x + width
                && my >= y && my <= y + height;
    }

    public static String slug(String value) {
        return value == null ? "unknown" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private static String formatCategory(
            ModuleCategory category
    ) {
        String value = category.name()
                .toLowerCase(Locale.ROOT);
        return PoorestLang.category(value);
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.0001) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static int colorWithAlpha(
            int rgb,
            int alpha
    ) {
        return (Math.max(0, Math.min(255, alpha)) << 24)
                | (rgb & 0x00FFFFFF);
    }

    private static int blend(
            int from,
            int to,
            float amount,
            int alpha
    ) {
        amount = clamp(amount, 0, 1);

        int fr = (from >>> 16) & 0xFF;
        int fg = (from >>> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >>> 16) & 0xFF;
        int tg = (to >>> 8) & 0xFF;
        int tb = to & 0xFF;

        int r = (int) (fr + (tr - fr) * amount);
        int g = (int) (fg + (tg - fg) * amount);
        int b = (int) (fb + (tb - fb) * amount);

        return (alpha << 24)
                | (r << 16)
                | (g << 8)
                | b;
    }

    private static void load() {
        try {
            if (!Files.exists(CONFIG)) {
                return;
            }

            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(CONFIG)) {
                properties.load(input);
            }

            width = clamp(
                    Float.parseFloat(
                            properties.getProperty(
                                    "width",
                                    Float.toString(DEFAULT_WIDTH)
                            )
                    ),
                    MIN_WIDTH,
                    MAX_WIDTH
            );

            height = clamp(
                    Float.parseFloat(
                            properties.getProperty(
                                    "height",
                                    Float.toString(DEFAULT_HEIGHT)
                            )
                    ),
                    MIN_HEIGHT,
                    MAX_HEIGHT
            );

            x = Float.parseFloat(
                    properties.getProperty("x", "-1")
            );

            y = Float.parseFloat(
                    properties.getProperty("y", "-1")
            );
        } catch (Exception ignored) {
            width = DEFAULT_WIDTH;
            height = DEFAULT_HEIGHT;
            x = -1;
            y = -1;
        }
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG.getParent());

            Properties properties = new Properties();
            properties.setProperty("width", Float.toString(width));
            properties.setProperty("height", Float.toString(height));
            properties.setProperty("x", Float.toString(x));
            properties.setProperty("y", Float.toString(y));

            try (OutputStream output = Files.newOutputStream(CONFIG)) {
                properties.store(output, "Poorest Visuals UI");
            }
        } catch (IOException ignored) {
        }
    }
}
