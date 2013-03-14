package de.derflash.plugins.cnvote.wrapper;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultWrapper {
    private static Logger log;
    private static Economy provider;

    public static void setLogger(Logger log) {
        VaultWrapper.log = log;
    }

    private static void connect() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            provider = rsp.getProvider();
            log.info("Connected to " + provider.getName() + " for economy support.");
        } else {
            log.warning("Vault could not find any economy plugin to connect to. Please install one or disable economy.");
        }
    }

    public static void depositPlayer(String voterName, double amount) {
        if (provider == null) {
            connect();
        }

        if (provider != null) {
            provider.depositPlayer(voterName, amount);
        }
    }
}
