package appeng.data.providers;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BiomeModifierProvider;

import appeng.core.AppEng;
import appeng.worldgen.AE2Features;

public final class AEBiomeModifierProvider extends BiomeModifierProvider {
    public AEBiomeModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, AppEng.MOD_ID, lookup);
    }

    @Override
    protected void registerModifiers(HolderLookup.Provider registries) {
        addFeature("add_certus_ore", overworldBiomes(), AE2Features.CERTUS_ORE_PLACED);
        addFeature("add_meteorites", overworldBiomes(), AE2Features.METEORITE_PLACED);
    }
}
