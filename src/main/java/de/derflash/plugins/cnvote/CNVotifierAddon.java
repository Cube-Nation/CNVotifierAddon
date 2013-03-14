package de.derflash.plugins.cnvote;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.cubenation.plugins.utils.commandapi.CommandsManager;
import de.cubenation.plugins.utils.commandapi.exception.CommandException;
import de.cubenation.plugins.utils.commandapi.exception.CommandManagerException;
import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
import de.derflash.plugins.cnvote.commands.VoteTestCommand;
import de.derflash.plugins.cnvote.model.PayOutSave;
import de.derflash.plugins.cnvote.model.Vote;
import de.derflash.plugins.cnvote.services.ChatService;
import de.derflash.plugins.cnvote.services.PermissionService;
import de.derflash.plugins.cnvote.wrapper.VaultWrapper;

public class CNVotifierAddon extends JavaPlugin {
    HashMap<String, ArrayList<PayOutSave>> tempPayouts = new HashMap<String, ArrayList<PayOutSave>>();

    private HashMap<String, Date> lastVotes = new HashMap<String, Date>();
    private ArrayList<Vote> tempVotes = new ArrayList<Vote>();

    private PermissionService permissionService;
    private CommandsManager commandsManager;
    private ChatService chatService;

    @Override
    public void onEnable() {
        VaultWrapper.setLogger(getLogger());
        permissionService = new PermissionService();
        chatService = new ChatService();

        setupDatabase();

        saveDefaultConfig();
        reloadConfig();

        new VoteEventListener(this);
        new PluginPlayerListener(this);

        Thread voteSaverThread = new Thread("VoteSaver") {
            @Override
            public void run() {
                saveVotes();
            }
        };
        Thread lastVoteCleanerThread = new Thread("LastVoteCleaner") {
            @Override
            public void run() {
                cleanLastVotes();
            }
        };

        getServer().getScheduler().scheduleSyncRepeatingTask(this, voteSaverThread, 12000L, 12000L);
        getServer().getScheduler().runTaskTimer(this, lastVoteCleanerThread, 20 * 60, 20 * 60); // every
                                                                                                // minute

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

        saveVotes();
    }

    private void saveVotes() {
        if (!tempVotes.isEmpty()) {
            getDatabase().save(tempVotes);
        }
        tempVotes.clear();

        for (ArrayList<PayOutSave> tempPayoutArray : tempPayouts.values()) {
            if (!tempPayoutArray.isEmpty()) {
                getDatabase().save(tempPayoutArray);
            }
        }
        tempPayouts.clear();
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

    public void payPlayer(String username, String service) {
        Player voter = getServer().getPlayerExact(username);
        if (voter == null || !voter.isOnline()) {
            PayOutSave tempPayout = new PayOutSave();
            tempPayout.setPlayerName(username);
            tempPayout.setTime(new Date());
            tempPayout.setServiceName(service);

            ArrayList<PayOutSave> payOutList = tempPayouts.get(username);
            if (payOutList == null) {
                payOutList = new ArrayList<PayOutSave>();
            }
            payOutList.add(tempPayout);

            tempPayouts.put(username, payOutList);
            return;
        }

        int amount = getConfig().getInt("reward_amount", 50);

        if (voter.getWorld().getName().equalsIgnoreCase("pandora")) {
            payEmeralds(voter, amount, service);
        } else {
            payMoney(voter, amount, service);
        }

    }

    private void payEmeralds(Player voter, int amount, String service) {
        voter.getInventory().addItem(new ItemStack(Material.EMERALD, amount));

        chatService.showPayedIntoInventory(voter, service, amount);
    }

    private void payMoney(Player voter, int amount, String service) {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultWrapper.depositPlayer(voter.getName(), amount);
        } else {
            getLogger().warning("Coult not find Vault plugin, but economy is enabled. Please install Vault or disable economy.");
            payEmeralds(voter, amount, service);
        }

        chatService.showPayedIntoBank(voter, service, amount);
    }

    public void countVote(String username, String service, String ip) {
        Vote vote = new Vote();
        vote.setPlayerName(username);
        vote.setTime(new Date());
        vote.setServiceName(service);
        vote.setIp(ip);
        tempVotes.add(vote);
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

    public void broadcastVote(String username, String serviceName) {
        if (lastVotes.containsKey(username)) {
            return;
        }
        lastVotes.put(username, new Date());

        chatService.broadcastVote(username);
    }

    private void cleanLastVotes() {
        Date old = new Date();
        old.setTime(old.getTime() - 60000 * 10); // 10 minutes
        ArrayList<String> toDelete = null;
        for (Entry<String, Date> lv : lastVotes.entrySet()) {
            if (lv.getValue().before(old)) {
                if (toDelete == null) {
                    toDelete = new ArrayList<String>();
                }
                toDelete.add(lv.getKey());
            }
        }
        if (toDelete != null) {
            for (String key : toDelete) {
                lastVotes.remove(key);
            }
        }
    }
}
