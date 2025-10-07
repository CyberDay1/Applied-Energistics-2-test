package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.LevelEmitterMode;
import appeng.blockentity.misc.LevelEmitterBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.guisync.GuiSync;

public class LevelEmitterMenu extends AEBaseMenu {

    private static final String ACTION_SET_THRESHOLD = "setThreshold";

    public static final MenuType<LevelEmitterMenu> TYPE = MenuTypeBuilder
            .create(LevelEmitterMenu::new, LevelEmitterBlockEntity.class)
            .withInitialData((host, buffer) -> {
                buffer.writeVarLong(host.getThreshold());
                buffer.writeEnum(host.getMode());
            }, (host, menu, buffer) -> {
                menu.threshold = buffer.readVarLong();
                menu.mode = buffer.readEnum(LevelEmitterMode.class);
            })
            .build("level_emitter_block");

    @GuiSync(0)
    public long threshold;

    @GuiSync(1)
    public LevelEmitterMode mode = LevelEmitterMode.GREATER_OR_EQUAL;

    public LevelEmitterMenu(MenuType<LevelEmitterMenu> menuType, int id, Inventory inventory,
            LevelEmitterBlockEntity host) {
        super(menuType, id, inventory, host);

        registerClientAction(ACTION_SET_THRESHOLD, Long.class, this::handleSetThreshold);
        createPlayerInventorySlots(inventory);
    }

    public LevelEmitterMenu(int id, Inventory inventory, LevelEmitterBlockEntity host) {
        this(TYPE, id, inventory, host);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide() && getBlockEntity() instanceof LevelEmitterBlockEntity levelEmitter) {
            threshold = levelEmitter.getThreshold();
            mode = levelEmitter.getMode();
        }
        super.broadcastChanges();
    }

    public long getThreshold() {
        return threshold;
    }

    public LevelEmitterMode getMode() {
        return mode;
    }

    public void setThreshold(long value) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_THRESHOLD, value);
        } else if (getBlockEntity() instanceof LevelEmitterBlockEntity levelEmitter) {
            levelEmitter.setThreshold(value);
        }
    }

    private void handleSetThreshold(Long value) {
        if (value != null && getBlockEntity() instanceof LevelEmitterBlockEntity levelEmitter) {
            levelEmitter.setThreshold(value);
        }
    }
}
