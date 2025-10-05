package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.storage.StorageBusBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;

public class StorageBusBlockMenu extends UpgradeableMenu<StorageBusBlockEntity> {

    public static final MenuType<StorageBusBlockMenu> TYPE = MenuTypeBuilder
            .create(StorageBusBlockMenu::new, StorageBusBlockEntity.class)
            .build("storage_bus_block");

    public StorageBusBlockMenu(MenuType<? extends StorageBusBlockMenu> menuType, int id, Inventory inventory,
            StorageBusBlockEntity host) {
        super(menuType, id, inventory, host);
    }

    @Override
    protected void setupUpgrades() {
        super.setupUpgrades();
    }

    @Override
    protected void setupConfig() {
        var wrapper = getHost().getConfig().createMenuWrapper();
        for (int i = 0; i < wrapper.size(); i++) {
            addSlot(new FakeSlot(wrapper, i), SlotSemantics.CONFIG);
        }
    }

}
