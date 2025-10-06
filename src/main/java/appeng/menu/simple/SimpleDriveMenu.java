package appeng.menu.simple;

import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;

import appeng.api.config.RedstoneMode;
import appeng.blockentity.simple.DriveBlockEntity;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;

public class SimpleDriveMenu extends AEBaseMenu {
    public static final int BUTTON_TOGGLE_REDSTONE = 1;

    public static final MenuType<SimpleDriveMenu> TYPE = MenuTypeBuilder
            .create(SimpleDriveMenu::new, DriveBlockEntity.class)
            .withMenuTitle(host -> Component.translatable("block.ae2.drive"))
            .build("simple_drive");

    private static final int DATA_REDSTONE_MODE = 0;
    private static final int DATA_OFFLINE_REASON = 1;
    private static final int DATA_ONLINE = 2;

    private final DriveBlockEntity drive;
    private final SimpleContainerData syncData = new SimpleContainerData(3);

    public SimpleDriveMenu(int id, Inventory inventory, DriveBlockEntity drive) {
        this(TYPE, id, inventory, drive);
    }

    protected SimpleDriveMenu(MenuType<? extends SimpleDriveMenu> type, int id, Inventory inventory,
            DriveBlockEntity drive) {
        super(type, id, inventory, Objects.requireNonNull(drive, "drive"));
        this.drive = drive;

        addDataSlots(syncData);
        addDriveSlots();
        addPlayerInventorySlots(inventory);
    }

    private void addDriveSlots() {
        var container = drive.getCellContainer();
        int index = 0;
        int baseX = 62;
        int baseY = 20;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int x = baseX + col * 18;
                int y = baseY + row * 18;
                addSlot(new Slot(container, index++, x, y), SlotSemantics.STORAGE_CELL);
            }
        }
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                int x = 8 + col * 18;
                int y = 84 + row * 18;
                addSlot(new Slot(inventory, slotIndex, x, y), SlotSemantics.PLAYER_INVENTORY);
            }
        }

        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 142;
            addSlot(new Slot(inventory, col, x, y), SlotSemantics.PLAYER_HOTBAR);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return drive != null && !drive.isRemoved();
    }

    @Override
    public void broadcastChanges() {
        if (!getPlayer().level().isClientSide()) {
            syncData.set(DATA_REDSTONE_MODE, drive.getRedstoneMode().ordinal());
            syncData.set(DATA_OFFLINE_REASON, drive.getOfflineReason().ordinal());
            syncData.set(DATA_ONLINE, drive.isGridOnline() ? 1 : 0);
        }
        super.broadcastChanges();
    }

    public RedstoneMode getRedstoneMode() {
        if (getPlayer().level().isClientSide()) {
            return enumByOrdinal(RedstoneMode.values(), syncData.get(DATA_REDSTONE_MODE), RedstoneMode.IGNORE);
        }
        return drive.getRedstoneMode();
    }

    public OfflineReason getOfflineReason() {
        if (getPlayer().level().isClientSide()) {
            return enumByOrdinal(OfflineReason.values(), syncData.get(DATA_OFFLINE_REASON), OfflineReason.NONE);
        }
        return drive.getOfflineReason();
    }

    public boolean isGridOnline() {
        if (getPlayer().level().isClientSide()) {
            return syncData.get(DATA_ONLINE) != 0;
        }
        return drive.isGridOnline();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_TOGGLE_REDSTONE) {
            if (player instanceof ServerPlayer) {
                drive.setRedstoneMode(nextRedstoneMode(drive.getRedstoneMode()));
            }
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    private static RedstoneMode nextRedstoneMode(RedstoneMode mode) {
        return switch (mode) {
            case IGNORE -> RedstoneMode.HIGH_SIGNAL;
            case HIGH_SIGNAL -> RedstoneMode.LOW_SIGNAL;
            default -> RedstoneMode.IGNORE;
        };
    }

    private static <E extends Enum<E>> E enumByOrdinal(E[] values, int ordinal, E fallback) {
        if (ordinal < 0 || ordinal >= values.length) {
            return fallback;
        }
        return values[ordinal];
    }
}
