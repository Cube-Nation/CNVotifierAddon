package de.derflash.plugins.cnvote;

import java.util.List;

import org.bukkit.event.Listener;

import de.cubenation.plugins.utils.pluginapi.BasePlugin;
import de.cubenation.plugins.utils.pluginapi.CommandSet;
import de.cubenation.plugins.utils.pluginapi.ScheduleTask;
import de.derflash.plugins.cnvote.commands.VoteTestCommand;
import de.derflash.plugins.cnvote.eventlistener.PluginPlayerListener;
import de.derflash.plugins.cnvote.eventlistener.VoteEventListener;
import de.derflash.plugins.cnvote.model.PayOutSave;
import de.derflash.plugins.cnvote.model.Vote;
import de.derflash.plugins.cnvote.services.VotesService;

public class CNVotifierAddon extends BasePlugin {
    // local services
    private VotesService votesService;

    @Override
    protected void initialCustomServices() {
        votesService = new VotesService(getDatabase(), chatService, getConfig(), getLogger());
    }

    @Override
    protected void registerCustomEventListeners(List<Listener> list) {
        list.add(new VoteEventListener(votesService));
        list.add(new PluginPlayerListener(this, votesService));
    }

    @Override
    protected void registerScheduledTasks(List<ScheduleTask> list) {
        Thread voteSaverThread = new Thread("VoteSaver") {
            @Override
            public void run() {
                votesService.saveVotes();
            }
        };
        Thread lastVoteCleanerThread = new Thread("LastVoteCleaner") {
            @Override
            public void run() {
                votesService.cleanLastVotes();
            }
        };

        // every 10 minutes
        list.add(new ScheduleTask(voteSaverThread, 20 * 60 * 10, 20 * 60 * 10));

        // every minute
        list.add(new ScheduleTask(lastVoteCleanerThread, 20 * 60, 20 * 60));
    }

    @Override
    protected void registerCommands(List<CommandSet> list) {
        list.add(new CommandSet(VoteTestCommand.class));
    }

    @Override
    protected void stopCustomServices() {
        votesService.saveVotes();
    }

    @Override
    protected void registerDatabaseModel(List<Class<?>> list) {
        list.add(Vote.class);
        list.add(PayOutSave.class);
    }
}
