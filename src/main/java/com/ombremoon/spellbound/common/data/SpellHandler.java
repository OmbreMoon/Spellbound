package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class SpellHandler implements INBTSerializable<CompoundTag> {
    private SpellEventListener listener;
    public Player caster;
    protected boolean castMode;
    protected float mana;
    protected Set<SpellType<?>> spellSet = new LinkedHashSet<>();
    protected ObjectOpenHashSet<AbstractSpell> activeSpells = new ObjectOpenHashSet<>();
    protected SpellType<?> selectedSpell;
    protected Map<Integer, Set<Integer>> activeSummons = new HashMap<>();
    public int castTick;
    private boolean channelling;
    protected boolean initialized = false;

    public SpellHandler() {

    }

    public void save(Player player) {
        player.setData(DataInit.SPELL_HANDLER, this);
        PayloadHandler.syncSpellsToClient(player);
    }

    public void initData(Player player) {
        this.caster = player;
        this.listener = new SpellEventListener(player);
        this.initialized = true;
    }

    public Set<Integer> getSummonsForRemoval(int tickCount) {
        Set<Integer> expiredSummons = activeSummons.get(tickCount);
        activeSummons.remove(tickCount);
        return expiredSummons == null ? Set.of() : expiredSummons;
    }

    public void clearAllSummons(ServerLevel level) {
        for (Set<Integer> summons : activeSummons.values()) {
            for (int mob : summons) {
                if (level.getEntity(mob) == null) continue;
                level.getEntity(mob).kill();
            }
        }
        activeSummons = new HashMap<>();
    }

    public Set<Integer> getAllSummons() {
        Set<Integer> toReturn = new HashSet<>();
        for (Set<Integer> mobs : activeSummons.values()) {
            toReturn.addAll(mobs);
        }
        return toReturn;
    }

    public void addSummons(int expirationTime, Set<Integer> mobIds) {
        activeSummons.put(expirationTime, mobIds);
    }

    public boolean isInitialized() {
        return this.initialized = true;
    }

    public Player getCaster() {
        return this.caster;
    }

    public boolean inCastMode() {
        return this.castMode;
    }

    public void switchMode() {
        this.castMode = !this.castMode;
    }

    public float getMana() {
        return this.mana;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }

    public boolean consumeMana(float amount, boolean forceConsume) {
        float currentFP = this.getMana();
        if (this.caster instanceof Player player && player.getAbilities().instabuild) {
            return true;
        } else if (currentFP < amount) {
            return false;
        } else {
            if (forceConsume) {
                float fpCost = currentFP - amount;
                this.setMana(fpCost);
            }
            return true;
        }
    }

    public Set<SpellType<?>> getSpellList() {
        return this.spellSet;
    }

    public void learnSpell(SpellType<?> spellType) {
        this.spellSet.add(spellType);
    }

    public ObjectOpenHashSet<AbstractSpell> getActiveSpells() {
        return this.activeSpells;
    }

    public SpellType<?> getSelectedSpell() {
        return this.selectedSpell != null ? this.selectedSpell : !getSpellList().isEmpty() && getSpellList().iterator().hasNext() ? getSpellList().iterator().next() : null;
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

    public SpellEventListener getListener() {
        return this.listener;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("CastMode", this.castMode);
        compoundTag.putBoolean("Channeling", this.channelling);
        ListTag spellList = new ListTag();

        if (this.selectedSpell != null)
            compoundTag.putString("SelectedSpell", this.selectedSpell.getResourceLocation().toString());

        if (!spellSet.isEmpty()) {
            for (SpellType<?> spellType : spellSet) {
                if (spellType != null)
                    spellList.add(SpellUtil.storeSpell(spellType));
            }
        }
        compoundTag.put("Spells", spellList);

        ListTag activeSumms = new ListTag();
        for (int time : activeSummons.keySet()) {
            CompoundTag summonTag = new CompoundTag();
            summonTag.putInt("Time", time);
            ListTag summons = new ListTag();
            for (int summon : activeSummons.get(time)) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Summon", summon);
                summons.add(tag);
            }
            summonTag.put("Summons", summons);
            activeSumms.add(summonTag);
        }
        compoundTag.put("Summons", activeSumms);

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("CastMode", 99)) {
            this.castMode = nbt.getBoolean("CastMode");
        }
        if (nbt.contains("Channeling", 99)) {
            this.channelling = nbt.getBoolean("Channeling");
        }
        if (nbt.contains("SelectedSpell", 8)) {
            SpellType<?> spellType = AbstractSpell.getSpellByName(SpellUtil.getSpellId(nbt, "SelectedSpell"));
            this.selectedSpell = spellType != null ? spellType : SpellInit.TEST_SPELL.get();
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
        if (nbt.contains("Summons", Tag.TAG_LIST)) {
            ListTag activeSumms = nbt.getList("Summons", Tag.TAG_LIST);
            for (int i = 0; i < activeSumms.size(); i++) {
                CompoundTag tag = activeSumms.getCompound(i);
                Set<Integer> summonSet = Set.of();
                for (int j = 0; j < tag.getList("Summons", Tag.TAG_LIST).size(); j++) {
                    summonSet.add(tag.getList("Summons", Tag.TAG_LIST).getInt(j));
                }
                this.activeSummons.put(tag.getInt("Time"), summonSet);
            }
        }
    }
}
