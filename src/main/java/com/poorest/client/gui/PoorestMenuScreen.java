package com.poorest.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.poorest.client.PoorestClient;
import com.poorest.client.core.keybind.KeybindManager;
import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.gui.render.PoorestRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public final class PoorestMenuScreen extends Screen {

    /*
     * ============================================================
     * Layout
     * ============================================================
     */

    private static final int PANEL_WIDTH = 900;
    private static final int PANEL_HEIGHT = 540;

    private static final int TOP_BAR_HEIGHT = 82;

    private static final int CONTENT_PADDING = 30;
    private static final int CONTENT_GAP = 14;

    private static final int CARD_HEIGHT = 82;

    /*
     * ============================================================
     * Colors
     * ============================================================
     */

    private static final int BACKGROUND_COLOR = 0xB907080C;

    private static final int PANEL_COLOR = 0xEC0D0E14;
    private static final int PANEL_TOP_COLOR = 0xF00F1017;

    private static final int CARD_COLOR = 0x9F12131A;
    private static final int CARD_HOVER_COLOR = 0xB51A1B25;

    private static final int CARD_ENABLED_COLOR = 0xB71A1726;

    private static final int BORDER_COLOR = 0x251F2029;
    private static final int BORDER_HOVER_COLOR = 0x553B354A;

    private static final int TEXT_PRIMARY = 0xFFF3F1F7;
    private static final int TEXT_SECONDARY = 0xFF8E8C97;
    private static final int TEXT_MUTED = 0xFF5E5C66;

    private static final int ACCENT = 0xFF9B6CFF;
    private static final int ACCENT_BRIGHT = 0xFFB28CFF;
    private static final int ACCENT_DARK = 0xFF5C3D9A;

    private static final int TOGGLE_OFF = 0xFF25262E;
    private static final int TOGGLE_KNOB_OFF = 0xFF65636D;

    private static final int TOGGLE_ON = 0xFF7045C9;
    private static final int TOGGLE_KNOB_ON = 0xFFF4F0FF;

    /*
     * ============================================================
     * Animation
     * ============================================================
     */

    private static final long OPEN_DURATION = 360L;
    private static final long CLOSE_DURATION = 240L;

    private long animationStart;

    private boolean closing;

    private ModuleCategory selectedCategory;

    private final float[] categoryAnimations;

    private final float[] moduleAnimations;

    /*
     * ============================================================
     * Constructor
     * ============================================================
     */

    public PoorestMenuScreen() {
        super(
                Component.literal(
                        PoorestClient.NAME
                )
        );

        selectedCategory = ModuleCategory.HUD;

        animationStart = System.currentTimeMillis();

        closing = false;

        categoryAnimations =
                new float[
                        ModuleCategory.values().length
                        ];

        moduleAnimations =
                new float[128];
    }

    /*
     * ============================================================
     * Screen lifecycle
     * ============================================================
     */

    @Override
    protected void init() {
        super.init();

        animationStart =
                System.currentTimeMillis();

        closing = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        releaseMovementKeys();

        super.removed();
    }

    /*
     * ============================================================
     * Main render
     * ============================================================
     */

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        float animation =
                getAnimation();

        if (animation <= 0.0F) {
            minecraft.setScreen(null);
            return;
        }

        renderBackground(
                graphics,
                animation
        );

        int panelX =
                (width - PANEL_WIDTH) / 2;

        int panelY =
                (height - PANEL_HEIGHT) / 2;

        float scale =
                0.94F +
                        animation * 0.06F;

        float offsetY =
                (1.0F - animation) * 16.0F;

        float centerX =
                panelX +
                        PANEL_WIDTH / 2.0F;

        float centerY =
                panelY +
                        PANEL_HEIGHT / 2.0F;

        graphics.pose().pushPose();

        graphics.pose().translate(
                centerX,
                centerY + offsetY,
                0.0F
        );

        graphics.pose().scale(
                scale,
                scale,
                1.0F
        );

        graphics.pose().translate(
                -centerX,
                -centerY,
                0.0F
        );

        renderPanel(
                graphics,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        graphics.pose().popPose();
    }

    /*
     * ============================================================
     * Background
     * ============================================================
     */

    private void renderBackground(
            GuiGraphics graphics,
            float animation
    ) {
        int alpha =
                (int) (
                        150.0F *
                                animation
                );

        alpha =
                Math.max(
                        0,
                        Math.min(
                                255,
                                alpha
                        )
                );

        int color =
                (alpha << 24) |
                        (BACKGROUND_COLOR & 0x00FFFFFF);

        graphics.fill(
                0,
                0,
                width,
                height,
                color
        );

        /*
         * Very subtle center glow.
         *
         * This is intentionally weak so the GUI
         * remains readable without looking like
         * a giant purple overlay.
         */

        int glowAlpha =
                (int) (
                        18.0F *
                                animation
                );

        int glow =
                (glowAlpha << 24) |
                        0x6E4CB0;

        int glowLeft =
                width / 2 - 260;

        int glowRight =
                width / 2 + 260;

        int glowTop =
                height / 2 - 170;

        int glowBottom =
                height / 2 + 170;

        graphics.fill(
                glowLeft,
                glowTop,
                glowRight,
                glowBottom,
                glow
        );
    }

    /*
     * ============================================================
     * Panel
     * ============================================================
     */

    private void renderPanel(
            GuiGraphics graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int right =
                panelX +
                        PANEL_WIDTH;

        int bottom =
                panelY +
                        PANEL_HEIGHT;

        /*
         * Shadow
         */

        PoorestRenderer.shadow(
                graphics,
                panelX - 16,
                panelY - 16,
                right + 16,
                bottom + 16,
                28,
                0x78000000
        );

        /*
         * Main panel
         */

        PoorestRenderer.roundedRect(
                graphics,
                panelX,
                panelY,
                right,
                bottom,
                22,
                PANEL_COLOR
        );

        /*
         * Top bar
         */

        PoorestRenderer.roundedRect(
                graphics,
                panelX,
                panelY,
                right,
                panelY + TOP_BAR_HEIGHT,
                22,
                PANEL_TOP_COLOR
        );

        /*
         * Cover the lower rounded part of top bar.
         */

        graphics.fill(
                panelX,
                panelY + 28,
                right,
                panelY + TOP_BAR_HEIGHT,
                PANEL_TOP_COLOR
        );

        /*
         * Border
         */

        PoorestRenderer.outline(
                graphics,
                panelX,
                panelY,
                right,
                bottom,
                22,
                BORDER_COLOR
        );

        /*
         * Top separator
         */

        graphics.fill(
                panelX + CONTENT_PADDING,
                panelY + TOP_BAR_HEIGHT - 1,
                right - CONTENT_PADDING,
                panelY + TOP_BAR_HEIGHT,
                0x301E1F28
        );

        renderTopBar(
                graphics,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        renderContent(
                graphics,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        renderFooter(
                graphics,
                panelX,
                panelY
        );
    }

    /*
     * ============================================================
     * Top bar
     * ============================================================
     */

    private void renderTopBar(
            GuiGraphics graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int x =
                panelX +
                        CONTENT_PADDING;

        int y =
                panelY +
                        18;

        /*
         * Logo
         */

        PoorestRenderer.roundedRect(
                graphics,
                x,
                y,
                x + 42,
                y + 42,
                12,
                0xFF17131F
        );

        PoorestRenderer.outline(
                graphics,
                x,
                y,
                x + 42,
                y + 42,
                12,
                0x559B6CFF
        );

        graphics.drawString(
                font,
                "P",
                x + 14,
                y + 12,
                ACCENT_BRIGHT,
                false
        );

        /*
         * Brand
         */

        graphics.drawString(
                font,
                "POOREST",
                x + 54,
                y + 6,
                TEXT_PRIMARY,
                false
        );

        graphics.drawString(
                font,
                "CLIENT",
                x + 54,
                y + 20,
                TEXT_MUTED,
                false
        );

        /*
         * Version
         */

        graphics.drawString(
                font,
                "v" + PoorestClient.VERSION,
                x + 54,
                y + 34,
                TEXT_MUTED,
                false
        );

        /*
         * Search box
         */

        int searchWidth = 190;
        int searchHeight = 38;

        int searchX =
                panelX +
                        PANEL_WIDTH -
                        CONTENT_PADDING -
                        searchWidth -
                        42;

        int searchY =
                panelY +
                        20;

        PoorestRenderer.roundedRect(
                graphics,
                searchX,
                searchY,
                searchX + searchWidth,
                searchY + searchHeight,
                19,
                0x60171921
        );

        PoorestRenderer.outline(
                graphics,
                searchX,
                searchY,
                searchX + searchWidth,
                searchY + searchHeight,
                19,
                0x251F2029
        );

        /*
         * Search icon
         */

        int iconX =
                searchX + 14;

        int iconY =
                searchY + 11;

        graphics.fill(
                iconX,
                iconY,
                iconX + 10,
                iconY + 2,
                0xFF5E5C68
        );

        graphics.fill(
                iconX,
                iconY + 2,
                iconX + 2,
                iconY + 10,
                0xFF5E5C68
        );

        graphics.fill(
                iconX + 2,
                iconY + 8,
                iconX + 8,
                iconY + 10,
                0xFF5E5C68
        );

        graphics.fill(
                iconX + 8,
                iconY + 8,
                iconX + 12,
                iconY + 12,
                0xFF5E5C68
        );

        graphics.drawString(
                font,
                "Search",
                searchX + 34,
                searchY + 13,
                TEXT_MUTED,
                false
        );

        /*
         * Theme button
         */

        int themeX =
                panelX +
                        PANEL_WIDTH -
                        CONTENT_PADDING -
                        30;

        int themeY =
                panelY +
                        25;

        drawThemeIcon(
                graphics,
                themeX,
                themeY
        );
    }

    private void drawThemeIcon(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        /*
         * Small sun-like icon.
         */

        int centerX = x + 7;
        int centerY = y + 7;

        graphics.fill(
                centerX - 3,
                centerY - 3,
                centerX + 4,
                centerY + 4,
                0xFF77717F
        );

        graphics.fill(
                centerX,
                centerY - 10,
                centerX + 1,
                centerY - 6,
                0xFF77717F
        );

        graphics.fill(
                centerX,
                centerY + 7,
                centerX + 1,
                centerY + 11,
                0xFF77717F
        );

        graphics.fill(
                centerX - 10,
                centerY,
                centerX - 6,
                centerY + 1,
                0xFF77717F
        );

        graphics.fill(
                centerX + 7,
                centerY,
                centerX + 11,
                centerY + 1,
                0xFF77717F
        );
    }

    /*
     * ============================================================
     * Content
     * ============================================================
     */

    private void renderContent(
            GuiGraphics graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int contentX =
                panelX +
                        CONTENT_PADDING;

        int contentY =
                panelY +
                        TOP_BAR_HEIGHT +
                        18;

        int contentWidth =
                PANEL_WIDTH -
                        CONTENT_PADDING * 2;

        renderCategoryNavigation(
                graphics,
                contentX,
                contentY,
                contentWidth,
                mouseX,
                mouseY
        );

        int cardsY =
                contentY + 54;

        renderModuleGrid(
                graphics,
                contentX,
                cardsY,
                contentWidth,
                mouseX,
                mouseY
        );
    }

    /*
     * ============================================================
     * Categories
     * ============================================================
     */

    private void renderCategoryNavigation(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        ModuleCategory[] categories =
                ModuleCategory.values();

        int currentX = x;

        for (int i = 0;
             i < categories.length;
             i++) {

            ModuleCategory category =
                    categories[i];

            String name =
                    formatCategoryName(
                            category
                    );

            int textWidth =
                    font.width(name);

            int itemWidth =
                    textWidth + 28;

            int itemHeight =
                    34;

            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            currentX,
                            y,
                            itemWidth,
                            itemHeight
                    );

            boolean selected =
                    selectedCategory ==
                            category;

            float target =
                    selected || hovered
                            ? 1.0F
                            : 0.0F;

            categoryAnimations[i] =
                    animate(
                            categoryAnimations[i],
                            target,
                            0.18F
                    );

            float animation =
                    categoryAnimations[i];

            /*
             * Selected background.
             */

            if (selected) {
                int selectedColor =
                        lerpColor(
                                0x401C1825,
                                0x802A2138,
                                animation
                        );

                PoorestRenderer.roundedRect(
                        graphics,
                        currentX,
                        y,
                        currentX + itemWidth,
                        y + itemHeight,
                        17,
                        selectedColor
                );

                /*
                 * Accent line.
                 */

                PoorestRenderer.roundedRect(
                        graphics,
                        currentX + 9,
                        y + itemHeight - 4,
                        currentX + itemWidth - 9,
                        y + itemHeight - 2,
                        2,
                        ACCENT
                );
            } else if (animation > 0.0F) {
                int hoverColor =
                        lerpColor(
                                0x00000000,
                                0x321A1B23,
                                animation
                        );

                PoorestRenderer.roundedRect(
                        graphics,
                        currentX,
                        y,
                        currentX + itemWidth,
                        y + itemHeight,
                        17,
                        hoverColor
                );
            }

            int textColor =
                    selected
                            ? TEXT_PRIMARY
                            : lerpColor(
                            TEXT_SECONDARY,
                            TEXT_PRIMARY,
                            animation
                    );

            graphics.drawString(
                    font,
                    name,
                    currentX + 14,
                    y + 11,
                    textColor,
                    false
            );

            currentX +=
                    itemWidth +
                            5;

            if (currentX >
                    x + width) {
                break;
            }
        }
    }

    /*
     * ============================================================
     * Module grid
     * ============================================================
     */

    private void renderModuleGrid(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        List<Module> modules =
                ModuleManager.getModules(
                        selectedCategory
                );

        if (modules.isEmpty()) {
            renderEmptyState(
                    graphics,
                    x,
                    y,
                    width
            );

            return;
        }

        int columnGap =
                CONTENT_GAP;

        int cardWidth =
                (width - columnGap) / 2;

        int leftX =
                x;

        int rightX =
                x +
                        cardWidth +
                        columnGap;

        int row = 0;

        for (int index = 0;
             index < modules.size();
             index++) {

            if (index >=
                    moduleAnimations.length) {
                break;
            }

            Module module =
                    modules.get(index);

            int column =
                    index % 2;

            if (column == 0) {
                row =
                        index / 2;
            }

            int cardX =
                    column == 0
                            ? leftX
                            : rightX;

            int cardY =
                    y +
                            row *
                                    (CARD_HEIGHT + CONTENT_GAP);

            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            cardX,
                            cardY,
                            cardWidth,
                            CARD_HEIGHT
                    );

            float target =
                    hovered
                            ? 1.0F
                            : 0.0F;

            moduleAnimations[index] =
                    animate(
                            moduleAnimations[index],
                            target,
                            0.20F
                    );

            renderModuleCard(
                    graphics,
                    module,
                    cardX,
                    cardY,
                    cardWidth,
                    CARD_HEIGHT,
                    moduleAnimations[index]
            );
        }
    }

    /*
     * ============================================================
     * Module card
     * ============================================================
     */

    private void renderModuleCard(
            GuiGraphics graphics,
            Module module,
            int x,
            int y,
            int width,
            int height,
            float hover
    ) {
        boolean enabled =
                module.isEnabled();

        int baseColor =
                enabled
                        ? CARD_ENABLED_COLOR
                        : CARD_COLOR;

        int cardColor =
                lerpColor(
                        baseColor,
                        CARD_HOVER_COLOR,
                        hover
                );

        /*
         * Card shadow.
         */

        if (hover > 0.01F) {
            PoorestRenderer.shadow(
                    graphics,
                    x - 2,
                    y - 2,
                    x + width + 2,
                    y + height + 2,
                    10,
                    0x28000000
            );
        }

        /*
         * Card.
         */

        PoorestRenderer.roundedRect(
                graphics,
                x,
                y,
                x + width,
                y + height,
                18,
                cardColor
        );

        /*
         * Card border.
         */

        int borderColor =
                lerpColor(
                        BORDER_COLOR,
                        BORDER_HOVER_COLOR,
                        hover
                );

        PoorestRenderer.outline(
                graphics,
                x,
                y,
                x + width,
                y + height,
                18,
                borderColor
        );

        /*
         * Enabled accent.
         */

        if (enabled) {
            PoorestRenderer.roundedRect(
                    graphics,
                    x + 1,
                    y + 18,
                    x + 3,
                    y + height - 18,
                    1,
                    ACCENT
            );
        }

        /*
         * Module name.
         */

        int titleColor =
                enabled
                        ? TEXT_PRIMARY
                        : lerpColor(
                        TEXT_SECONDARY,
                        TEXT_PRIMARY,
                        hover
                );

        graphics.drawString(
                font,
                module.getName(),
                x + 20,
                y + 18,
                titleColor,
                false
        );

        /*
         * Description.
         */

        String description =
                module.getDescription();

        if (description.length() > 42) {
            description =
                    description.substring(
                            0,
                            39
                    ) + "...";
        }

        graphics.drawString(
                font,
                description,
                x + 20,
                y + 42,
                TEXT_MUTED,
                false
        );

        /*
         * Toggle.
         */

        renderToggle(
                graphics,
                module,
                x + width - 56,
                y + 28
        );
    }

    /*
     * ============================================================
     * Toggle
     * ============================================================
     */

    private void renderToggle(
            GuiGraphics graphics,
            Module module,
            int x,
            int y
    ) {
        boolean enabled =
                module.isEnabled();

        int width = 38;
        int height = 22;

        int background =
                enabled
                        ? TOGGLE_ON
                        : TOGGLE_OFF;

        /*
         * Glow for enabled state.
         */

        if (enabled) {
            PoorestRenderer.shadow(
                    graphics,
                    x - 3,
                    y - 3,
                    x + width + 3,
                    y + height + 3,
                    9,
                    0x509B6CFF
            );
        }

        PoorestRenderer.roundedRect(
                graphics,
                x,
                y,
                x + width,
                y + height,
                11,
                background
        );

        int knobX =
                enabled
                        ? x + 19
                        : x + 3;

        int knobColor =
                enabled
                        ? TOGGLE_KNOB_ON
                        : TOGGLE_KNOB_OFF;

        PoorestRenderer.roundedRect(
                graphics,
                knobX,
                y + 3,
                knobX + 16,
                y + 19,
                8,
                knobColor
        );
    }

    /*
     * ============================================================
     * Empty state
     * ============================================================
     */

    private void renderEmptyState(
            GuiGraphics graphics,
            int x,
            int y,
            int width
    ) {
        int height = 160;

        PoorestRenderer.roundedRect(
                graphics,
                x,
                y,
                x + width,
                y + height,
                18,
                CARD_COLOR
        );

        PoorestRenderer.outline(
                graphics,
                x,
                y,
                x + width,
                y + height,
                18,
                BORDER_COLOR
        );

        String title =
                "Nothing here yet";

        String description =
                "Modules for this category will appear here.";

        int titleWidth =
                font.width(title);

        int descriptionWidth =
                font.width(description);

        /*
         * Small P icon.
         */

        int iconSize = 42;

        int iconX =
                x +
                        (width - iconSize) / 2;

        int iconY =
                y + 32;

        PoorestRenderer.roundedRect(
                graphics,
                iconX,
                iconY,
                iconX + iconSize,
                iconY + iconSize,
                12,
                0xFF18141F
        );

        graphics.drawString(
                font,
                "P",
                iconX + 14,
                iconY + 12,
                ACCENT,
                false
        );

        graphics.drawString(
                font,
                title,
                x +
                        (width - titleWidth) / 2,
                y + 87,
                TEXT_PRIMARY,
                false
        );

        graphics.drawString(
                font,
                description,
                x +
                        (width - descriptionWidth) / 2,
                y + 109,
                TEXT_SECONDARY,
                false
        );
    }

    /*
     * ============================================================
     * Footer
     * ============================================================
     */

    private void renderFooter(
            GuiGraphics graphics,
            int panelX,
            int panelY
    ) {
        int y =
                panelY +
                        PANEL_HEIGHT -
                        30;

        int x =
                panelX +
                        CONTENT_PADDING;

        /*
         * Left hint.
         */

        drawKeyHint(
                graphics,
                x,
                y,
                "SHIFT",
                "Open menu"
        );

        /*
         * Right hint.
         */

        String closeText =
                "ESC  Close";

        graphics.drawString(
                font,
                closeText,
                panelX +
                        PANEL_WIDTH -
                        CONTENT_PADDING -
                        font.width(closeText),
                y + 2,
                TEXT_MUTED,
                false
        );
    }

    private void drawKeyHint(
            GuiGraphics graphics,
            int x,
            int y,
            String key,
            String text
    ) {
        int keyWidth =
                font.width(key) + 12;

        PoorestRenderer.roundedRect(
                graphics,
                x,
                y - 3,
                x + keyWidth,
                y + 14,
                7,
                0x551C1D25
        );

        graphics.drawString(
                font,
                key,
                x + 6,
                y + 1,
                TEXT_SECONDARY,
                false
        );

        graphics.drawString(
                font,
                text,
                x + keyWidth + 7,
                y + 1,
                TEXT_MUTED,
                false
        );
    }

    /*
     * ============================================================
     * Mouse
     * ============================================================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (closing) {
            return true;
        }

        if (button !=
                GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        int panelX =
                (width - PANEL_WIDTH) / 2;

        int panelY =
                (height - PANEL_HEIGHT) / 2;

        if (!isInside(
                mouseX,
                mouseY,
                panelX,
                panelY,
                PANEL_WIDTH,
                PANEL_HEIGHT
        )) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (handleCategoryClick(
                mouseX,
                mouseY,
                panelX,
                panelY
        )) {
            return true;
        }

        if (handleModuleClick(
                mouseX,
                mouseY,
                panelX,
                panelY
        )) {
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private boolean handleCategoryClick(
            double mouseX,
            double mouseY,
            int panelX,
            int panelY
    ) {
        int x =
                panelX +
                        CONTENT_PADDING;

        int y =
                panelY +
                        TOP_BAR_HEIGHT +
                        18;

        ModuleCategory[] categories =
                ModuleCategory.values();

        int currentX = x;

        for (ModuleCategory category :
                categories) {

            String name =
                    formatCategoryName(
                            category
                    );

            int itemWidth =
                    font.width(name) +
                            28;

            int itemHeight =
                    34;

            if (isInside(
                    mouseX,
                    mouseY,
                    currentX,
                    y,
                    itemWidth,
                    itemHeight
            )) {
                selectedCategory =
                        category;

                resetModuleAnimations();

                return true;
            }

            currentX +=
                    itemWidth +
                            5;
        }

        return false;
    }

    private boolean handleModuleClick(
            double mouseX,
            double mouseY,
            int panelX,
            int panelY
    ) {
        int x =
                panelX +
                        CONTENT_PADDING;

        int y =
                panelY +
                        TOP_BAR_HEIGHT +
                        18 +
                        54;

        int width =
                PANEL_WIDTH -
                        CONTENT_PADDING * 2;

        int gap =
                CONTENT_GAP;

        int cardWidth =
                (width - gap) / 2;

        List<Module> modules =
                ModuleManager.getModules(
                        selectedCategory
                );

        for (int index = 0;
             index < modules.size();
             index++) {

            Module module =
                    modules.get(index);

            int column =
                    index % 2;

            int row =
                    index / 2;

            int cardX =
                    column == 0
                            ? x
                            : x +
                            cardWidth +
                            gap;

            int cardY =
                    y +
                            row *
                                    (CARD_HEIGHT + gap);

            if (isInside(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY,
                    cardWidth,
                    CARD_HEIGHT
            )) {
                module.toggle();

                return true;
            }
        }

        return false;
    }

    /*
     * ============================================================
     * Keyboard
     * ============================================================
     */

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        InputConstants.Key pressedKey =
                InputConstants.getKey(
                        keyCode,
                        scanCode
                );

        /*
         * The same key that opens the menu
         * closes it.
         */

        if (KeybindManager.OPEN_MENU
                .isActiveAndMatches(
                        pressedKey
                )) {

            closeWithAnimation();

            return true;
        }

        /*
         * Escape.
         */

        if (keyCode ==
                GLFW.GLFW_KEY_ESCAPE) {

            closeWithAnimation();

            return true;
        }

        /*
         * Forward normal movement keys.
         */

        handleMovementKey(
                pressedKey,
                true
        );

        return true;
    }

    @Override
    public boolean keyReleased(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        InputConstants.Key releasedKey =
                InputConstants.getKey(
                        keyCode,
                        scanCode
                );

        handleMovementKey(
                releasedKey,
                false
        );

        return true;
    }

    private void handleMovementKey(
            InputConstants.Key key,
            boolean pressed
    ) {
        if (minecraft == null) {
            return;
        }

        Minecraft mc =
                Minecraft.getInstance();

        if (matches(
                mc.options.keyUp,
                key
        )) {
            mc.options.keyUp.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keyDown,
                key
        )) {
            mc.options.keyDown.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keyLeft,
                key
        )) {
            mc.options.keyLeft.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keyRight,
                key
        )) {
            mc.options.keyRight.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keyJump,
                key
        )) {
            mc.options.keyJump.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keyShift,
                key
        )) {
            mc.options.keyShift.setDown(
                    pressed
            );
        }

        if (matches(
                mc.options.keySprint,
                key
        )) {
            mc.options.keySprint.setDown(
                    pressed
            );
        }
    }

    private boolean matches(
            KeyMapping mapping,
            InputConstants.Key key
    ) {
        return mapping
                .isActiveAndMatches(key);
    }

    private void releaseMovementKeys() {
        if (minecraft == null) {
            return;
        }

        Minecraft mc =
                Minecraft.getInstance();

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
        mc.options.keySprint.setDown(false);
    }

    /*
     * ============================================================
     * Close animation
     * ============================================================
     */

    public void closeWithAnimation() {
        if (closing) {
            return;
        }

        releaseMovementKeys();

        closing = true;

        animationStart =
                System.currentTimeMillis();
    }

    @Override
    public void onClose() {
        closeWithAnimation();
    }

    /*
     * ============================================================
     * Animation helpers
     * ============================================================
     */

    private float getAnimation() {
        long elapsed =
                System.currentTimeMillis()
                        - animationStart;

        if (!closing) {
            float progress =
                    elapsed /
                            (float) OPEN_DURATION;

            progress =
                    clamp(progress);

            return easeOutCubic(
                    progress
            );
        }

        float progress =
                elapsed /
                        (float) CLOSE_DURATION;

        progress =
                clamp(progress);

        return 1.0F -
                easeInCubic(
                        progress
                );
    }

    private static float easeOutCubic(
            float value
    ) {
        float inverse =
                1.0F - value;

        return 1.0F -
                inverse *
                        inverse *
                        inverse;
    }

    private static float easeInCubic(
            float value
    ) {
        return value *
                value *
                value;
    }

    private static float animate(
            float current,
            float target,
            float speed
    ) {
        return current +
                (target - current) *
                        speed;
    }

    private static float clamp(
            float value
    ) {
        return Math.max(
                0.0F,
                Math.min(
                        1.0F,
                        value
                )
        );
    }

    /*
     * ============================================================
     * Utility
     * ============================================================
     */

    private void resetModuleAnimations() {
        for (int i = 0;
             i < moduleAnimations.length;
             i++) {

            moduleAnimations[i] =
                    0.0F;
        }
    }

    private static boolean isInside(
            double mouseX,
            double mouseY,
            double x,
            double y,
            double width,
            double height
    ) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    private static int lerpColor(
            int from,
            int to,
            float progress
    ) {
        progress =
                clamp(progress);

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

    private static String formatCategoryName(
            ModuleCategory category
    ) {
        String name =
                category
                        .name()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return Character.toUpperCase(
                name.charAt(0)
        ) +
                name.substring(1);
    }
}