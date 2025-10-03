package appeng.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitScreens;

@EventBusSubscriber(modid = AppEng.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2ClientSetup {

    private AE2ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            InitScreens.registerDirect();

            ItemBlockRenderTypes.setRenderLayer(AEBlocks.QUARTZ_GLASS.block(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColors blockColors = event.getBlockColors();
        InitBlockColors.init(blockColors);
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        InitItemColors.init(event);
    }
}
