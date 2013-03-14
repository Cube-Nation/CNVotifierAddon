package de.derflash.plugins.cnvote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

/**
 * Handle events for all Player related events
 */
public class VoteEventListener implements Listener {
    CNVotifierAddon plugin;

    VoteEventListener(CNVotifierAddon p) {
        this.plugin = p;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        String username = vote.getUsername();

        plugin.countVote(username, vote.getServiceName(), vote.getAddress());
        plugin.payPlayer(username, vote.getServiceName());
        plugin.broadcastVote(username, vote.getServiceName());

    }
}
