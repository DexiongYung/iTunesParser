package com.example.dylanyung.ideatree;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dylanyung.ideatree.Objects.Cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TrackDetailsActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private ImageView imageViewArtwork;
    private ImageButton imageButtonPreview;
    private TextView textViewCollection;
    private TextView textViewTrackName;
    private TextView textViewCost;
    private TextView textViewArtist;
    private TextView textViewUrl;

    private String trackUrl;
    private String previewUrl;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        instantiateViews();
        setViews();
    }

    @Override
    public void onClick(View v) {
        if (v == textViewUrl) {
            if (this.mediaPlayer.isPlaying()) {
                setImageButtonToPlay();
                this.mediaPlayer.pause();
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(trackUrl));
            startActivity(browserIntent);
        } else if (v == imageButtonPreview) {
            if (!mediaPlayer.isPlaying()) {
                setImageButtonToPause();
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.start();
            } else {
                setImageButtonToPlay();
                mediaPlayer.pause();
            }
        }
    }

    @Override
    public void onDestroy() {
        this.mediaPlayer.stop();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            setImageButtonToPlay();
        }
        super.onPause();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setImageButtonToPlay();
    }

    private void instantiateViews() {
        textViewTrackName = findViewById(R.id.details_track_name);
        textViewCollection = findViewById(R.id.details_collection_name);
        imageViewArtwork = findViewById(R.id.details_artwork);
        textViewCost = findViewById(R.id.details_cost);
        textViewArtist = findViewById(R.id.details_artist);
        textViewUrl = findViewById(R.id.details_url);
        imageButtonPreview = findViewById(R.id.details_preview);
        imageButtonPreview.setOnClickListener(this);
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

        this.trackUrl = (String) resultHash.get("trackViewUrl");
        this.previewUrl = (String) resultHash.get("previewUrl");
        textViewUrl.setOnClickListener(this);
        textViewUrl.setMovementMethod(LinkMovementMethod.getInstance());
        textViewArtist.setText((String) resultHash.get("artistName"));
        textViewTrackName.setText(trackName);
        textViewCollection.setText(collectionName);
        setupMediaPlayer();
    }

    private void setupMediaPlayer() {
        if (this.mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        try {
            this.mediaPlayer.setDataSource(this.previewUrl);
            this.mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setImageButtonToPlay() {
        String uri = "@drawable/ic_play_arrow_24dp";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable resource = getResources().getDrawable(imageResource);
        imageButtonPreview.setImageDrawable(resource);
    }

    private void setImageButtonToPause() {
        String uri = "@drawable/ic_pause_24dp";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable resource = getResources().getDrawable(imageResource);
        imageButtonPreview.setImageDrawable(resource);
    }
}
