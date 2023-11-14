package com.archmageinc.playerlocations.plugin.velocity;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.archmageinc.playerlocations.plugin.AbstractSocketServer;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public class VelocitySocketServer extends AbstractSocketServer {
    InetSocketAddress address;
    VelocityPlayerLocations plugin;

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given
     * <var>address</var>
     * 
     * @param plugin  The instance of the plugin responsible for the socket server
     * @param address The address to listen to
     */
    public VelocitySocketServer(VelocityPlayerLocations plugin, InetSocketAddress address) {
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
        plugin.logger.info(log);
    }

    @Override
    public void logWarning(String log) {
        plugin.logger.warn(log);
    }

    @Override
    public void logSevere(String log) {
        plugin.logger.error(log);
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
            this.plugin.logger.warn("Unable to start socket server, address in use. Waiting to try again.");
            this.plugin.server.getScheduler()
                    .buildTask(plugin, (new VelocityServerRetryTask(this)))
                    .delay(30L, TimeUnit.SECONDS)
                    .schedule();
        }
    }
}
