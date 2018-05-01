package com.example.dylanyung.ideatree.Objects;

import java.util.HashMap;

public class Cache {
    private static Cache instance;
    private HashMap<String, HashMap> storage = new HashMap<>();

    private Cache(){}

    public static Cache getInstance() {
        if(instance == null){
            instance = new Cache();
        }
        return instance;
    }

    public void addHashToStorage(String key, HashMap entry){
        storage.put(key, entry);
    }

    public HashMap<String, HashMap> getStorage() {
        return storage;
    }
}
