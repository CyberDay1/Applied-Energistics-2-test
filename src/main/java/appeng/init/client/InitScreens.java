/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.init.client;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.ExportBusBlockScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.EnergyLevelEmitterScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.ImportBusBlockScreen;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.MEChestScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.PatternEncodingTerminalScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialAnchorScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.StorageBusBlockScreen;
import appeng.client.gui.implementations.StorageBusScreen;
import appeng.client.gui.implementations.LevelEmitterScreen;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.client.gui.implementations.VibrationChamberScreen;
import appeng.client.gui.implementations.WirelessAccessPointScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusScreen;
import appeng.client.gui.me.crafting.SetStockAmountScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.me.networktool.NetworkToolScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.screen.PartitionedCellScreen;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.menu.implementations.CondenserMenu;
import appeng.menu.implementations.DriveMenu;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.implementations.ExportBusBlockMenu;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.IOPortMenu;
import appeng.menu.implementations.InscriberMenu;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.MEChestMenu;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.implementations.PatternEncodingTerminalMenu;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.implementations.PriorityMenu;
import appeng.menu.implementations.QNBMenu;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.implementations.SetStockAmountMenu;
import appeng.menu.implementations.SkyChestMenu;
import appeng.menu.implementations.SpatialAnchorMenu;
import appeng.menu.implementations.SpatialIOPortMenu;
import appeng.menu.implementations.ImportBusBlockMenu;
import appeng.menu.implementations.StorageBusBlockMenu;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.implementations.LevelEmitterMenu;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.implementations.WirelessAccessPointMenu;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.me.items.BasicCellChestMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;
import appeng.menu.PartitionedCellMenu;

/**
 * The server sends the client a menu identifier, which the client then maps onto a screen using {@link MenuScreens}.
 * This class registers our screens.
 */
public final class InitScreens {

    @VisibleForTesting
    static final Map<MenuType<?>, String> MENU_STYLES = new IdentityHashMap<>();

    private InitScreens() {
    }

    public static void init(RegisterMenuScreensEvent event) {
        registerAll((type, factory, stylePath) -> register(event, type, factory, stylePath));
    }

    public static void registerDirect() {
        registerAll(InitScreens::register);
    }

    private static void registerAll(StyledScreenRegistrar registrar) {
        // spotless:off
        registrar.register(QNBMenu.TYPE, QNBScreen::new, "/screens/qnb.json");
        registrar.register(SkyChestMenu.TYPE, SkyChestScreen::new, "/screens/sky_chest.json");
        registrar.register(MEChestMenu.TYPE, MEChestScreen::new, "/screens/me_chest.json");
        registrar.register(WirelessAccessPointMenu.TYPE, WirelessAccessPointScreen::new, "/screens/wireless_access_point.json");
        registrar.register(NetworkStatusMenu.NETWORK_TOOL_TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        registrar.register(NetworkStatusMenu.CONTROLLER_TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        registrar.register(CraftingCPUMenu.TYPE, CraftingCPUScreen<CraftingCPUMenu>::new, "/screens/crafting_cpu.json");
        registrar.register(NetworkToolMenu.TYPE, NetworkToolScreen::new, "/screens/network_tool.json");
        registrar.register(QuartzKnifeMenu.TYPE, QuartzKnifeScreen::new, "/screens/quartz_knife.json");
        registrar.register(DriveMenu.TYPE, DriveScreen::new, "/screens/drive.json");
        registrar.register(VibrationChamberMenu.TYPE, VibrationChamberScreen::new, "/screens/vibration_chamber.json");
        registrar.register(CondenserMenu.TYPE, CondenserScreen::new, "/screens/condenser.json");
        registrar.register(InterfaceMenu.TYPE, InterfaceScreen<InterfaceMenu>::new, "/screens/interface.json");
        registrar.register(IOBusMenu.EXPORT_TYPE, IOBusScreen::new, "/screens/export_bus.json");
        registrar.register(IOBusMenu.IMPORT_TYPE, IOBusScreen::new, "/screens/import_bus.json");
        registrar.register(IOPortMenu.TYPE, IOPortScreen::new, "/screens/io_port.json");
        registrar.register(StorageBusMenu.TYPE, StorageBusScreen::new, "/screens/storage_bus.json");
        registrar.register(StorageBusBlockMenu.TYPE, StorageBusBlockScreen::new, "/screens/storage_bus_block.json");
        registrar.register(ImportBusBlockMenu.TYPE, ImportBusBlockScreen::new, "/screens/import_bus_block.json");
        registrar.register(ExportBusBlockMenu.TYPE, ExportBusBlockScreen::new, "/screens/export_bus_block.json");
        registrar.register(SetStockAmountMenu.TYPE, SetStockAmountScreen::new, "/screens/set_stock_amount.json");
        registrar.register(FormationPlaneMenu.TYPE, FormationPlaneScreen::new, "/screens/formation_plane.json");
        registrar.register(PriorityMenu.TYPE, PriorityScreen::new, "/screens/priority.json");
        registrar.register(StorageLevelEmitterMenu.TYPE, StorageLevelEmitterScreen::new, "/screens/level_emitter.json");
        registrar.register(LevelEmitterMenu.TYPE, LevelEmitterScreen::new, "/screens/level_emitter_block.json");
        registrar.register(EnergyLevelEmitterMenu.TYPE, EnergyLevelEmitterScreen::new, "/screens/energy_level_emitter.json");
        registrar.register(SpatialIOPortMenu.TYPE, SpatialIOPortScreen::new, "/screens/spatial_io_port.json");
        registrar.register(InscriberMenu.TYPE, InscriberScreen::new, "/screens/inscriber.json");
        registrar.register(CellWorkbenchMenu.TYPE, CellWorkbenchScreen::new, "/screens/cell_workbench.json");
        registrar.register(PartitionedCellMenu.TYPE, PartitionedCellScreen::new, "/screens/partitioned_cell.json");
        registrar.register(PatternProviderMenu.TYPE, PatternProviderScreen<PatternProviderMenu>::new, "/screens/pattern_provider.json");
        registrar.register(MolecularAssemblerMenu.TYPE, MolecularAssemblerScreen::new, "/screens/molecular_assembler.json");
        registrar.register(CraftAmountMenu.TYPE, CraftAmountScreen::new, "/screens/craft_amount.json");
        registrar.register(CraftConfirmMenu.TYPE, CraftConfirmScreen::new, "/screens/craft_confirm.json");
        registrar.register(CraftingStatusMenu.TYPE, CraftingStatusScreen::new, "/screens/crafting_status.json");
        registrar.register(SpatialAnchorMenu.TYPE, SpatialAnchorScreen::new, "/screens/spatial_anchor.json");

        // Terminals
        registrar.register(MEStorageMenu.TYPE,
                MEStorageScreen::new,
                "/screens/terminals/terminal.json");
        registrar.register(BasicCellChestMenu.TYPE,
                MEStorageScreen::new,
                "/screens/terminals/terminal.json");
        registrar.register(MEStorageMenu.PORTABLE_ITEM_CELL_TYPE,
                MEStorageScreen::new,
                "/screens/terminals/portable_item_cell.json");
        registrar.register(MEStorageMenu.PORTABLE_FLUID_CELL_TYPE,
                MEStorageScreen::new,
                "/screens/terminals/portable_fluid_cell.json");
        registrar.register(MEStorageMenu.WIRELESS_TYPE,
                MEStorageScreen::new,
                "/screens/terminals/wireless_terminal.json");
        registrar.register(CraftingTermMenu.TYPE,
                CraftingTermScreen::new,
                "/screens/terminals/crafting_terminal.json");
        registrar.register(WirelessCraftingTermMenu.TYPE,
                CraftingTermScreen::new,
                "/screens/terminals/crafting_terminal.json");
        registrar.register(PatternEncodingTermMenu.TYPE,
                PatternEncodingTermScreen::new,
                "/screens/terminals/pattern_encoding_terminal.json");
        registrar.register(PatternEncodingTerminalMenu.TYPE,
                PatternEncodingTerminalScreen::new,
                "/screens/terminals/pattern_encoding_terminal.json");
        registrar.register(PatternAccessTermMenu.TYPE, PatternAccessTermScreen::new,
                "/screens/terminals/pattern_access_terminal.json");
        // spotless:on
    }

    /**
     * Registers a screen for a given menu and ensures the given style is applied after opening the screen.
     */
    private static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(MenuType<M> type,
            StyledScreenFactory<M, U> factory,
            String stylePath) {
        MENU_STYLES.put(type, stylePath);
        MenuScreens.register(type, (menu, playerInv, title) -> {
            var style = StyleManager.loadStyleDoc(stylePath);

            return factory.create(menu, playerInv, title, style);
        });
    }

    public static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(RegisterMenuScreensEvent event,
            MenuType<M> type,
            StyledScreenFactory<M, U> factory,
            String stylePath) {
        MENU_STYLES.put(type, stylePath);
        event.<M, U>register(type, (menu, playerInv, title) -> {
            var style = StyleManager.loadStyleDoc(stylePath);

            return factory.create(menu, playerInv, title, style);
        });
    }

    @FunctionalInterface
    private interface StyledScreenRegistrar {
        <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(MenuType<M> type,
                StyledScreenFactory<M, U> factory,
                String stylePath);
    }

    /**
     * A type definition that matches the constructors of our screens, which take an additional {@link ScreenStyle}
     * argument.
     */
    @FunctionalInterface
    public interface StyledScreenFactory<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T t, Inventory pi, Component title, ScreenStyle style);
    }

}
