package com.example.dylanyung.ideatree;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

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

    public void convertUrlToFilePath(HashMap resultsHash, Context context, HashSet cachedUrls) {
        // Because one of the JSON Objects in the array doesn't convert to string properly
        if (resultsHash == null) {
            return;
        }
        try {
            URL url = new URL((String) resultsHash.get("artworkUrl"));
            File cacheDirectory = context.getCacheDir();
            String filePath = cacheDirectory.getPath() + "/" + resultsHash.get("collectionName") + ".jpg";
            if (!cachedUrls.contains(url)) {
                cachedUrls.add(url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream iStream = urlConnection.getInputStream();
                File tmpFile = new File(filePath);
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                Bitmap bitmap = BitmapFactory.decodeStream(iStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);
                fOutStream.flush();
                fOutStream.close();
            }
            resultsHash.put("artworkUrl", filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
