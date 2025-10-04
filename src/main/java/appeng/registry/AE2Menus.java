package appeng.registry;

import appeng.AE2Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2Menus {
    public static final RegistryObject<MenuType<?>> INSCRIBER_MENU =
        AE2Registries.MENUS.register("inscriber", () -> new MenuType<>((id, inv) -> {
            // TODO: return new InscriberMenu(id, inv);
            return null;
        }));

    private AE2Menus() {}
}
