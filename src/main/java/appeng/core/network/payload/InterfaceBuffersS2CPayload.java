package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.item.ItemStack;

import appeng.core.AppEng;

public record InterfaceBuffersS2CPayload(BlockPos pos, ItemStack input, ItemStack output)
        implements CustomPacketPayload {

    public static final Type<InterfaceBuffersS2CPayload> TYPE = new Type<>(AppEng.makeId("interface_buffers"));

    public static final StreamCodec<FriendlyByteBuf, InterfaceBuffersS2CPayload> STREAM_CODEC = StreamCodec.of(
            InterfaceBuffersS2CPayload::write, InterfaceBuffersS2CPayload::read);

    private static InterfaceBuffersS2CPayload read(FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var input = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        var output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        return new InterfaceBuffersS2CPayload(pos, input, output);
    }

    private static void write(FriendlyByteBuf buf, InterfaceBuffersS2CPayload payload) {
        buf.writeBlockPos(payload.pos());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.input());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.output());
    }

    @Override
    public Type<InterfaceBuffersS2CPayload> type() {
        return TYPE;
    }
}
