package appeng.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.registry.AE2Menus;

public class ChargerMenu extends AbstractContainerMenu {
    public ChargerMenu(int id, Inventory inv) {
        super(AE2Menus.CHARGER_MENU.get(), id);
        // TODO: slots
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
