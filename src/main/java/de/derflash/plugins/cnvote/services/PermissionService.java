package de.derflash.plugins.cnvote.services;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.PermissionInterface;
import de.derflash.plugins.cnvote.wrapper.PermissionsExWrapper;

public class PermissionService implements PermissionInterface {
    public boolean hasPermission(Player player, String rightName) {
        boolean has = false;

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
            has = PermissionsExWrapper.hasPermission(player, rightName);
        } else {
            has = player.hasPermission(rightName);
        }
        return has;
    }
}
