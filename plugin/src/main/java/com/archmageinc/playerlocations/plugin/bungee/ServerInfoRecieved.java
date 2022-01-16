package com.archmageinc.playerlocations.plugin.bungee;

import java.time.Duration;
import java.time.Instant;

public class ServerInfoRecieved {
    public String jsonData;
    public Long recieved;
    
    public ServerInfoRecieved(String json) {
        this.jsonData = json;
        Instant instantTime = Instant.now();
        this.recieved = instantTime.getEpochSecond();
    }

    public int getAgeInSeconds() {
        Instant instantTime = Instant.now();
        Long now = instantTime.getEpochSecond();
        return (int)(now-this.recieved);
    }
}
