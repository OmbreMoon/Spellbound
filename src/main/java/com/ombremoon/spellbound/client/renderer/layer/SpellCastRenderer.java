package com.ombremoon.spellbound.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.spellbound.client.event.ClientEventFactory;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayersContainer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.List;

public class SpellCastRenderer<T extends AbstractSpell> extends HumanoidModel implements GeoRenderer<T> {
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected final GeoModel<T> model;

    protected T animatable;
    protected HumanoidModel<?> baseModel;
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    protected Matrix4f entityRenderTranslations = new Matrix4f();
    protected Matrix4f modelRenderTranslations = new Matrix4f();

    protected BakedGeoModel lastModel = null;
    protected GeoBone rightArm = null;
    protected GeoBone castPosition = null;

    protected LivingEntity currentEntity = null;
    protected MultiBufferSource bufferSource = null;
    protected float partialTick;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float netHeadYaw;
    protected float headPitch;

    public SpellCastRenderer(GeoModel<T> model) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));

        this.model = model;
        this.young = false;
    }

    /**
     * Gets the model instance for this renderer
     */
    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    /**
     * Gets the {@link GeoItem} instance currently being rendered
     */
    public T getAnimatable() {
        return this.animatable;
    }

    /**
     * Returns the entity currently being rendered with armour equipped
     */
    public LivingEntity getCurrentEntity() {
        return this.currentEntity;
    }

    /**
     * Gets the id that represents the current animatable's instance for animation purposes
     * <p>
     * This is mostly useful for things like items, which have a single registered instance for all objects
     */
    @Override
    public long getInstanceId(T animatable) {
        return this.currentEntity.getId();
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.itemEntityTranslucentCull(texture);
    }

    /**
     * Gets the {@link RenderType} to render the given animatable with
     * <p>
     * Uses the {@link RenderType#armorCutoutNoCull} {@code RenderType} by default
     * <p>
     * Override this to change the way a model will render (such as translucent models, etc)
     */

    /**
     * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
     */
    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    public SpellCastRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public SpellCastRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public SpellCastRenderer<T> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    public GeoBone getRightArmBone(GeoModel<T> model) {
        return model.getBone("bipedRightArm").orElse(null);
    }

    public GeoBone getCastPositionBone(GeoModel<T> model) {
        return model.getBone("castPosition").orElse(null);
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling and translating
     * <p>
     * {@link PoseStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int colour) {
        this.entityRenderTranslations = new Matrix4f(poseStack.last().pose());

        applyBaseModel(this.baseModel);
        grabRelevantBones(model);
        applyBaseTransformations(this.baseModel);
        scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    @ApiStatus.Internal
    public void renderToBuffer(PoseStack poseStack, @Nullable VertexConsumer buffer, int packedLight,
                               int packedOverlay, int colour) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource bufferSource =  mc.levelRenderer.renderBuffers.bufferSource();

        if (mc.levelRenderer.shouldShowEntityOutlines() && mc.shouldEntityAppearGlowing(this.currentEntity))
            bufferSource =  mc.levelRenderer.renderBuffers.outlineBufferSource();

        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);

        defaultRender(poseStack, this.animatable, bufferSource, null, buffer,
                0, partialTick, packedLight);

        this.animatable = null;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks
     * <p>
     * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
     */
    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable RenderType renderType,
                               MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {
        poseStack.pushPose();
        poseStack.translate(0, 24 / 16f, 0);
        poseStack.scale(-1, -1, 1);

        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        if (buffer != null)
            GeoRenderer.super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
                    packedLight, packedOverlay, colour);

        poseStack.popPose();
    }

    /**
     * Called after all render operations are completed and the render pass is considered functionally complete.
     * <p>
     * Use this method to clean up any leftover persistent objects stored during rendering or any other post-render maintenance tasks as required
     */
    @Override
    public void doPostRenderCleanup() {
        this.baseModel = null;
        this.currentEntity = null;
        this.animatable = null;
        this.bufferSource = null;
        this.partialTick = 0;
        this.limbSwing = 0;
        this.limbSwingAmount = 0;
        this.netHeadYaw = 0;
        this.headPitch = 0;
    }

    /**
     * Renders the provided {@link GeoBone} and its associated child bones
     */
    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                                  int packedOverlay, int colour) {
        poseStack.pushPose();
        RenderUtil.translateMatrixToBone(poseStack, bone);
        RenderUtil.translateToPivotPoint(poseStack, bone);
        RenderUtil.rotateMatrixAroundBone(poseStack, bone);
        RenderUtil.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, this.entityRenderTranslations);

            bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtil.translateMatrix(localMatrix, Vec3.ZERO.toVector3f()));
            bone.setWorldSpaceMatrix(RenderUtil.translateMatrix(new Matrix4f(localMatrix), this.currentEntity.position().add(0, this.currentEntity.getEyeHeight(), 0).toVector3f()));
        }

        RenderUtil.translateAwayFromPivotPoint(poseStack, bone);

        buffer = checkAndRefreshBuffer(isReRender, buffer, bufferSource, renderType);

        renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);

        if (!isReRender)
            applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        poseStack.popPose();
    }

    /**
     * Gets and caches the relevant armor model bones for this baked model if it hasn't been done already
     */
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        if (this.lastModel == bakedModel)
            return;

        GeoModel<T> model = getGeoModel();
        this.lastModel = bakedModel;
        this.rightArm = getRightArmBone(model);
        this.castPosition = getCastPositionBone(model);
    }

    @Deprecated(forRemoval = true)
    public void prepForRender(@Nullable LivingEntity entity, AbstractSpell spell, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<?> baseModel) {
        if (entity == null || slot == null || baseModel == null)
            return;

        final Minecraft mc = Minecraft.getInstance();

        prepForRender(entity, spell, baseModel, mc.levelRenderer.renderBuffers.bufferSource(), mc.getTimer().getGameTimeDeltaPartialTick(true), 0, 0, 0, 0);
    }

    /**
     * Prepare the renderer for the current render pass
     * <p>
     * Must be called prior to render as the default HumanoidModel doesn't give render context
     *
     * @param entity The entity being rendered with the armor on
     * @param baseModel The default (vanilla) model that would have been rendered if this model hadn't replaced it
     * @param bufferSource The buffer supplier for the current render context
     * @param partialTick The fraction of a tick passed since the last game tick
     * @param limbSwing The position in the limb swing cycle that the entity is in
     * @param limbSwingAmount The frame-relative velocity of the entity's limb swing
     * @param netHeadYaw The entity's Y rotation, discounting any head rotation
     * @param headPitch The entity's X rotation
     */
    public void prepForRender(LivingEntity entity, AbstractSpell spell, HumanoidModel<?> baseModel, MultiBufferSource bufferSource,
                              float partialTick, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        this.baseModel = baseModel;
        this.currentEntity = entity;
        this.animatable = (T) spell;
        this.bufferSource = bufferSource;
        this.partialTick = partialTick;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
    }

    /**
     * Applies settings and transformations pre-render based on the default model
     */
    protected void applyBaseModel(HumanoidModel<?> baseModel) {
        this.young = baseModel.young;
        this.crouching = baseModel.crouching;
        this.riding = baseModel.riding;
        this.rightArmPose = baseModel.rightArmPose;
        this.leftArmPose = baseModel.leftArmPose;
    }

    /**
     * Transform the currently rendering {@link GeoModel} to match the positions and rotations of the base model
     */
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        if (this.rightArm != null) {
            ModelPart rightArmPart = baseModel.rightArm;

            RenderUtil.matchModelPartRot(rightArmPart, this.rightArm);
            this.rightArm.updatePosition(rightArmPart.x + 5, 2 - rightArmPart.y, rightArmPart.z);
        }

        if (this.castPosition != null) {
            ModelPart rightArmPart = baseModel.rightArm;

            RenderUtil.matchModelPartRot(rightArmPart, this.castPosition);
            this.castPosition.updatePosition(rightArmPart.x + 5, 2 - rightArmPart.y, rightArmPart.z);
        }
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        setBoneVisible(this.rightArm, pVisible);
        setBoneVisible(this.castPosition, pVisible);
    }

    /**
     * Sets a bone as visible or hidden, with nullability
     */
    protected void setBoneVisible(@Nullable GeoBone bone, boolean visible) {
        if (bone == null)
            return;

        bone.setHidden(!visible);
    }

    @Override
    public void updateAnimatedTextureFrame(T animatable) {

    }

    /**
     * Create and fire the relevant {@code CompileLayers} event hook for this renderer
     */
    @Override
    public void fireCompileRenderLayersEvent() {
        ClientEventFactory.fireSpellCastCompileRenderLayers(this);
    }

    /**
     * Create and fire the relevant {@code Pre-Render} event hook for this renderer
     *
     * @return Whether the renderer should proceed based on the cancellation state of the event
     */
    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return ClientEventFactory.fireSpellCastPreRender(this, poseStack, model, bufferSource, partialTick, packedLight);
    }

    /**
     * Create and fire the relevant {@code Post-Render} event hook for this renderer
     */
    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        ClientEventFactory.fireSpellCastPostRender(this, poseStack, model, bufferSource, partialTick, packedLight);
    }
}
