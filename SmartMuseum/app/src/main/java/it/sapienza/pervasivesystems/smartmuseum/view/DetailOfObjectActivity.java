package it.sapienza.pervasivesystems.smartmuseum.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import it.sapienza.pervasivesystems.smartmuseum.R;
import it.sapienza.pervasivesystems.smartmuseum.SmartMuseumApp;
import it.sapienza.pervasivesystems.smartmuseum.business.exhibits.WorkofartBusiness;
import it.sapienza.pervasivesystems.smartmuseum.model.entity.WorkofartModel;

public class DetailOfObjectActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageView image;
    private TextView title;
    private TextView desc;

    private SeekBar seek_bar;
    private Button play_button;
    private MediaPlayer player;
    private TextView musicCurrentLoc;
    private TextView musicDuration;
    private Handler seekHandler = new Handler();
    private Utilities utils;
    String mp3 = "http://www.stephaniequinn.com/Music/Allegro%20from%20Duet%20in%20C%20Major.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_of_object);

        Intent mIntent = getIntent();

//        String idWork = mIntent.getStringExtra("idWork");
        String key = mIntent.getStringExtra("key");
//        WorkofartModel objectDtl = (WorkofartModel) new WorkofartBusiness().getWorkOfArtDetailFAKE("2048:8066:1");

        WorkofartModel objectDtl = (WorkofartModel) new WorkofartBusiness().getWorkofartDetail(SmartMuseumApp.workofartModelHashMap, key);

        image = (ImageView) findViewById(R.id.obj_dtl_image);
        title = (TextView) findViewById(R.id.obj_dtl_title);
        desc = (TextView) findViewById(R.id.obj_dtl_desc);

        Picasso.with(this).load(objectDtl.getImage()).into(image);
        title.setText(objectDtl.getTitle());
        desc.setText(objectDtl.getLongDescription());

        try {
            getInit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getInit() throws IOException {
        seek_bar = (SeekBar) findViewById(R.id.musicSeekBar);
        play_button = (Button) findViewById(R.id.playPauseButton);

        musicDuration = (TextView) findViewById(R.id.musicDuration);
        musicCurrentLoc = (TextView) findViewById(R.id.musicCurrentLoc);


        play_button.setOnClickListener(this);
        player = new MediaPlayer();
        player.setDataSource(mp3);
        player.prepare();
        utils = new Utilities();

        seek_bar.setOnSeekBarChangeListener(this);
        player.setOnCompletionListener(this);

        seek_bar.setMax(player.getDuration());
        seek_bar.setClickable(false);

    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            int totalDuration = player.getDuration();
            int currentDuration = player.getCurrentPosition();

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seek_bar.setProgress(progress);

            // Running this thread after 100 milliseconds
            seekHandler.postDelayed(this, 1000);

            String totalTime = String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                    TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration))
            );

            String curTime = String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(currentDuration),
                    TimeUnit.MILLISECONDS.toSeconds(currentDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration))
            );


            musicDuration.setText(totalTime);
            musicCurrentLoc.setText(curTime);

        }
    };

    @Override
    public void onClick(View view) {
        if (player.isPlaying()) {
            player.pause();
            play_button.setBackgroundResource(R.drawable.pause_icon_pink);

        } else {
            player.start();
            play_button.setBackgroundResource(R.drawable.play_icon_pink);

            seekHandler.postDelayed(run, 1000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekHandler.removeCallbacks(run);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
