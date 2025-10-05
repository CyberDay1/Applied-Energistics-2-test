package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.io.ImportBusBlockEntity;

public class ImportBusBlockMenu extends IOBusBlockMenu<ImportBusBlockEntity> {

    public static final MenuType<ImportBusBlockMenu> TYPE = MenuTypeBuilder
            .create(ImportBusBlockMenu::new, ImportBusBlockEntity.class)
            .build("import_bus_block");

    public ImportBusBlockMenu(MenuType<? extends ImportBusBlockMenu> menuType, int id, Inventory inventory,
            ImportBusBlockEntity host) {
        super(menuType, id, inventory, host);
    }
}
