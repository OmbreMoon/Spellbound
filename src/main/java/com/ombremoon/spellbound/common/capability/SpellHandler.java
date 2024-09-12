package com.ombremoon.spellbound.common.capability;

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
//        livingEntity.getEntityData().define(POISON, 0);
//        livingEntity.getEntityData().define(SCARLET_ROT, 0);
//        livingEntity.getEntityData().define(BLOOD_LOSS, 0);
//        livingEntity.getEntityData().define(FROSTBITE, 0);
//        livingEntity.getEntityData().define(SLEEP, 0);
//        livingEntity.getEntityData().define(MADNESS, 0);
//        livingEntity.getEntityData().define(DEATH_BLIGHT, 0);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
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
        if (nbt.contains("SelectedSpell", 8)) {
            this.selectedSpell = AbstractSpell.getSpellByName(SpellUtil.getSpellId(nbt, "SelectedSpell"));
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
