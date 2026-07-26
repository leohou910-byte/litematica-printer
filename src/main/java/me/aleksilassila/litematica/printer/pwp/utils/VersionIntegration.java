package me.aleksilassila.litematica.printer.pwp.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class VersionIntegration {
    public static void overlayMessage(final Minecraft client, final String message, final boolean animate) {
        if (client == null || client.gui == null) return;

        //#if MC >= 260200
        client.gui.hud.setOverlayMessage(Component.nullToEmpty(message), animate);
        //#else
        //$$ client.gui.setOverlayMessage(Component.nullToEmpty(message), animate);
        //#endif
    }

    public static void displaySystemMessage(final Minecraft client,final String message) {
        if (client == null || client.player == null) return;

        //#if MC >= 260100
        client.player.sendSystemMessage(Component.nullToEmpty(message));
        //#else
        //$$ client.player.displayClientMessage(Component.nullToEmpty(message), false);
        //#endif
    }
}