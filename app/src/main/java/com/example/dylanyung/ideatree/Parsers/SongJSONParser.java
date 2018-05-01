package com.example.dylanyung.ideatree.Parsers;

import com.example.dylanyung.ideatree.Objects.Cache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SongJSONParser {
    public List<HashMap<String, Object>> getSongs(JSONArray jArray) {
        int songCount = jArray.length();
        List<HashMap<String, Object>> songList = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < songCount; i++) {
            try {
                HashMap<String, Object> song = getSong((JSONObject) jArray.get(i));
                //One of the JSON objects isn't converting to string properly
                if (song != null) {
                    songList.add(song);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return songList;
    }

    private HashMap<String, Object> getSong(JSONObject jObject) {
        HashMap<String, Object> song = new HashMap<String, Object>();

        try {
            String collectionName = jObject.getString("collectionName");
            String trackName = jObject.getString("trackName");
            song.put("collectionName", collectionName);
            song.put("trackName", trackName);
            song.put("artworkUrl", jObject.getString("artworkUrl100"));

            //Normally I'd cache this data to MySQL DB and lookup by trackId if we had more complex data, but I'm assuming
            //MySQL is considered "third-party" tech which isn't allowed so I just went with this cache.
            HashMap<String, String> resultHash = new HashMap<>();
            resultHash.put("collectionName", collectionName);
            resultHash.put("trackName", trackName);
            resultHash.put("trackPrice", jObject.getString("trackPrice"));
            resultHash.put("artistName", jObject.getString("artistName"));
            resultHash.put("trackViewUrl", jObject.getString("trackViewUrl"));
            resultHash.put("previewUrl", jObject.getString("previewUrl"));
            Cache.getInstance().addHashToStorage(collectionName + trackName, resultHash);
        } catch (JSONException e) {
            return null;
        }
        return song;
    }
}
