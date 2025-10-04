package appeng.menu.locator;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.compat.CuriosCompat;

/**
 * Implements {@link ItemMenuHostLocator} for items equipped in curios slots.
 */
record CuriosItemLocator(int curioSlot, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public ItemStack locateItem(Player player) {
        var handler = CuriosCompat.getCuriosHandler(player, curioSlot);
        if (handler.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return handler.orElseThrow().getStackInSlot(curioSlot);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(curioSlot);
        buf.writeOptional(Optional.ofNullable(hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(
                buf.readInt(),
                buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        return "curios slot " + curioSlot;
    }
}
