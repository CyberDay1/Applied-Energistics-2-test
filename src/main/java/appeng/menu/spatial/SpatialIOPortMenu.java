package appeng.menu.spatial;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity.LastAction;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.guisync.GuiSync;

public class SpatialIOPortMenu extends AEBaseMenu {
    public static final MenuType<SpatialIOPortMenu> TYPE = MenuTypeBuilder
            .create(SpatialIOPortMenu::new, SpatialIOPortBlockEntity.class)
            .build("spatial_io_port");

    private final SpatialIOPortBlockEntity port;

    @GuiSync(0)
    public int regionSizeX;
    @GuiSync(1)
    public int regionSizeY;
    @GuiSync(2)
    public int regionSizeZ;
    @GuiSync(3)
    public int lastActionOrdinal;
    @GuiSync(4)
    public boolean inProgress;

    private static final int STATUS_MESSAGE_TICKS = 60;

    private int completionTicks;
    private int cancelTicks;
    private int rollbackTicks;

    public SpatialIOPortMenu(int id, Inventory playerInventory, SpatialIOPortBlockEntity port) {
        super(TYPE, id, playerInventory, Objects.requireNonNull(port, "port"));
        this.port = port;

        var cached = port.getRegionSize();
        updateRegionSize(cached);
        updateLastAction(port.getLastAction());
        setInProgress(port.isInProgress());

        addSlot(new Slot(port.getInternalInventory().toContainer(), 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ISpatialStorageCell;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1;
            }
        }, SlotSemantics.MACHINE_INPUT);

        addPlayerInventorySlots(playerInventory);
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

    public SpatialIOPortBlockEntity getBlockEntity() {
        return port;
    }

    public BlockPos getBlockPos() {
        return port.getBlockPos();
    }

    public BlockPos getRegionSize() {
        return new BlockPos(regionSizeX, regionSizeY, regionSizeZ);
    }

    public void updateRegionSize(BlockPos size) {
        this.regionSizeX = size.getX();
        this.regionSizeY = size.getY();
        this.regionSizeZ = size.getZ();
    }

    public LastAction getLastAction() {
        var actions = LastAction.values();
        if (lastActionOrdinal < 0 || lastActionOrdinal >= actions.length) {
            return LastAction.NONE;
        }
        return actions[lastActionOrdinal];
    }

    public void updateLastAction(LastAction action) {
        this.lastActionOrdinal = action.ordinal();
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
        if (inProgress) {
            completionTicks = 0;
            cancelTicks = 0;
            rollbackTicks = 0;
        }
    }

    public void handleOperationComplete() {
        setInProgress(false);
        completionTicks = STATUS_MESSAGE_TICKS;
        cancelTicks = 0;
        rollbackTicks = 0;
    }

    public void handleOperationCancelled() {
        setInProgress(false);
        completionTicks = 0;
        cancelTicks = STATUS_MESSAGE_TICKS;
        rollbackTicks = 0;
    }

    public void handleOperationRolledBack() {
        setInProgress(false);
        completionTicks = 0;
        cancelTicks = 0;
        rollbackTicks = STATUS_MESSAGE_TICKS;
    }

    public void clientTick() {
        if (completionTicks > 0) {
            completionTicks--;
        }
        if (cancelTicks > 0) {
            cancelTicks--;
        }
        if (rollbackTicks > 0) {
            rollbackTicks--;
        }
    }

    public boolean isShowingCompletionMessage() {
        return completionTicks > 0;
    }

    public boolean isShowingCancelledMessage() {
        return cancelTicks > 0;
    }

    public boolean isShowingRolledBackMessage() {
        return rollbackTicks > 0;
    }

    public ItemStack getSpatialCellStack() {
        return port.getInternalInventory().getStackInSlot(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return !port.isRemoved();
    }
}
