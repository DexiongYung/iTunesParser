package com.example.dylanyung.ideatree;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.dylanyung.ideatree.HttpLoaders.DataRetrievalHelpers;
import com.example.dylanyung.ideatree.HttpLoaders.ImageLoaderTask;
import com.example.dylanyung.ideatree.Parsers.SongJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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
        String trackName = ((TextView) view.findViewById(R.id.track_name)).getText().toString();
        String collectionName = ((TextView) view.findViewById(R.id.track_name)).getText().toString();
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

            //Setting artwork URL prints some errors, but I wanted an async loading of images after the list was generated
            String[] from = {"collectionName", "trackName", "artworkUrl"};
            int[] to = {R.id.collection_name, R.id.track_name, R.id.artwork};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), data, R.layout.listview_layout, from, to);

            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter adapter) {
            progressBar.setVisibility(View.GONE);
            trackListView.setAdapter(adapter);

            for (int i = 0; i < adapter.getCount(); i++) {
                HashMap<String, Object> hashMap = (HashMap<String, Object>) adapter.getItem(i);
                ImageLoaderTask imageLoaderTask = new ImageLoaderTask(getBaseContext(), trackListView);
                hashMap.put("position", i);
                imageLoaderTask.execute(hashMap);
            }
        }
    }
}
