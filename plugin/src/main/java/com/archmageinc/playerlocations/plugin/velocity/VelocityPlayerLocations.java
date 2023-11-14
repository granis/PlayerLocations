package com.archmageinc.playerlocations.plugin.velocity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.archmageinc.playerlocations.api.InfoHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import dev.dejvokep.boostedyaml.YamlDocument;

public class VelocityPlayerLocations {

    protected final ProxyServer server;
    protected final Logger logger;
    private final Path dataDirectory;
    private final YamlDocument config;
    private final String socketserverhost;
    private final Integer socketserverport;
    private final Integer tick;
    protected final Boolean usingFileInstead;
    protected final String usingFilePath;
    protected final ConcurrentHashMap<String, ServerInfoRecieved> latestServerInfo;
    VelocitySocketServer socketServer;
    Set<InfoHandler> dataHandlers = new HashSet();
    private VelocityCoordInfoTask infoTask;
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier
            .from("playerlocations:main");

    @Inject
    public VelocityPlayerLocations(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
            throws IOException {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = YamlDocument.create(new File(this.dataDirectory.toString(), "config.yml"),
                getResource("/config.yml"));
        this.socketserverhost = this.config.getString("socket_server.host");
        this.socketserverport = this.config.getInt("socket_server.port");
        this.usingFileInstead = this.config.getBoolean("using_file_instead", false);
        this.usingFilePath = this.config.getString("using_file_path", "");
        this.tick = this.config.getInt("socket_server.tick_interval", 100);
        this.latestServerInfo = new ConcurrentHashMap<String, ServerInfoRecieved>();

    }

    private InputStream getResource(String path) {
        return this.getClass().getResourceAsStream(path);
    }

    @Subscribe
    void onProxyInitialization(final ProxyInitializeEvent event) {
        if (usingFileInstead) {
            socketServer = null;
        } else {
            socketServer = new VelocitySocketServer(this, new InetSocketAddress(socketserverhost, socketserverport));
            socketServer.start();
        }
        this.server.getChannelRegistrar().register(IDENTIFIER);
        this.infoTask = new VelocityCoordInfoTask(this, socketServer);
        this.server.getScheduler()
                .buildTask(this, infoTask)
                .delay(2L, TimeUnit.SECONDS)
                .repeat(2L, TimeUnit.SECONDS)
                .schedule();
    }

    @Subscribe
    public void onPluginMessageFromServer(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        if (event.getIdentifier() != IDENTIFIER) {
            return;
        }
        ServerConnection backend = (ServerConnection) event.getSource();
        ByteArrayDataInput inData = ByteStreams.newDataInput(event.getData());
        String json = inData.readUTF();
        latestServerInfo.put(backend.getServerInfo().getName(), new ServerInfoRecieved(json));
    }
}
