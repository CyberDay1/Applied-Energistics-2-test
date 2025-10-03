package appeng.recipes.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.conditions.ICondition;

public record HasItemCondition(ResourceLocation itemId) implements ICondition {
    public static final MapCodec<HasItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("item").forGetter(HasItemCondition::itemId))
            .apply(instance, HasItemCondition::new));

    @Override
    public boolean test(IContext context) {
        return context.registryAccess()
                .registry(Registries.ITEM)
                .map(registry -> registry.containsKey(itemId))
                .orElse(false);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
