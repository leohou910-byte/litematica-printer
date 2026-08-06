package me.aleksilassila.litematica.printer.pwp.refactor.config;

import static me.aleksilassila.litematica.printer.LitematicaMixinMod.PRINT;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.PRINTER_MODE;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.PRINT_INTERVAL;
import static me.aleksilassila.litematica.printer.LitematicaMixinMod.TOGGLE_PRINTING_MODE;

import me.aleksilassila.litematica.printer.printer.State.PrintModeType;

public class PrinterConfigUtils {
    public static boolean isPrinterEnabled() {
        return TOGGLE_PRINTING_MODE.getBooleanValue() || PRINT.getKeybind().isPressed();
    }

    public static int getPrintInterVal() {
        return PRINT_INTERVAL.getIntegerValue();
    }

    public static PrintModeType getPrintModeType() {
        if (PRINTER_MODE.getOptionListValue() instanceof PrintModeType modeType) {
            return modeType;
        }
        return null;
    }
}