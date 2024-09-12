package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.capability.SpellHandler;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.clientbound.ClientSyncSpellPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class SpellUtil {

    public static SpellHandler getSpellHandler(LivingEntity livingEntity) {
        return livingEntity.getData(DataInit.SPELL_HANDLER);
    }

    public static CompoundTag storeSpell(SpellType<?> spellType) {
        CompoundTag compoundTag = new CompoundTag();
        return storeSpell(compoundTag, spellType);
    }

    private static CompoundTag storeSpell(CompoundTag compoundTag, SpellType<?> spellType) {
        compoundTag.putString("Spell", spellType.getResourceLocation().toString());
        return compoundTag;
    }

    public static ResourceLocation getSpellId(CompoundTag compoundTag, String tagKey) {
        return ResourceLocation.tryParse(compoundTag.getString(tagKey));
    }

    public static void activateSpell(LivingEntity livingEntity, AbstractSpell abstractSpell) {
        var handler = getSpellHandler(livingEntity);
        handler.getActiveSpells().add(abstractSpell);
        handler.setRecentlyActivatedSpell(abstractSpell);
    }

    public static void syncToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new ClientSyncSpellPacket(
                        player.getData(DataInit.SPELL_HANDLER)
                                .serializeNBT(player.level().registryAccess())
                ));
    }
}
