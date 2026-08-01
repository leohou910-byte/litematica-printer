package me.aleksilassila.litematica.refactor.core;

import java.util.ArrayList;
import java.util.List;

import me.aleksilassila.litematica.refactor.core.task.AbstractPrinterTask;
import me.aleksilassila.litematica.refactor.core.task.impl.*;

public class PrinterTaskManager {
    private boolean enablePrinter;
    private List<AbstractPrinterTask> printerTasks;

    public PrinterTaskManager(){
        this.enablePrinter = false;
        this.printerTasks =  new ArrayList<>();

        this.printerTasks.add(new PrintTask());
        this.printerTasks.add(new FillTask());
    }

    public void tick(){
        enablePrinter = false;
        if (enablePrinter != true) return;
        
        for (AbstractPrinterTask task : printerTasks) {
            task.tick();
        }
    }
}