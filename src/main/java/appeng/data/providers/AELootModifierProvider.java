package appeng.data.providers;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import appeng.core.AppEng;
import appeng.loot.AE2CertusLootModifier;
import appeng.loot.AE2PressLootModifier;

public final class AELootModifierProvider extends GlobalLootModifierProvider {
    public AELootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AppEng.MOD_ID);
    }

    @Override
    protected void start() {
        add("add_presses", new AE2PressLootModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(AppEng.makeId("blocks/mysterious_cube")).build() }));
        add("certus_extra_drops", new AE2CertusLootModifier(new LootItemCondition[] {
                LootItemRandomChanceCondition.randomChance(0.35f).build() }));
    }
}
