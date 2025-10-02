package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record AE2LoginSyncS2CPayload(byte[] configBlob) implements CustomPacketPayload {
    public static final Type<AE2LoginSyncS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "login_sync_s2c"));

    public static final StreamCodec<FriendlyByteBuf, AE2LoginSyncS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeByteArray(payload.configBlob()),
            buf -> new AE2LoginSyncS2CPayload(buf.readByteArray()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
