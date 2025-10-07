package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class AnnihilationPlaneMenu extends FormationPlaneMenu {

    public static final MenuType<AnnihilationPlaneMenu> TYPE = MenuTypeBuilder
            .create(AnnihilationPlaneMenu::new, FormationPlaneMenuHost.class)
            .build("annihilationplane");

    public AnnihilationPlaneMenu(MenuType<AnnihilationPlaneMenu> type, int id, Inventory ip,
            FormationPlaneMenuHost host) {
        super(type, id, ip, host);
    }
}
