package appeng.data.providers;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;

public final class AEBlockStateProvider extends BlockStateProvider {
    public AEBlockStateProvider(PackOutput output, ExistingFileHelper existing) {
        super(output, AppEng.MOD_ID, existing);
    }

    @Override
    protected void registerStatesAndModels() {
        // simpleBlock(AEBlocks.QUARTZ_ORE.get());
    }
}
