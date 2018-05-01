package com.example.dylanyung.ideatree;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dylanyung.ideatree.Objects.Cache;

import java.io.File;
import java.util.HashMap;

public class TrackDetailsActivity extends AppCompatActivity {
    ImageView imageViewArtwork;
    TextView textViewCollection;
    TextView textViewTrackName;
    TextView textViewCost;
    TextView textViewArtist;
    TextView textViewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        instantiateViews();
        setViews();
    }

    private void instantiateViews() {
        textViewTrackName = findViewById(R.id.details_track_name);
        textViewCollection = findViewById(R.id.details_collection_name);
        imageViewArtwork = findViewById(R.id.details_artwork);
        textViewCost = findViewById(R.id.details_cost);
        textViewArtist = findViewById(R.id.details_artist);
        textViewUrl = findViewById(R.id.details_url);
    }

    private void setViews() {
        String collectionName = getIntent().getStringExtra("collectionName");
        String trackName = getIntent().getStringExtra("trackName");
        HashMap storage = Cache.getInstance().getStorage();
        HashMap resultHash = (HashMap) storage.get(collectionName + trackName);

        //I know the images are cached so just do a look up based on collectName
        File cacheDirectory = getBaseContext().getCacheDir();
        File imgFile = new File(cacheDirectory.getPath() + "/" + collectionName + ".jpg");
        if (imgFile.exists()) {
            Bitmap imgFileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageViewArtwork.setImageBitmap(imgFileBitmap);
        }

        //Some costs were negative which doesn't make sense so applied absolute val to them.
        String cost = (String) resultHash.get("trackPrice");
        Double costAsDouble = Math.abs(Double.parseDouble(cost));
        textViewCost.setText(costAsDouble.toString());

        setTrackUrl((String) resultHash.get("trackViewUrl"));
        textViewArtist.setText((String) resultHash.get("artistName"));
        textViewTrackName.setText(trackName);
        textViewCollection.setText(collectionName);
    }

    private void setTrackUrl(String str){
        final String trackUrl = str;
        textViewUrl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(trackUrl));
                startActivity(browserIntent);
            }
        });
        textViewUrl.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
