package appeng.data.providers;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BiomeModifierProvider;

import appeng.core.AppEng;

public final class AEBiomeModifierProvider extends BiomeModifierProvider {
    public AEBiomeModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, AppEng.MOD_ID, lookup);
    }

    @Override
    protected void registerModifiers(HolderLookup.Provider registries) {
        // add("add_quartz_ore", yourPlacedFeatureKey, yourBiomeSelector);
    }
}
