package net.islandearth.anvillogin.util;

import net.md_5.bungee.api.ChatColor;

public final class Colors {

    private Colors() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    @SuppressWarnings("deprecation")
    public static String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
