package de.derflash.plugins.cnvote.wrapper;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExWrapper {
    public static boolean hasPermission(Player player, String rightName) {
        PermissionManager permissions = PermissionsEx.getPermissionManager();
        return permissions.has(player, rightName);
    }
}
