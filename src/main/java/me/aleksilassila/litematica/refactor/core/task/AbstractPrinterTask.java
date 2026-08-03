package me.aleksilassila.litematica.refactor.core.task;

import java.util.Iterator;

import me.aleksilassila.litematica.refactor.util.BlockPosIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractPrinterTask {
    protected BlockPos pos;
    protected Iterator<BlockPos> posIterator;
    
    public AbstractPrinterTask(){
    }
    
    public final void tick(Minecraft minecraft, int printDistance, int executionPertick) {
        Vec3 playerEyeVec3 = minecraft.player.getEyePosition();
        this.pos = getPos(playerEyeVec3, printDistance);
        if (pos == null) return;
        
        if (canExecute(pos)) {
            execute(pos);
        }
    }

    protected BlockPos getPos(Vec3 eyeVec3, double reachDistance) {
        if (eyeVec3 == null || reachDistance <= 0) return null;

        if (this.posIterator == null) {
            BlockPos eyePos = BlockPos.containing(eyeVec3);
            int distanceInteger = ((int) reachDistance) + 2;
            this.posIterator = new BlockPosIterator(eyePos, distanceInteger).iterator();
        }

        double reachDistanceSqr = reachDistance * reachDistance;
        while (posIterator.hasNext()) {
            BlockPos nextPos = posIterator.next();
            
            Vec3 posVec3 = Vec3.atCenterOf(nextPos);
            if (eyeVec3.distanceToSqr(posVec3) > reachDistanceSqr) continue;

            return nextPos;
        }

        return null;
    }

    protected abstract boolean canExecute(BlockPos pos);

    protected abstract void execute(BlockPos pos);
}
