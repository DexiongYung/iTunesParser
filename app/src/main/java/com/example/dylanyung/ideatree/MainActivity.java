package com.example.dylanyung.ideatree;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.example.dylanyung.ideatree.Parsers.SongJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
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

        trackListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private class DownloadJSON extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... v) {
            progressBar.setVisibility(View.VISIBLE);
            String data = "";
            try {
                DataRetrievalHelpers downloadUrl = new DataRetrievalHelpers();
                data = downloadUrl.downloadURL();
            } catch (IOException e) {
                e.printStackTrace();
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
                DataRetrievalHelpers dataRetrievalHelpers = new DataRetrievalHelpers();
                dataRetrievalHelpers.convertUrlToFilePath(resultsHashMap, getBaseContext(), cachedUrls);
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
    }
}
