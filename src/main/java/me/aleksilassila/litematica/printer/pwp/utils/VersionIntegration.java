package me.aleksilassila.litematica.printer.pwp.utils;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.client;

import net.minecraft.network.chat.Component;

public class VersionIntegration {
    public static void overlayMessage(final String message, final boolean animate) {
        //#if MC >= 260200
        client.gui.hud.setOverlayMessage(Component.nullToEmpty(message), animate);
        //#else
        //$$ client.gui.setOverlayMessage(Component.nullToEmpty(message), animate);
        //#endif
    }
}