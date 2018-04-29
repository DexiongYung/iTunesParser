package com.example.dylanyung.ideatree;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.example.dylanyung.ideatree.Parsers.SongJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView trackListView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        trackListView = findViewById(R.id.listview_tracks);

        DownloadJSON downloadJSON = new DownloadJSON();
        downloadJSON.execute();
    }

    private String downloadURL() throws IOException {
        progressBar.setVisibility(View.VISIBLE);
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

    private class DownloadJSON extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... v) {
            String data = "";
            try {
                data = downloadURL();
            } catch (IOException e) {
                Log.d("Error downloading url", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();
            listViewLoaderTask.execute(result);
        }
    }

    private class ListViewLoaderTask extends AsyncTask<String, Void, SimpleAdapter> {
        JSONObject jsonObject;
        HashSet<URL> cachedUrls = new HashSet<>();

        @Override
        protected SimpleAdapter doInBackground(String... stringJson) {
            List<HashMap<String, Object>> data = null;
            try {
                jsonObject = new JSONObject(stringJson[0]);
                SongJSONParser songJsonParser = new SongJSONParser();
                data = songJsonParser.getSongs(jsonObject.getJSONArray("results"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (HashMap resultsHashMap : data) {
                convertUrlToFilePath(resultsHashMap);
            }

            String[] from = {"collectionName", "trackName", "artworkUrl"};
            int[] to = {R.id.collection_name, R.id.track_name, R.id.artwork};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), data, R.layout.listview_layout, from, to);

            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter) {
            progressBar.setVisibility(View.GONE);
            trackListView.setAdapter(adapter);
        }

        private void convertUrlToFilePath(HashMap resultsHash) {
            // Because one of the JSON Objects in the array doesn't convert to string properly
            if (resultsHash == null) {
                return;
            }
            try {
                URL url = new URL((String) resultsHash.get("artworkUrl"));
                File cacheDirectory = getBaseContext().getCacheDir();
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
}
