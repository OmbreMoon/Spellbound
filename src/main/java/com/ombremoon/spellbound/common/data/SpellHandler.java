package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
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
import java.util.stream.Collectors;

public class SpellHandler implements INBTSerializable<CompoundTag> {
    private SpellEventListener listener;
    public Player caster;
    protected boolean castMode;
    protected Set<SpellType<?>> spellSet = new LinkedHashSet<>();
    protected ObjectOpenHashSet<AbstractSpell> activeSpells = new ObjectOpenHashSet<>();
    protected SpellType<?> selectedSpell;
    protected Map<SummonSpell, Set<Integer>> activeSummons = new HashMap<>();
    public int castTick;
    private boolean channelling;

    public SpellHandler() {

    }

    public void sync() {
        PayloadHandler.syncSpellsToClient(this.caster);
    }

    public void initData(Player player) {
        this.caster = player;
        this.listener = new SpellEventListener(player);
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

    public boolean consumeMana(float amount, boolean forceConsume) {
        float currentFP = caster.getData(DataInit.MANA);
        if (this.caster.getAbilities().instabuild) {
            return true;
        } else if (currentFP < amount) {
            return false;
        } else {
            if (forceConsume) {
                float fpCost = currentFP - amount;
                caster.setData(DataInit.MANA, fpCost);
            }
            return true;
        }
    }

    public Set<SpellType<?>> getSpellList() {
        return this.spellSet;
    }

    public void removeSpell(SpellType<?> spellType) {
        this.spellSet.remove(spellType);
        UpgradeTree tree = this.caster.getData(DataInit.UPGRADE_TREE);
        var locations = spellType.getSkills().stream().map(Skill::location).collect(Collectors.toSet());
        tree.remove(locations);
        tree.update(this.caster, locations);
    }

    public void learnSpell(SpellType<?> spellType) {
        this.spellSet.add(spellType);
        UpgradeTree tree = this.caster.getData(DataInit.UPGRADE_TREE);
        tree.addAll(spellType.getSkills());
        tree.update(this.caster, spellType.getSkills());
    }

    public ObjectOpenHashSet<AbstractSpell> getActiveSpells() {
        return this.activeSpells;
    }

    public SpellType<?> getSelectedSpell() {
        return this.selectedSpell != null ? this.selectedSpell : !getSpellList().isEmpty() && getSpellList().iterator().hasNext() ? getSpellList().iterator().next() : null;
    }

    public void setSelectedSpell(SpellType<?> selectedSpell) {
        this.selectedSpell = selectedSpell;
        Constants.LOG.debug("Selected spell: {}", selectedSpell != null ? selectedSpell.createSpell().getSpellName().getString() : null);
    }

    public boolean isChannelling() {
        return this.channelling;
    }

    public void setChannelling(boolean channelling) {
        this.channelling = channelling;
    }

    public Set<Integer> getSummonsForRemoval(SummonSpell spell) {
        Set<Integer> expiredSummons = activeSummons.get(spell);
        activeSummons.remove(spell);
        return expiredSummons == null ? Set.of() : expiredSummons;
    }

    public void clearAllSummons(ServerLevel level) {
        for (Set<Integer> summons : activeSummons.values()) {
            for (int mob : summons) {
                if (level.getEntity(mob) == null) continue;
                level.getEntity(mob).discard();
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

    public void addSummons(SummonSpell spell, Set<Integer> mobIds) {
        activeSummons.put(spell, mobIds);
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
    }
}
