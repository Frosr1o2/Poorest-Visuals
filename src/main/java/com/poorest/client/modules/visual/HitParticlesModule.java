package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/** Cosmetic client-side particles around entities when they take damage. */
public final class HitParticlesModule extends Module {
    private final NumberSetting amount = addSetting(new NumberSetting(
            "Amount", "Particles per damage event.", 4.0, 1.0, 10.0, 1.0
    ));
    private final NumberSetting spread = addSetting(new NumberSetting(
            "Spread", "Particle spread around the entity.", 0.35, 0.05, 0.80, 0.05
    ));

    public HitParticlesModule() {
        super("Hit Particles", "Small cosmetic particles when a visible entity takes damage.", ModuleCategory.VISUAL);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(24.0D), e -> e != mc.player && e.isAlive() && !e.isInvisible())) {
            if (entity.hurtTime != entity.hurtDuration - 1) continue;
            for (int i = 0; i < (int) amount.getValueAsDouble(); i++) {
                double spreadValue = spread.getValueAsDouble();
                double x = entity.getX() + (mc.level.random.nextDouble() - 0.5D) * spreadValue;
                double y = entity.getY() + 0.4D + mc.level.random.nextDouble() * Math.max(0.2D, entity.getBbHeight() * 0.7D);
                double z = entity.getZ() + (mc.level.random.nextDouble() - 0.5D) * spreadValue;
                mc.level.addParticle(ParticleTypes.CRIT, x, y, z, 0.0D, 0.02D, 0.0D);
            }
        }
    }
}
