package appeng.block.io;

import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.io.ImportBusBlockEntity;
import appeng.menu.implementations.ImportBusBlockMenu;

public class ImportBusBlock extends IOBusBlock<ImportBusBlockEntity> {

    @Override
    protected MenuType<?> getMenuType() {
        return ImportBusBlockMenu.TYPE;
    }
}
