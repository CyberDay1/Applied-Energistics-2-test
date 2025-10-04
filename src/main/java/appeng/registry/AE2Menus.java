package appeng.registry;

import appeng.AE2Registries;
import appeng.menu.ChargerMenu;
import appeng.menu.InscriberMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2Menus {
    public static final RegistryObject<MenuType<InscriberMenu>> INSCRIBER_MENU =
        AE2Registries.MENUS.register("inscriber", () -> new MenuType<>(InscriberMenu::new));

    public static final RegistryObject<MenuType<ChargerMenu>> CHARGER_MENU =
        AE2Registries.MENUS.register("charger", () -> new MenuType<>(ChargerMenu::new));

    private AE2Menus() {}
}
