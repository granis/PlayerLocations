package com.archmageinc.playerlocations.plugin;

import com.archmageinc.playerlocations.api.InfoRegistrar;
import com.archmageinc.playerlocations.api.InfoHandler;
import com.archmageinc.playerlocations.plugin.infohandlers.PlayerInfoHandler;
import com.archmageinc.playerlocations.plugin.infohandlers.ServerInfoHandler;
import com.archmageinc.playerlocations.plugin.tasks.InfoTask;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerLocations extends JavaPlugin implements InfoRegistrar {

    BukkitSocketServer socketServer;
    Set<InfoHandler> dataHandlers = new HashSet();
    InfoTask infoTask;
    public Boolean usingBungee;
    public static final String PLUGIN_MESSAGING_CHANNEL = "BungeeCord";
    public static final String PLUGIN_MESSAGING_SUBCHANNEL = "playerlocations";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        usingBungee = this.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);

        if (usingBungee) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, PLUGIN_MESSAGING_CHANNEL);
            this.getLogger().info("BungeeCord mode enabled, no socket-server running.");
        } else {
            socketServer = new BukkitSocketServer(this, new InetSocketAddress(
                    getConfig().getString("socket_server.host"), getConfig().getInt("socket_server.port")));
            socketServer.start();
        }

        registerInfoHandler(new ServerInfoHandler(this, socketServer));
        registerInfoHandler(new PlayerInfoHandler(this));
        infoTask = new InfoTask(this, socketServer);
        infoTask.runTaskTimer(this, getConfig().getInt("socket_server.tick_interval", 100),
                getConfig().getInt("socket_server.tick_interval", 100));
    }

    @Override
    public void onDisable() {
        if (socketServer != null) {
            try {
                socketServer.stop();
            } catch (InterruptedException ex) {
                /* Intentionally ignored */
            }
        }
        if (usingBungee) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        }
    }

    @Override
    public void registerInfoHandler(InfoHandler handler) {
        dataHandlers.add(handler);
    }

    @NotNull
    @Override
    public Set<InfoHandler> getInfoHandlers() {
        return dataHandlers;
    }

    public void sendToBungee(String json) {
        if (this.getServer().getOnlinePlayers().isEmpty()) {
            // cant send message without <1 player online
            return;
        }

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(PLUGIN_MESSAGING_SUBCHANNEL);
        out.writeUTF(json);
        player.sendPluginMessage(this, PLUGIN_MESSAGING_CHANNEL, out.toByteArray());
    }
}
