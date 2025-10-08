package appeng.client;

//? if eval(current.version, ">=1.21.4") {
import net.neoforged.neoforge.client.gui.ScreenManager;
//? } else {
import net.minecraft.client.gui.screens.MenuScreens;
//? }
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.AE2Registries;
import appeng.client.screen.ChargerScreen;
import appeng.client.screen.CraftingMonitorScreen;
import appeng.client.screen.CraftingTerminalScreen;
import appeng.client.screen.InscriberScreen;
import appeng.client.screen.PatternTerminalScreen;
import appeng.client.screen.SimpleDriveScreen;
import appeng.client.screen.spatial.SpatialIOPortScreen;
import appeng.client.screen.TerminalScreen;
import appeng.registry.AE2Menus;
import appeng.menu.terminal.TerminalMenu;
import appeng.menu.simple.SimpleDriveMenu;
import appeng.menu.spatial.SpatialIOPortMenu;

@EventBusSubscriber(modid = AE2Registries.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2ClientSetup {
    private AE2ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        //? if eval(current.version, ">=1.21.4") {
        registerScreens();
        //? } else {
        event.enqueueWork(AE2ClientSetup::registerScreens);
        //? }
    }

    private static void registerScreens() {
        //? if eval(current.version, ">=1.21.4") {
        ScreenManager.registerFactory(AE2Menus.INSCRIBER_MENU, InscriberScreen::new);
        ScreenManager.registerFactory(AE2Menus.CHARGER_MENU, ChargerScreen::new);
        ScreenManager.registerFactory(AE2Menus.CRAFTING_TERMINAL_MENU, CraftingTerminalScreen::new);
        ScreenManager.registerFactory(AE2Menus.PATTERN_TERMINAL_MENU, PatternTerminalScreen::new);
        ScreenManager.registerFactory(AE2Menus.CRAFTING_MONITOR_MENU, CraftingMonitorScreen::new);
        ScreenManager.registerFactory(() -> TerminalMenu.TYPE, TerminalScreen::new);
        ScreenManager.registerFactory(() -> SimpleDriveMenu.TYPE, SimpleDriveScreen::new);
        ScreenManager.registerFactory(() -> SpatialIOPortMenu.TYPE, SpatialIOPortScreen::new);
        //? } else {
        MenuScreens.register(AE2Menus.INSCRIBER_MENU.get(), InscriberScreen::new);
        MenuScreens.register(AE2Menus.CHARGER_MENU.get(), ChargerScreen::new);
        MenuScreens.register(AE2Menus.CRAFTING_TERMINAL_MENU.get(), CraftingTerminalScreen::new);
        MenuScreens.register(AE2Menus.PATTERN_TERMINAL_MENU.get(), PatternTerminalScreen::new);
        MenuScreens.register(AE2Menus.CRAFTING_MONITOR_MENU.get(), CraftingMonitorScreen::new);
        MenuScreens.register(TerminalMenu.TYPE, TerminalScreen::new);
        MenuScreens.register(SimpleDriveMenu.TYPE, SimpleDriveScreen::new);
        MenuScreens.register(SpatialIOPortMenu.TYPE, SpatialIOPortScreen::new);
        //? }
    }
}
