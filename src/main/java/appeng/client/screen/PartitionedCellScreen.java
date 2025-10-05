package appeng.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.PartitionedCellMenu;

public class PartitionedCellScreen extends AEBaseScreen<PartitionedCellMenu> {
    private Button clearButton;

    public PartitionedCellScreen(PartitionedCellMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void init() {
        super.init();
        clearButton = addRenderableWidget(Button.builder(Component.translatable("gui.ae2.ClearWhitelist"),
                btn -> menu.clearWhitelist())
                .bounds(leftPos + 8, topPos + 74, 120, 20)
                .build());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (clearButton != null) {
            clearButton.active = !menu.getHost().getWhitelistInventory().isEmpty();
        }
    }
}
