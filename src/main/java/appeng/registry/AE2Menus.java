package appeng.registry;

import appeng.AE2Registries;
import appeng.menu.ChargerMenu;
import appeng.menu.InscriberMenu;
import appeng.menu.terminal.CraftingTerminalMenu;
import appeng.menu.terminal.PatternTerminalMenu;
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

    private AE2Menus() {}
}
