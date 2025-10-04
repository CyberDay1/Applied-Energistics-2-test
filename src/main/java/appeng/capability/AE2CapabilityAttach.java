package appeng.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;

import appeng.api.grid.IGridHost;
import appeng.capability.provider.GridNodeCapabilityProvider;
import appeng.capability.provider.InWorldGridNodeHostProvider;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.core.AppEng;

@EventBusSubscriber(modid = AppEng.MOD_ID)
public final class AE2CapabilityAttach {
    private AE2CapabilityAttach() {
    }

    private static ResourceLocation rl(String path) {
        return AppEng.makeId(path);
    }

    @SubscribeEvent
    public static void onAttachBlockEntity(final AttachCapabilitiesEvent<BlockEntity> event) {
        final BlockEntity be = event.getObject();
        if (be instanceof IInWorldGridNodeHost host) {
            event.addCapability(rl("inworld_gridnode_host"), new InWorldGridNodeHostProvider(be, host));
        }
        if (be instanceof IGridHost host) {
            event.addCapability(rl("grid_node"), new GridNodeCapabilityProvider(be, host));
        }
    }

    @SubscribeEvent
    public static void onAttachItem(final AttachCapabilitiesEvent<ItemStack> event) {
        // Intentionally left empty; item capabilities will be wired up as part of the migration.
    }

    @SubscribeEvent
    public static void onAttachEntity(final AttachCapabilitiesEvent<Entity> event) {
        // Intentionally left empty; entity capabilities will be wired up as part of the migration.
    }
}
