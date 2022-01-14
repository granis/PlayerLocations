package com.archmageinc.playerlocations.plugin.bungee;

import com.archmageinc.playerlocations.api.InfoHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class BungeePlayerLocations extends Plugin implements Listener {

    BungeeSocketServer socketServer;
    Set<InfoHandler> dataHandlers = new HashSet();
    BungeeCoordInfoTask infoTask;
    public static final String PLUGIN_MESSAGING_SUBCHANNEL = "playerlocations";
    ConcurrentHashMap<String, ServerInfoRecieved> latestServerInfo = new ConcurrentHashMap<String, ServerInfoRecieved>();
    public Boolean usingFileInstead;
    public String usingFilePath;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Configuration configuration;

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            this.getLogger().severe("Couldnt load config.yml, giving up, surrendering, bye!");
            return;
        }

        this.getProxy().getPluginManager().registerListener(this, this);

        usingFileInstead = configuration.getBoolean("using_file_instead", false);
        usingFilePath = configuration.getString("using_file_path", "");
        int tick = configuration.getInt("socket_server.tick_interval", 100);

        if (usingFileInstead) {
            socketServer = null;
        } else {
            socketServer = new BungeeSocketServer(this, new InetSocketAddress(
                    configuration.getString("socket_server.host"), configuration.getInt("socket_server.port")));
            socketServer.start();
        }

        infoTask = new BungeeCoordInfoTask(this, socketServer);
        this.getProxy().getScheduler().schedule(this, infoTask, (long) (tick / 20), (long) (tick / 20),
                TimeUnit.SECONDS);
    }

    @EventHandler
    public void messageEvent(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("BungeeCord")) {
            return;
        }

        ByteArrayDataInput byteArray = ByteStreams.newDataInput(event.getData());
        String subChannel = byteArray.readUTF();

        if (!subChannel.equals(PLUGIN_MESSAGING_SUBCHANNEL)) {
            return;
        }

        Server server = (Server) event.getSender();
        ServerInfo serverInfo = server.getInfo();
        String serverName = serverInfo.getName();

        String json = byteArray.readUTF();
        latestServerInfo.put(serverName, new ServerInfoRecieved(json));

    }

    private void saveDefaultConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    }
}
