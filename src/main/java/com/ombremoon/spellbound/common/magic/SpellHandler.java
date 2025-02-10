package com.ombremoon.spellbound.common.magic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.util.EffectManager;
import com.ombremoon.spellbound.common.init.SBAttributes;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.acquisition.divine.PlayerDivineActions;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler class for all things spells related
 */
@SuppressWarnings("unchecked")
public class SpellHandler implements INBTSerializable<CompoundTag>, Loggable {
    protected static final Logger LOGGER = Constants.LOG;
    private SpellEventListener listener;
    public LivingEntity caster;
    private SkillHolder skillHolder;
    private EffectManager effectManager;
    private UpgradeTree upgradeTree;
    private PlayerDivineActions divineActions;
    protected boolean castMode;
    protected Set<SpellType<?>> spellSet = new ObjectOpenHashSet<>();
    public Set<SpellType<?>> equippedSpellSet = new ObjectOpenHashSet<>(/*10*/);
    protected Multimap<SpellType<?>, AbstractSpell> activeSpells = ArrayListMultimap.create();
    protected SpellType<?> selectedSpell;
    protected AbstractSpell currentlyCastingSpell;
    private final Map<SpellType<?>, Integer> spellFlags = new Object2IntOpenHashMap<>();
    private final Map<SkillBuff<?>, Integer> skillBuffs = new Object2IntOpenHashMap<>();
    private final Set<Integer> glowEntities = new IntOpenHashSet();
    private IntOpenHashSet openArenas = new IntOpenHashSet();
    private int lastArenaEntered;
    private BlockPos lastArenaPosition;
    public int castTick;
    private boolean channelling;
    private int stationaryTicks;
    public boolean castKeyDown;
    private float zoomModifier = 1.0F;
    private boolean initialized;

    /**
     * Syncs spells handler data from the server to the client.
     */
    public void sync() {
        if (this.caster instanceof Player player)
            PayloadHandler.syncSpellsToClient(player);
    }

    /**
     * <p>
     * Initializes the spells handler.
     * </p>
     * See:
     * <ul>
     *     <li>{@code SpellEventListener}</li>
     *     <li>{@code SkillHolder}</li>
     *     <li>{@code UpgradeTree}</li>
     * </ul>
     * @param caster
     */
    public void initData(LivingEntity caster) {
        this.caster = caster;
        this.listener = new SpellEventListener(caster);
        this.skillHolder = SpellUtil.getSkillHolder(this.caster);
        this.skillHolder.init(caster);
        this.effectManager = SpellUtil.getSpellEffects(this.caster);
        this.effectManager.init(caster);
        this.upgradeTree = this.caster.getData(SBData.UPGRADE_TREE);

        if (!caster.level().isClientSide)
            this.getDivineActions();

        this.initialized = true;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Checks if the player is in cast mode. Spells can only be cast while in this mode.
     * @return Whether the player is in cast mode
     */
    public boolean inCastMode() {
        return this.castMode;
    }

    /**
     * Switches the players mode from normal mode to cast mode and vice versa.
     */
    public void switchMode() {
        this.castMode = !this.castMode;
    }

    /**
     * Handles ticking logic for the spells handler. Called every tick on both the client and the server.
     */
    public void tick() {
        activeSpells.forEach((spellType, spell) -> spell.tick());
        activeSpells.entries().removeIf(entry -> entry.getValue().isInactive);

        if (this.stationaryTicks > 0)
            this.stationaryTicks--;


        this.tickSkillBuffs();
        this.skillHolder.getCooldowns().tick();
    }

    /**
     * Consumes a specified amount of mana from the player.
     * @param amount The amount of mana consumed
     * @return If the mana was actually consumed.
     */
    public boolean consumeMana(float amount) {
        return consumeMana(amount, true);
    }

    /**
     * Consumes a specified amount of mana from the player.
     * @param amount The amount of mana consumed
     * @param forceConsume Whether the mana should actually be consumed, if possible
     * @return If the mana is actually can be consumed
     */
    public boolean consumeMana(float amount, boolean forceConsume) {
        double currentFP = caster.getData(SBData.MANA);
        if (this.caster instanceof Player player && player.getAbilities().instabuild) {
            return true;
        } else if (currentFP < amount && !forceConsume) {
            return false;
        } else {
            if (forceConsume) {
                double fpCost = currentFP - amount;
                caster.setData(SBData.MANA, Math.max(fpCost, 0));
            }
            return true;
        }
    }

    /**
     * Grants a specified amount of mana to the player.
     * @param mana The amount of mana received
     */
    public void awardMana(float mana) {
        this.caster.setData(SBData.MANA, Math.min(caster.getData(SBData.MANA) + mana, this.caster.getAttribute(SBAttributes.MAX_MANA).getValue()));
        if (this.caster instanceof Player player)
            PayloadHandler.syncMana(player);
    }

    /**
     * Returns the set of spells known by the player.
     * @return The spells set
     */
    public Set<SpellType<?>> getSpellList() {
        return this.spellSet;
    }

    /**
     * Adds a spells to the player's spells set. This also adds data to both the {@link SkillHolder} and {@link UpgradeTree}.
     * @param spellType
     */
    public void learnSpell(SpellType<?> spellType) {
        if (this.spellSet.isEmpty()) this.selectedSpell = spellType;
        this.spellSet.add(spellType);
        if (this.equippedSpellSet.size() < 10)
            this.equippedSpellSet.add(spellType);

        this.skillHolder.unlockSkill(spellType.getRootSkill());
        this.upgradeTree.addAll(spellType.getSkills());
        if (this.caster instanceof Player player) {
            this.upgradeTree.update(player, spellType.getSkills());
            sync();
            this.skillHolder.sync();
        }
    }

    /**
     * Removes a spells from the player's spells set. This also removes data from both the {@link SkillHolder} and {@link UpgradeTree}.
     * @param spellType The spells to be removed
     */
    public void removeSpell(SpellType<?> spellType) {
        this.skillHolder.resetSkills(spellType);

        var locations = spellType.getSkills().stream().map(Skill::location).collect(Collectors.toSet());
        this.spellSet.remove(spellType);
        this.equippedSpellSet.remove(spellType);
        this.spellFlags.remove(spellType);
        this.upgradeTree.remove(locations);
        if (this.caster instanceof Player player) {
            this.upgradeTree.update(player, locations);
            sync();
        }
    }

    /**
     * Clears the player's known spells set. This also clears data in both the {@link SkillHolder} and {@link UpgradeTree}.
     */
    public void clearList() {
        this.spellSet.forEach(skillHolder::resetSkills);
        this.spellSet.forEach(skillHolder::resetSpellXP);
        this.skillHolder.clearModifiers();
        this.spellSet.clear();
        this.equippedSpellSet.clear();
        this.spellFlags.clear();
        this.selectedSpell = null;
        if (this.caster instanceof Player player) {
            this.upgradeTree.clear(player);
            sync();
        }
    }

    /**
     * Adds an {@link AbstractSpell} to the active spells map.
     * @param spell The AbstractSpell
     */
    public void activateSpell(AbstractSpell spell) {
        this.activeSpells.put(spell.getSpellType(), spell);
    }

    /**
     * Clears all spells in the active spells map. This will not call {@link AbstractSpell#endSpell()}.
     */
    public void clearSpells() {
        this.activeSpells.clear();
    }

    /**
     * Ends all spells in the active spells map.
     */
    public void endSpells() {
        this.activeSpells.forEach((spellType, spell) -> spell.endSpell());
    }

    /**
     * Replaces an {@link AbstractSpell} in the active spells map. This is only called is the spells has {@link AbstractSpell#fullRecast} checked in the builder method. Recast spells will all call {@link AbstractSpell#endSpell()} unless they have {@link AbstractSpell#skipEndOnRecast()} checked in the spells builder.
     * @param spell
     */
    public void recastSpell(AbstractSpell spell) {
        if (this.activeSpells.containsKey(spell.getSpellType()))
            this.activeSpells.get(spell.getSpellType()).stream().filter(abstractSpell -> !abstractSpell.skipEndOnRecast()).forEach(AbstractSpell::endSpell);

        this.activeSpells.replaceValues(spell.getSpellType(), List.of(spell));
    }

    /**
     * Returns a list of {@link AbstractSpell}s of a certain {@link SpellType}.
     * @param spellType The spells type
     * @return The list of spells active
     */
    public List<AbstractSpell> getActiveSpells(SpellType<?> spellType) {
        return this.activeSpells.get(spellType).stream().toList();
    }

    /**
     * Returns a collection of all active spells.
     * @return The collection of active spells
     */
    public Collection<AbstractSpell> getActiveSpells() {
        return this.activeSpells.values();
    }

    /**
     * Returns the first instance of an {@link AbstractSpell} in the active spells map of a given {@link SpellType}.
     * @param spellType The spells type
     * @return The AbstractSpell
     * @param <T>
     */
    public <T extends AbstractSpell> T getSpell(SpellType<T> spellType) {
        return getSpell(spellType, 1);
    }

    /**
     * Returns an instance of an {@link AbstractSpell} in the active spells map given its {@link SpellType} and cast id.
     * @param spellType The spells type
     * @param id The cast id of the spells instance
     * @return The AbstractSpell
     * @param <T>
     */
    public <T extends AbstractSpell> T getSpell(SpellType<T> spellType, int id) {
        for (var spell : getActiveSpells(spellType)) {
            if (spell.getId() == id) return (T) spell;
        }
        return null;
    }

    /**
     * Checks whether there is an active instance of a spells.
     * @param spellType The spells type
     * @return If the spells type is active
     */
    public boolean hasActiveSpell(SpellType<?> spellType) {
        return !getActiveSpells(spellType).isEmpty();
    }

    /**
     * Returns the currently selected spells of the player.
     * @return The selected spells
     */
    public SpellType<?> getSelectedSpell() {
        return this.selectedSpell != null ? this.selectedSpell : !getSpellList().isEmpty() && getSpellList().iterator().hasNext() ? getSpellList().iterator().next() : null;
    }

    /**
     * Sets the currently selected spells of the player.
     * @param selectedSpell The selected spells
     */
    public void setSelectedSpell(SpellType<?> selectedSpell) {
        this.selectedSpell = selectedSpell;
        this.currentlyCastingSpell = null;
        Constants.LOG.debug("Selected spells: {}", selectedSpell != null ? selectedSpell.createSpell().getName().getString() : null);
    }

    /**
     * Returns the spells that is currently being cast by the player. Necessary for defining casting specific {@link SpellContext}.
     * @return The spells being cast
     */
    public AbstractSpell getCurrentlyCastSpell() {
        return this.currentlyCastingSpell;
    }

    /**
     * Sets the spells that is currently being cast.
     * @param abstractSpell The spells being cast
     */
    public void setCurrentlyCastingSpell(AbstractSpell abstractSpell) {
        this.currentlyCastingSpell = abstractSpell;
    }

    public void addSkillBuff(SkillBuff<?> skillBuff, int ticks) {
        for (SkillBuff<?> buff : this.skillBuffs.keySet()) {
            if (buff.is(skillBuff))
                return;
        }

        if (!this.skillBuffs.containsKey(skillBuff)) {
            int duration = this.caster.tickCount + ticks;
            if (ticks == -1)
                duration = -1;

            skillBuff.addBuff(this.caster);
            this.skillBuffs.put(skillBuff, duration);
        }
    }

    public void removeSkillBuff(SkillBuff<?> skillBuff) {
        if (this.skillBuffs.containsKey(skillBuff)) {
            skillBuff.removeBuff(this.caster);
            this.skillBuffs.remove(skillBuff);
        }
    }

    public Set<SkillBuff<?>> getBuffs() {
        return this.skillBuffs.keySet();
    }

    public Optional<SkillBuff<?>> getSkillBuff(Skill skill) {
        return this.skillBuffs.keySet().stream().filter(skillBuff -> skillBuff.isSkill(skill)).findAny();
    }

    private void tickSkillBuffs() {
        if (!this.skillBuffs.isEmpty()) {
            for (var entry : this.skillBuffs.entrySet()) {
                if (entry.getValue() > 0 && entry.getValue() <= this.caster.tickCount)
                    this.removeSkillBuff(entry.getKey());
            }
        }
    }

    public int getFlag(SpellType<?> spellType) {
        return this.spellFlags.getOrDefault(spellType, 0);
    }

    public void setFlag(SpellType<?> spellType, int flag) {
        this.spellFlags.put(spellType, flag);
        if (this.caster.level().isClientSide)
            PayloadHandler.updateFlag(spellType, flag);
    }

    /**
     * Checks whether the player should remain stationary. That is to say all player movement inputs will be disregarded.
     * @return If the player is supposed to be stationary
     */
    public boolean isStationary() {
        return this.stationaryTicks > 0;
    }

    /**
     * Sets the amount of ticks the player's movement inputs should be disregarded.
     * @param ticks The duration
     */
    public void setStationaryTicks(int ticks) {
        this.stationaryTicks = ticks + 1;
    }

    /**
     * Returns a float from 0 to 1 representing the current zoom modifier for the player's fov.
     * @return The
     */
    public float getZoom() {
        return this.zoomModifier;
    }

    /**
     * Sets the zoom modifier for the player's fov.
     * @param zoomModifier The zoom modifier
     */
    public void setZoom(float zoomModifier) {
        this.zoomModifier = zoomModifier;
    }

    /**
     * Checks if the player is channeling a spells.
     * @see ChanneledSpell
     * @return Whether the player is channeling a spells
     */
    public boolean isChannelling() {
        return this.channelling;
    }

    /**
     * Sets whether the player is channeling a spells.
     * @param channelling If a spells is being channeled
     */
    public void setChannelling(boolean channelling) {
        this.channelling = channelling;
    }

    public Set<Integer> getSummons() {
        Set<Integer> summons = new IntOpenHashSet();
        for (var spell : this.getActiveSpells()) {
            if (spell instanceof SummonSpell summonSpell)
                summons.addAll(summonSpell.getSummons());
        }
        return summons;
    }

    /**
     * Adds a glow effect around a living entity for the player only. <b><u>MUST</u></b> be called from the client.
     * @param livingEntity The living entity that will glow
     */
    public void addGlowEffect(LivingEntity livingEntity) {
        this.glowEntities.add(livingEntity.getId());
    }

    /**
     * Removes a glow effect around a living entity.
     * @param livingEntity The living entity with the glow effect
     */
    public void removeGlowEffect(LivingEntity livingEntity) {
        this.glowEntities.remove(livingEntity.getId());
    }

    /**
     * Checks whether a living entity should be glowing for the player
     * @param livingEntity The living entity
     * @return Whether the player perceives the living entity as glowing
     */
    public boolean hasGlowEffect(LivingEntity livingEntity) {
        return this.glowEntities.contains(livingEntity.getId());
    }

    public boolean isArenaOwner(int arenaId) {
        return this.openArenas.contains(arenaId);
    }

    public void openArena(int arenaId) {
        this.openArenas.add(arenaId);
    }

    public void closeArena(int arenaId) {
        this.openArenas.remove(arenaId);
    }

    public int getLastArenaEntered() {
        return this.lastArenaEntered;
    }

    public void setLastArenaEntered(int lastArenaEntered) {
        this.lastArenaEntered = lastArenaEntered;
    }

    public BlockPos getLastArenaPosition() {
        return this.lastArenaPosition;
    }

    public void setLastArenaPosition(BlockPos lastArenaPosition) {
        this.lastArenaPosition = lastArenaPosition;
    }

    /**
     * Returns the {@link SpellEventListener} for the player.
     * @return The spells event listener
     */
    public SpellEventListener getListener() {
        return this.listener != null ? this.listener : new SpellEventListener(this.caster);
    }

    /**
     * Returns the {@link SkillHolder} for the player.
     * @return The skill holder
     */
    public SkillHolder getSkillHolder() {
        return this.skillHolder;
    }

    /**
     * Returns the {@link EffectManager} for the player.
     * @return The effect manager
     */
    public EffectManager getEffectManager() {
        return this.effectManager;
    }

    public PlayerDivineActions getDivineActions() {
        if (!(this.caster instanceof Player)) return null;
        if (this.caster.level().isClientSide) {
            warn("Tried to retrieve Divine Actions from the client, but they do not exist.");
            return null;
        }
        if (this.divineActions == null)
            this.divineActions = new PlayerDivineActions((ServerPlayer) this.caster);

        return this.divineActions;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("CastMode", this.castMode);
        compoundTag.putBoolean("Channeling", this.channelling);
        ListTag spellList = new ListTag();
        ListTag equippedSpellList = new ListTag();
        ListTag arenaList = new ListTag();

        if (this.selectedSpell != null)
            compoundTag.putString("SelectedSpell", this.selectedSpell.location().toString());

        if (!spellSet.isEmpty()) {
            for (SpellType<?> spellType : spellSet) {
                if (spellType != null)
                    spellList.add(SpellUtil.storeSpell(spellType));
            }
        }
        compoundTag.put("Spells", spellList);

        if (!equippedSpellSet.isEmpty()) {
            for (SpellType<?> spellType : equippedSpellSet) {
                if (spellType != null)
                    equippedSpellList.add(SpellUtil.storeSpell(spellType));
            }
        }
        compoundTag.put("EquippedSpells", equippedSpellList);

        if (!openArenas.isEmpty()) {
            for (Integer i : this.openArenas) {
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("ArenaId", i);
                arenaList.add(nbt);
            }
        }
        compoundTag.put("OpenArenas", arenaList);
        compoundTag.putInt("PortalId", this.lastArenaEntered);
        if (this.lastArenaPosition != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.lastArenaPosition).resultOrPartial(LOGGER::error).ifPresent(nbt -> compoundTag.put("PortalPos", nbt));

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
            this.selectedSpell = spellType != null ? spellType : SBSpells.TEST_SPELL.get();
        } else {
            this.selectedSpell = SBSpells.TEST_SPELL.get();
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
        if (nbt.contains("EquippedSpells", 9)) {
            ListTag spellList = nbt.getList("EquippedSpells", 10);
            Set<SpellType<?>> set = new ObjectOpenHashSet<>();
            for (int i = 0; i < spellList.size(); i++) {
                CompoundTag compoundTag = spellList.getCompound(i);
                set.add(AbstractSpell.getSpellByName(SpellUtil.getSpellId(compoundTag, "Spell")));
            }
            this.equippedSpellSet = set;
        }
        if (nbt.contains("OpenArenas", 9)) {
            ListTag arenas = nbt.getList("OpenArenas", 10);
            IntOpenHashSet set = new IntOpenHashSet();
            for (int i = 0; i < arenas.size(); i++) {
                CompoundTag compoundTag = arenas.getCompound(i);
                set.add(compoundTag.getInt("ArenaId"));
            }
            this.openArenas = set;
        }

        if (nbt.contains("PortalId", 99)) {
            this.lastArenaEntered = nbt.getInt("PortalId");
        }

        if (nbt.get("PortalPos") != null)
            BlockPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("PortalPos")).resultOrPartial(LOGGER::error).ifPresent(blockPos -> this.lastArenaPosition = blockPos);
    }
}
