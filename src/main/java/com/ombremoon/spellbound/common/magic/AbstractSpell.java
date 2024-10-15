package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.CameraEngine;
import com.ombremoon.spellbound.client.renderer.layer.GenericSpellLayer;
import com.ombremoon.spellbound.client.renderer.layer.SpellLayerModel;
import com.ombremoon.spellbound.client.renderer.layer.SpellLayerRenderer;
import com.ombremoon.spellbound.common.init.SBDataTypes;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.init.SBStats;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.ModifierType;
import com.ombremoon.spellbound.common.magic.events.SpellEvent;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.sync.SpellDataHolder;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * The main class used to create spells. Spells exist on both the client and server and must be handled as such. In general, spells should extend {@link AnimatedSpell} unless you don't want the player to have an animation while casting the spell.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSpell implements GeoAnimatable, SpellDataHolder, Loggable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final DataTicket<AbstractSpell> DATA_TICKET = new DataTicket<>("abstract_spell", AbstractSpell.class);
    protected static final SpellDataKey<BlockPos> CAST_POS = SyncedSpellData.define(AbstractSpell.class, SBDataTypes.BLOCK_POS.get());
    private final SpellType<?> spellType;
    private final int manaCost;
    private final int duration;
    private final int castTime;
    private final BiPredicate<SpellContext, AbstractSpell> castPredicate;
    private final CastType castType;
    private final SoundEvent castSound;
    private final boolean fullRecast;
    private final boolean partialRecast;
    private final boolean skipEndOnRecast;
    private final boolean hasLayer;
    private final int updateInterval;
    protected final SyncedSpellData spellData;
    private Level level;
    private Player caster;
    private BlockPos blockPos;
    private String nameId;
    private String descriptionId;
    private SpellContext context;
    private SpellContext castContext;
    private boolean isRecast;
    public int ticks = 0;
    public boolean isInactive = false;
    public boolean init = false;
    private int castId = 0;

    /**
     * <p>
     * Creates a static instance of a spell builder.
     * </p>
     * See:
     * <ul>
     *     <li>{@code AnimatedSpell.Builder}</li>
     *     <li>{@code ChanneledSpell.Builder}</li>
     *     <li>{@code SummonSpell.Builder}</li>
     * </ul>
     * @return The spell builder
     */
    public static <T extends AbstractSpell> Builder<T> createBuilder() {
        return new Builder<>();
    }

    public AbstractSpell(SpellType<?> spellType, Builder<? extends AbstractSpell> builder) {
        this.spellType = spellType;
        this.manaCost = builder.manaCost;
        this.duration = builder.duration;
        this.castTime = builder.castTime;
        this.castPredicate = (BiPredicate<SpellContext, AbstractSpell>) builder.castPredicate;
        this.castType = builder.castType;
        this.castSound = builder.castSound;
        this.fullRecast = builder.fullRecast;
        this.partialRecast = builder.partialRecast;
        this.skipEndOnRecast = builder.skipEndOnRecast;
        this.hasLayer = builder.hasLayer;
        this.updateInterval = builder.updateInterval;
        SyncedSpellData.Builder dataBuilder = new SyncedSpellData.Builder(this);
        dataBuilder.define(CAST_POS, BlockPos.ZERO);
        this.defineSpellData(dataBuilder);
        this.spellData = dataBuilder.build();
    }

    /**
     * Returns the spell type of the spell
     * @return The spell type
     */
    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public float getManaCost() {
        return getManaCost(this.caster);
    }

    /**
     * Returns the total mana cost to cast the spell.
     * @param player The player casting the spell
     * @return The mana cost
     */
    public float getManaCost(Player player) {
        return this.manaCost * getModifier(ModifierType.MANA, player);
    }

    /**
     * Returns the total duration the spell will remain active.
     * @return The spell duration
     */
    public int getDuration() {
        return (int) Math.floor(this.duration * getModifier(ModifierType.DURATION));
    }

    /**
     * Returns the sound played when the spell cast is complete.
     * @return The cast sound
     */
    protected SoundEvent getCastSound() {
        return this.castSound;
    }

    /**
     * Returns the type of cast the spell takes. Instant spells will cast instantly after spell cast, charged spell have the capability to be charged after spell cast, and channeled spells must be channeled to keep the spell active.
     * @return The cast type
     */
    public CastType getCastType() {
        return this.castType;
    }

    /**
     * Returns the amount of time it takes to cast the spell.
     * @return The cast time
     */
    public int getCastTime() {
        return this.castTime;
    }

    /**
     * Returns the resource location of the current spell's spell type.
     * @return The spell type's resource location
     */
    public ResourceLocation location() {
        return SBSpells.REGISTRY.getKey(this.spellType);
    }

    /**
     * Returns the name id for GUI text.
     * @return The name id
     */
    public String getNameId() {
        return this.getOrCreateNameId();
    }

    /**
     * Returns the name id for GUI text, or creates one if it doesn't exist.
     * @return The name id
     */
    protected String getOrCreateNameId() {
        if (this.nameId == null) {
            this.nameId = Util.makeDescriptionId("spell", this.location());
        }
        return this.nameId;
    }

    /**
     * Returns the description id for language data gen.
     * @return The description id
     */
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    /**
     * Gets the description id for language data gen, or creates one if it doesn't exist.
     * @return The description id
     */
    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("spell.description", this.location());
        }
        return this.descriptionId;
    }

    /**
     * Returns the texture location of the spell used in the casting overlay
     * @return The spell texture resource location
     */
    public ResourceLocation getTexture() {
        ResourceLocation name = this.location();
        return CommonClass.customLocation("textures/gui/spells/" + name.getPath() + ".png");
    }

    /**
     * Returns a component of the spell name
     * @return The spell name
     */
    public MutableComponent getName() {
        return Component.translatable(this.getNameId());
    }

    /**
     * Returns a component of the spell description.
     * @return The spell description
     */
    public MutableComponent getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    /**
     * Returns the spell type from the spell registry given a resource location.
     * @param resourceLocation The spell type resource location
     * @return The spell type
     */
    public static SpellType<?> getSpellByName(ResourceLocation resourceLocation) {
        return SBSpells.REGISTRY.get(resourceLocation);
    }

    /**
     * Returns the spell path of the spell.
     * @return The spell path
     */
    public SpellPath getPath() {
        return this.getSpellType().getPath();
    }

    /**
     * Returns the sub path of the spell if present
     * @return The spell sub path
     */
    public @Nullable SpellPath getSubPath() {
        return this.getSpellType().getSubPath();
    }

    /**
     * Returns a temporary {@link SpellContext} for the spell. Specifically used for casting mechanics.
     * @return The casting specific spell context
     */
    public SpellContext getCastContext() {
        return this.castContext;
    }

    /**
     * Sets a temporary {@link SpellContext} for the spell. Specifically used for casting mechanics.
     * @param context The casting specific spell context
     */
    public void setCastContext(SpellContext context) {
        this.castContext = context;
    }

    /**
     * The specific cast id for this spell type within the caster's active spells. Useful for getting a specific instance(s) of a spell.
     * @return The spell's cast id
     */
    public int getId() {
        return this.castId;
    }

    /**
     * Defines the default data values of the synced data keys
     * @param builder The synced spell data builder
     */
    protected void defineSpellData(SyncedSpellData.Builder builder) {

    }

    /**
     * Returns the synced spell data. Is used to get/set spell data on server that is synced with the client every update interval tick. Default data values must be defined before they can be manipulated.
     * @return The synced spell data
     */
    public SyncedSpellData getSpellData() {
        return this.spellData;
    }

    /**
     * Saves data on recast spells.
     * @param compoundTag The save data tag
     * @return A compound tag with new save data
     */
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        return compoundTag;
    }

    /**
     * Loads recast data from the previously cast spell.
     * @param nbt The saved data tag
     */
    public void load(CompoundTag nbt) {

    }

    /**
     * Spell ticking logic. Should not be overridden. Override {@link AbstractSpell#onSpellTick(SpellContext)} for ticking functionality.
     */
    @ApiStatus.Internal
    public void tick() {
        ticks++;
        if (init) {
            this.startSpell();
        } else if (!isInactive) {
            if (this.shouldTickEffect(this.context)) {
                this.onSpellTick(this.context);
            }
            if (this.getCastType() != CastType.CHANNEL && ticks % getDuration() == 0) {
                this.endSpell();
            }
        }

        if (!this.level.isClientSide) {
            if (this.spellData.isDirty() || this.ticks % this.updateInterval == 0)
                this.sendDirtySpellData();
        }
    }

    /**
     * Triggers the spell to start ticking
     */
    private void startSpell() {
        this.init = false;
        this.onSpellStart(this.context);
        if (this.isRecast)
            this.onSpellRecast(this.context);
    }

    /**
     * Ends the spell. Can be called to end the spell early.
     */
    public void endSpell() {
        this.onSpellStop(this.context);
        this.init = false;
        this.isInactive = true;
        this.ticks = 0;
    }

    /**
     * Called every tick while a spell is active
     * @param context The spell context
     */
    protected void onSpellTick(SpellContext context) {
    }

    /**
     * Called when a spell starts
     * @param context The spell context
     */
    protected void onSpellStart(SpellContext context) {
    }

    /**
     * Called when a spell is recast
     * @param context The spell context
     */
    protected void onSpellRecast(SpellContext context) {
    }

    /**
     * Called when a spell ends
     * @param context The spell context
     */
    protected void onSpellStop(SpellContext context) {
    }

    //TODO: CHECK
    /**
     * Called at the start of casting
     * @param context The casting specific spell context
     */
    public void onCastStart(SpellContext context) {
        if (!context.getLevel().isClientSide) {
            this.spellData.set(CAST_POS, context.getBlockPos());
            this.sendDirtySpellData();
        }
    }

    /**
     * Called every tick while the caster is casting
     * @param context The casting specific spell context
     * @param castTime The current cast tick
     */
    public void whenCasting(SpellContext context, int castTime) {
        if (!context.getLevel().isClientSide) {
            if (this.spellData.isDirty() || castTime % this.updateInterval == 0)
                this.sendDirtySpellData();
        }
        Constants.LOG.info("{}", castTime);
    }

    /**
     * Called when a cast condition isn't met or if the cast key is released before the cast duration.
     * @param context The casting specific spell context
     */
    public void onCastReset(SpellContext context) {
        context.getSpellHandler().setCurrentlyCastingSpell(null);
    }

    /**
     * Called when spell data is synced from the server to the client
     * @param newData The list of data values being synced
     */
    @Override
    public void onSpellDataUpdated(List<SyncedSpellData.DataValue<?>> newData) {
    }

    /**
     * Called when spell data is synced from the server to the client
     * @param dataKey The specific spell data key being synced
     */
    @Override
    public void onSpellDataUpdated(SpellDataKey<?> dataKey) {
    }

    /**
     * Checks if the spell should tick. Can be used to start/stop spell effects in certain conditions
     * @param context The spell context
     * @return Whether the spell should tick
     */
    protected boolean shouldTickEffect(SpellContext context) {
        return true;
    }

    /**
     * Adds a {@link SpellModifier} to a living entity for a specified amount of ticks.
     * @param livingEntity The living entity
     * @param spellModifier The spell modifier
     * @param expiryTick The amount of ticks the modifier should persist
     */
    public void addTimedModifier(LivingEntity livingEntity, SpellModifier spellModifier, int expiryTick) {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        skills.addModifierWithExpiry(spellModifier, livingEntity.tickCount + expiryTick);
    }

    /**
     * Adds a {@link SpellEventListener} to a living entity for a specified amount of ticks.
     * @param livingEntity The living entity
     * @param event The event
     * @param uuid The specific id for the listener
     * @param consumer Callback for when the event is fired
     * @param expiryTicks The amount of ticks the listener should persist
     * @param <T> The spell event class
     */
    public <T extends SpellEvent> void addTimedListener(LivingEntity livingEntity, SpellEventListener.IEvent<T> event, UUID uuid, Consumer<T> consumer, int expiryTicks) {
        var listener = SpellUtil.getSpellHandler(livingEntity).getListener();
        if (!listener.hasListener(event, uuid))
            listener.addListenerWithExpiry(event, uuid, consumer, livingEntity.tickCount + expiryTicks);
    }

    /**
     * Returns the modified potency of the spell
     * @param initialAmount The initial amount of the spell damage/effect
     * @return The modified damage/effect amount
     */
    protected float potency(float initialAmount) {
        return initialAmount * getModifier(ModifierType.POTENCY);
    }

    /**
     * Returns the modifier amount for the caster of the spell specifically.
     * @param modifierType The type of modifier
     * @return The modifier amount
     */
    private float getModifier(ModifierType modifierType) {
        return getModifier(modifierType, this.caster);
    }

    /**
     * Returns the total modifier amount of a given {@link SpellModifier}.
     * @param modifierType The type of modifier
     * @param livingEntity The living entity
     * @return The modifier amount
     */
    public float getModifier(ModifierType modifierType, LivingEntity livingEntity) {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        float f = 1;
        for (var modifier : skills.getModifiers()) {
            if (modifierType.equals(modifier.modifierType()) && modifier.spellPredicate().test(getSpellType())) {
                f *= modifier.modifier();
            }
        }
        return f;
    }

    /**
     * Checks if the caster of the spell has an attribute modifier
     * @param attribute The attribute to check if modified
     * @param modifier ResourceLocation of the AttributeModifier
     * @return true if the modifier is present, false otherwise
     */
    public boolean hasAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, ResourceLocation modifier) {
        return livingEntity.getAttribute(attribute).hasModifier(modifier);
    }

    /**
     * Adds an attribute modifier to a living entity for a specified amount of time.
     * @param livingEntity The entity to receive the attribute modifier
     * @param attribute The modified attribute
     * @param modifier The attribute modifier
     * @param ticks The amount of ticks the attribute modifier lasts
     */
    public void addTimedAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, AttributeModifier modifier, int ticks) {
        addAttributeModifier(livingEntity, attribute, modifier);
        SpellUtil.getSpellHandler(livingEntity).addTransientModifier(attribute, modifier, ticks);
    }

    /**
     * Adds a given attribute modifier to a chosen attribute on the caster
     * @param attribute The attribute to apply a modifier to
     * @param modifier the AttributeModifier to apply
     */
    public void addAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, AttributeModifier modifier) {
        if (!hasAttributeModifier(livingEntity, attribute, modifier.id()))
            livingEntity.getAttribute(attribute).addTransientModifier(modifier);
    }

    /**
     * Removes an attribute modifier from the caster
     * @param attribute The attribute the modifier affects
     * @param modifier The ResourceLocation of the modifier to remove
     */
    public void removeAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, ResourceLocation modifier) {
        livingEntity.getAttribute(attribute).removeModifier(modifier);
    }

    /**
     * Checks whether the entity is the caster of this spell
     * @param livingEntity The living entity
     * @return If the living entity is the caster
     */
    protected boolean isCaster(LivingEntity livingEntity) {
        return livingEntity.is(this.caster);
    }

    /**
     * Returns a number from 0 to 1 based on the amount of time a spell was charged.
     * @param charge The amount of time the spell has been charged
     * @param totalCharge The amount of time need for the spell to reach maximum charge
     * @return A proportion of the charge to total charge
     */
    public static float getPowerForTime(int charge, float totalCharge) {
        float f = (float) charge / totalCharge;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    /**
     * Adds a cooldown to skills
     * @param skill The skill to go on cooldown
     * @param ticks The amount of ticks the cooldown will last
     */
    protected void addCooldown(Skill skill, int ticks) {
        this.context.getSkills().getCooldowns().addCooldown(skill, ticks);
    }

    protected void shakeScreen(Player player) {
        shakeScreen(player, 10);
    }

    protected void shakeScreen(Player player, int duration) {
        shakeScreen(player, duration, 1);
    }

    protected void shakeScreen(Player player, int duration, float intensity) {
        shakeScreen(player, duration, intensity, 0.25F);
    }

    protected void shakeScreen(Player player, int duration, float intensity, float maxOffset) {
        shakeScreen(player, duration, intensity, maxOffset, 10);
    }

    /**
     * Shakes a player entity's screen. <b><u>MUST</u></b> be called from the client.
     * @param player The player that receives screen shake
     * @param duration The duration of the screen shake in ticks
     * @param intensity The intensity of the screen shake (max. 10.0F)
     * @param maxOffset The maximum offset from the center the screen will move from while shaking (max. 0.5F)
     * @param freq The speed of which the screen will shake
     */
    protected void shakeScreen(Player player, int duration, float intensity, float maxOffset, int freq) {
        if (this.level.isClientSide())
            CameraEngine.getOrAssignEngine(player).shakeScreen(player.getRandom().nextInt(), duration, intensity, maxOffset, freq);
    }

    /**
     * Returns a {@link BlockHitResult} of where the caster is looking within a certain range.
     * @param range The hit result range
     * @return The block hit result
     */
    public BlockHitResult getTargetBlock(double range) {
        return this.level.clip(setupRayTraceContext(this.caster, range, ClipContext.Fluid.NONE));
    }

    protected @Nullable LivingEntity getTargetEntity(double range) {
        return getTargetEntity(this.caster, range);
    }

    public @Nullable LivingEntity getTargetEntity(LivingEntity livingEntity, double range) {
        return getTargetEntity(livingEntity, livingEntity.getEyePosition(1.0F), range);
    }

    /**
     * Returns an entity the camera entity is looking at within a certain distance.
     * @param livingEntity The camera entity
     * @param startPosition The position the camera entity is starting
     * @param range The maximum range of the ray cast
     * @return An entity
     */
    private @Nullable LivingEntity getTargetEntity(LivingEntity livingEntity, Vec3 startPosition, double range) {
        Vec3 lookVec = livingEntity.getViewVector(1.0F);
        Vec3 maxLength = startPosition.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        AABB aabb = livingEntity.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(livingEntity, startPosition, maxLength, aabb, EntitySelector.NO_CREATIVE_OR_SPECTATOR, range * range);
        BlockHitResult blockHitResult = livingEntity.level().clip(setupRayTraceContext(livingEntity, range, ClipContext.Fluid.NONE));

        if (hitResult == null)
            return null;


        if (hitResult.getEntity() instanceof LivingEntity targetEntity) {

            if (!blockHitResult.getType().equals(BlockHitResult.Type.MISS)) {
                double blockDistance = blockHitResult.getLocation().distanceTo(startPosition);
                if (blockDistance > targetEntity.distanceTo(livingEntity)) {
                    return targetEntity;
                }
            } else {
                return targetEntity;
            }
        }
        return null;
    }

    /**
     * Prepares a {@link ClipContext} for ray cast results.
     * @param livingEntity The camera entity
     * @param distance The maximum distance of the ray cast
     * @param fluidContext The type of fluid context to determine if the ray cast should account for fluids
     * @return The clip context of the camera entity
     */
    protected static ClipContext setupRayTraceContext(LivingEntity livingEntity, double distance, ClipContext.Fluid fluidContext) {
        float pitch = livingEntity.getXRot();
        float yaw = livingEntity.getYRot();
        Vec3 fromPos = livingEntity.getEyePosition(1.0F);
        float float_3 = Mth.cos(-yaw * 0.017453292F - 3.1415927F);
        float float_4 = Mth.sin(-yaw * 0.017453292F - 3.1415927F);
        float float_5 = -Mth.cos(-pitch * 0.017453292F);
        float xComponent = float_4 * float_5;
        float yComponent = Mth.sin(-pitch * 0.017453292F);
        float zComponent = float_3 * float_5;
        Vec3 toPos = fromPos.add((double) xComponent * distance, (double) yComponent * distance,
                (double) zComponent * distance);
        return new ClipContext(fromPos, toPos, ClipContext.Block.OUTLINE, fluidContext, livingEntity);
    }

    /**
     * Sends manipulated {@link SyncedSpellData} from the server to the client. Will only be called once every update interval tick.
     */
    private void sendDirtySpellData() {
        List<SyncedSpellData.DataValue<?>> list = this.spellData.packDirty();
        if (list != null) {
            Player player = !this.isInactive ? this.castContext.getPlayer() : this.caster;
            PayloadHandler.setSpellData(player, getSpellType(), this.castId, list);
        }
    }

    /**
     * Will allow a spell to be recast without calling the end methods of the previously cast spell.
     * @return Whether the spell should skip the end methods on recast.
     */
    public boolean skipEndOnRecast() {
        return this.skipEndOnRecast;
    }

    /**
     * Checks if the {@link GenericSpellLayer} should render a vfx layer when the spell is active.
     * @return Whether the spell has a render layer
     */
    public boolean hasLayer() {
        return this.hasLayer;
    }

    public void initSpell(Player player, Level level, BlockPos blockPos) {
        initSpell(player, level, blockPos, this.getTargetEntity(player, 10));
    }

    /**
     * Initializes spell data before activation. Will only activate the spell upon a successful cast condition.
     * @param player The casting player
     * @param level The current level
     * @param blockPos The block position the player is in when the cast timer ends
     * @param livingEntity The target entity of the caster
     */
    public void initSpell(Player player, Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        this.level = level;
        this.caster = player;
        this.blockPos = blockPos;

        var handler = SpellUtil.getSpellHandler(player);
        var list = handler.getActiveSpells(getSpellType());
        if (!list.isEmpty()) this.isRecast = true;
        this.context = new SpellContext(this.caster, this.level, this.blockPos, livingEntity, this.isRecast);

        boolean incrementId = true;
        if (this.isRecast) {
            AbstractSpell prevSpell = null;
            if (this.partialRecast) {
                prevSpell = this.getPreviouslyCastSpell();
            } else if (this.fullRecast) {
                prevSpell = handler.getActiveSpells(getSpellType()).stream().findFirst().orElse(null);
            }

            if (prevSpell != null) {
                this.castId = prevSpell.castId + 1;
                incrementId = false;
                CompoundTag nbt = prevSpell.saveData(new CompoundTag());
                this.load(nbt);
            }
        }

        //Play Fail Animation
        if (!this.castPredicate.test(this.context, this)) {
            onCastReset(this.context);
            return;
        }

        activateSpell();
        player.awardStat(SBStats.SPELLS_CAST.get());
        if (incrementId) this.castId++;

        this.init = true;
    }

    /**
     * Returns the spell cast prior to this one of the same spell type. Necessary for saving/loading data on recast spells.
     * @return The previously cast spell
     */
    private AbstractSpell getPreviouslyCastSpell() {
        var handler = this.context.getSpellHandler();
        var spells = handler.getActiveSpells(getSpellType());
        AbstractSpell spell = this;
        for (AbstractSpell abstractSpell : spells) {
            if (abstractSpell.castId > spell.castId)
                spell = abstractSpell;
        }
        return spell;
    }

    /**
     * Called when the spell actually activates. That is after mana consumption and cast condition have already been taken into account.
     */
    private void activateSpell() {
        var handler = this.context.getSpellHandler();
        if (this.fullRecast) {
            handler.recastSpell(this);
        } else {
            handler.activateSpell(this);
            if (!this.level.isClientSide)
                this.sendDirtySpellData();
        }
        handler.consumeMana(getManaCost(), true);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Registers the controllers for the spell vfx layer. Layers must be geckolib models that use the {@link SpellLayerModel} and {@link SpellLayerRenderer}.
     * @param controllers The object to register your controller instances to
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public double getTick(Object object) {
        return this.ticks;
    }

    @Override
    public int hashCode() {
        int i = this.spellType.hashCode();
        i = 31 * i + this.castId;
        i = 31 * i + this.blockPos.hashCode();
        i = 31 * i + (this.caster != null ? this.caster.getId() : 0);
        return 31 * i + (this.isRecast ? 1 : 0);
    }

    /**
     * Base builder class for all spell builders. Should always be instantiated statically using the helper method from its parent class.
     * @param <T> Type extends AbstractSpell to give access to private fields in a static context
     */
    public static class Builder<T extends AbstractSpell> {
        protected int duration = 10;
        protected int manaCost;
        protected int castTime = 1;
        protected BiPredicate<SpellContext, T> castPredicate = (context, abstractSpell) -> true;
        protected CastType castType = CastType.INSTANT;
        protected SoundEvent castSound;
        protected boolean partialRecast;
        protected boolean fullRecast;
        protected boolean skipEndOnRecast;
        protected boolean hasLayer;
        protected int updateInterval = 3;

        /**
         * Sets the mana cost of the spell.
         * @param manaCost The mana cost
         * @return The spell builder
         */
        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        /**
         * Sets the duration of the spell.
         * @param duration The duration
         * @return The spell builder
         */
        public Builder<T> duration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets the amount of time it takes to cast the spell.
         * @param castTime The cast time
         * @return The spell builder
         */
        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        /**
         * Sets the cast type of the spell.
         * @param castType The cast type
         * @return The spell builder
         */
        public Builder<T> castType(CastType castType) {
            this.castType = castType;
            return this;
        }

        /**
         * Sets the sound played on a successful cast.
         * @param castSound The cast sound
         * @return The spell builder
         */
        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        /**
         * Sets the necessary condition for a spell to successfully be cast.
         * @param castCondition The cast predicate
         * @return The spell builder
         */
        public Builder<T> castCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = castCondition;
            return this;
        }

        /**
         * Used to append conditions of spells whose parents already have cast conditions.
         * @param castCondition The appended cast predicate
         * @return The spell builder
         */
        public Builder<T> additionalCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = this.castPredicate.and(castCondition);
            return this;
        }

        /**
         * Allows the spell to add on to the active spell list. Will save data on recast.
         * @return The spell builder
         */
        public Builder<T> partialRecast() {
            this.partialRecast = true;
            this.fullRecast = false;
            return this;
        }

        /**
         * Allows the spell to replace the previously cast spell of the same type in the active spell list. Will save data on recast.
         * @return The spell builder
         */
        public Builder<T> fullRecast() {
            this.fullRecast = true;
            this.partialRecast = false;
            return this;
        }

        /**
         * Allows the spell to skip the endSpell method. Mainly used for recasting.
         * @return The spell builder
         */
        public Builder<T> skipEndOnRecast() {
            this.skipEndOnRecast = true;
            return this;
        }

        /**
         * Must be set to render the layer set by this spell.
         * @return The spell builder
         */
        public Builder<T> hasLayer() {
            this.hasLayer = true;
            return this;
        }

        /**
         * Sets the update interval for when spell data should be synced from the server to the client.
         * @param updateInterval The update interval
         * @return The spell builder
         */
        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }
    }

    public enum CastType {
        INSTANT, CHARGING, CHANNEL
    }
}
