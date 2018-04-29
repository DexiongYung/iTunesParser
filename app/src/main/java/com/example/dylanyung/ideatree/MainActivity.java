package com.example.dylanyung.ideatree;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.dylanyung.ideatree.Parsers.SongJSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView trackListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadJSON downloadJSON = new DownloadJSON();
        downloadJSON.execute();
        trackListView = findViewById(R.id.listview_tracks);
    }

    private String downloadURL() throws IOException {
        InputStream iStream = null;
        URL url = new URL("https://itunes.apple.com/search?term=Michael+jackson");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        iStream = urlConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iStream));
        StringBuffer jsonStringBuffer = new StringBuffer();

        String line = "";

        while((line = bufferedReader.readLine()) != null){
            jsonStringBuffer.append(line);
        }
        bufferedReader.close();
        iStream.close();

        return jsonStringBuffer.toString();
    }

    private class DownloadJSON extends AsyncTask<Void, Integer, String> {
        String data = null;

        @Override
        protected String doInBackground(Void... v){
            try {
                data = downloadURL();
            } catch (IOException e) {
                Log.d("Error downloading url", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result){
            ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();
            listViewLoaderTask.execute(result);
        }
    }

    private class ListViewLoaderTask extends AsyncTask<String, Void, SimpleAdapter> {
        JSONObject jsonObject;

        @Override
        protected SimpleAdapter doInBackground(String... stringJson){
            List<HashMap<String, Object>> tracksJsonArray = null;
            try {
                jsonObject = new JSONObject(stringJson[0]);
                SongJSONParser songJsonParser = new SongJSONParser();
                tracksJsonArray = songJsonParser.getSongs(jsonObject.getJSONArray("results"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String[] from = {"collectionName", "trackName", "artwork"};
            int[] to = {R.id.collection_name, R.id.track_name, R.id.artwork};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), tracksJsonArray, R.layout.listview_layout, from, to);

            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter){
            trackListView.setAdapter(adapter);

            for(int i = 0 ; i < adapter.getCount() ; i++){
                HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                if(hm == null){
                    continue;
                }
                String imgUrl = (String) hm.get("artworkUrl");
                ImageLoaderTask imageLoaderTask = new ImageLoaderTask();
                hm.put("artworkUrl", imgUrl);
                hm.put("position", i);
                imageLoaderTask.execute(hm);
            }
        }
    }

    private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>>{

        @Override
        protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {
            InputStream iStream = null;
            String imgUrl = (String) hm[0].get("artworkUrl");
            int position = (Integer) hm[0].get("position");

            try {
                URL url = new URL(imgUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                File cacheDirectory = getBaseContext().getCacheDir();
                File tmpFile = new File(cacheDirectory.getPath() + "/wpta_" + position + ".jpg");
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                Bitmap b = BitmapFactory.decodeStream(iStream);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);
                fOutStream.flush();
                fOutStream.close();
                HashMap<String, Object> hmBitmap = new HashMap<>();
                hmBitmap.put("artwork", tmpFile.getPath());
                hmBitmap.put("position", position);
                return hmBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result){
            String path = (String) result.get("artwork");
            int position = (Integer) result.get("position");
            SimpleAdapter adapter = (SimpleAdapter) trackListView.getAdapter();
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);
            hm.put("artwork", path);
            adapter.notifyDataSetChanged();
        }
    }
}
