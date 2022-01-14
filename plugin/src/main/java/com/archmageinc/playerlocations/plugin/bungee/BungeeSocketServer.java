package com.archmageinc.playerlocations.plugin.bungee;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.archmageinc.playerlocations.plugin.AbstractSocketServer;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeeSocketServer extends AbstractSocketServer {
    InetSocketAddress address;
    Plugin plugin;

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given
     * <var>address</var>
     * 
     * @param plugin  The instance of the plugin responsible for the socket server
     * @param address The address to listen to
     */
    public BungeeSocketServer(Plugin plugin, InetSocketAddress address) {
        super(address);
        this.plugin = plugin;
        this.address = address;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
            ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        builder.put("Server", "Minecraft Player Locations");
        return builder;
    }

    @Override
    public void logInfo(String log) {
        plugin.getLogger().info(log);
    }

    @Override
    public void logWarning(String log) {
        plugin.getLogger().warning(log);
    }

    @Override
    public void logSevere(String log) {
        plugin.getLogger().severe(log);
    }

    /**
     * Checks if the SocketServer is able to bind to the given address. If not,
     * schedule a task to try again.
     */
    @Override
    public void start() {
        if (this.portAvailable(getAddress().getPort())) {
            super.start();
        } else {
            this.plugin.getLogger().warning("Unable to start socket server, address in use. Waiting to try again.");
            this.plugin.getProxy().getScheduler().schedule(this.plugin, (new BungeeServerRetryTask(this)), 10,
                    TimeUnit.SECONDS);
        }
    }
}
