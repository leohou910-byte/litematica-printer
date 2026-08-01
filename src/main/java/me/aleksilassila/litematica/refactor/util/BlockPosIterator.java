package me.aleksilassila.litematica.refactor.util;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockPosIterator implements Iterator<BlockPos> {
    private Vec3 eyePos;
    private BlockPos pos;

    public BlockPosIterator(LocalPlayer player) {
        this.eyePos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
    }

    @Override
    public boolean hasNext() {
        return pos == null;
    }

    @Override
    public BlockPos next() {
        // TODO Auto-generated method stub
        return null;
    }
}