package com.archmageinc.playerlocations.plugin.bungee;

import java.time.Duration;
import java.time.ZonedDateTime;

public class ServerInfoRecieved {
    public String jsonData;
    public ZonedDateTime recieved;
    
    public ServerInfoRecieved(String json) {
        this.jsonData = json;
        this.recieved = ZonedDateTime.now();
    }

    public int getAgeInSeconds() {
        ZonedDateTime now = ZonedDateTime.now();
        Duration duration = Duration.between(this.recieved, now);
        return duration.toSecondsPart();
    }
}
