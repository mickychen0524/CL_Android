package dev.countryfair.player.playlazlo.com.countryfair;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;

import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by Dev01 on 10/3/2017.
 */

public class SocialScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socialscan);
        ImageView ivSocialScan = (ImageView) findViewById(R.id.ivSocialScan);
        final String localFilePathForSocialImage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/"+Constants.FILE_SOCIALIMAGE+"/";
        AndroidUtilities.loadImage(ivSocialScan,new File(localFilePathForSocialImage));
    }
}
