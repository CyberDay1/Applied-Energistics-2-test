package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.io.ExportBusBlockEntity;

public class ExportBusBlockMenu extends IOBusBlockMenu<ExportBusBlockEntity> {

    public static final MenuType<ExportBusBlockMenu> TYPE = MenuTypeBuilder
            .create(ExportBusBlockMenu::new, ExportBusBlockEntity.class)
            .build("export_bus_block");

    public ExportBusBlockMenu(MenuType<? extends ExportBusBlockMenu> menuType, int id, Inventory inventory,
            ExportBusBlockEntity host) {
        super(menuType, id, inventory, host);
    }
}
