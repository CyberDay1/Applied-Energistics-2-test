package appeng.datagen;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import appeng.registry.AE2Blocks;
import appeng.registry.AE2Items;

public class AE2LootTableProvider extends LootTableProvider {
    public AE2LootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(new SubProviderEntry(AE2BlockLoot::new, LootContextParamSets.BLOCK)));
    }

    private static class AE2BlockLoot implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> out) {
            out.accept(AE2Blocks.CERTUS_QUARTZ_ORE.getId(),
                    LootTable.lootTable().withPool(
                            LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(AE2Items.CERTUS_QUARTZ_CRYSTAL.get()))));

            out.accept(AE2Blocks.INSCRIBER.getId(),
                    LootTable.lootTable().withPool(
                            LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(AE2Blocks.INSCRIBER.get()))));

            out.accept(AE2Blocks.CHARGER.getId(),
                    LootTable.lootTable().withPool(
                            LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(AE2Blocks.CHARGER.get()))));
        }
    }
}
