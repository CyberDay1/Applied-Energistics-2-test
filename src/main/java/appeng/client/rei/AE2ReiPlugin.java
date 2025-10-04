package appeng.client.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

import appeng.api.compat.ReiCompat;
import appeng.integration.modules.rei.ReiPlugin;

@REIPluginClient
public final class AE2ReiPlugin extends ReiPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        super.registerCategories(registry);
        ReiCompat.reportBridgeInitialized();
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        super.registerDisplays(registry);
        ReiCompat.reportBridgeInitialized();
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        super.registerTransferHandlers(registry);
        ReiCompat.reportBridgeInitialized();
    }
}
