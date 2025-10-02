package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record AE2HelloS2CPayload(String message) implements CustomPacketPayload {
    public static final Type<AE2HelloS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "hello_s2c"));

    public static final StreamCodec<FriendlyByteBuf, AE2HelloS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.message()),
            buf -> new AE2HelloS2CPayload(buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
