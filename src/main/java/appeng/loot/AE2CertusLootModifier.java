//? if (>=1.21.4) {
package appeng.loot;

import java.util.Set;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;

/**
 * Adds extra certus quartz drops for AE2 ores.
 */
public class AE2CertusLootModifier extends LootModifier {
    public static final MapCodec<AE2CertusLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, AE2CertusLootModifier::new));

    private static final Set<ResourceLocation> TARGET_LOOT_TABLES = Set.of(
            AppEng.makeId("blocks/certus_quartz_ore"),
            AppEng.makeId("blocks/deepslate_certus_quartz_ore"),
            AppEng.makeId("blocks/charged_certus_quartz_ore"),
            AppEng.makeId("blocks/charged_deepslate_certus_quartz_ore"));

    protected AE2CertusLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (TARGET_LOOT_TABLES.contains(context.getQueriedLootTableId())) {
            generatedLoot.add(AEItems.CERTUS_QUARTZ_CRYSTAL.stack());

            if (context.getRandom().nextFloat() < 0.15f) {
                generatedLoot.add(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack());
            }
        }

        return generatedLoot;
    }
}
//? endif
