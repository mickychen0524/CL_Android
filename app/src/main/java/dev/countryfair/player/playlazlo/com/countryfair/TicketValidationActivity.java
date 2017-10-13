package dev.countryfair.player.playlazlo.com.countryfair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by mymac on 3/21/17.
 */

public class TicketValidationActivity extends AppCompatActivity {

    private float totalAmount;
    private String claimLicenseCode;
    private JSONObject claimTicketItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ticket_validation_activity);

        // set initial project variables from intent extra
        totalAmount = getIntent().getFloatExtra("totalAmount", 0.0f);
        claimLicenseCode = getIntent().getStringExtra("claimLicenseCode");

        ImageView qrCodeimage = (ImageView) findViewById(R.id.validation_qrcode_image);
        TextView totalAmountTxt = (TextView) findViewById(R.id.validation_award_amount_txt);
        totalAmountTxt.setText(String.format(Locale.US, "$%.2f Award!", totalAmount));

         MultiFormatWriter writer =new MultiFormatWriter();

        try {
            claimTicketItem = new JSONObject(getIntent().getStringExtra("claimTicketData"));
            String licenseData = Uri.encode(claimLicenseCode, "utf-8");
            BitMatrix bm = writer.encode(licenseData, BarcodeFormat.QR_CODE,250, 250);
            Bitmap ImageBitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < 250; i++) {//width
                for (int j = 0; j < 250; j++) {//height
                    ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }
            qrCodeimage.setImageBitmap(ImageBitmap);

        } catch (Exception e) {
            Log.e("checkout_dlg_e-->", e.getMessage());
        }
    }

    public void back(View view) {
        Intent i = new Intent(this, TicketListActivity.class);
        startActivity(i);
        finish();
    }
    public void claimTicket(View view) {
        Intent i = new Intent(this, CardsListActivity.class);
        i.putExtra("totalAmount", totalAmount);
        i.putExtra("claimLicenseCode", claimLicenseCode);
        i.putExtra("claimTicketData", claimTicketItem.toString());
        startActivity(i);
        finish();
    }
}
