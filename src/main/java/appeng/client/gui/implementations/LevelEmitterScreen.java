package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.LevelEmitterMode;
import appeng.api.config.Settings;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.LevelEmitterMenu;

public class LevelEmitterScreen extends AEBaseScreen<LevelEmitterMenu> {

    private final ServerSettingToggleButton<LevelEmitterMode> modeButton;
    private final NumberEntryWidget thresholdEntry;
    private final Slot filterSlot;

    public LevelEmitterScreen(LevelEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.modeButton = new ServerSettingToggleButton<>(Settings.LEVEL_EMITTER_MODE, menu.getMode());
        this.addToLeftToolbar(this.modeButton);

        this.thresholdEntry = widgets.addNumberEntryWidget("level", NumberEntryType.of(menu.getMonitoredKey()));
        this.thresholdEntry.setTextFieldStyle(style.getWidget("levelInput"));
        this.thresholdEntry.setLongValue(menu.getThreshold());
        this.thresholdEntry.setOnChange(this::saveThreshold);
        this.thresholdEntry.setOnConfirm(this::onClose);

        var configSlots = menu.getSlots(SlotSemantics.CONFIG);
        this.filterSlot = configSlots.isEmpty() ? null : configSlots.get(0);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.modeButton.set(menu.getMode());
        this.thresholdEntry.setType(NumberEntryType.of(menu.getMonitoredKey()));
        if (!this.thresholdEntry.isFocused()) {
            this.thresholdEntry.setLongValue(menu.getThreshold());
        }
    }

    private void saveThreshold() {
        this.thresholdEntry.getLongValue().ifPresent(menu::setThreshold);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (filterSlot != null && hoveredSlot == filterSlot && !filterSlot.hasItem()) {
            var tooltip = getMonitoringTooltip();
            if (!tooltip.isEmpty()) {
                drawTooltip(guiGraphics, mouseX, mouseY, tooltip);
            }
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));

        if (hoveredSlot == filterSlot) {
            var monitoringTooltip = getMonitoringTooltip();
            if (!monitoringTooltip.isEmpty()) {
                if (!tooltip.isEmpty()) {
                    tooltip.add(Component.literal(""));
                }
                tooltip.addAll(monitoringTooltip);
            }
        }

        return tooltip;
    }

    private List<Component> getMonitoringTooltip() {
        var tooltip = new ArrayList<Component>(2);
        AEKey monitoredKey = menu.getMonitoredKey();
        boolean monitoringFluids = monitoredKey instanceof AEFluidKey;

        tooltip.add(Component.translatable(
                monitoringFluids ? "gui.ae2.level_emitter.mode.fluids" : "gui.ae2.level_emitter.mode.items"));

        Component value = monitoredKey != null ? monitoredKey.getDisplayName().copy()
                : (monitoringFluids ? GuiText.Fluids.text() : GuiText.Items.text());

        tooltip.add(Component.translatable("gui.ae2.level_emitter.filter")
                .append(Component.literal(": "))
                .append(value));

        return tooltip;
    }
}
