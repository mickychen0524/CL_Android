package dev.countryfair.player.playlazlo.com.countryfair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;

import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;

/**
 * Created by mymac on 3/21/17.
 */

public class TicketDetailActivty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ticket_detail_activity);

        String filePath = getIntent().getStringExtra("ticketFilePath");

        VideoView ticketVideoView = (VideoView) findViewById(R.id.ticket_detail_video);
        ticketVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
//                back();
                return false;
            }
        });
        ImageView ticketImageView = (ImageView) findViewById(R.id.ticket_detail_immage);
        ticketImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                back();
            }
        });
        if (filePath.contains("mp4")) {
            ticketImageView.setVisibility(View.GONE);
            ticketVideoView.setVisibility(View.VISIBLE);
            ticketVideoView.setVideoPath(filePath);
            ticketVideoView.start();
        } else {
            try {
                ticketImageView.setVisibility(View.VISIBLE);
                ticketVideoView.setVisibility(View.GONE);

                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    AndroidUtilities.loadImage(ticketImageView,imageFile);
                }
            } catch (Exception e) {
                Log.e("file path E-->", e.getMessage());
            }
        }

    }

    private void back() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
