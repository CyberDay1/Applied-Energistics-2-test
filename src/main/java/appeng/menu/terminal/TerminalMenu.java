package appeng.menu.terminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.storage.ItemStackView;
import appeng.blockentity.terminal.TerminalBlockEntity;
import appeng.core.network.clientbound.TerminalItemUpdatePacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.storage.impl.StorageService;
import appeng.api.config.RedstoneMode;
import appeng.grid.SimpleGridNode.OfflineReason;

public class TerminalMenu extends AEBaseMenu {
    public static final MenuType<TerminalMenu> TYPE = MenuTypeBuilder
            .create(TerminalMenu::new, TerminalBlockEntity.class)
            .build("terminal");
    public static final int BUTTON_TOGGLE_REDSTONE = 1;

    private final TerminalBlockEntity terminal;
    private final Player player;
    private List<ItemStackView> lastSentItems = List.of();
    private boolean lastSentOnline;
    private List<ItemStackView> clientItems = new ArrayList<>();
    private boolean clientOnline;
    private OfflineReason lastSentOfflineReason = OfflineReason.NONE;
    private OfflineReason clientOfflineReason = OfflineReason.NONE;
    private RedstoneMode lastSentRedstoneMode = RedstoneMode.IGNORE;
    private RedstoneMode clientRedstoneMode = RedstoneMode.IGNORE;

    protected TerminalMenu(MenuType<? extends TerminalMenu> type, int id, Inventory inv,
            TerminalBlockEntity terminal) {
        super(type, id, inv, terminal);
        this.terminal = terminal;
        this.player = inv.player;
        this.createPlayerInventorySlots(inv);
    }

    public TerminalMenu(int id, Inventory inv, TerminalBlockEntity terminal) {
        this(TYPE, id, inv, terminal);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            var items = terminal.getStoredItems();
            boolean online = terminal.isGridOnline();
            var offlineReason = terminal.getOfflineReason();
            var redstoneMode = terminal.getRedstoneMode();
            if (!items.equals(lastSentItems) || lastSentOnline != online
                    || lastSentOfflineReason != offlineReason
                    || lastSentRedstoneMode != redstoneMode) {
                lastSentItems = List.copyOf(items);
                lastSentOnline = online;
                lastSentOfflineReason = offlineReason;
                lastSentRedstoneMode = redstoneMode;
                PacketDistributor.sendToPlayer(serverPlayer,
                        new TerminalItemUpdatePacket(containerId, online, offlineReason, redstoneMode, items));
            }
        }
    }

    public List<ItemStackView> getClientItems() {
        if (player.level().isClientSide()) {
            return Collections.unmodifiableList(clientItems);
        }
        return terminal.getStoredItems();
    }

    public boolean isGridOnline() {
        if (player.level().isClientSide()) {
            return clientOnline;
        }
        return terminal.isGridOnline();
    }

    public OfflineReason getOfflineReason() {
        if (player.level().isClientSide()) {
            return clientOfflineReason;
        }
        return terminal.getOfflineReason();
    }

    public RedstoneMode getRedstoneMode() {
        if (player.level().isClientSide()) {
            return clientRedstoneMode;
        }
        return terminal.getRedstoneMode();
    }

    public void handleClientUpdate(List<ItemStackView> items, boolean online, OfflineReason offlineReason,
            RedstoneMode redstoneMode) {
        this.clientItems = new ArrayList<>(items);
        this.clientOnline = online;
        this.clientOfflineReason = offlineReason;
        this.clientRedstoneMode = redstoneMode;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_TOGGLE_REDSTONE) {
            if (player instanceof ServerPlayer) {
                var current = terminal.getRedstoneMode();
                terminal.setRedstoneMode(nextRedstoneMode(current));
            }
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    private static RedstoneMode nextRedstoneMode(RedstoneMode current) {
        return switch (current) {
            case IGNORE -> RedstoneMode.HIGH_SIGNAL;
            case HIGH_SIGNAL -> RedstoneMode.LOW_SIGNAL;
            default -> RedstoneMode.IGNORE;
        };
    }

    public void handleExtract(Item item, int amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (amount <= 0 || item == null) {
            return;
        }
        if (!terminal.isGridOnline()) {
            return;
        }

        var node = terminal.getGridNode();
        var gridId = node != null ? node.getGridId() : null;
        int removed = StorageService.extractFromNetwork(gridId, item, amount, false);
        if (removed <= 0) {
            return;
        }

        int remaining = removed;
        int maxStackSize = item.getDefaultMaxStackSize();
        while (remaining > 0) {
            int toGive = Math.min(maxStackSize, remaining);
            ItemStack stack = new ItemStack(item, toGive);
            if (!serverPlayer.getInventory().add(stack)) {
                serverPlayer.drop(stack, false);
            }
            remaining -= toGive;
        }
    }
}
