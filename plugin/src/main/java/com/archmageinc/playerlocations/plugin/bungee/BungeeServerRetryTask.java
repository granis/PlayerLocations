package com.archmageinc.playerlocations.plugin.bungee;

public class BungeeServerRetryTask implements Runnable {
    BungeeSocketServer socketServer;
    
    /**
     * Creates a runnable task to defer starting the socket server.
     * 
     * @param socketServer the instance of SocketServer to defer starting
     */
    public BungeeServerRetryTask(BungeeSocketServer socketServer) {
        this.socketServer = socketServer;
    }
    
    @Override
    public void run() {
        socketServer.start();
    }
}
