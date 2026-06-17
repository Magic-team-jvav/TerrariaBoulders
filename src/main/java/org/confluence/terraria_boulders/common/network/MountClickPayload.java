package org.confluence.terraria_boulders.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.confluence.terraria_boulders.TerrariaBoulders;

public record MountClickPayload(boolean isLeftClick) implements CustomPacketPayload {
    public static final Type<MountClickPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "mount_click"));
    public static final StreamCodec<ByteBuf, MountClickPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MountClickPayload::isLeftClick,
            MountClickPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}