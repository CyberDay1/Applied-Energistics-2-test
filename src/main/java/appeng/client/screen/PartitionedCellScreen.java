package appeng.client.screen;

import java.util.OptionalInt;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.OfflineOverlayRenderer;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.menu.PartitionedCellMenu;

public class PartitionedCellScreen extends AEBaseScreen<PartitionedCellMenu> {
    private final NumberEntryWidget priorityField;
    private Button clearButton;
    private Button modeButton;
    private int lastRenderedPriority;

    public PartitionedCellScreen(PartitionedCellMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.priorityField = widgets.addNumberEntryWidget("priority", NumberEntryType.UNITLESS);
        this.priorityField.setTextFieldStyle(style.getWidget("priorityInput"));
        this.priorityField.setMinValue(Integer.MIN_VALUE);
        this.priorityField.setMaxValue(Integer.MAX_VALUE);
        this.lastRenderedPriority = this.menu.getPriority();
        this.priorityField.setLongValue(this.lastRenderedPriority);
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
        clearButton.setTooltip(Tooltip.create(Component.translatable("gui.ae2.ClearWhitelist")));

        modeButton = addRenderableWidget(Button.builder(Component.empty(), btn -> toggleMode())
                .bounds(leftPos + 8, topPos + 124, 120, 20)
                .build());
        modeButton.setTooltip(Tooltip.create(Component.translatable("ae2.partitioned_cell.mode.tooltip")));
        updateModeButton();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (clearButton != null) {
            clearButton.active = !menu.getHost().getWhitelistInventory().isEmpty();
        }
        int menuPriority = menu.getPriority();
        if (menuPriority != lastRenderedPriority) {
            priorityField.setLongValue(menuPriority);
            lastRenderedPriority = menuPriority;
        }
        updateModeButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        OfflineOverlayRenderer.drawIfOffline(graphics, this.font, menu.getOfflineReason(), leftPos, topPos, imageWidth,
                imageHeight);
    }

    private void savePriority() {
        OptionalInt priority = this.priorityField.getIntValue();
        if (priority.isPresent()) {
            int value = priority.getAsInt();
            menu.setPriority(value);
            lastRenderedPriority = value;
        }
    }

    private void toggleMode() {
        menu.setWhitelist(!menu.isWhitelist());
    }

    private void updateModeButton() {
        if (modeButton == null) {
            return;
        }

        Component message = menu.isWhitelist()
                ? Component.translatable("ae2.partitioned_cell.mode.whitelist")
                : Component.translatable("ae2.partitioned_cell.mode.blacklist");
        modeButton.setMessage(message);
    }
}
