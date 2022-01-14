package com.archmageinc.playerlocations.plugin.tasks;

import com.archmageinc.playerlocations.plugin.BukkitSocketServer;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerRetryTask extends BukkitRunnable {
    BukkitSocketServer socketServer;
    
    /**
     * Creates a runnable task to defer starting the socket server.
     * 
     * @param socketServer the instance of SocketServer to defer starting
     */
    public ServerRetryTask(BukkitSocketServer socketServer) {
        this.socketServer = socketServer;
    }
    
    @Override
    public void run() {
        socketServer.start();
    }
}
