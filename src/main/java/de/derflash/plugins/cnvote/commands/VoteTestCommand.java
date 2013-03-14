package de.derflash.plugins.cnvote.commands;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;

public class VoteTestCommand {
    @Command(main = "cnvote", sub = "test", max = 0, help = "Bei Ausführung des Befehls wird dem Spieler DerFlash ein Vote hinzugefügt.")
    @CommandPermissions("cnvotifieraddon.admin")
    public void test(Player sender) {
        Vote vote = new Vote();
        vote.setAddress("0.1.2.3");
        vote.setServiceName("Test");
        vote.setUsername("DerFlash");
        vote.setTimeStamp(new Date().toString());

        VotifierEvent event = new VotifierEvent(vote);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
