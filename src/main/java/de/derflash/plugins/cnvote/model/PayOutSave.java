package de.derflash.plugins.cnvote.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "cn_vote_payoutSave")
public class PayOutSave {

    @Id
    private int id;

    @NotNull
    private String playerName;

    @NotNull
    private Date time;

    @Column
    private String serviceName;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String ply) {
        this.playerName = ply;
    }

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(playerName);
    }

    public void setPlayer(Player player) {
        this.playerName = player.getName();
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}