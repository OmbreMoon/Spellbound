package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.common.init.SBParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WildMushroom extends SpellEntity<WildMushroomSpell> {

    public WildMushroom(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.blockPosition().getBottomCenter();
        if (this.random.nextBoolean())
            this.level().addParticle(
                    SBParticles.MUSHROOM_SPORE.get(),
                    vec3.x() + this.random.nextDouble() / 3.0 * (double)(this.random.nextBoolean() ? 1 : -1),
                    vec3.y() + this.random.nextDouble(),
                    vec3.z() + this.random.nextDouble() / 3.0 * (double)(this.random.nextBoolean() ? 1 : -1),
                    0.0,
                    0.07,
                    0.0
            );
    }
}
