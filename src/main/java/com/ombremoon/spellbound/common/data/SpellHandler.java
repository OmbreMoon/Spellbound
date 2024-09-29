package com.ombremoon.spellbound.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ombremoon.spellbound.CommonClass;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class SpellHandler implements INBTSerializable<CompoundTag> {
    private SpellEventListener listener;
    public Player caster;
    private  SkillHandler skillHandler;
    private UpgradeTree upgradeTree;
    protected boolean castMode;
    protected Set<SpellType<?>> spellSet = new ObjectOpenHashSet<>();
    protected Multimap<SpellType<?>, AbstractSpell> activeSpells = ArrayListMultimap.create();
    protected SpellType<?> selectedSpell;
    protected Map<SummonSpell, Set<Integer>> activeSummons = new Object2ObjectOpenHashMap<>();
    public int castTick;
    private boolean channelling;

    public void sync() {
        PayloadHandler.syncSpellsToClient(this.caster);
    }

    public void debug() {
        if (CommonClass.isDevEnv() && this.caster == null)
            Constants.LOG.warn("Something fishy is happening");
    }

    public void initData(Player player) {
        this.caster = player;
        this.listener = new SpellEventListener(player);
        this.skillHandler = this.caster.getData(DataInit.SKILL_HANDLER);
        this.upgradeTree = this.caster.getData(DataInit.UPGRADE_TREE);
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
        double currentFP = caster.getData(DataInit.MANA);
        if (this.caster.getAbilities().instabuild) {
            return true;
        } else if (currentFP < amount) {
            return false;
        } else {
            if (forceConsume) {
                double fpCost = currentFP - amount;
                caster.setData(DataInit.MANA, fpCost);
            }
            return true;
        }
    }

    public void awardMana(float mana) {
        caster.setData(DataInit.MANA, caster.getData(DataInit.MANA) + mana);
        PayloadHandler.syncMana(caster);
    }

    public Set<SpellType<?>> getSpellList() {
        return this.spellSet;
    }

    public void removeSpell(SpellType<?> spellType) {
        this.skillHandler.resetSkills(spellType);

        var locations = spellType.getSkills().stream().map(Skill::location).collect(Collectors.toSet());
        this.upgradeTree.remove(locations);
        this.upgradeTree.update(this.caster, locations);
        this.spellSet.remove(spellType);
        sync();
    }

    public void clearList() {
        this.spellSet.forEach(skillHandler::resetSkills);
        this.upgradeTree.clear(this.caster);
        this.spellSet.clear();
        this.selectedSpell = null;
        sync();
    }

    public void learnSpell(SpellType<?> spellType) {
        if (this.spellSet.isEmpty()) this.selectedSpell = spellType;
        this.spellSet.add(spellType);
        this.skillHandler.unlockSkill(Skill.byName(spellType.location()));
        this.upgradeTree.addAll(spellType.getSkills());
        this.upgradeTree.update(this.caster, spellType.getSkills());
        sync();
    }

    public void activateSpell(AbstractSpell spell) {
        this.activeSpells.put(spell.getSpellType(), spell);
    }

    public void clearSpells() {
        this.activeSpells.clear();
    }

    public void recastSpell(AbstractSpell spell) {
        if (this.activeSpells.containsKey(spell.getSpellType()))
            this.activeSpells.get(spell.getSpellType()).forEach(AbstractSpell::endSpell);

        this.activeSpells.put(spell.getSpellType(), spell);
    }

    public Multimap<SpellType<?>, AbstractSpell> getActiveSpells() {
        return this.activeSpells;
    }

    public Collection<AbstractSpell> getActiveSpells(SpellType<?> spellType) {
        return this.activeSpells.get(spellType);
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

    public SkillHandler getSkillHandler() {
        return this.skillHandler;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("CastMode", this.castMode);
        compoundTag.putBoolean("Channeling", this.channelling);
        ListTag spellList = new ListTag();

        if (this.selectedSpell != null)
            compoundTag.putString("SelectedSpell", this.selectedSpell.location().toString());

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
            Set<SpellType<?>> set = new ObjectOpenHashSet<>();
            for (int i = 0; i < spellList.size(); i++) {
                CompoundTag compoundTag = spellList.getCompound(i);
                set.add(AbstractSpell.getSpellByName(SpellUtil.getSpellId(compoundTag, "Spell")));
            }
            this.spellSet = set;
        }
    }
}
