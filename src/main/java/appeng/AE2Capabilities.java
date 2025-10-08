package appeng;

//? if (>=1.21.4) {
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;
//? else {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
//? endif

import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageService;

public final class AE2Capabilities {
    //? if (>=1.21.4) {
    public static final CapabilityToken<IGridNode> GRID_NODE_TOKEN = new CapabilityToken<>() {};
    public static final Capability<IGridNode> GRID_NODE = CapabilityManager.get(GRID_NODE_TOKEN);
    public static final CapabilityToken<IStorageService> STORAGE_SERVICE_TOKEN = new CapabilityToken<>() {};
    public static final Capability<IStorageService> STORAGE_SERVICE =
            CapabilityManager.get(STORAGE_SERVICE_TOKEN);
    //? else {
    public static final Capability<IGridNode> GRID_NODE =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IStorageService> STORAGE_SERVICE =
            CapabilityManager.get(new CapabilityToken<>() {});
    //? endif

    private AE2Capabilities() {
    }
}
