package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.sync.SpellDataType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class DataTypeInit {
    public static final ResourceKey<Registry<SpellDataType<?>>> DATA_TYPE_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("spell_data_type"));
    public static final Registry<SpellDataType<?>> REGISTRY = new RegistryBuilder<>(DATA_TYPE_KEY).sync(true).create();
    public static final DeferredRegister<SpellDataType<?>> DATA_TYPES = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<SpellDataType<Byte>> BYTE = registerDataType("byte", ByteBufCodecs.BYTE);
    public static final Supplier<SpellDataType<Integer>> INT = registerDataType("int", ByteBufCodecs.VAR_INT);
    public static final Supplier<SpellDataType<Long>> LONG = registerDataType("long", ByteBufCodecs.VAR_LONG);
    public static final Supplier<SpellDataType<Float>> FLOAT = registerDataType("float", ByteBufCodecs.FLOAT);
    public static final Supplier<SpellDataType<String>> STRING = registerDataType("string", ByteBufCodecs.STRING_UTF8);
    public static final Supplier<SpellDataType<Boolean>> BOOLEAN = registerDataType("boolean", ByteBufCodecs.BOOL);
    public static final Supplier<SpellDataType<Component>> COMPONENT = registerDataType("component", ComponentSerialization.TRUSTED_STREAM_CODEC);
    public static final Supplier<SpellDataType<BlockPos>> BLOCK_POS = registerDataType("block_pos", BlockPos.STREAM_CODEC);
    public static final Supplier<SpellDataType<Vector3f>> VECTOR3 = registerDataType("vec3", ByteBufCodecs.VECTOR3F);

    private static <T> Supplier<SpellDataType<T>> registerDataType(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return DATA_TYPES.register(name, () -> SpellDataType.forValueType(streamCodec));
    }

    @Nullable
    public static SpellDataType<?> getDataType(int id) {
        return DataTypeInit.REGISTRY.byId(id);
    }

    public static int getDataTypeId(SpellDataType<?> dataType) {
        return DataTypeInit.REGISTRY.getId(dataType);
    }

    public static void register(IEventBus modEventBus) {
        DATA_TYPES.register(modEventBus);
    }
}
