package de.derflash.plugins.cnvote.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avaje.ebean.EbeanServer;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.wrapperapi.VaultWrapper;
import de.cubenation.plugins.utils.wrapperapi.WrapperManager;
import de.derflash.plugins.cnvote.model.PayOutSave;
import de.derflash.plugins.cnvote.model.Vote;

public class VotesService {
    private EbeanServer dbConnection;
    private ChatService chatService;
    private FileConfiguration config;
    private Logger log;

    private HashMap<String, ArrayList<PayOutSave>> tempPayouts = new HashMap<String, ArrayList<PayOutSave>>();
    private HashMap<String, Date> lastVotes = new HashMap<String, Date>();
    private ArrayList<Vote> tempVotes = new ArrayList<Vote>();

    public VotesService(EbeanServer dbConnection, ChatService chatService, FileConfiguration config, Logger log) {
        this.dbConnection = dbConnection;
        this.chatService = chatService;
        this.config = config;
        this.log = log;
    }

    public void saveVotes() {
        if (!tempVotes.isEmpty()) {
            dbConnection.save(tempVotes);
        }
        tempVotes.clear();

        for (ArrayList<PayOutSave> tempPayoutArray : tempPayouts.values()) {
            if (!tempPayoutArray.isEmpty()) {
                dbConnection.save(tempPayoutArray);
            }
        }
        tempPayouts.clear();
    }

    public void payPlayer(String username, String service) {
        Player voter = Bukkit.getServer().getPlayerExact(username);
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

        String _items = config.getString("reward_items", "MONEY:50");
        String[] items = _items.split(",");
        String item = items[new Random().nextInt(items.length)];
        
        String[] itemArray = item.split(":");
        
        String itemName = itemArray[0];
        int itemAmount = 1;
        try { if (itemArray.length > 1) itemAmount = Integer.parseInt(itemArray[1]); } catch (Exception e) {}

        if (itemName.equalsIgnoreCase("money")) {
            payMoney(voter, service, itemAmount);
        } else {
            payItem(voter, service, itemName, itemAmount);
        }
    }

    public void payPlayerOnJoin(String playerName) {
        // check for open payouts
        ArrayList<PayOutSave> payoutList = tempPayouts.get(playerName);
        List<PayOutSave> fromDataBase = dbConnection.find(PayOutSave.class).where().eq("playerName", playerName).findList();
        if (fromDataBase != null && !fromDataBase.isEmpty()) {
            if (payoutList == null) {
                payoutList = new ArrayList<PayOutSave>();
            }
            payoutList.addAll(fromDataBase);
        }

        // found some? then pay them now
        if (payoutList != null) {
            for (PayOutSave payout : payoutList) {
                payPlayer(payout.getPlayerName(), payout.getServiceName());
            }

            dbConnection.createSqlUpdate("delete from cn_vote_payoutSave where player_name = '" + playerName + "'").execute();
            tempPayouts.remove(playerName);
        }
    }

    private void payItem(Player voter, String service, String item, int amount) {
        // http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html
        voter.getInventory().addItem(new ItemStack(Material.getMaterial(item), amount));

        chatService.one(voter, "player.payedIntoInventory", service, amount, item, Material.getMaterial(item).name());
    }

    private void payMoney(Player voter, String service, int amount) {
        if (WrapperManager.isPluginEnabled(WrapperManager.PLUGIN_NAME_VAULT)) {
            VaultWrapper.getService().depositPlayer(voter.getName(), amount);
        } else {
            log.warning("Coult not find Vault plugin, but economy is enabled. Please install Vault or disable economy.");
            payItem(voter, service, "EMERALD", amount);
        }

        chatService.one(voter, "player.payedIntoBank", service, amount);
    }

    public void countVote(String username, String service, String ip) {
        Vote vote = new Vote();
        vote.setPlayerName(username);
        vote.setTime(new Date());
        vote.setServiceName(service);
        vote.setIp(ip);
        tempVotes.add(vote);
    }

    public void broadcastVote(String username, String serviceName) {
        if (lastVotes.containsKey(username)) {
            return;
        }
        lastVotes.put(username, new Date());

        chatService.all("player.broadcastVote", username);
    }

    public void cleanLastVotes() {
        Date old = new Date();
        old.setTime(old.getTime() - 1000 * 60 * 10); // 10 minutes
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
