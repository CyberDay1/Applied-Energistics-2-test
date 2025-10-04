package appeng.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import appeng.blockentity.InscriberBlockEntity;
import appeng.registry.AE2Menus;

public class InscriberMenu extends AbstractContainerMenu {
    public InscriberMenu(int id, Inventory inv, InscriberBlockEntity be) {
        super(AE2Menus.INSCRIBER_MENU.get(), id);

        InvWrapper handler = be.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, 0, 44, 20));
        this.addSlot(new SlotItemHandler(handler, 1, 44, 56));
        this.addSlot(new SlotItemHandler(handler, 2, 80, 38));
        this.addSlot(new SlotItemHandler(handler, 3, 116, 38));

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
