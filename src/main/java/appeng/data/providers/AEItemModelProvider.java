package appeng.data.providers;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.registry.AE2Blocks;

public final class AEItemModelProvider extends ItemModelProvider {
    public AEItemModelProvider(PackOutput output, ExistingFileHelper existing) {
        super(output, AppEng.MOD_ID, existing);
    }

    @Override
    protected void registerModels() {
        withExistingParent(AE2Blocks.CABLE.getId().getPath(), modLoc("block/cable"));
    }
}
