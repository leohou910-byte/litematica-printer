package me.aleksilassila.litematica.refactor.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.core.BlockPos;

public class BlockPosIterator implements Iterable<BlockPos> {
    private BlockPos pos1;
    private BlockPos pos2;
    private BlockPosIteratorType posIteratorType;

    public BlockPosIterator() {
    }

    public BlockPosIterator(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.pos1 = new BlockPos(
            Math.min(x1, x2), 
            Math.min(y1, y2), 
            Math.min(z1, z2)
        );
        this.pos2 = new BlockPos(
            Math.max(x1, x2), 
            Math.max(y1, y2), 
            Math.max(z1, z2)
        );
        this.posIteratorType = BlockPosIteratorType.CUBE;
    }

    public BlockPosIterator(BlockPos pos1, BlockPos pos2) {
        this.pos1 = new BlockPos(
            Math.min(pos1.getX(), pos2.getX()), 
            Math.min(pos1.getY(), pos2.getY()), 
            Math.min(pos1.getZ(), pos2.getZ())
        );
        this.pos2 = new BlockPos(
            Math.max(pos1.getX(), pos2.getX()), 
            Math.max(pos1.getY(), pos2.getY()), 
            Math.max(pos1.getZ(), pos2.getZ())
        );
        this.posIteratorType = BlockPosIteratorType.CUBE;
    }

    public BlockPosIterator(BlockPos centerPos, int distance) {
        int centerX = centerPos.getX();
        int centerY = centerPos.getY();
        int centerZ = centerPos.getZ();
        this.pos1 = new BlockPos(
            centerX - distance, 
            centerY - distance, 
            centerZ - distance
        );
        this.pos2 = new BlockPos(
            centerX + distance, 
            centerY + distance, 
            centerZ + distance
        );
        this.posIteratorType = BlockPosIteratorType.SPHERE;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    public void setPosIteratorType(BlockPosIteratorType type) {
        this.posIteratorType = type;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<BlockPos> {
        BlockPos currentPos;
        BlockPos returnPos;

        public Itr() {
            initCurrPos();
        }
        
        @Override
        public boolean hasNext() {
            return currentPos != null;
        }

        @Override
        public BlockPos next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in BlockPosIterator");
            }

            returnPos = currentPos;

            int x = currentPos.getX();
            int y = currentPos.getY();
            int z = currentPos.getZ();

            if (++x > pos2.getX()) {
                x = pos1.getX();
                if (++z > pos2.getZ()) {
                    z = pos1.getZ();
                    if (++y > pos2.getY()) {
                        currentPos = null;
                        return returnPos;
                    }
                }
            }

            currentPos = new BlockPos(x, y, z);
            return returnPos;
        }

        public void initCurrPos() {
            switch (posIteratorType) {
                case CUBE:
                    if (pos1 == null || 
                        pos2 == null ||
                        pos1.getX() > pos2.getX() || 
                        pos1.getY() > pos2.getY() || 
                        pos1.getZ() > pos2.getZ()
                    ) {
                        currentPos = null;
                    } else {
                        currentPos = pos1;
                    }

                    break;
                case SPHERE:
                    
                    break;
                default:
                    break;
            }
        }


    }
}