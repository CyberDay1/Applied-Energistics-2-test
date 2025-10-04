package appeng.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.AE2Registries;
import appeng.registry.AE2Blocks;

public class AE2BlockStateProvider extends BlockStateProvider {
    public AE2BlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, AE2Registries.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(AE2Blocks.CERTUS_QUARTZ_ORE.get());
        simpleBlock(AE2Blocks.INSCRIBER.get());
        simpleBlock(AE2Blocks.CHARGER.get());
        simpleBlock(AE2Blocks.SKY_STONE.get());
    }
}
