package de.derflash.plugins.cnvote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

import de.derflash.plugins.cnvote.services.VotesService;

public class VoteEventListener implements Listener {
    private VotesService votesService;

    public VoteEventListener(VotesService votesService) {
        this.votesService = votesService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        String username = vote.getUsername();

        votesService.countVote(username, vote.getServiceName(), vote.getAddress());
        votesService.payPlayer(username, vote.getServiceName());
        votesService.broadcastVote(username, vote.getServiceName());
    }
}
