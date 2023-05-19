package jp.ac.cm0107.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    AudioManager audioManager;
    private static final int EXTERNAL_STORAGE = 1;
    private final int SEARCH_REQCD = 123;
    private MediaPlayer mPlayer;
    private String path;
    Button btnPlay;
    Button btnPause;
    Button btnStop;
    SeekBar scrubber ;
    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    ||ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        EXTERNAL_STORAGE);
            }
        }

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new BtnEvent());
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new BtnEvent());
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new BtnEvent());
        scrubber  = (SeekBar)findViewById(R.id.seekBar);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        SeekBar volumeController = (SeekBar) findViewById(R.id.seekBarVolume);
        volumeController.setMax(maxVolume);
        volumeController.setProgress(curVolume);
        volumeController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Button btnSDList = (Button) findViewById(R.id.btnSDList);
        btnSDList.setOnClickListener (new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (mPlayer != null && mPlayer.isPlaying()){
                    mPlayer.stop();
                }
                Intent i = new Intent(MainActivity.this,SDListActivity.class);
                startActivityForResult(i,SEARCH_REQCD);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permission,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if (grantResults.length <= 0) {
            return;
        }
        switch (requestCode) {
            case EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this,
                            "起動できません", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        return;
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        setDefaultButtons();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_REQCD && resultCode == RESULT_OK){
            path = data.getStringExtra("SELECT_FILE");
            TextView txt = (TextView)findViewById(R.id.txtFileName);
            txt.setText(path);
            Log.i(path, "path: ");
            ImageView imageView = findViewById(R.id.imgMusic);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(MainActivity.this, Uri.parse(path));
            byte[] binary = mediaMetadataRetriever.getEmbeddedPicture();
            if (binary != null) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(binary, 0, binary.length));
            } else {
                ContentResolver contentResolver = MainActivity.this.getContentResolver();
                try {
                    InputStream inputStream = contentResolver.openInputStream(Uri.parse(path));
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        }
    }
    class BtnEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId()==R.id.btnPlay) {
                mPlayer = new MediaPlayer();

                try {
                    mPlayer = new MediaPlayer();
                    mPlayer = MediaPlayer.create(MainActivity.this, Uri.parse(path));

                    scrubber.setMax(mPlayer.getDuration());
                        scrubber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if(b){
                                mPlayer.seekTo(i);
                                seekBar.setProgress(i);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Toast.makeText(MainActivity.this,"再々終了",Toast.LENGTH_SHORT).show();
                            mPlayer.stop();
                            setDefaultButtons();
                        }
                    });
                    mPlayer.seekTo(0);
                    mPlayer.start();
                    mPlayer.prepare();
                    setPlayingStateButtons();
                } catch (Exception e) {
                    Log.e("MainActivity", e.toString());
                }
                mPlayer.seekTo(0);
                mPlayer.start();
                setPlayingStateButtons();
            }else if (v.getId()==R.id.btnPause){
                if (mPlayer.isPlaying()){
                    mPlayer.pause();
                }else{
                    mPlayer.start();
                }
                setPlayingStateButtons();
            }else if (v.getId()==R.id.btnStop){
                mPlayer.stop();
                scrubber.setProgress(0);
                setDefaultButtons();
            }
        }
    }
    private void setDefaultButtons(){
        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
    }
    private void setPlayingStateButtons(){
        btnPlay.setEnabled(false);
        btnPause.setEnabled(true);
        btnStop.setEnabled(true);
    }
    private Handler threadHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i("MainActivity", "msg.what = " + msg.what);
            scrubber.setProgress(msg.what);
        }
    };
}