package appeng.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.AE2Registries;
import appeng.client.screen.ChargerScreen;
import appeng.client.screen.InscriberScreen;
import appeng.client.screen.TerminalScreen;
import appeng.registry.AE2Menus;
import appeng.menu.terminal.TerminalMenu;

@EventBusSubscriber(modid = AE2Registries.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2ClientSetup {
    private AE2ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(AE2Menus.INSCRIBER_MENU.get(), InscriberScreen::new);
            MenuScreens.register(AE2Menus.CHARGER_MENU.get(), ChargerScreen::new);
            MenuScreens.register(TerminalMenu.TYPE, TerminalScreen::new);
        });
    }
}
