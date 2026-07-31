package me.aleksilassila.litematica.refactor.core.task;

public abstract class AbstractPrinterTask {
    public final void tick() {
        if (canExcute()) {
            excute();
        }
    }

    protected abstract boolean canExcute();

    protected abstract void excute();
}
