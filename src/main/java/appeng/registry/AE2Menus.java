package appeng.registry;

import appeng.AE2Registries;
import appeng.menu.ChargerMenu;
import appeng.menu.InscriberMenu;
import appeng.menu.me.crafting.CraftingMonitorMenu;
import appeng.menu.terminal.CraftingTerminalMenu;
import appeng.menu.implementations.AnnihilationPlaneMenu;
import appeng.menu.implementations.PatternEncodingTerminalMenu;
import appeng.menu.terminal.PatternTerminalMenu;
import appeng.menu.simple.SimpleDriveMenu;
import appeng.menu.spatial.SpatialIOPortMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2Menus {
    public static final RegistryObject<MenuType<InscriberMenu>> INSCRIBER_MENU =
        AE2Registries.MENUS.register("inscriber", () -> new MenuType<>(InscriberMenu::new));

    public static final RegistryObject<MenuType<ChargerMenu>> CHARGER_MENU =
        AE2Registries.MENUS.register("charger", () -> new MenuType<>(ChargerMenu::new));

    public static final RegistryObject<MenuType<CraftingTerminalMenu>> CRAFTING_TERMINAL_MENU =
        AE2Registries.MENUS.register("crafting_terminal", () -> CraftingTerminalMenu.TYPE);

    public static final RegistryObject<MenuType<PatternTerminalMenu>> PATTERN_TERMINAL_MENU =
        AE2Registries.MENUS.register("pattern_terminal", () -> PatternTerminalMenu.TYPE);

    public static final RegistryObject<MenuType<PatternEncodingTerminalMenu>> PATTERN_ENCODING_TERMINAL_MENU =
        AE2Registries.MENUS.register("pattern_encoding_terminal", () -> PatternEncodingTerminalMenu.TYPE);

    public static final RegistryObject<MenuType<CraftingMonitorMenu>> CRAFTING_MONITOR_MENU =
        AE2Registries.MENUS.register("crafting_monitor", () -> CraftingMonitorMenu.TYPE);

    public static final RegistryObject<MenuType<SimpleDriveMenu>> SIMPLE_DRIVE_MENU =
        AE2Registries.MENUS.register("simple_drive", () -> SimpleDriveMenu.TYPE);

    public static final RegistryObject<MenuType<SpatialIOPortMenu>> SPATIAL_IO_PORT_MENU =
        AE2Registries.MENUS.register("spatial_io_port", () -> SpatialIOPortMenu.TYPE);

    public static final RegistryObject<MenuType<AnnihilationPlaneMenu>> ANNIHILATION_PLANE_MENU =
        AE2Registries.MENUS.register("annihilation_plane", () -> AnnihilationPlaneMenu.TYPE);

    private AE2Menus() {}
}
