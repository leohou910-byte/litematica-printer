package me.aleksilassila.litematica.refactor.core.task;

import net.minecraft.core.BlockPos;

public abstract class AbstractPrinterTask {
    protected BlockPos pos;
    
    public AbstractPrinterTask(){
        this.pos = null;
    }
    
    public final void tick() {
        if (canExcute()) {
            excute();
        }
    }

    protected BlockPos getPos() {
        return null;
    }

    protected abstract boolean canExcute();

    protected abstract void excute();
}
