package dev.countryfair.player.playlazlo.com.countryfair;

import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;


/**
 * Created by Dev01 on 9/2/2017.
 */

public class GiftCardDetailsActivity extends FragmentActivity {

    private ImageView ivGiftCard;
    private TextView tvTerms;

    private String merchandiseImageUrl,merchantTerms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giftcard_details);
        init();
    }


    private void init(){

        merchandiseImageUrl = getIntent().getStringExtra("merchandiseImageUrl");
        merchantTerms = getIntent().getStringExtra("merchantTerms");

        ivGiftCard = (ImageView) findViewById(R.id.ivGiftCard);
        tvTerms = (TextView) findViewById(R.id.tvTerms);

        new Thread(new Runnable() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtilities.loadImage(ivGiftCard,merchandiseImageUrl);
                        }

                    });
                } catch (Exception e) {
                }
            }
        }).start();
        tvTerms.setText(merchantTerms);


    }


}
