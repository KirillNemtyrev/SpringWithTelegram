package com.community.server.game;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Component
public class GameStorage {
    private Map<Long, Timer> storage = new HashMap<>();
    private Map<Long, StartGame> game = new HashMap<>();

    public Timer getTimer(Long id){
        return storage.get(id);
    }
    public void putTimer(Long id, Timer timer){
        storage.put(id, timer);
    }
    public void putObject(Long id, StartGame object) {
        game.put(id, object);
    }
    public StartGame getObject(Long id) {
        return game.get(id);
    }
}
