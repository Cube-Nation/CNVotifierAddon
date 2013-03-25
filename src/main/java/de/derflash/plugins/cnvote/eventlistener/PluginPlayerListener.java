package de.derflash.plugins.cnvote.eventlistener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.derflash.plugins.cnvote.CNVotifierAddon;
import de.derflash.plugins.cnvote.services.VotesService;

/**
 * Handle events for all Player related events
 */
public class PluginPlayerListener implements Listener {
    private CNVotifierAddon plugin;
    private VotesService votesService;

    public PluginPlayerListener(CNVotifierAddon plugin, VotesService votesService) {
        this.plugin = plugin;
        this.votesService = votesService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Thread joinPayerThread = new Thread("JoinPayer") {
            @Override
            public void run() {
                votesService.payPlayerOnJoin(player.getName());
            }
        };

        Bukkit.getServer().getScheduler().runTaskLater(plugin, joinPayerThread, 5);
    }
}
