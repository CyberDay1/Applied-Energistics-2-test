package appeng.integration.compat;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import appeng.api.compat.CuriosCompat;
import appeng.api.compat.JeiCompat;
import appeng.api.compat.ReiCompat;
import appeng.api.ids.AEConstants;

@Mod.EventBusSubscriber(modid = AEConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class InteropBootstrap {
    private InteropBootstrap() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (JeiCompat.isLoaded()) {
                JeiCompat.reportBridgeInitialized();
            }
            if (ReiCompat.isLoaded()) {
                ReiCompat.reportBridgeInitialized();
            }
            if (CuriosCompat.isLoaded()) {
                CuriosCompat.reportBridgeInitialized();
            }
        });
    }

    @Mod.EventBusSubscriber(modid = AEConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Common {
        private Common() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                if (CuriosCompat.isLoaded()) {
                    CuriosCompat.reportBridgeInitialized();
                }
            });
        }
    }
}
