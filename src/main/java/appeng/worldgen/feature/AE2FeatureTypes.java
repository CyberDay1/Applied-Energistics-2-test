package appeng.worldgen.feature;

import net.neoforged.neoforge.registries.RegistryObject;

import appeng.AE2Registries;
import appeng.worldgen.config.MeteoriteConfig;

public final class AE2FeatureTypes {
    public static final RegistryObject<MeteoriteFeature> METEORITE =
            AE2Registries.FEATURES.register("meteorite", () -> new MeteoriteFeature(MeteoriteConfig.CODEC));

    private AE2FeatureTypes() {
    }
}
