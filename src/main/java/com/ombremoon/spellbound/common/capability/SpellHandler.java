package com.ombremoon.spellbound.common.capability;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.LinkedHashSet;

public class SpellHandler implements ISpellHandler, INBTSerializable<CompoundTag> {
    protected final LivingEntity livingEntity;
    protected boolean castMode;
    protected LinkedHashSet<SpellType<?>> spellSet = new LinkedHashSet<>();
    protected ObjectOpenHashSet<AbstractSpell> activeSpells = new ObjectOpenHashSet<>();
    protected SpellType<?> selectedSpell;
    protected AbstractSpell recentlyActivatedSpell;
    private boolean channelling;
    protected boolean initialized = false;

    public SpellHandler(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

//    public void initStatus(LivingEntity livingEntity) {
//        this.livingEntity = livingEntity;
//    }

    public boolean isInitialized() {
        return this.initialized = true;
    }

    @Override
    public boolean inCastMode() {
        return this.castMode;
    }

    @Override
    public void switchMode(boolean castMode) {
        this.castMode = castMode;
    }

    public LinkedHashSet<SpellType<?>> getSpellSet() {
        return this.spellSet;
    }

    public ObjectOpenHashSet<AbstractSpell> getActiveSpells() {
        return this.activeSpells;
    }

    public AbstractSpell getRecentlyActivatedSpell() {
        return this.recentlyActivatedSpell;
    }

    public void setRecentlyActivatedSpell(AbstractSpell recentlyActivatedSpell) {
        this.recentlyActivatedSpell = recentlyActivatedSpell;
    }

    public SpellType<?> getSelectedSpell() {
        return this.selectedSpell;
    }

    public void setSelectedSpell(SpellType<?> selectedSpell) {
        this.selectedSpell = selectedSpell;
    }

    public boolean isChannelling() {
        return this.channelling;
    }

    public void setChannelling(boolean channelling) {
        this.channelling = channelling;
    }

    public void defineEntityData(LivingEntity livingEntity) {
        this.initialized = true;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("CastMode", this.castMode);
        ListTag spellList = new ListTag();

        if (this.selectedSpell != null)
            compoundTag.putString("SelectedSpell", this.selectedSpell.getResourceLocation().toString());

        for (SpellType<?> spellType : spellSet) {
            spellList.add(SpellUtil.storeSpell(spellType));
        }
        compoundTag.put("Spells", spellList);

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("CastMode", 99)) {
            this.castMode = nbt.getBoolean("CastMode");
        }
        if (nbt.contains("SelectedSpell", 8)) {
            this.selectedSpell = AbstractSpell.getSpellByName(SpellUtil.getSpellId(nbt, "SelectedSpell"));
        } else {
            this.selectedSpell = SpellInit.TEST_SPELL.get();
        }
        if (nbt.contains("Spells", 9)) {
            ListTag spellList = nbt.getList("Spells", 10);
            for (int i = 0; i < spellList.size(); i++) {
                CompoundTag compoundTag = spellList.getCompound(i);
                this.spellSet.add(AbstractSpell.getSpellByName(SpellUtil.getSpellId(compoundTag, "Spell")));
            }
        }
    }
}
