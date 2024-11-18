package com.ombremoon.spellbound.common.magic.api.buff;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ModifierData(Holder<Attribute> attribute, AttributeModifier attributeModifier) {}

