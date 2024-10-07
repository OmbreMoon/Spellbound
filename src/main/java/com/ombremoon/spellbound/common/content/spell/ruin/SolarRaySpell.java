package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.sentinellib.api.box.OBBSentinelBox;
import com.ombremoon.sentinellib.common.IPlayerSentinel;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

public class SolarRaySpell extends ChanneledSpell {
    protected static final SpellDataKey<Integer> SOLAR_RAY = SyncedSpellData.define(SolarRaySpell.class, DataTypeInit.INT.get());
    public static final OBBSentinelBox SOLAR_BEAM = OBBSentinelBox.Builder.of("solar_beam")
            .sizeAndOffset(0.75F, 0.75F, 10.0F, 0.0F, 1.7F, 10.0F)
            .noDuration(entity -> false)
            .onHurtTick((entity, livingEntity) -> {
                Player player = (Player) entity;
                if (SpellUtil.getSkillHandler(player).hasSkill(SkillInit.AFTERGLOW.value()))
                    livingEntity.addEffect(new SBEffectInstance(player, EffectInit.AFTERGLOW, 100, 0, false, false));
            })
            .typeDamage(DamageTypes.IN_FIRE, 15.0F).build();

    public static Builder<ChanneledSpell> createSolarRayBuilder() {
        return createChannelledSpellBuilder().castTime(18);
    }

    public SolarRaySpell() {
        super(SpellInit.SOLAR_RAY.get(), createSolarRayBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(SOLAR_RAY, 0);
    }

    @Override
    public void onCastStart(SpellContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        var handler = context.getSpellHandler();
        handler.setStationary(true);
        if (!level.isClientSide) {
            SolarRay solarRay = EntityInit.SOLAR_RAY.get().create(level);
            if (solarRay != null) {
                this.setSolarRay(solarRay.getId());
                solarRay.setOwner(player);
                solarRay.setPos(player.position());
                solarRay.setYRot(player.getYRot());
                solarRay.setStartTick(18);
                level.addFreshEntity(solarRay);
            }
        }
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Player player = context.getPlayer();
        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null)
            solarRay.setYRot(player.getYRot());
    }

    @Override
    public void onCastReset(SpellContext context) {
        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) solarRay.discard();
        var handler = context.getSpellHandler();
        handler.setStationary(false);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        if (!context.getLevel().isClientSide)
         BoxUtil.triggerPlayerBox(context.getPlayer(), SOLAR_BEAM);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        if (!context.getLevel().isClientSide)
            player.setPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());

        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null)
            solarRay.setYRot(player.getYRot());
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        ((IPlayerSentinel)context.getPlayer()).removeSentinelInstance(SOLAR_BEAM);
        var handler = context.getSpellHandler();
        handler.setStationary(false);

        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null)
            solarRay.setEndTick(15);
    }

    private void setSolarRay(int solarRay) {
        this.spellData.set(SOLAR_RAY, solarRay);
    }

    private SolarRay getSolarRay(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(SOLAR_RAY));
        return entity instanceof SolarRay solarRay ? solarRay : null;
    }
}
