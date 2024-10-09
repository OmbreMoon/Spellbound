package com.ombremoon.spellbound.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.AttributesInit;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.*;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SpellHandler implements INBTSerializable<CompoundTag> {
    private SpellEventListener listener;
    public Player caster;
    private  SkillHandler skillHandler;
    private UpgradeTree upgradeTree;
    protected boolean castMode;
    protected Set<SpellType<?>> spellSet = new ObjectOpenHashSet<>();
    protected Multimap<SpellType<?>, AbstractSpell> activeSpells = ArrayListMultimap.create();
    protected SpellType<?> selectedSpell;
    protected AbstractSpell currentlyCastingSpell;
    private final Map<ModifierData, Integer> transientModifiers = new Object2IntOpenHashMap<>();
    private final Set<Integer> glowEntities = new IntOpenHashSet();
    public int castTick;
    private boolean channelling;
    private boolean isStationary;
    private float zoomModifier = 1.0F;

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
        this.skillHandler = SpellUtil.getSkillHandler(this.caster);
        this.upgradeTree = this.caster.getData(DataInit.UPGRADE_TREE);
    }

    public boolean inCastMode() {
        return this.castMode;
    }

    public void switchMode() {
        this.castMode = !this.castMode;
    }

    public void tick() {
        activeSpells.forEach((spellType, spell) -> spell.tick());
        activeSpells.entries().removeIf(entry -> entry.getValue().isInactive);

        if (!this.caster.level().isClientSide)
            serverTick();
    }

    /**
     * Called every tick on the server
     */
    public void serverTick() {
        for (var entry : this.transientModifiers.entrySet()) {
            if (entry.getValue() <= this.caster.tickCount) {
                this.caster.getAttribute(entry.getKey().attribute()).removeModifier(entry.getKey().attributeModifier());
                this.transientModifiers.remove(entry.getKey());
            }
        }

        this.skillHandler.tickModifiers(this.caster);
        this.skillHandler.getCooldowns().tick();
        this.listener.tickInstances();

        debug();
    }

    public boolean consumeMana(float amount) {
        return consumeMana(amount, true);
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
        caster.setData(DataInit.MANA, Math.min(caster.getData(DataInit.MANA) + mana, this.caster.getAttribute(AttributesInit.MAX_MANA).getValue()));
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
        this.spellSet.forEach(skillHandler::resetSpellXP);
        this.upgradeTree.clear(this.caster);
        this.spellSet.clear();
        this.selectedSpell = null;
        sync();
    }

    public void learnSpell(SpellType<?> spellType) {
        if (this.spellSet.isEmpty()) this.selectedSpell = spellType;
        this.spellSet.add(spellType);
        this.skillHandler.unlockSkill(spellType.getRootSkill());
        this.upgradeTree.addAll(spellType.getSkills());
        this.upgradeTree.update(this.caster, spellType.getSkills());
        sync();
        this.skillHandler.sync(this.caster);
    }

    public void activateSpell(AbstractSpell spell) {
        this.activeSpells.put(spell.getSpellType(), spell);
    }

    public void clearSpells() {
        this.activeSpells.clear();
    }

    public void recastSpell(AbstractSpell spell) {
        if (this.activeSpells.containsKey(spell.getSpellType()))
            this.activeSpells.get(spell.getSpellType()).stream().filter(abstractSpell -> !abstractSpell.shouldPersist()).forEach(AbstractSpell::endSpell);

        this.activeSpells.replaceValues(spell.getSpellType(), List.of(spell));
    }

    public List<AbstractSpell> getActiveSpells(SpellType<?> spellType) {
        return this.activeSpells.get(spellType).stream().toList();
    }

    public Collection<AbstractSpell> getActiveSpells() {
        return this.activeSpells.values();
    }

    public <T extends AbstractSpell> T getSpell(SpellType<T> spellType) {
        return getSpell(spellType, 1);
    }

    public <T extends AbstractSpell> T getSpell(SpellType<T> spellType, int id) {
        for (var spell : getActiveSpells(spellType)) {
            if (spell.getId() == id) return (T) spell;
        }
        return null;
    }

    public boolean isSpellActive(SpellType<?> spellType) {
        return !getActiveSpells(spellType).isEmpty();
    }

    public SpellType<?> getSelectedSpell() {
        return this.selectedSpell != null ? this.selectedSpell : !getSpellList().isEmpty() && getSpellList().iterator().hasNext() ? getSpellList().iterator().next() : null;
    }

    public AbstractSpell getCurrentlyCastSpell() {
        return this.currentlyCastingSpell;
    }

    public void setCurrentlyCastingSpell(AbstractSpell abstractSpell) {
        this.currentlyCastingSpell = abstractSpell;
    }

    public boolean isStationary() {
        return this.isStationary;
    }

    public void setStationary(boolean stationary) {
        this.isStationary = stationary;
    }

    public boolean canZoom() {
        return !Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public float getZoom() {
        return this.zoomModifier;
    }

    public void setZoom(float zoomModifier) {
        this.zoomModifier = zoomModifier;
    }

    public void setSelectedSpell(SpellType<?> selectedSpell) {
        this.selectedSpell = selectedSpell;
        this.currentlyCastingSpell = null;
        Constants.LOG.debug("Selected spell: {}", selectedSpell != null ? selectedSpell.createSpell().getName().getString() : null);
    }

    public boolean isChannelling() {
        return this.channelling;
    }

    public void setChannelling(boolean channelling) {
        this.channelling = channelling;
    }

    public void addTransientModifier(Holder<Attribute> attribute, AttributeModifier attributeModifier, int ticks) {
        this.transientModifiers.put(new ModifierData(attribute, attributeModifier), this.caster.tickCount + ticks);
    }

    public void addGlowEffect(LivingEntity livingEntity) {
        this.glowEntities.add(livingEntity.getId());
    }

    public void removeGlowEffect(LivingEntity livingEntity) {
        this.glowEntities.remove(livingEntity.getId());
    }

    public boolean hasGlowEffect(LivingEntity livingEntity) {
        return this.glowEntities.contains(livingEntity.getId());
    }

    public void endSpells() {
        this.activeSpells.forEach((spellType, spell) -> spell.endSpell());
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

    private record ModifierData(Holder<Attribute> attribute, AttributeModifier attributeModifier) {}
}
