package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.LevelEmitterMode;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.menu.implementations.LevelEmitterMenu;

public class LevelEmitterScreen extends AEBaseScreen<LevelEmitterMenu> {

    private final ServerSettingToggleButton<LevelEmitterMode> modeButton;
    private final NumberEntryWidget thresholdEntry;

    public LevelEmitterScreen(LevelEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.modeButton = new ServerSettingToggleButton<>(Settings.LEVEL_EMITTER_MODE, menu.getMode());
        this.addToLeftToolbar(this.modeButton);

        this.thresholdEntry = widgets.addNumberEntryWidget("level", NumberEntryType.UNITLESS);
        this.thresholdEntry.setTextFieldStyle(style.getWidget("levelInput"));
        this.thresholdEntry.setLongValue(menu.getThreshold());
        this.thresholdEntry.setOnChange(this::saveThreshold);
        this.thresholdEntry.setOnConfirm(this::onClose);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.modeButton.set(menu.getMode());
        if (!this.thresholdEntry.isFocused()) {
            this.thresholdEntry.setLongValue(menu.getThreshold());
        }
    }

    private void saveThreshold() {
        this.thresholdEntry.getLongValue().ifPresent(menu::setThreshold);
    }
}
