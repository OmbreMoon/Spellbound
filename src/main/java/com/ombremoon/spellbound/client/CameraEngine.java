package com.ombremoon.spellbound.client;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.util.math.NoiseGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.UUID;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class CameraEngine {
    private static final Object2ObjectOpenHashMap<UUID, CameraEngine> ENGINE = new Object2ObjectOpenHashMap<>();
    private static final float MAX_OFFSET = 0.5F;
    private static final float MAX_INTENSITY = 10.0F;
    private static final Logger LOGGER = Constants.LOG;
    private final Player player;
    private int seed;
    private int shakeDuration;
    private float shakeIntensity;
    private int shakeFrequency;
    private float maxOffset;

    public CameraEngine(Player player) {
        this.player = player;
    }

    public static CameraEngine getOrAssignEngine(Player player) {
        if (!ENGINE.containsKey(player.getUUID())) {
            ENGINE.put(player.getUUID(), new CameraEngine(player));
        }
        return ENGINE.get(player.getUUID());
    }

    public int getSeed() {
        return this.seed;
    }

    public int getShakeDuration() {
        return this.shakeDuration;
    }

    public float getShakeIntensity() {
        return this.shakeIntensity;
    }

    public int getShakeFrequency() {
        return this.shakeFrequency;
    }

    public float getMaxOffset() {
        return this.maxOffset;
    }

    public boolean shouldShakeCamera() {
        return this.shakeDuration > 0;
    }

    private void tickDuration() {
        this.shakeDuration--;
        if (shakeDuration <= 0) {
            this.shakeDuration = 0;
        }
    }

    public void shakeScreen() {
        shakeScreen(player.getRandom().nextInt());
    }

    public void shakeScreen(int seed) {
        shakeScreen(seed, 10);
    }

    public void shakeScreen(int seed, int duration) {
        shakeScreen(seed, duration, 1);
    }

    public void shakeScreen(int seed, int duration, float intensity) {
        shakeScreen(seed, duration, intensity, 0.25F);
    }

    public void shakeScreen(int seed, int duration, float intensity, float maxOffset) {
        shakeScreen(seed, duration, intensity, maxOffset, 10);
    }

    public void shakeScreen(int seed, int duration, float intensity, float maxOffset, int freq) {
        this.seed = seed;
        this.shakeDuration = duration;
        this.shakeIntensity = Mth.clamp(intensity, 0.0F, MAX_INTENSITY);
        this.shakeFrequency = freq;
        this.maxOffset = Mth.clamp(maxOffset, 0, MAX_OFFSET);
    }

    @SubscribeEvent
    public static void onScreenShake(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;

        CameraEngine cameraEngine = getOrAssignEngine(player);
        if (cameraEngine.shouldShakeCamera()) {
            cameraEngine.tickDuration();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        if (entity instanceof Player player) {
            CameraEngine cameraEngine = getOrAssignEngine(player);
            int time = camera.getEntity().tickCount;
            if (cameraEngine != null && cameraEngine.shouldShakeCamera()) {
                int seed = cameraEngine.getSeed();
                float intensity = cameraEngine.getShakeIntensity();
                float offset = cameraEngine.getMaxOffset();
                int freq = cameraEngine.getShakeFrequency();
                double d0 = getNoise(seed, offset, intensity, time * freq);
                double d1 = getNoise(seed + 1, offset, intensity, time * freq);
                double d2 = getNoise(seed + 2, offset, intensity, time * freq);
                event.setPitch((float) (event.getPitch() + d0));
                event.setRoll((float) (event.getRoll() + d1));
                event.setYaw((float) (event.getYaw() + d2));
            }
        }
    }

    private static double getNoise(int seed, float maxOffset, float intensity, int x) {
        NoiseGenerator noiseGenerator = new NoiseGenerator(seed);
        return maxOffset * intensity * noiseGenerator.noise(x);
    }
}
