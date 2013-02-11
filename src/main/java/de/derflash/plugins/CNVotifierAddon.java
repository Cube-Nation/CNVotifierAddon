package de.derflash.plugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.PersistenceException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.VotifierEvent;

public class CNVotifierAddon extends JavaPlugin {
	
    HashMap<String, ArrayList<PayOutSave>> tempPayouts = new HashMap<String, ArrayList<PayOutSave>>();
    HashMap<String, Date> lastVotes = new HashMap<String, Date>();
    ArrayList<Vote> tempVotes = new ArrayList<Vote>();

    Economy _economy = null;
    public Economy economy() {
        if (_economy == null) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    _economy = rsp.getProvider();
                    this.getLogger().info("Connected to " + _economy.getName() + " for economy support.");
                } else {
                    this.getLogger().warning("Vault could not find any economy plugin to connect to. Please install one or disable economy.");
                }
            } else {
                this.getLogger().warning("Coult not find Vault plugin, but economy is enabled. Please install Vault or disable economy.");
            }
        }
        return _economy;
    }
    
    public void onEnable() {        
        setupDatabase();
        
        saveDefaultConfig();
        reloadConfig();

        new VoteEventListener(this);
        new PluginPlayerListener(this);
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() { saveVotes(); }
        }, 12000L, 12000L);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() { cleanLastVotes(); }
        }, 20 * 60, 20 * 60);   // every minute
    }
    
    public void onDisable() {
        saveVotes();
    }
    
    private void saveVotes() {
        if (!tempVotes.isEmpty()) getDatabase().save(tempVotes);
        tempVotes.clear();
        
        for (ArrayList<PayOutSave> tempPayoutArray : tempPayouts.values()) {
            if (!tempPayoutArray.isEmpty()) getDatabase().save(tempPayoutArray);
        }
        tempPayouts.clear();
    }
    
    private void setupDatabase() {
        try {
            getDatabase().find(Vote.class).findRowCount();
            getDatabase().find(PayOutSave.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
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
            if (payOutList == null) payOutList = new ArrayList<PayOutSave>();
            payOutList.add(tempPayout);
            
            tempPayouts.put(username, payOutList);
            return;
        }
        
        int amount = getConfig().getInt("reward_amount", 50);
        
        if (voter.getWorld().getName().equalsIgnoreCase("pandora")) payEmeralds(voter, amount, service);
        else payMoney(voter, amount, service);
        
    }
    
    private void payEmeralds(Player voter, int amount, String service) {
        voter.getInventory().addItem(new ItemStack(Material.EMERALD, amount));
        voter.sendMessage(ChatColor.AQUA + "[Vote]" + ChatColor.WHITE + " Danke, dass du auf " + service + " gevotet hast! Als kleines Dankeschön wurden dir " + amount + " Smaragde in dein Invenvar gelegt.");
    }
    
    private void payMoney(Player voter, int amount, String service) {
        economy().depositPlayer(voter.getName(), amount);
        voter.sendMessage(ChatColor.AQUA + "[Vote]" + ChatColor.WHITE + " Danke, dass du auf " + service + " gevotet hast! Als kleines Dankeschön wurden dir " + amount + " Smaragde auf deinem Bankkonto gutgeschrieben.");
    }

    public void countVote(String username, String service, String ip) {
        Vote vote = new Vote();
        vote.setPlayerName(username);
        vote.setTime(new Date());
        vote.setServiceName(service);
        vote.setIp(ip);
        tempVotes.add(vote);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (! ( !(sender instanceof Player) || ((Player)sender).hasPermission("cnvotifieraddon.admin") ) ) return true;
        
        if(args.length > 0 && args[0].equalsIgnoreCase("test")) {
            com.vexsoftware.votifier.model.Vote vote = new com.vexsoftware.votifier.model.Vote();
            vote.setAddress("0.1.2.3");
            vote.setServiceName("Test");
            vote.setUsername("DerFlash");
            vote.setTimeStamp(new Date().toString());
            
            VotifierEvent event = new VotifierEvent(vote);
            getServer().getPluginManager().callEvent(event);

        }
        return true;
    }

    public void broadcastVote(String username, String serviceName) {
        if (lastVotes.containsKey(username)) return;
        lastVotes.put(username, new Date());
        
        getServer().broadcastMessage(ChatColor.AQUA + "[Vote]" + ChatColor.WHITE + " " + username + ChatColor.WHITE + " hat den Server gerade mit einem Vote unterstützt. Klickt hier und helft mit:" + ChatColor.AQUA + " cube-nation.de/danke");
    }
    
    private void cleanLastVotes() {
        Date old = new Date();
        old.setTime(old.getTime() - 60000 * 10);    // 10 minutes
        ArrayList<String> toDelete = null;
        for ( Entry<String, Date> lv : lastVotes.entrySet() ) {
            if (lv.getValue().before(old)) {
                if (toDelete == null) toDelete = new ArrayList<String>();
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
