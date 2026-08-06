package me.aleksilassila.litematica.printer.pwp.refactor.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.core.BlockPos;

/**
 * 提供三維空間區域內方塊位置 ({@link BlockPos}) 的走訪迭代器。
 */
public class BlockPosIterable implements Iterable<BlockPos> {

    private BlockPos pos1;
    private BlockPos pos2;

    public BlockPosIterable() {
    }

    public BlockPosIterable(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public BlockPosIterable(int x1, int y1, int z1, int x2, int y2, int z2) {
        this(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
    }

    public BlockPosIterable(BlockPos centerPos, int distance) {
        this(centerPos.offset(-distance, -distance, -distance), centerPos.offset(distance, distance, distance));
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        if (this.pos1 == null || this.pos2 == null) {
            return Collections.emptyIterator();
        }

        BlockPos minPos = new BlockPos(
            Math.min(this.pos1.getX(), this.pos2.getX()),
            Math.min(this.pos1.getY(), this.pos2.getY()),
            Math.min(this.pos1.getZ(), this.pos2.getZ())
        );

        BlockPos maxPos = new BlockPos(
            Math.max(this.pos1.getX(), this.pos2.getX()),
            Math.max(this.pos1.getY(), this.pos2.getY()),
            Math.max(this.pos1.getZ(), this.pos2.getZ())
        );

        return new Itr(minPos, maxPos);
    }

    private static class Itr implements Iterator<BlockPos> {

        private final BlockPos minPos;
        private final BlockPos maxPos;
        private int x;
        private int y;
        private int z;
        private boolean hasNext;

        public Itr(BlockPos minPos, BlockPos maxPos) {
            this.minPos = minPos;
            this.maxPos = maxPos;

            this.x = this.minPos.getX();
            this.y = this.minPos.getY();
            this.z = this.minPos.getZ();
            
            this.hasNext = (
                this.minPos.getX() <= this.maxPos.getX() && 
                this.minPos.getY() <= this.maxPos.getY() && 
                this.minPos.getZ() <= this.maxPos.getZ()
            );
        }
        
        @Override
        public boolean hasNext() {
            return this.hasNext;
        }

        @Override
        public BlockPos next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in BlockPosIterator");
            }
            
            BlockPos result = new BlockPos(this.x, this.y, this.z);

            this.x++;
            if (this.x > this.maxPos.getX()) {
                this.x = this.minPos.getX();

                this.z++;
                if (this.z > this.maxPos.getZ()) {
                    this.z = this.minPos.getZ();

                    this.y++;
                    if (this.y > this.maxPos.getY()) {
                        this.hasNext = false;
                    }
                }
            }

            return result;
        }
    }
}