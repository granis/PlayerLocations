package com.archmageinc.playerlocations.plugin.velocity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class VelocityCoordInfoTask implements java.lang.Runnable {
    VelocityPlayerLocations plugin;
    VelocitySocketServer socketServer;

    public VelocityCoordInfoTask(VelocityPlayerLocations velocityPlayerLocations, VelocitySocketServer socketServer) {
        this.plugin = velocityPlayerLocations;
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

                    JsonNode node = player.get("position").get("world");
                    ((ObjectNode) player.get("position")).put("world",
                            String.format("%s_%s", key, node.asText()));

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
            plugin.logger.error("Error while broadcasting/saving data.", ex);
        }

    }

}
