package com.poorest.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.poorest.client.core.keybind.KeybindManager;
import com.poorest.client.discord.DiscordPresence;
import com.poorest.client.core.ClientConfigManager;
import com.poorest.client.audio.PoorestSounds;
import com.poorest.client.modules.misc.HitSoundModule;
import com.poorest.client.modules.misc.InvMoveModule;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.modules.hud.ArraylistModule;
import com.poorest.client.modules.hud.ArmorHudModule;
import com.poorest.client.modules.hud.BiomeModule;
import com.poorest.client.modules.hud.ClockModule;
import com.poorest.client.modules.hud.CoordinatesModule;
import com.poorest.client.modules.hud.CpsModule;
import com.poorest.client.modules.hud.CrosshairModule;
import com.poorest.client.modules.hud.DirectionModule;
import com.poorest.client.modules.hud.FPSModule;
import com.poorest.client.modules.hud.InputTracker;
import com.poorest.client.modules.hud.KeystrokesModule;
import com.poorest.client.modules.hud.PingModule;
import com.poorest.client.modules.hud.PoorestHud;
import com.poorest.client.modules.hud.PoorestHudLayout;
import com.poorest.client.modules.hud.PotionHudModule;
import com.poorest.client.modules.hud.TargetHudModule;
import com.poorest.client.modules.hud.WatermarkModule;
import com.poorest.client.modules.visual.AmbienceModule;
import com.poorest.client.modules.visual.BlockHighlightModule;
import com.poorest.client.modules.visual.CleanCrosshairModule;
import com.poorest.client.modules.visual.ClearWeatherModule;
import com.poorest.client.modules.visual.ChinaHatModule;
import com.poorest.client.modules.visual.HitParticlesModule;
import com.poorest.client.modules.visual.ItemPhysicsModule;
import com.poorest.client.modules.visual.ColorfulCrosshairModule;
import com.poorest.client.modules.visual.CustomAnimationsModule;
import com.poorest.client.modules.visual.EntityShadowsModule;
import com.poorest.client.modules.visual.FullbrightModule;
import com.poorest.client.modules.visual.HitColorModule;
import com.poorest.client.modules.visual.PerformanceModule;
import com.poorest.client.modules.visual.HitboxesModule;
import com.poorest.client.modules.visual.HurtCamModule;
import com.poorest.client.modules.visual.JumpCirclesModule;
import com.poorest.client.modules.visual.NoDebuffsModule;
import com.poorest.client.modules.visual.NoFogModule;
import com.poorest.client.modules.visual.NoPumpkinOverlayModule;
import com.poorest.client.modules.visual.NoViewBobbingModule;
import com.poorest.client.modules.visual.OverlayBlockerModule;
import com.poorest.client.modules.visual.TimeChangerModule;
import com.poorest.client.modules.visual.TrailEffectsModule;
import com.poorest.client.modules.visual.WorldVisualRenderer;
import com.poorest.client.modules.visual.FovModule;
import com.poorest.client.modules.visual.ZoomModule;
import com.poorest.client.ui.PoorestUI;
import com.poorest.client.ui.PoorestTheme;
import com.poorest.client.ui.DisplayManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(PoorestClient.MOD_ID)
public final class PoorestClient {
    public static final String MOD_ID = "poorest_client";
    public static final String NAME = "Poorest Visuals";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PoorestClient(IEventBus modEventBus) {
        registerModules();
        PoorestHudLayout.load();
        PoorestTheme.load();
        DiscordPresence.start();
        modEventBus.addListener(KeybindManager::register);
        PoorestSounds.SOUNDS.register(modEventBus);
        ClientConfigManager.load();
        DisplayManager.load();
        LOGGER.info("{} {} initialized", NAME, VERSION);
    }

    private void registerModules() {
        ModuleManager.register(new FPSModule());
        ModuleManager.register(new WatermarkModule());
        ModuleManager.register(new CoordinatesModule());
        ModuleManager.register(new PingModule());
        ModuleManager.register(new ClockModule());
        ModuleManager.register(new DirectionModule());
        ModuleManager.register(new BiomeModule());
        ModuleManager.register(new CrosshairModule());
        ModuleManager.register(new TargetHudModule());
        ModuleManager.register(new KeystrokesModule());
        ModuleManager.register(new CpsModule());
        ModuleManager.register(new ArmorHudModule());
        ModuleManager.register(new PotionHudModule());
        ModuleManager.register(new ArraylistModule());

        ModuleManager.register(new FullbrightModule());
        ModuleManager.register(new PerformanceModule());
        ModuleManager.register(new ClearWeatherModule());
        ModuleManager.register(new NoPumpkinOverlayModule());
        ModuleManager.register(new BlockHighlightModule());
        ModuleManager.register(new TimeChangerModule());
        ModuleManager.register(new CleanCrosshairModule());
        ModuleManager.register(new NoDebuffsModule());
        ModuleManager.register(new ColorfulCrosshairModule());
        ModuleManager.register(new HitColorModule());
        ModuleManager.register(new EntityShadowsModule());
        ModuleManager.register(new HurtCamModule());
        ModuleManager.register(new FovModule());
        ModuleManager.register(new ZoomModule());
        ModuleManager.register(new AmbienceModule());
        ModuleManager.register(new NoFogModule());
        ModuleManager.register(new CustomAnimationsModule());
        ModuleManager.register(new JumpCirclesModule());
        ModuleManager.register(new TrailEffectsModule());
        ModuleManager.register(new HitboxesModule());
        ModuleManager.register(new ChinaHatModule());
        ModuleManager.register(new ItemPhysicsModule());
        ModuleManager.register(new HitParticlesModule());
        ModuleManager.register(new HitSoundModule());
        ModuleManager.register(new InvMoveModule());

        registerOverlay("No Vignette", "Removes the dark vignette around the screen edges.", "vignette");
        registerOverlay("No Fire Overlay", "Removes the fire overlay while burning.", "fire_overlay");
        registerOverlay("No Portal Overlay", "Removes the Nether portal overlay.", "portal_overlay");
        registerOverlay("No Spyglass Overlay", "Removes the spyglass scope overlay.", "spyglass");
        registerOverlay("No Powder Snow Overlay", "Removes the powder snow screen overlay.", "powder_snow_outline");
        registerOverlay("No Boss Bar", "Hides boss bar UI layers.", "boss_event_progress");
        registerOverlay("No Scoreboard", "Hides the vanilla scoreboard layer.", "scoreboard");
        registerOverlay("No Player List", "Hides the vanilla player list overlay.", "player_list");
        registerOverlay("No Block Overlay", "Removes the inside-block screen texture.", "block_overlay");

        // Performance is enabled after Minecraft has finished constructing the client.
        // Enabling it from the mod constructor touches LevelRenderer-backed options too early
        // on NeoForge 1.21.1 and can crash during mod loading.
        LOGGER.info("Registered {} modules", ModuleManager.getModules().size());
    }

    private void registerOverlay(String name, String description, String path) {
        ModuleManager.register(new OverlayBlockerModule(name, description, path));
    }

    @EventBusSubscriber(modid = MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        private static int configSaveTicks;

        @SubscribeEvent
        public static void onClientChat(ClientChatEvent event) {
            KeybindManager.handleChatCommand(event);
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (event.getEntity() == null || !event.getEntity().level().isClientSide()) return;
            HitSoundModule hitSound = module("Hit Sound", HitSoundModule.class);
            if (hitSound != null) hitSound.playForHit();
        }

        @SubscribeEvent
        public static void onCriticalHit(CriticalHitEvent event) {
            if (event.getEntity() == null || !event.getEntity().level().isClientSide()) return;
            HitSoundModule hitSound = module("Hit Sound", HitSoundModule.class);
            if (hitSound != null && event.isCriticalHit()) hitSound.playForCritical();
        }

        @SubscribeEvent
        public static void onPlaySound(PlaySoundEvent event) {
            HitSoundModule hitSound = module("Hit Sound", HitSoundModule.class);
            if (hitSound == null || !hitSound.isEnabled() || !hitSound.muteVanillaCritical()) return;
            if (event.getSound() != null && event.getSound().getLocation().toString().equals("minecraft:entity.player.attack.crit")) {
                event.setSound(null);
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            PoorestUI.tick();
            if (++configSaveTicks >= 100) {
                configSaveTicks = 0;
                ClientConfigManager.save();
            }
            ModuleManager.onTick();
            ZoomModule zoom = module("Zoom", ZoomModule.class);
            if (zoom != null && zoom.isEnabled()) {
                boolean down = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
                zoom.tickZoom(down);
            }
        }

        @SubscribeEvent
        public static void onVanillaGuiLayer(RenderGuiLayerEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) &&
                    (mc.screen != null || hasCustomCrosshair() || PoorestUI.isOpen())) {
                event.setCanceled(true);
                return;
            }
            String layerPath = event.getName().getPath();
            for (var module : ModuleManager.getModules()) {
                if (module instanceof OverlayBlockerModule blocker && module.isEnabled() && blocker.getLayerPath().equalsIgnoreCase(layerPath)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onBlockScreenEffect(RenderBlockScreenEffectEvent event) {
            if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && enabled("No Fire Overlay")) {
                event.setCanceled(true);
            } else if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK && enabled("No Block Overlay")) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onFov(ViewportEvent.ComputeFov event) {
            Minecraft mc = Minecraft.getInstance();
            ZoomModule zoom = module("Zoom", ZoomModule.class);
            if (zoom != null && zoom.isEnabled() && zoom.isZooming()) {
                double base = event.getFOV();
                double target = zoom.getZoomFov();
                double scale = zoom.getScale();
                event.setFOV(base + (target - base) * (1.0D - scale));
                return;
            }
            FovModule fov = module("FOV", FovModule.class);
            if (fov != null && fov.isEnabled()) {
                event.setFOV(fov.getFov());
            }
        }

        @SubscribeEvent
        public static void onFovModifier(ComputeFovModifierEvent event) {
            // Keep this hook available for future movement-FOV presets; the base FOV event handles the actual slider.
        }

        @SubscribeEvent
        public static void onCameraAngles(ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            HurtCamModule hurt = module("Hurt Cam", HurtCamModule.class);
            if (hurt == null || !hurt.isEnabled() || mc.player == null || mc.player.hurtTime <= 0) return;
            float progress = Math.min(1.0F, mc.player.hurtTime / 10.0F);
            float roll = (float) Math.sin(progress * Math.PI) * 4.0F * hurt.getStrength();
            event.setRoll(event.getRoll() + roll);
        }

        @SubscribeEvent
        public static void onFogColor(ViewportEvent.ComputeFogColor event) {
            AmbienceModule ambience = module("Ambience", AmbienceModule.class);
            if (ambience == null || !ambience.isEnabled()) return;
            event.setRed(ambience.red());
            event.setGreen(ambience.green());
            event.setBlue(ambience.blue());
        }

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            NoFogModule noFog = module("No Fog", NoFogModule.class);
            if (noFog == null || !noFog.isEnabled()) return;
            if (event.getMode() == FogMode.FOG_TERRAIN || event.getMode() == FogMode.FOG_SKY) {
                event.setNearPlaneDistance(100000.0F);
                event.setFarPlaneDistance(100000.0F);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            CustomAnimationsModule animations = module("Custom Animations", CustomAnimationsModule.class);
            if (animations == null || !animations.isEnabled()) return;
            if (animations.itemOnly() && event.getItemStack().isEmpty()) return;

            var pose = event.getPoseStack();
            float p = clamp01(event.getSwingProgress());
            float sign = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? 1.0F : -1.0F;

            float speed = animations.swingSpeed();
            float strength = animations.swingStrength();
            float curve = (float) Math.pow(Math.sin(p * Math.PI), 0.70F / Math.max(0.25F, speed));
            float forward = (float) Math.sin(p * Math.PI * 0.5F);
            float snap = (float) Math.sin(p * Math.PI);

            pose.translate(animations.positionX(), animations.positionY(), animations.positionZ());

            switch (animations.style()) {
                case "1.7 / Old 1.7" -> {
                    pose.translate(-0.10F * sign * curve * strength, 0.045F * curve * strength, -0.025F * forward * strength);
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 18.0F * curve * strength));
                    pose.mulPose(Axis.XP.rotationDegrees(-12.0F * curve * strength));
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * -8.0F * curve * strength));
                }
                case "1.8 / Vanilla" -> {
                    pose.translate(0.0F, 0.025F * curve * strength, -0.055F * forward * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-12.0F * curve * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 5.0F * snap * strength));
                }
                case "Smooth / Exhibition" -> {
                    float smooth = curve * curve;
                    pose.translate(-0.045F * sign * smooth * strength, 0.065F * smooth * strength, -0.09F * forward * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-20.0F * smooth * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 13.0F * smooth * strength));
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * -7.0F * smooth * strength));
                }
                case "Push / Punch" -> {
                    pose.translate(0.0F, 0.02F * snap * strength, -0.22F * forward * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-8.0F * snap * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 4.0F * snap * strength));
                }
                case "Slide" -> {
                    pose.translate(0.10F * sign * curve * strength, -0.03F * curve * strength, -0.08F * forward * strength);
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * 24.0F * curve * strength));
                    pose.mulPose(Axis.XP.rotationDegrees(-10.0F * curve * strength));
                }
                case "Stab / Poke" -> {
                    pose.translate(0.0F, 0.02F * curve * strength, -0.28F * forward * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-4.0F * curve * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 3.0F * curve * strength));
                }
                case "Swank" -> {
                    pose.translate(-0.07F * sign * curve * strength, 0.035F * curve * strength, -0.04F * forward * strength);
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * 30.0F * curve * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 20.0F * curve * strength));
                    pose.mulPose(Axis.XP.rotationDegrees(-14.0F * curve * strength));
                }
                case "Tap / Tap 1.7" -> {
                    float tap = (float) Math.sin(Math.min(1.0F, p * 1.8F) * Math.PI);
                    pose.translate(-0.025F * sign * tap * strength, 0.015F * tap * strength, -0.035F * tap * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-8.0F * tap * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 7.0F * tap * strength));
                }
                case "Sigma" -> {
                    pose.translate(-0.12F * sign * curve * strength, 0.05F * curve * strength, -0.07F * forward * strength);
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * -38.0F * curve * strength));
                    pose.mulPose(Axis.YP.rotationDegrees(sign * 28.0F * curve * strength));
                    pose.mulPose(Axis.XP.rotationDegrees(-18.0F * curve * strength));
                }
                case "Shield / Shield Block" -> {
                    boolean blocking = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem();
                    if (blocking) {
                        pose.translate(animations.blockX(), animations.blockY(), animations.blockZ());
                        pose.translate(0.12F * sign, -0.10F, -0.18F);
                        pose.mulPose(Axis.YP.rotationDegrees(sign * 35.0F));
                        pose.mulPose(Axis.XP.rotationDegrees(-20.0F));
                        pose.mulPose(Axis.ZP.rotationDegrees(sign * -12.0F));
                    } else {
                        pose.translate(0.0F, 0.02F * curve * strength, -0.05F * forward * strength);
                        pose.mulPose(Axis.XP.rotationDegrees(-8.0F * curve * strength));
                    }
                }
                case "Vertical / Spin" -> {
                    pose.translate(0.0F, 0.08F * curve * strength, -0.06F * forward * strength);
                    pose.mulPose(Axis.XP.rotationDegrees(-25.0F * curve * strength));
                    pose.mulPose(Axis.ZP.rotationDegrees(sign * 180.0F * curve * strength));
                }
            }

            pose.scale(animations.scaleX(), animations.scaleY(), animations.scaleZ());
        }

        @SubscribeEvent
        public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
            var module = module("Block Highlight", BlockHighlightModule.class);
            if (module == null || !module.isEnabled()) return;
            event.setCanceled(true);
            var target = event.getTarget();
            var pose = event.getPoseStack();
            var camera = event.getCamera().getPosition();
            var buffer = event.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.lines());
            var rgb = switch (module.color()) {
                case "Red" -> new float[]{1.0F, 0.35F, 0.4F};
                case "Green" -> new float[]{0.35F, 1.0F, 0.55F};
                case "Cyan" -> new float[]{0.30F, 0.90F, 1.0F};
                case "Yellow" -> new float[]{1.0F, 0.85F, 0.30F};
                case "White" -> new float[]{1.0F, 1.0F, 1.0F};
                default -> new float[]{0.61F, 0.42F, 1.0F};
            };
            var pos = target.getBlockPos();
            var state = Minecraft.getInstance().level.getBlockState(pos);
            var shape = state.getShape(Minecraft.getInstance().level, pos);
            var box = shape.isEmpty() ? new net.minecraft.world.phys.AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
            pose.pushPose();
            pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, rgb[0], rgb[1], rgb[2], 0.95F);
            pose.popPose();
        }

        @SubscribeEvent
        public static void onRenderFrame(RenderFrameEvent.Pre event) {
            TimeChangerModule timeChanger = module("Time Changer", TimeChangerModule.class);
            if (timeChanger != null) {
                timeChanger.applyForRender();
            }
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            WorldVisualRenderer.render(event);
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                if (mc.screen == null) PoorestHud.render(event.getGuiGraphics());
                if (PoorestUI.isOpen()) PoorestUI.render(event.getGuiGraphics());
            }
            ModuleManager.onRender();
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            PoorestHud.render(event.getGuiGraphics());
            if (PoorestUI.isOpen()) PoorestUI.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void onGlobalKey(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS && event.getAction() != GLFW.GLFW_REPEAT) return;
            Minecraft mc = Minecraft.getInstance();
            if (event.getKey() == GLFW.GLFW_KEY_F8) {
                PoorestHudLayout.setEditing(!PoorestHudLayout.isEditing());
                return;
            }
            if (mc.screen != null) return;
            if (event.getAction() == GLFW.GLFW_PRESS) {
                KeybindManager.handleKey(event.getKey());
            }
            boolean handled = PoorestUI.handleKeyPressed(event.getKey(), event.getScanCode(), event.getModifiers());
            if (PoorestUI.isOpen() && !handled) forwardMovementKey(event.getKey(), event.getScanCode(), true);
        }

        @SubscribeEvent
        public static void onGlobalMouse(InputEvent.MouseButton.Pre event) {
            if (!PoorestUI.isOpen() && !PoorestHudLayout.isEditing()) {
                if (event.getAction() == GLFW.GLFW_PRESS && (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT || event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                    InputTracker.recordClick(event.getButton());
                }
                if (event.getAction() == GLFW.GLFW_PRESS) {
                    KeybindManager.handleMouse(event.getButton());
                }
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) return;
            long window = mc.getWindow().getWindow();
            double[] mouseX = {0}, mouseY = {0};
            GLFW.glfwGetCursorPos(window, mouseX, mouseY);
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (PoorestHudLayout.mousePressed(mouseX[0] / mc.getWindow().getGuiScale(), mouseY[0] / mc.getWindow().getGuiScale(), event.getButton())) {
                    event.setCanceled(true);
                    return;
                }
                PoorestUI.handleMousePressed(mouseX[0] / mc.getWindow().getGuiScale(), mouseY[0] / mc.getWindow().getGuiScale(), event.getButton());
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                if (PoorestHudLayout.mouseReleased(event.getButton())) {
                    event.setCanceled(true);
                    return;
                }
                PoorestUI.handleMouseReleased(mouseX[0] / mc.getWindow().getGuiScale(), mouseY[0] / mc.getWindow().getGuiScale(), event.getButton());
            }
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onGlobalScroll(InputEvent.MouseScrollingEvent event) {
            if (!PoorestUI.isOpen()) return;
            if (PoorestHudLayout.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaY())) {
                event.setCanceled(true);
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            double guiScale = Math.max(1.0D, mc.getWindow().getGuiScale());
            PoorestUI.handleScroll(event.getMouseX() / guiScale, event.getMouseY() / guiScale, event.getScrollDeltaY());
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            if (!PoorestUI.isOpen() && isMenuKey(event.getKeyCode(), event.getScanCode())) {
                PoorestUI.open();
                event.setCanceled(true);
                return;
            }
            if (!PoorestUI.isOpen()) return;
            if (event.getKeyCode() == GLFW.GLFW_KEY_F8) {
                PoorestHudLayout.setEditing(!PoorestHudLayout.isEditing());
                event.setCanceled(true);
                return;
            }
            boolean handled = PoorestUI.handleKeyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
            if (!handled) forwardMovementKey(event.getKeyCode(), event.getScanCode(), true);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenKeyReleased(ScreenEvent.KeyReleased.Pre event) {
            if (!PoorestUI.isOpen()) return;
            forwardMovementKey(event.getKeyCode(), event.getScanCode(), false);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenCharTyped(ScreenEvent.CharacterTyped.Pre event) {
            if (!PoorestUI.isOpen()) return;
            PoorestUI.handleCharTyped(event.getCodePoint());
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
            if (!PoorestUI.isOpen()) return;
            if (PoorestHudLayout.mousePressed(event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
                return;
            }
            PoorestUI.handleMousePressed(event.getMouseX(), event.getMouseY(), event.getButton());
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
            if (!PoorestUI.isOpen()) return;
            if (PoorestHudLayout.mouseReleased(event.getButton())) {
                event.setCanceled(true);
                return;
            }
            PoorestUI.handleMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenMouseDragged(ScreenEvent.MouseDragged.Pre event) {
            if (!PoorestUI.isOpen()) return;
            PoorestUI.handleMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onScreenMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
            if (!PoorestUI.isOpen()) return;
            PoorestUI.handleScroll(event.getMouseX(), event.getMouseY(), event.getScrollDeltaY());
            event.setCanceled(true);
        }

        private static boolean isMenuKey(int keyCode, int scanCode) {
            return KeybindManager.OPEN_MENU.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
        }

        private static void forwardMovementKey(int keyCode, int scanCode, boolean down) {
            Minecraft mc = Minecraft.getInstance();
            InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
            setIfMatches(mc.options.keyUp, key, down);
            setIfMatches(mc.options.keyDown, key, down);
            setIfMatches(mc.options.keyLeft, key, down);
            setIfMatches(mc.options.keyRight, key, down);
            setIfMatches(mc.options.keyJump, key, down);
            // Shift is handled directly by KeyboardInputMixin as normal sneak.
            // Do not register/forward Shift here.
            // Sprint is left to Minecraft's normal key handling.
        }

        private static void setIfMatches(KeyMapping mapping, InputConstants.Key key, boolean down) {
            if (mapping.isActiveAndMatches(key)) mapping.setDown(down);
        }

        private static float clamp01(float value) {
            return Math.max(0.0F, Math.min(1.0F, value));
        }

        private static boolean enabled(String name) {
            var module = ModuleManager.getModule(name);
            return module != null && module.isEnabled();
        }

        private static boolean hasCustomCrosshair() {
            return enabled("Crosshair") || enabled("Clean Crosshair") || enabled("Crosshair+");
        }

        private static <T> T module(String name, Class<T> type) {
            var module = ModuleManager.getModule(name);
            return type.isInstance(module) ? type.cast(module) : null;
        }
    }
}
