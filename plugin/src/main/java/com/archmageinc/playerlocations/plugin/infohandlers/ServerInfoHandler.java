package com.archmageinc.playerlocations.plugin.infohandlers;

import com.archmageinc.playerlocations.plugin.PlayerLocations;
import com.archmageinc.playerlocations.plugin.BukkitSocketServer;
import java.util.HashMap;
import java.util.Map;
import com.archmageinc.playerlocations.api.InfoHandler;

public class ServerInfoHandler implements InfoHandler{
    PlayerLocations plugin;
    BukkitSocketServer socketServer;
    
    public ServerInfoHandler(PlayerLocations plugin, BukkitSocketServer socketServer) {
        this.plugin = plugin;
        this.socketServer = socketServer;
    }
    
    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> map = new HashMap();
        map.put("timeOfDay", plugin.getServer().getWorlds().get(0).getTime());
        
        if(!plugin.usingBungee) {
            map.put("webClients", socketServer.getClientCount());
        }
        
        return map;
    }
    
}
