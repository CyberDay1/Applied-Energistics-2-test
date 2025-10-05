package appeng.client.screen;

import java.util.OptionalInt;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.menu.PartitionedCellMenu;

public class PartitionedCellScreen extends AEBaseScreen<PartitionedCellMenu> {
    private final NumberEntryWidget priorityField;
    private Button clearButton;

    public PartitionedCellScreen(PartitionedCellMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.priorityField = widgets.addNumberEntryWidget("priority", NumberEntryType.UNITLESS);
        this.priorityField.setTextFieldStyle(style.getWidget("priorityInput"));
        this.priorityField.setMinValue(Integer.MIN_VALUE);
        this.priorityField.setMaxValue(Integer.MAX_VALUE);
        this.priorityField.setLongValue(this.menu.getPriority());
        this.priorityField.setOnChange(this::savePriority);
        this.priorityField.setOnConfirm(this::savePriority);
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

    private void savePriority() {
        OptionalInt priority = this.priorityField.getIntValue();
        if (priority.isPresent()) {
            menu.setPriority(priority.getAsInt());
        }
    }
}
