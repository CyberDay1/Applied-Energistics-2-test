package appeng.block.io;

import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.io.ExportBusBlockEntity;
import appeng.menu.implementations.ExportBusBlockMenu;

public class ExportBusBlock extends IOBusBlock<ExportBusBlockEntity> {

    @Override
    protected MenuType<?> getMenuType() {
        return ExportBusBlockMenu.TYPE;
    }
}
