package de.derflash.plugins.cnvote.services;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatService {
    private static final String CHAT_NAME = "Vote";
    private static final ChatColor COLOR_DARK = ChatColor.BLUE;
    private static final ChatColor COLOR_LIGHT = ChatColor.WHITE;

    public void broadcastVote(String playerName) {
        Bukkit.getServer().broadcastMessage(
                COLOR_DARK + "[" + CHAT_NAME + "]" + COLOR_LIGHT + " " + playerName
                        + " hat den Server gerade mit einem Vote unterstützt. Klickt hier und helft mit:" + COLOR_DARK + " cube-nation.de/danke");
    }

    public void showPayedIntoInventory(Player player, String service, int amount) {
        player.sendMessage(COLOR_DARK + "[" + CHAT_NAME + "]" + ChatColor.WHITE + " Danke, dass du auf " + service
                + " gevotet hast! Als kleines Dankeschön wurden dir " + amount + " Smaragde in dein Invenvar gelegt.");
    }

    public void showPayedIntoBank(Player player, String service, int amount) {
        player.sendMessage(COLOR_DARK + "[" + CHAT_NAME + "]" + ChatColor.WHITE + " Danke, dass du auf " + service
                + " gevotet hast! Als kleines Dankeschön wurden dir " + amount + " Smaragde auf deinem Bankkonto gutgeschrieben.");
    }
}
