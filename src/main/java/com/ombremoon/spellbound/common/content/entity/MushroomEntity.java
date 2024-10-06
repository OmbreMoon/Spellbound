package com.ombremoon.spellbound.common.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MushroomEntity extends SpellEntity {

    public MushroomEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //TODO: manage particle explosion
    @Override
    public void tick() {
        super.tick();
    }

    public void explode() {
        //Explosion code from the spell
//                if (intervalProgress <= 12 && intervalProgress % 6 == 0) {
//            Vec3 center = damageZone.getCenter();
//            for (double i = 1; i <= 20; i++) {
//                for (double j = 1; j <= 5; j++) {
//                    double rot = Math.toRadians(i*18);
//                    Vec3 pos = new Vec3(center.x + ((intervalProgress/6)+2-(j/2.5)) * Math.cos(rot),
//                            damageZone.minY + (j/4),
//                            center.z + ((intervalProgress/6)+2-(j/2.5)) * Math.sin(rot));
//
//                    ((ServerLevel) context.getLevel()).sendParticles(
//                            ParticleTypes.FLAME,
//                            pos.x, pos.y, pos.z,
//                            1, 0, 0, 0, 0);
//                    }
//                }
//        }
    }
}
