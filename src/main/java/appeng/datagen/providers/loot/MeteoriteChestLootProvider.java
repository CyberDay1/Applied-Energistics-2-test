package appeng.datagen.providers.loot;

import java.util.function.BiConsumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import appeng.core.AppEng;
import appeng.registry.AE2Items;

public class MeteoriteChestLootProvider implements net.minecraft.data.loot.LootTableSubProvider {
    private static final ResourceKey<LootTable> METEORITE_CHEST = ResourceKey.create(
            Registries.LOOT_TABLE, AppEng.makeId("chests/meteorite"));

    public MeteoriteChestLootProvider(HolderLookup.Provider provider) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        var lootTable = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2.0F, 4.0F))
                        .add(LootItem.lootTableItem(AE2Items.SKY_STONE.get())
                                .setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                        .add(LootItem.lootTableItem(AE2Items.CERTUS_QUARTZ_CRYSTAL.get())
                                .setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                        .add(LootItem.lootTableItem(AE2Items.CHARGED_CERTUS_QUARTZ_CRYSTAL.get())
                                .setWeight(1)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(AE2Items.INSCRIBER_SILICON_PRESS.get()))
                        .add(LootItem.lootTableItem(AE2Items.INSCRIBER_LOGIC_PRESS.get()))
                        .add(LootItem.lootTableItem(AE2Items.INSCRIBER_ENGINEERING_PRESS.get()))
                        .add(LootItem.lootTableItem(AE2Items.INSCRIBER_CALCULATION_PRESS.get())));

        consumer.accept(METEORITE_CHEST, lootTable);
    }
}
