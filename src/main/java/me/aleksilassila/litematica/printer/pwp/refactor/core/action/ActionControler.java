package me.aleksilassila.litematica.printer.pwp.refactor.core.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ActionControler {
    
    // look
    public static float[] getLookBlockAngle(LocalPlayer player, BlockPos pos) {
        if (player == null) {
            return null;
        };

        if (pos == null) {
            return new float[]{player.getYRot(), player.getXRot()};
        }
        
        // 計算眼睛座標和目標座標差距
        Vec3 targetVec = Vec3.atCenterOf(pos);
        double dx = targetVec.x - player.getX();
        double dy = targetVec.y - player.getEyeY();
        double dz = targetVec.z - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // 計算左右偏航角(yaw)與上下俯仰角(pitch)
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distance)));

        return new float[]{yaw, pitch};
    }

    public static void lookBlock(LocalPlayer player, BlockPos pos) {
        if (player == null || pos == null) return;

        // 獲得看向該座標的角度
        float[] lookBlockAngle = getLookBlockAngle(player, pos);
        float yaw = lookBlockAngle[0];
        float pitch = lookBlockAngle[1];

        // 發送轉向封包
        //#if MC >= 12102
        player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
        //#else
        //$$ player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround()));
        //#endif
    }

    // click
    public static void rightClickBlock(final Minecraft client, final BlockPos pos) {
        //#if MC >= 11902
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
        //#else
        //$$client.gameMode.useItemOn(client.player, client.level, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false));
        //#endif
    }
}
