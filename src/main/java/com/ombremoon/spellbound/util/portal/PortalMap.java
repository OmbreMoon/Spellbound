package com.ombremoon.spellbound.util.portal;

import com.ombremoon.spellbound.common.content.entity.PortalEntity;
import com.ombremoon.spellbound.networking.PayloadHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalMap<T extends PortalEntity> extends Int2ObjectOpenHashMap<PortalInfo> {

    public void summonPortal(T portal, int maxPortals, int spawnTicks) {
        if (this.size() >= maxPortals) {
            this.shiftPortals(portal.level(), portal.getId(), portal.position());
        } else {
            PortalInfo info = new PortalInfo(this.size(), portal.position());
            this.put(portal.getId(), info);
        }
        portal.setStartTick(spawnTicks);
    }

    public boolean attemptTeleport(LivingEntity entity, T portal) {
        if (entity != null && !portal.isOnCooldown(entity)) {
            T adjacentRift = this.getAdjacentPortal(portal, portal.level());
            if (adjacentRift != null) {
                Vec3 position = adjacentRift.position();
                adjacentRift.addCooldown(entity);
                entity.teleportTo(position.x, position.y, position.z);
                if (entity instanceof Player teleportedPlayer)
                    PayloadHandler.setRotation(teleportedPlayer, teleportedPlayer.getXRot(), adjacentRift.getYRot());

                return true;
            }
        }
        return false;
    }

    private void shiftPortals(Level level, int portalID, Vec3 position) {
        for (var entry : this.entrySet()) {
            var info = entry.getValue();
            if (info.id() == 0) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity != null)
                    entity.discard();

                this.remove(entry.getKey());
            } else {
                PortalInfo newInfo = new PortalInfo(info.id() - 1, info.position());
                this.replace(entry.getKey(), newInfo);
            }
        }
        PortalInfo info = new PortalInfo(this.size(), position);
        this.put(portalID, info);
    }

    @SuppressWarnings("unchecked")
    public T getAdjacentPortal(T portal, Level level) {
        int activePortals = this.size();
        if (activePortals < 2) return null;

        int id = portal.getId();
        PortalInfo info = this.get(id);
        if (info != null) {
            int portalId = info.id() + 1;
            if (portalId >= activePortals) portalId = 0;
            for (var entry : this.entrySet()) {
                if (portalId == entry.getValue().id())
                    return (T) level.getEntity(entry.getKey());
            }
        }
        return null;
    }

    public int getPreviousPortal() {
        int i = 0;
        for (var entry : this.entrySet()) {
            if (entry.getValue().id() > i)
                i = entry.getValue().id();
        }
        return getPortalFromID(i);
    }

    private int getPortalFromID(int id) {
        for (var entry : this.entrySet()) {
            if (entry.getValue().id() == id)
                return entry.getKey();
        }
        return 0;
    }
}
