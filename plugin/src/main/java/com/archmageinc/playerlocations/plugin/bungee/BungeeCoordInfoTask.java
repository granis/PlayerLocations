package com.archmageinc.playerlocations.plugin.bungee;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Level;

public class BungeeCoordInfoTask implements Runnable {
    BungeePlayerLocations plugin;
    BungeeSocketServer socketServer;

    public BungeeCoordInfoTask(BungeePlayerLocations bungeePlayerLocations, BungeeSocketServer socketServer) {
        this.plugin = bungeePlayerLocations;

        this.socketServer = socketServer;
    }

    @Override
    public void run() {
        try {

            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
            ObjectNode output = objectMapper.createObjectNode();
            ArrayNode fullPlayerList = output.putArray("players");
            @SuppressWarnings("unused")
            JsonNode timeOfDay = output.put("timeOfDay", 1337);
            ArrayList<String> playersAdded = new ArrayList<String>();

            for (String key : plugin.latestServerInfo.keySet()) {
                if (plugin.latestServerInfo.get(key).getAgeInSeconds() > 10) {
                    // stale data, server dead or noone logged in
                    continue;
                }

                JsonNode serverTree = objectMapper.readTree(plugin.latestServerInfo.get(key).jsonData);
                JsonNode playerList = serverTree.get("players");

                if (!playerList.isArray()) {
                    continue;
                }

                for (JsonNode player : playerList) {
                    String playerName = player.get("name").asText();

                    // player already added from some server, probably switched server, ignore it!
                    if (playersAdded.contains(playerName)) {
                        continue;
                    }

                    JsonNode dimension = player.get("position").get("dimension");
                    ((ObjectNode) player.get("position")).put("dimension",
                            String.format("%s_%s", key, dimension.asText()));
                    fullPlayerList.add(player);
                    playersAdded.add(playerName);
                }
            }

            if (plugin.usingFileInstead) {
                File file = new File(plugin.usingFilePath);
                FileWriter writer = new FileWriter(file);
                writer.write(output.toPrettyString());
                writer.close();
            } else {
                socketServer.broadcast(output.toPrettyString());
            }

        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Error while broadcasting/saving data.", ex);
        }

    }

}
