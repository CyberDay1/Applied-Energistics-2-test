package appeng.menu.me.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.network.PacketDistributor;

import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.crafting.monitor.CraftingMonitorEntry;
import appeng.core.network.clientbound.CraftingMonitorUpdatePacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;

public class CraftingMonitorMenu extends AEBaseMenu {

    public static final MenuType<CraftingMonitorMenu> TYPE = MenuTypeBuilder
            .create(CraftingMonitorMenu::new, CraftingMonitorBlockEntity.class)
            .build("crafting_monitor");

    private final CraftingMonitorBlockEntity monitor;
    private List<CraftingMonitorEntry> lastSentJobs = List.of();
    private final List<CraftingMonitorEntry> clientJobs = new ArrayList<>();

    protected CraftingMonitorMenu(MenuType<? extends CraftingMonitorMenu> type, int id, Inventory playerInventory,
            CraftingMonitorBlockEntity monitor) {
        super(type, id, playerInventory, monitor);
        this.monitor = monitor;
        this.createPlayerInventorySlots(playerInventory);
    }

    public CraftingMonitorMenu(int id, Inventory playerInventory, CraftingMonitorBlockEntity monitor) {
        this(TYPE, id, playerInventory, monitor);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (getPlayer() instanceof ServerPlayer) {
            var jobs = monitor.getCurrentJobs();
            if (!jobs.equals(lastSentJobs)) {
                lastSentJobs = List.copyOf(jobs);
                monitor.updateTrackedJobs(jobs);
                PacketDistributor.sendToPlayer((ServerPlayer) getPlayer(),
                        new CraftingMonitorUpdatePacket(containerId, jobs));
            }
        }
    }

    public List<CraftingMonitorEntry> getJobs() {
        if (getPlayer().level().isClientSide()) {
            return List.copyOf(clientJobs);
        }
        return monitor.getCurrentJobs();
    }

    public void handleClientUpdate(List<CraftingMonitorEntry> jobs) {
        clientJobs.clear();
        clientJobs.addAll(jobs);
        monitor.updateTrackedJobs(jobs);
    }
}
