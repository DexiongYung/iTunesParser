package com.example.dylanyung.ideatree.Parsers;

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
            song.put("collectionName", jObject.getString("collectionName"));
            song.put("trackName", jObject.getString("trackName"));
            song.put("artworkUrl", jObject.getString("artworkUrl30"));
        } catch (JSONException e) {
            return null;
        }
        return song;
    }
}
