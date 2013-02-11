package de.derflash.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handle events for all Player related events
 * @author TJ09
 */
public class PluginPlayerListener implements Listener {    
    CNVotifierAddon plugin;

	PluginPlayerListener(CNVotifierAddon p) {
		this.plugin = p;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                // check for open payouts
                ArrayList<PayOutSave> payoutList = plugin.tempPayouts.get(player.getName());
                List<PayOutSave> fromDataBase = plugin.getDatabase().find(PayOutSave.class).where().eq("playerName", player.getName()).findList();
                if (fromDataBase != null && !fromDataBase.isEmpty()) {
                    if (payoutList == null) payoutList = new ArrayList<PayOutSave>();
                    payoutList.addAll(fromDataBase);
                }
                
                // found some? then pay them now
                if (payoutList != null) {
                    for (PayOutSave payout : payoutList) {
                        plugin.payPlayer(payout.getPlayerName(), payout.getServiceName());
                    }
                    
                    plugin.getDatabase().createSqlUpdate("DELETE FROM cn_vote_payoutSave where player_name = '" + player.getName() + "'").execute();
                    plugin.tempPayouts.remove(player.getName());
                }
            }
        }, 5);
	}
	
}

