package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.divine.HealingBlossomSpell;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.checkerframework.checker.units.qual.A;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

public class HealingBlossom extends SpellEntity<HealingBlossomSpell> {
    private static final EntityDataAccessor<Boolean> FAST_BLOOMING = SynchedEntityData.defineId(HealingBlossom.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> EMPOWERED = SynchedEntityData.defineId(HealingBlossom.class, EntityDataSerializers.BOOLEAN);

    public HealingBlossom(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isFastBlooming() || isSlowBlooming()) return;

        if (skills.hasSkill(SBSkills.HEALING_WINDS)) {
            Entity entity = getOwner();
            if (!(entity instanceof LivingEntity caster)) return;
            float distanceTo = caster.distanceTo(this);
            if (distanceTo > 15) {
                this.teleportToAroundBlockPos(caster.blockPosition());
            } else if (distanceTo > 7) {
                this.move(MoverType.SELF, caster.position().add(0, 1, 0).subtract(this.position()).normalize().scale(0.2f));
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.sidedSuccess(level().isClientSide);
        Entity caster = getOwner();
        if (caster == null || !caster.is(player) || isEmpowered()
                || !SpellUtil.getSkillHolder(player).hasSkill(SBSkills.REBIRTH)) return InteractionResult.PASS;

        ItemStack item = player.getItemInHand(hand);
        if (!item.is(SBItems.HOLY_SHARD.get())) return InteractionResult.PASS;

        setEmpowered(true);
        item.shrink(1);
        return InteractionResult.CONSUME;
    }

    public void teleportToAroundBlockPos(BlockPos pos) {
        for(int i = 0; i < 10; ++i) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }

    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.moveTo((double)x + 0.5, (double)y, (double)z + 0.5, this.getYRot(), this.getXRot());

            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        BlockPos blockpos = pos.subtract(this.blockPosition());
        return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bloomController", 0, this::bloomController));
        controllers.add(new AnimationController<>(this, "actionController", 0, this::actionController));
        controllers.add(new AnimationController<>(this, "rebirthController", 20, this::rebirthController));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackController));
    }

    protected <T extends GeoAnimatable> PlayState bloomController(AnimationState<T> data) {
        if (isFastBlooming()) {
            data.setAnimation(RawAnimation.begin().thenPlay("bloom_fast"));
        } else if (isSlowBlooming()) {
            data.setAnimation(RawAnimation.begin().thenPlay("bloom"));
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("float_idle"));
        }


        return PlayState.CONTINUE;
    }

    protected <T extends GeoAnimatable> PlayState actionController(AnimationState<T> data) {
        if (!isFastBlooming() && !isSlowBlooming())
            data.setAnimation(RawAnimation.begin().thenLoop("heal_idle"));

        return PlayState.CONTINUE;
    }

    protected <T extends GeoAnimatable> PlayState rebirthController(AnimationState<T> data) {
        if (isEmpowered()) {
            data.setAnimation(RawAnimation.begin().thenPlay("rebirth"));
        } else return PlayState.STOP;

        return PlayState.CONTINUE;
    }

    protected <T extends GeoAnimatable> PlayState attackController(AnimationState<T> data) {
        if (canAttack())
            data.setAnimation(RawAnimation.begin().thenLoop("attack"));
        else return PlayState.STOP;

        return PlayState.CONTINUE;
    }

    public boolean canAttack() {
        if (isSlowBlooming() || isFastBlooming()) return false;
        Entity owner = getOwner();
        if (!(owner instanceof LivingEntity)) return false;
        return SpellUtil.getSkillHolder((LivingEntity) owner).hasSkill(SBSkills.THORNY_VINES);
    }

    public boolean isFastBlooming() {
        return this.entityData.get(FAST_BLOOMING) && tickCount < 20;
    }

    public boolean isSlowBlooming() {
        return !this.entityData.get(FAST_BLOOMING) && tickCount < 200;
    }

    public void fastBloom() {
        this.entityData.set(FAST_BLOOMING, true);
    }

    public void setEmpowered(boolean empowered) {
        this.entityData.set(EMPOWERED, empowered);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FAST_BLOOMING, false);
        builder.define(EMPOWERED, false);
    }

    public boolean isEmpowered() {
        return this.entityData.get(EMPOWERED);
    }
}
