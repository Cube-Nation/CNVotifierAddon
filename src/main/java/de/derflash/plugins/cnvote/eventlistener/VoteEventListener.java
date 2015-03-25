package de.derflash.plugins.cnvote.eventlistener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import de.derflash.plugins.cnvote.services.VotesService;

public class VoteEventListener implements PluginMessageListener, Listener {
    private VotesService votesService;

    public VoteEventListener(VotesService votesService) {
        this.votesService = votesService;
    }

    @Override
    public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {        
        if (!arg0.equalsIgnoreCase("bungeecord")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(arg2));
        try {
            String subchannel = in.readUTF();

            if (!subchannel.equalsIgnoreCase("bungeefier")) return;
            
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);
                        
            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            
            String ip = msgin.readUTF();
            String serviceName = msgin.readUTF();
            /*String time = */msgin.readUTF();
            String username = msgin.readUTF();

            votesService.countVote(username, serviceName, ip);
            votesService.payPlayer(username, serviceName);
            votesService.broadcastVote(username, serviceName);

            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
