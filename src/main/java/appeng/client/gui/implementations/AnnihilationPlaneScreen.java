package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.AnnihilationPlaneMenu;

public class AnnihilationPlaneScreen extends FormationPlaneScreen {

    public AnnihilationPlaneScreen(AnnihilationPlaneMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}
