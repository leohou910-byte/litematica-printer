package me.aleksilassila.litematica.printer.pwp.refactor.core.action;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;

public class PlaceBlockAction {
    
    private BlockPos hitblockPos;
    private Vec2 lookVec2;
    private Direction hitBlockDirection;

    public PlaceBlockAction(BlockPos hitblockPos, Vec2 lookVec2, Direction hitBlockDirection) {
        this.hitblockPos = hitblockPos;
        this.lookVec2 = lookVec2;
        this.hitBlockDirection = hitBlockDirection;
    }

    public BlockPos getHitblockPos() {
        return hitblockPos;
    }

    public Vec2 getLookVec2() {
        return lookVec2;
    }
    
    public Direction getHitBlockDirection() {
        return hitBlockDirection;
    }
}
