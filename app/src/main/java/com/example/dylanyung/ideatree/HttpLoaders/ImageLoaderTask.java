package com.example.dylanyung.ideatree.HttpLoaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>> {
    private Context context;
    private ListView trackListView;

    public ImageLoaderTask(Context context, ListView trackListView) {
        this.context = context;
        this.trackListView = trackListView;
    }

    @Override
    protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hashMaps) {
        String imageUrl = (String) hashMaps[0].get("artworkUrl");
        int position = (Integer) hashMaps[0].get("position");
        String collectionName = (String) hashMaps[0].get("collectionName");
        //Decided to cache because there weren't too many images and since it's 1 artist there will be a ton of repeat images
        File cacheDirectory = this.context.getCacheDir();
        File tempFile = new File(cacheDirectory.getPath() + "/" + collectionName + ".jpg");

        if (!tempFile.exists()) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream iStream = urlConnection.getInputStream();
                FileOutputStream fOutStream = new FileOutputStream(tempFile);
                Bitmap bitmap = BitmapFactory.decodeStream(iStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);
                fOutStream.flush();
                fOutStream.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, Object> hashBitMap = new HashMap();
        hashBitMap.put("artworkPath", tempFile.getPath());
        hashBitMap.put("position", position);
        return hashBitMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, Object> result) {
        String path = (String) result.get("artworkPath");
        int position = (Integer) result.get("position");
        SimpleAdapter adapter = (SimpleAdapter) trackListView.getAdapter();
        HashMap<String, Object> hashMap = (HashMap<String, Object>) adapter.getItem(position);
        hashMap.put("artworkUrl", path);
        adapter.notifyDataSetChanged();
    }
}
