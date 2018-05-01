package com.example.dylanyung.ideatree.HttpLoaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataRetrievalHelpers {
    public String downloadURL() throws IOException {
        URL url = new URL("https://itunes.apple.com/search?term=Michael+jackson");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer jsonStringBuffer = new StringBuffer();

        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            jsonStringBuffer.append(line);
        }
        bufferedReader.close();
        inputStream.close();

        return jsonStringBuffer.toString();
    }
}
