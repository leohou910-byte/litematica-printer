package me.aleksilassila.litematica.refactor.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockPosIterable implements Iterable<BlockPos> {
    private BlockPos pos1;
    private BlockPos pos2;
    private Vec3 centerVec3;
    private double distance;
    private BlockPosIteratorType posIteratorType;

    public BlockPosIterable() {
    }

    public BlockPosIterable(int x1, int y1, int z1, int x2, int y2, int z2) {
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

    public BlockPosIterable(BlockPos pos1, BlockPos pos2) {
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

    public BlockPosIterable(Vec3 centerVec3, double distance) {
        this.centerVec3 = centerVec3;
        this.distance = distance;
        this.posIteratorType = BlockPosIteratorType.SPHERE;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    public void setCenterVec3(Vec3 center) {
        this.centerVec3 = center;
    }

    public void setDistance(double dis) {
        this.distance = dis;
    }

    public void setPosIteratorType(BlockPosIteratorType type) {
        this.posIteratorType = type;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<BlockPos> {
        private BlockPos currentPos;
        private BlockPos returnPos;

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

            switch (BlockPosIterable.this.posIteratorType) {
                case CUBE:
                    BlockPos p1 = BlockPosIterable.this.pos1;
                    BlockPos p2 = BlockPosIterable.this.pos2;

                    x++;
                    if (x <= p2.getX()) break;
                    x = p1.getX();
                    
                    z++;
                    if (z <= p2.getZ()) break;
                    z = p1.getZ();

                    y++;
                    if (y <= p2.getY()) break;
                    currentPos = null;
                    
                    break;
                case SPHERE:
                    Vec3 center = BlockPosIterable.this.centerVec3;
                    double dis = BlockPosIterable.this.distance;
                    double sqrDX = (x - center.x) * (x - center.x);
                    double sqrDY = (y - center.y) * (y - center.y);
                    double sqrDZ = (z - center.z) * (z - center.z);
                    double sqrDis = dis * dis;

                    x++;
                    if (x <= getSphereOffSet(center.x, sqrD, sqrDZ, sqrDis, true)) break;
                    x = getSphereOffSet(center.x, sqrDY, sqrDZ, sqrDis, false);
                    
                    z++;
                    if (z <= getSphereOffSet(center.z, sqrDX, sqrDY, sqrDis, true)) break;
                    z = getSphereOffSet(center.z, sqrDX, sqrDY, sqrDis, false);

                    y++;
                    if (++y <= getSphereOffSet(center.y, sqrDX, sqrDZ, sqrDis, true)) break;
                    currentPos = null;

                    break;
                default:
                    return null;
            }

            if (currentPos != null) {
                this.currentPos = new BlockPos(x, y, z);
            }
            return returnPos;
        }

        public void initCurrPos() {
            switch (BlockPosIterable.this.posIteratorType) {
                case CUBE:
                    if (
                        pos1 == null || 
                        pos2 == null ||
                        pos1.getX() > pos2.getX() || 
                        pos1.getY() > pos2.getY() || 
                        pos1.getZ() > pos2.getZ()
                    ) {
                        currentPos = null;
                    }

                    currentPos = pos1;

                    break;
                case SPHERE:
                    if (
                        centerVec3 == null ||
                        distance <= 0
                    ) {
                        currentPos = null;
                    }

                    Vec3 center = BlockPosIterable.this.centerVec3;
                    double dis = BlockPosIterable.this.distance;
                    double sqrX = (center.x) * (center.x);
                    double sqrZ = (center.z) * (center.z);
                    double sqrDis = dis * dis;

                    int y = getSphereOffSet(center.y, sqrX, sqrZ, sqrDis, false);
                    int z = getSphereOffSet(center.z, sqrX, y * y, sqrDis, false);
                    int x = getSphereOffSet(center.x, y * y, z*z, sqrDis, false);

                    currentPos = new BlockPos(x, y, z);

                    break;
                default:
                    break;
            }
        }
        
        private int getSphereOffSet(double centerAxis, double sqrD1, double sqrD2, double sqrDis, boolean isMax) {
            double sqrOffset = sqrDis - sqrD1 - sqrD2;
            if (sqrOffset <= 0) return (int) Math.floor(centerAxis);

            double offset = Math.sqrt(sqrOffset);
            return (int) Math.floor(centerAxis + (isMax ? offset : -offset));
        }
    }
}