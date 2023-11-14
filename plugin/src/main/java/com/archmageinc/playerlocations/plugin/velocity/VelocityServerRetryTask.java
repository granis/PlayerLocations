package com.archmageinc.playerlocations.plugin.velocity;

public class VelocityServerRetryTask implements java.lang.Runnable {
    VelocitySocketServer socketServer;

    /**
     * Creates a runnable task to defer starting the socket server.
     * 
     * @param socketServer the instance of SocketServer to defer starting
     */
    public VelocityServerRetryTask(VelocitySocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @Override
    public void run() {
        socketServer.start();
    }
}
