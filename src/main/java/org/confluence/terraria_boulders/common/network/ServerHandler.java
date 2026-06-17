package org.confluence.terraria_boulders.common.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.confluence.terraria_boulders.common.entity.CannonSeatEntity;

public class ServerHandler {
    public static void handleData(MountClickPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.getVehicle() instanceof CannonSeatEntity vehicle) {
                if (payload.isLeftClick()) {
                    vehicle.onLeftClick(player);
                } else {
                    vehicle.onRightClick(player);
                }
            }
        });
    }
}
