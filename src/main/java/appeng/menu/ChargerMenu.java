package appeng.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import appeng.blockentity.ChargerBlockEntity;
import appeng.registry.AE2Menus;

public class ChargerMenu extends AbstractContainerMenu {
    public ChargerMenu(int id, Inventory inv, ChargerBlockEntity be) {
        super(AE2Menus.CHARGER_MENU.get(), id);

        InvWrapper handler = be.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, 0, 62, 35));
        this.addSlot(new SlotItemHandler(handler, 1, 98, 35));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
