package com.archmageinc.playerlocations.plugin.tasks;

import com.archmageinc.playerlocations.plugin.PlayerLocations;
import com.archmageinc.playerlocations.plugin.BukkitSocketServer;
import com.archmageinc.playerlocations.plugin.info.Info;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitRunnable;

public class InfoTask extends BukkitRunnable {
    PlayerLocations plugin;
    BukkitSocketServer socketServer;

    public InfoTask(PlayerLocations plugin, BukkitSocketServer socketServer) {
        this.plugin = plugin;
        this.socketServer = socketServer;
    }

    @Override
    public void run() {
        if (!plugin.usingBungee) {
            if (socketServer.getClientCount() < 1) {
                return;
            }
        }

        Info info = new Info();

        plugin.getInfoHandlers().forEach(dataHandler -> {
            info.putAll(dataHandler.getInfo());
        });

        sendToSocket(info, plugin);
    }

    private void sendToSocket(Info info, PlayerLocations plugin) {
        try {
            String json = (new ObjectMapper()).writeValueAsString(info);
            if (plugin.usingBungee) {
                plugin.sendToBungee(json);
            } else {
                socketServer.broadcast(json);
            }
        } catch (JsonProcessingException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error while converting json data.", ex);
        }
    }
}
