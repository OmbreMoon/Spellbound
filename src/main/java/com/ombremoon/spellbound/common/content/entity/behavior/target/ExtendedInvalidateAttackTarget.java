package com.ombremoon.spellbound.common.content.entity.behavior.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

public class ExtendedInvalidateAttackTarget<E extends LivingEntity> extends InvalidateAttackTarget<E> {

    @Override
    protected void start(E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);

        if (target == null)
            return;

        if (isTargetInvalid(entity, target) || !canAttack(entity, target) ||
                isTiredOfPathing(entity) || this.customPredicate.test(entity, target)) {
            BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
            BrainUtils.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
        }
    }
}
