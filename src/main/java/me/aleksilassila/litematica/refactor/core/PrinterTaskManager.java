package me.aleksilassila.litematica.refactor.core;

import java.util.HashMap;
import java.util.Map;

import me.aleksilassila.litematica.printer.printer.State;
import me.aleksilassila.litematica.refactor.config.util.PrinterConfigUtils;
import me.aleksilassila.litematica.refactor.core.task.AbstractPrinterTask;

public class PrinterTaskManager {
    private Map<State.PrintModeType, AbstractPrinterTask> printerTasks;
    private int gameTick;

    public PrinterTaskManager() {
        this.printerTasks = new HashMap<>();
        this.gameTick = 0;
    }

    public void addTask(State.PrintModeType printerType, AbstractPrinterTask Task) {
        this.printerTasks.put(printerType, Task);
    }

    public void tick() {
        if (!PrinterConfigUtils.isPrinterEnabled()) return;

        this.gameTick++;
        int tickRate = PrinterConfigUtils.getPrintInterVal();
        if (this.gameTick < tickRate) {
            return;
        }
        this.gameTick = 0;

        AbstractPrinterTask currentTask = this.printerTasks.get(PrinterConfigUtils.getPrintModeType());
        if (currentTask == null) return;
        currentTask.tick();
    }
}