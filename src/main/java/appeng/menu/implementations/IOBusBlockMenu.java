package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.io.IOBusBlockEntity;

public abstract class IOBusBlockMenu<T extends IOBusBlockEntity> extends UpgradeableMenu<T> {

    protected IOBusBlockMenu(MenuType<? extends IOBusBlockMenu<T>> menuType, int id, Inventory inventory, T host) {
        super(menuType, id, inventory, host);
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
    }

    @Override
    protected void setupUpgrades() {
        super.setupUpgrades();
    }
}
