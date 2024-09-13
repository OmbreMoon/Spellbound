package com.ombremoon.spellbound.common.content.skill;

import java.util.Set;

public class Skill {
    public final String name;
    public final String description;
    public final int xpCost;
    public final Set<Skill> prerequisites;

    public Skill(String name, String description, int xpCost, Set<Skill> prerequisites) {
        this.name = name;
        this.description = description;
        this.xpCost = xpCost;
        this.prerequisites = prerequisites;
    }
}
