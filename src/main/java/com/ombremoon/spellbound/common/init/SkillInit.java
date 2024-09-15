package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Set;
import java.util.function.Supplier;

public class SkillInit {
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("skill"));
    public static final Registry<Skill> REGISTRY = new RegistryBuilder<>(SKILL_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<Skill> TEST_SKILL = registerSkill("test", 10, Set.of());

    private static Supplier<Skill> registerSkill(String name, int xpCost, Set<Skill> prereqs) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name),
               xpCost, prereqs));
    }
}
