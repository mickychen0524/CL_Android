package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.TicketDetailActivty;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by mymac on 3/21/17.
 */

public class GiftcardDownloadedListAdapter extends ArrayAdapter<JSONObject> {


    private static final String TAG = GiftcardDownloadedListAdapter.class.getSimpleName();

    private final Activity context;
    private List<JSONObject> cardsDataList;
    private float startX;
    private float startY;
    private int CLICK_ACTION_THRESHHOLD = 100;

    public GiftcardDownloadedListAdapter(Activity context, List<JSONObject> cardsDataList) {
        super(context, R.layout.giftcard_downloaded_row, cardsDataList);
        this.context = context;
        this.cardsDataList = cardsDataList;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        final JSONObject cardItem = cardsDataList.get(position);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.giftcard_downloaded_row, null, true);
        TextView txtName = (TextView) rowView.findViewById(R.id.giftcards_checked_name_txt);
        TextView txtPrice = (TextView) rowView.findViewById(R.id.giftcard_checked_price_txt);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.giftcards_checked_tile_image);


        if (cardItem.has("giftItemObj")) {

            rowView.findViewById(R.id.giftcard_checked_list_redeem_btn).setVisibility(View.VISIBLE);

            final String localFilePathForGiftcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + Constants.DIR_ROOT + "/" + Constants.DIR_GIFTCARDS + "/";
            String fileName = "";
            try {

                JSONObject giftItemObj = cardItem.getJSONObject("giftItemObj");
                txtName.setText(giftItemObj.getString("merchantName"));
                txtPrice.setText("$" + String.valueOf(cardItem.getDouble("value")));
                fileName = cardItem.getString("fileName");
                final File staticImgFile = new File(localFilePathForGiftcard + fileName);

                if (staticImgFile.exists()) {
                    try {
                        AndroidUtilities.loadImage(imageView, staticImgFile);
                    } catch (Exception e) {
                        Log.e("input stream error -->", e.getMessage());
                    }
                }


                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent i = new Intent(context, TicketDetailActivty.class);
                            i.putExtra("ticketFilePath", localFilePathForGiftcard + cardItem.getString("fileName"));
                            context.startActivity(i);
                        } catch (Exception e) {
                            Log.e("JSONError", e.getMessage());
                        }
                    }
                });

                rowView.findViewById(R.id.giftcard_checked_list_redeem_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayReedemDialog(staticImgFile, cardItem);

                    }
                });
            } catch (Exception ex) {
                Log.e("bmp_gettingerr-->", ex.getMessage());
            }
        } else {
//            rowView.findViewById(R.id.giftcard_checked_list_redeem_btn).setVisibility(View.GONE);

            final String localFilePathForCoupons = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + Constants.DIR_ROOT + "/" + Constants.DIR_COUPONS + "/";
            try {

                txtName.setText(cardItem.getString("fileName"));
                txtPrice.setText("$" + String.valueOf(cardItem.getDouble("value")));
                final String fileName = cardItem.getString("fileName");
                final File staticImgFile = new File(localFilePathForCoupons + fileName);


                if (staticImgFile.exists()) {
                    try {
                        AndroidUtilities.loadImage(imageView, staticImgFile);
                    } catch (Exception e) {
                        Log.e("input stream error -->", e.getMessage());
                    }
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent i = new Intent(context, TicketDetailActivty.class);
                            i.putExtra("ticketFilePath", localFilePathForCoupons + fileName);
                            context.startActivity(i);
                        } catch (Exception e) {
                            Log.e("JSONError", e.getMessage());
                        }
                    }
                });

                rowView.findViewById(R.id.giftcard_checked_list_redeem_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        coupon(staticImgFile, cardItem);

                    }
                });
            } catch (Exception ex) {
                Log.e("bmp_gettingerr-->", ex.getMessage());
            }
        }


        return rowView;
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > CLICK_ACTION_THRESHHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHHOLD) {
            return false;
        }
        return true;
    }


    public static String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        } catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
        }
        return contents;
    }

    private void displayReedemDialog(final File imageFile, final JSONObject cardItem) {

        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.modal_redeem_giftcard);
            dialog.setCancelable(false);


            ImageView ivTile = (ImageView) dialog.findViewById(R.id.ivTile);
            if (imageFile.exists()) {
                try {
                    AndroidUtilities.loadImage(ivTile, imageFile);
                } catch (Exception e) {
                    Log.e("input stream error -->", e.getMessage());
                }
            }

            final double value = cardItem.getDouble("value");
            TextView tvAmount = (TextView) dialog.findViewById(R.id.tvAmount);
            tvAmount.setText("$" + String.valueOf(value));

            final EditText etAmount = (EditText) dialog.findViewById(R.id.etAmount);

            Button btnRedeem = (Button) dialog.findViewById(R.id.btnRedeem);
            Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

            btnRedeem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (etAmount.getText().toString().trim().length() > 0 && (value >= Double.parseDouble(etAmount.getText().toString().trim()))) {
                        initiateClaim(imageFile, cardItem, Double.parseDouble(etAmount.getText().toString().trim()));
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
            AndroidUtilities.setDialogLayoutParams(context, dialog);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void initiateClaim(final File imageFile, final JSONObject cardItem,
                               double amountClaimed) {

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        final String validationCode = scanQRImage(bitmap);
        Log.i(TAG, "validationCode: " + validationCode);

        Bitmap decodedByte = null;
        try {
            JSONObject receivedClaimObject = APIInterface.giftcardClaim(cardItem, AndroidUtilities.getUUID(context), amountClaimed, validationCode);

            if (receivedClaimObject != null) {

                JSONObject data = receivedClaimObject.getJSONObject("datax");
                String imageString = data.getString("qrCodeBase64");
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
            Log.d(TAG, "initiateClaim: " + receivedClaimObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayQRDialog(decodedByte, cardItem, imageFile);
    }

    private void coupon(final File imageFile, final JSONObject cardItem) {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        final String validationCode = scanQRImage(bitmap);
        Log.i(TAG, "validationCode: " + validationCode);

        Bitmap decodedByte = null;
        try {
            JSONObject receivedClaimObject = APIInterface.couponItem(cardItem, AndroidUtilities.getUUID(context), validationCode);
            if (receivedClaimObject != null) {
                JSONObject data = receivedClaimObject.getJSONObject("data");
                String imageString = data.getString("qrCodeBase64");
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
            Log.d(TAG, "couponItem: " + receivedClaimObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayQRDialog(decodedByte, cardItem, imageFile);

    }

    private void displayQRDialog(final Bitmap qrcodeImage, final JSONObject cardItem, final File imageFile) {

        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.modal_redeem_giftcard_qrcode);
            dialog.setCancelable(false);


            ImageView ivTile = (ImageView) dialog.findViewById(R.id.ivTile);
            if (imageFile.exists()) {
                try {
                    AndroidUtilities.loadImage(ivTile, imageFile);
                } catch (Exception e) {
                    Log.e("input stream error -->", e.getMessage());
                }
            }

            ImageView ivQR = (ImageView) dialog.findViewById(R.id.ivQRCode);
            if (qrcodeImage != null) {
                ivQR.setImageBitmap(qrcodeImage);
            }

            TextView tvAmount = (TextView) dialog.findViewById(R.id.tvAmount);
            tvAmount.setText("$" + String.valueOf(cardItem.getDouble("value")));


            Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
            AndroidUtilities.setDialogLayoutParams(context, dialog);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
