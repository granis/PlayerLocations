package com.archmageinc.playerlocations.plugin;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

public abstract class AbstractSocketServer extends WebSocketServer {
    InetSocketAddress address;
    
    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>
     *
     * @param plugin The instance of the plugin responsible for the socket server
     * @param address The address to listen to
     */
    public AbstractSocketServer(InetSocketAddress address) {
        super(address);
        this.address = address;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        builder.put("Server", "Minecraft Player Locations");
        return builder;
    }

    public void logWarning(String log) {

    }

    public void logInfo(String log) {

    }

    public void logSevere(String log) {

    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        
    }

    @Override
    public void onMessage(WebSocket ws, String string) {
        
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        
    }

    @Override
    public void onStart() {
        this.logInfo(String.format("Socket server started on %s:%s", getAddress().getHostString(), address.getPort()));
    }
    
    public int getClientCount(){
        return getConnections().size();
    }
    
    public boolean portAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    this.logSevere("There was an error attempting to close test socket!");
                }
            }
        }

        return false;
    }

}
