package me.aleksilassila.litematica.refactor.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import kotlin.collections.builders.MapBuilder.Itr;
import net.minecraft.core.BlockPos;

public class BlockPosIterable implements Iterable<BlockPos> {
    private BlockPos pos1;
    private BlockPos pos2;

    public BlockPosIterable() {
    }

    public BlockPosIterable(int x1, int y1, int z1, int x2, int y2, int z2) {
        setBounds(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
    }

    public BlockPosIterable(BlockPos pos1, BlockPos pos2) {
        setBounds(pos1, pos2);
    }

    public BlockPosIterable(BlockPos centerPos, int distance) {
        if (centerPos != null && distance >= 0) {
            setBounds(
                new BlockPos(
                    centerPos.getX() - distance, 
                    centerPos.getY() - distance, 
                    centerPos.getZ() - distance
                ),
                new BlockPos(
                    centerPos.getX() + distance, 
                    centerPos.getY() + distance, 
                    centerPos.getZ() + distance
                )
            );
        }
    }

    private void setBounds(BlockPos p1, BlockPos p2) {
        if (p1 == null || p2 == null) {
            this.pos1 = null;
            this.pos2 = null;
            return;
        }
        this.pos1 = new BlockPos(
            Math.min(p1.getX(), p2.getX()),
            Math.min(p1.getY(), p2.getY()),
            Math.min(p1.getZ(), p2.getZ())
        );
        this.pos2 = new BlockPos(
            Math.max(p1.getX(), p2.getX()),
            Math.max(p1.getY(), p2.getY()),
            Math.max(p1.getZ(), p2.getZ())
        );
    }

    public void setPos1(BlockPos pos1) {
        setBounds(pos1, this.pos2);
    }

    public void setPos2(BlockPos pos2) {
        setBounds(this.pos1, pos2);
    }
    
    @Override
    public Iterator<BlockPos> iterator() {
        // 若未初始化或狀態不合法，回傳標準空迭代器，避免 Crash
        if (this.pos1 == null || this.pos2 == null) {
            return Collections.emptyIterator();
        }

        return new Itr(this.pos1, this.pos2);
    }

    private static class Itr implements Iterator<BlockPos> {
        private final BlockPos pos1;
        private final BlockPos pos2;
        private final BlockPos.MutableBlockPos cursor;
        private int x;
        private int y;
        private int z;
        private boolean hasNext;

        public Itr(BlockPos pos1, BlockPos pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;

            this.cursor = new BlockPos.MutableBlockPos(
                this.pos1.getX(),
                this.pos1.getY(),
                this.pos1.getZ()
            );
            this.x = this.cursor.getX();
            this.y = this.cursor.getY();
            this.z = this.cursor.getZ();
            // 確保 pos1 <= pos2 才能開始走訪
            this.hasNext = pos1.getX() <= pos2.getX() 
                        && pos1.getY() <= pos2.getY() 
                        && pos1.getZ() <= pos2.getZ();
        }
        
        @Override
        public boolean hasNext() {
            return this.hasNext;
        }

        @Override
        public BlockPos next() {
            // 沒有元素時先拋出例外，不再執行後續操作
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in BlockPosIterator");
            }
            
            // 寫入當前座標至重用物件中
            this.cursor.set(this.x, this.y, this.z);

            // 計算下一次要用的座標
            this.x++;
            if (this.x > this.pos2.getX()) {
                this.x = this.pos1.getX();

                this.z++;
                if (this.z > this.pos2.getZ()) {
                    this.z = this.pos1.getZ();

                    this.y++;
                    if (this.y > this.pos2.getY()) {
                        this.hasNext = false;
                    }
                }
            }

            return this.cursor;
        }
    }
}