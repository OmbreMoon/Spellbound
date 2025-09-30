package com.ombremoon.spellbound.common.magic.acquisition.bosses;

import com.ombremoon.spellbound.common.init.SBEntities;

public class BossFights {

    public static final EntityBasedBossFight.Builder WILD_MUSHROOM = EntityBasedBossFight.newBuilder()
            .withBoss(SBEntities.MINI_MUSHROOM)
            .scanTo(-100, 70, 100)
            .spawnAt(-35, 65, 35);
}
