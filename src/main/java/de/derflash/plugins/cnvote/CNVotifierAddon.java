package de.derflash.plugins.cnvote;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.CommandsManager;
import de.cubenation.plugins.utils.commandapi.exception.CommandException;
import de.cubenation.plugins.utils.commandapi.exception.CommandManagerException;
import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
import de.cubenation.plugins.utils.permissionapi.PermissionService;
import de.derflash.plugins.cnvote.commands.VoteTestCommand;
import de.derflash.plugins.cnvote.model.PayOutSave;
import de.derflash.plugins.cnvote.model.Vote;
import de.derflash.plugins.cnvote.services.VotesService;
import de.derflash.plugins.cnvote.wrapper.VaultWrapper;

public class CNVotifierAddon extends JavaPlugin {
    // framework services
    private ChatService chatService;
    private PermissionService permissionService;
    private CommandsManager commandsManager;

    // local services
    private VotesService votesService;

    @Override
    public void onEnable() {
        setupDatabase();

        saveDefaultConfig();
        reloadConfig();

        permissionService = new PermissionService();
        chatService = new ChatService(this);

        VaultWrapper.setLogger(getLogger());
        votesService = new VotesService(getDatabase(), chatService, getConfig(), getLogger());

        getServer().getPluginManager().registerEvents(new VoteEventListener(votesService), this);
        getServer().getPluginManager().registerEvents(new PluginPlayerListener(this, votesService), this);

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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, voteSaverThread, 20 * 60 * 10, 20 * 60 * 10);

        // every minute
        getServer().getScheduler().runTaskTimer(this, lastVoteCleanerThread, 20 * 60, 20 * 60);

        try {
            commandsManager = new CommandsManager(this);
            commandsManager.setPermissionInterface(permissionService);
            registerCommands();
        } catch (CommandWarmUpException e) {
            getLogger().log(Level.SEVERE, "error on register command", e);
        } catch (CommandManagerException e) {
            getLogger().log(Level.SEVERE, "error on inital command manager", e);
        }
    }

    private void registerCommands() throws CommandWarmUpException {
        commandsManager.add(VoteTestCommand.class);
    }

    @Override
    public void onDisable() {
        commandsManager.clear();

        votesService.saveVotes();
    }

    private void setupDatabase() {
        try {
            getDatabase().find(Vote.class).findRowCount();
            getDatabase().find(PayOutSave.class).findRowCount();
        } catch (PersistenceException ex) {
            getLogger().info("Installing database due to first time usage");
            installDDL();
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Vote.class);
        list.add(PayOutSave.class);
        return list;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            commandsManager.execute(sender, command, label, args);
        } catch (CommandException e) {
            getLogger().log(Level.SEVERE, "error on command", e);
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandsManager.getTabCompleteList(sender, command, alias, args);
    }
}
