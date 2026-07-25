package me.aleksilassila.litematica.printer.pwp.utils;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ActionControler {
    public static void lookBlock(Minecraft client, BlockPos pos) {
        if (client == null || client.player == null || pos == null) return;

        LocalPlayer player = client.player;

        // 計算眼睛座標和目標座標差距
        Vec3 targetVec = Vec3.atCenterOf(pos);
        double dx = targetVec.x - client.player.getX();
        double dy = targetVec.y - client.player.getEyeY();
        double dz = targetVec.z - client.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // 計算左右偏航角(yaw)與上下俯仰角(pitch)
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distance)));

        // 發送轉向封包
        //#if MC >= 12102
        player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
        //#else
        //$$ player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround()));
        //#endif
    }

    public static void rightClickBlock(final Minecraft client, final BlockPos pos) {
        //#if MC >= 11902
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
        //#else
        //$$client.gameMode.useItemOn(client.player, client.level, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
        //#endif
    }
}
