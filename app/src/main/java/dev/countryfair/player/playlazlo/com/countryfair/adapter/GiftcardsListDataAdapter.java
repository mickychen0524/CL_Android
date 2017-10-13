package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.refactor.lib.colordialog.ColorDialog;
import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.CheckoutFileDownloadActivity;
import dev.countryfair.player.playlazlo.com.countryfair.GiftCardDetailsActivity;
import dev.countryfair.player.playlazlo.com.countryfair.GiftcardCheckoutFileDownloadActivity;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.ShoppingCartListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.TicketRefundActivity;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.service.GiftcardCheckoutStatusGettingService;
import dev.countryfair.player.playlazlo.com.countryfair.service.ServiceResultReceiver;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mymac on 3/21/17.
 */

public class GiftcardsListDataAdapter extends ArrayAdapter<JSONObject> {

    private final Activity context;
    private List<JSONObject> cardsDataList;

    private Intent mServiceIntent;
    private ServiceResultReceiver mReceiverForCheckStatus;

    private boolean b_cancelBtnPressed = false;
    private float f_playPrice = 0.0f;

    private ProgressDialog mProgressDialog;
    private Dialog checkoutDlg;
    private Dialog generatingDlg;

    private JSONObject checkoutResData = new JSONObject();
    private JSONObject receivedObj = new JSONObject();
    private JSONArray shoppingCartArr = new JSONArray();

    public GiftcardsListDataAdapter(Activity context, List<JSONObject> cardsDataList) {
        super(context, R.layout.giftcard_list_row, cardsDataList);
        this.context = context;
        this.cardsDataList = cardsDataList;

    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        final JSONObject currentCardItem = cardsDataList.get(position);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.giftcard_list_row, null, true);

        try {
            TextView txtName = (TextView) rowView.findViewById(R.id.giftcards_list_name_txt);
            TextView txtVal = (TextView) rowView.findViewById(R.id.giftcards_list_value_txt);
            final ImageView imageView = (ImageView) rowView.findViewById(R.id.giftcards_list_tile_image);

            final String urlStr = currentCardItem.getString("merchandiseImageUrl");
            urlStr.replace("https://demolngcdn.blob.core.windows.net", "https://dev2lngmstrasia.blob.core.windows.net");

            final String terms = currentCardItem.getString("merchantTerms");


            txtName.setText(currentCardItem.getString("merchantName"));

            JSONArray priceArr = currentCardItem.getJSONArray("ranges");
            if (priceArr.length() > 0) {
                JSONObject rangeItem = priceArr.getJSONObject(0);
                txtVal.setText(context.getString(R.string.from_to_values, ("$" + String.valueOf(rangeItem.getInt("low"))), ("$" + String.valueOf(rangeItem.getInt("high")))));
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtilities.loadImage(imageView, urlStr);
                            }

                        });
                    } catch (Exception e) {
                        Log.e("bmp_gettingerr-->", e.getMessage());
                    }
                }
            }).start();


            Button buyNowBtn = (Button) rowView.findViewById(R.id.giftcards_list_buynow_btn);
            buyNowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createAddShoppingCartDialog(currentCardItem);
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(context, GiftCardDetailsActivity.class);
                    mIntent.putExtra("merchandiseImageUrl", urlStr);
                    mIntent.putExtra("merchantTerms", terms);
                    context.startActivity(mIntent);
                }
            });

            setupServiceReceiver();

        } catch (Exception ex) {
            Log.e("bmp_gettingerr-->", ex.getMessage());
        }
        return rowView;
    }

    private void createAddShoppingCartDialog(final JSONObject channelData) {

        final List<String> priceStrList = new ArrayList<String>();
        try {
            JSONArray priceArr = channelData.getJSONArray("ranges");
            if (priceArr.length() > 0) {
                JSONObject rangeItem = priceArr.getJSONObject(0);
                for (int i = rangeItem.getInt("low"); i < rangeItem.getInt("high"); i++) {
                    priceStrList.add(String.valueOf(i));
                }
            }

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.modal_add_giftcard_layout);

            final Spinner priceSpinner = (Spinner) dialog.findViewById(R.id.modal_giftcard_amount_list);

            ArrayAdapter<String> priceAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, priceStrList);

            priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            priceSpinner.setAdapter(priceAdapter);

            // spinner selected listener

            priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    f_playPrice = Float.parseFloat(priceStrList.get(position));
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    tv.setText("$" + priceStrList.get(position));
                    view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    view.invalidate();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            ImageView tileImage = (ImageView) dialog.findViewById(R.id.gift_cart_tile_image);
            final ImageView gameLogoImage = (ImageView) dialog.findViewById(R.id.gift_cart_game_logo);
            final ImageView brandLogoImage = (ImageView) dialog.findViewById(R.id.gift_cart_brand_logo);
            final TextView tvDisclaimer = (TextView) dialog.findViewById(R.id.tvDisclaimer);

            String disclaimer = channelData.getString("merchantDisclaimer");

            tvDisclaimer.setText((disclaimer != null) ? disclaimer : "");


            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String urlStr = channelData.getString("merchandiseImageUrl");
                        urlStr.replace("https://demolngcdn.blob.core.windows.net", "https://dev2lngmstrasia.blob.core.windows.net");
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                AndroidUtilities.loadImage(gameLogoImage, urlStr);
                                AndroidUtilities.loadImage(brandLogoImage, urlStr);
                            }

                        });
                    } catch (Exception e) {
                        Log.e("bmp_gettingerr-->", e.getMessage());
                    }
                }
            }).start();

            Button addBtn = (Button) dialog.findViewById(R.id.shopping_cart_add);
            Button cancelBtn = (Button) dialog.findViewById(R.id.gift_cart_cancel);

            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject giftcardObj = new JSONObject();
                    try {
/*
                    giftcardObj.put("merchantName", channelData.get("merchantName"));
                    giftcardObj.put("ranges", channelData.getJSONArray("ranges"));
                    giftcardObj.put("merchandiseImageUrl", channelData.getString("merchandiseImageUrl"));
                    giftcardObj.put("merchandiseRefId", channelData.get("merchandiseRefId"));
                    giftcardObj.put("Value", f_playPrice);

*/
                        giftcardObj = new JSONObject(channelData.toString());
                        giftcardObj.put("Value", f_playPrice);
                    } catch (JSONException e) {
                        Log.d("json_e-->", e.getMessage());
                    }


                    new AppHelper(context.getApplicationContext()).saveOneShoppingCartToLocalGift(giftcardObj);
                    Toast.makeText(context.getApplicationContext(), "shopping cart added successfully", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();

/*
                checkoutGiftCartWithAPI(channelData);
                dialog.dismiss();
*/
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
            AndroidUtilities.setDialogLayoutParams(context, dialog);

        } catch (Exception e) {


            Log.e("JSONError", e.getMessage());
        }


    }

    private void checkoutGiftCartWithAPI(final JSONObject channelData) {
        final SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        b_cancelBtnPressed = false;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Checkout...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    int simulationType = sharePref.getBoolean("isSystemGenerated", true) ? 9 : 0;
                    String uuid = AndroidUtilities.getUUID(context);
                    receivedObj = APIInterface.checkoutWithShopping(channelData, f_playPrice, simulationType, uuid);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    if (jsonData != null) {
                                        checkoutResData = jsonData;
                                        createCheckoutDlg(jsonData, channelData);
                                    } else {
                                        new PromptDialog(context)
                                                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                                .setAnimationEnable(true)
                                                .setTitleText(Constants.ERROR_TITLE)
                                                .setContentText(Constants.ERROR_MESSAGE)
                                                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                                    @Override
                                                    public void onClick(PromptDialog dialog) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }


                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                new PromptDialog(context)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText(Constants.ERROR_TITLE)
                                        .setContentText(Constants.ERROR_MESSAGE)
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    });
                } catch (Exception e) {

                    Log.e("register error--->", e.getMessage());
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            new PromptDialog(context)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText(Constants.ERROR_TITLE)
                                    .setContentText(Constants.ERROR_MESSAGE)
                                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });

                }
            }
        }).start();
    }

    private void getCheckoutStatus(JSONObject obj, JSONObject channelData) {
        if (!b_cancelBtnPressed) {

            mServiceIntent = new Intent(context, GiftcardCheckoutStatusGettingService.class);
            List<JSONObject> giftcadList = new ArrayList<JSONObject>();
            giftcadList.add(channelData);
            GiftcardCheckoutStatusGettingService.shoppingCartData = giftcadList;
            try {
                mServiceIntent.putExtra("receivedObj", obj.toString());
            } catch (Exception e) {
                Log.e("jsonparsing--->", e.getMessage());
            }

            mServiceIntent.putExtra("receiver", mReceiverForCheckStatus);
            GiftcardCheckoutStatusGettingService.isStoped = false;
            context.startService(mServiceIntent);
        } else {
            b_cancelBtnPressed = false;
        }
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        mReceiverForCheckStatus = new ServiceResultReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        mReceiverForCheckStatus.setReceiver(new ServiceResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {

                    try {

                        JSONArray ticketSuccessArr = new JSONArray();
                        JSONArray ticketErrorArr = new JSONArray();
                        JSONArray resDataArr = new JSONArray();
                        Intent i = new Intent(context, GiftcardCheckoutFileDownloadActivity.class);
                        switch (resultData.getInt("resultStatus")) {
                            case Constants.SHOPPING_CART_DOWNLOADING_SUCCESS:
                                generatingDlg.dismiss();
                                ticketSuccessArr = new JSONArray(resultData.getString("resultValueSuccess"));
                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));
//                                addNotification("Hi Dear, the checkout file download was completed.");
                                Toast.makeText(context.getApplicationContext(), "Generating completed", Toast.LENGTH_SHORT).show();

                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
                                i.putExtra("resDataArr", resDataArr.toString());
                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
                                context.startActivity(i);
                                context.finish();

                                break;
                            case Constants.SHOPPING_CART_GENERATING_SUCCESS:
                                checkoutDlg.dismiss();
                                createTicketGeneratingDlg();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_SUB_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if (generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                ticketSuccessArr = new JSONArray(resultData.getString("resultValueSuccess"));
                                ticketErrorArr = new JSONArray(resultData.getString("resultValueError"));
                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));

                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
                                i.putExtra("resDataArr", resDataArr.toString());
                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
                                context.startActivity(i);
                                context.finish();
                                Toast.makeText(context.getApplicationContext(), "Generating sub failed", Toast.LENGTH_SHORT).show();

                                break;
                            case Constants.SHOPPING_CART_GENERATING_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if (generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(context.getApplicationContext(), "Generating failed", Toast.LENGTH_SHORT).show();
//                                ticketErrorArr = new JSONArray(resultData.getString("resultValueError"));
//                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));
//                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
//                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
//                                i.putExtra("resDataArr", resDataArr.toString());
//                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
//                                startActivity(i);
//                                finish();

                                Toast.makeText(context.getApplicationContext(), "Generating failed", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_API_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if (generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                new PromptDialog(context)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText(Constants.ERROR_TITLE)
                                        .setContentText(Constants.ERROR_MESSAGE)
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_LOOP:
                                if (checkoutDlg.isShowing()) {
                                    Toast toast = Toast.makeText(context.getApplicationContext(), "Item(s) creating", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else if (generatingDlg.isShowing()) {
                                    Toast toast = Toast.makeText(context.getApplicationContext(), "Generating item(s)", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }
                                break;
                            case Constants.SHOPPING_CART_SERVICE_EXCEPTION:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if (generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(context.getApplicationContext(), resultData.getString("exceptionMessage"), Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_CANCELED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if (generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(context.getApplicationContext(), "Checkout service canceled", Toast.LENGTH_SHORT).show();
                                break;

                        }
                    } catch (Exception e) {

                    }
                }
            }
        });
    }

    private void cancelCheckout(final JSONObject obj) {

        b_cancelBtnPressed = true;
        context.stopService(mServiceIntent);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Checkout cancel...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(context);
                    receivedObj = APIInterface.checkoutCancel(obj, uuid);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {

                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    if (jsonData != null) {

                                        new PromptDialog(context)
                                                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                                                .setAnimationEnable(true)
                                                .setTitleText("Cancel successed")
                                                .setContentText("Checkout cancel successed")
                                                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                                    @Override
                                                    public void onClick(PromptDialog dialog) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    } else {
                                        ColorDialog dialog = new ColorDialog(context.getApplicationContext());
                                        dialog.setTitle("Checkout cancel error");
                                        dialog.setContentText("Oops! We seem to be having a problem connecting.\n Please try again later.");
                                        dialog.setPositiveListener("Try again", new ColorDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(ColorDialog dialog) {
                                                GiftcardCheckoutStatusGettingService.isStoped = true;
                                                cancelCheckout(obj);
                                                dialog.dismiss();

                                            }
                                        })
                                                .setNegativeListener("No thanks", new ColorDialog.OnNegativeListener() {
                                                    @Override
                                                    public void onClick(ColorDialog dialog) {

                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }


                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                ColorDialog dialog = new ColorDialog(context);
                                dialog.setTitle("Checkout cancel error");
                                dialog.setContentText("Oops! We seem to be having a problem connecting.\n Please try again later.");
                                dialog.setPositiveListener("Try again", new ColorDialog.OnPositiveListener() {
                                    @Override
                                    public void onClick(ColorDialog dialog) {
                                        GiftcardCheckoutStatusGettingService.isStoped = true;
                                        cancelCheckout(obj);
                                        dialog.dismiss();

                                    }
                                })
                                        .setNegativeListener("No thanks", new ColorDialog.OnNegativeListener() {
                                            @Override
                                            public void onClick(ColorDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    });
                } catch (Exception e) {

                    Log.e("register error--->", e.getMessage());
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            ColorDialog dialog = new ColorDialog(context);
                            dialog.setTitle("Checkout cancel error");
                            dialog.setContentText("Oops! We seem to be having a problem connecting.\n Please try again later.");
                            dialog.setPositiveListener("Try again", new ColorDialog.OnPositiveListener() {
                                @Override
                                public void onClick(ColorDialog dialog) {
                                    GiftcardCheckoutStatusGettingService.isStoped = true;
                                    cancelCheckout(obj);
                                    dialog.dismiss();
                                }
                            })
                                    .setNegativeListener("No thanks", new ColorDialog.OnNegativeListener() {
                                        @Override
                                        public void onClick(ColorDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });

                }
            }
        }).start();
    }

    // create checkout and ticket generating dialog dynamically
    private void createCheckoutDlg(final JSONObject obj, final JSONObject channelData) {

        checkoutDlg = new Dialog(context);
        checkoutDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkoutDlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        checkoutDlg.setContentView(R.layout.modal_checkout_layout);

        LinearLayout middleView = (LinearLayout) checkoutDlg.findViewById(R.id.checkout_middle_layout);
        TextView checkoutCode = (TextView) checkoutDlg.findViewById(R.id.checkout_ticketcode_text);
        TextView checkoutDescCode = (TextView) checkoutDlg.findViewById(R.id.checkout_main_text);
        ImageView qrcodeImg = (ImageView) checkoutDlg.findViewById(R.id.checkout_qrcode_img);
        com.google.zxing.MultiFormatWriter writer = new MultiFormatWriter();

        try {
            middleView.setBackgroundColor(Color.parseColor(obj.getString("visualIdentifierColor")));
            checkoutCode.setText(obj.getString("visualIdentifierCode"));
            checkoutCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(obj.getString("visualIdentifierColor"))));
            checkoutDescCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(obj.getString("visualIdentifierColor"))));
            String finaldata = obj.getString("licenseCypherText");

            BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 350, 350);
            Bitmap ImageBitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < 350; i++) {//width
                for (int j = 0; j < 350; j++) {//height
                    ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }

            if (ImageBitmap != null) {
                qrcodeImg.setImageBitmap(ImageBitmap);
            } else {
                Toast.makeText(context.getApplicationContext(), "QRCode image generating error", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("checkout_dlg_e-->", e.getMessage());
        }

        Button cancelBtn = (Button) checkoutDlg.findViewById(R.id.checkout_cancel);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiftcardCheckoutStatusGettingService.isStoped = true;
                cancelCheckout(obj);
                checkoutDlg.dismiss();
            }
        });

        Toast.makeText(context.getApplicationContext(), "Checkout completed. Creating item(s)...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCheckoutStatus(obj, channelData);
            }
        }, 500);

        checkoutDlg.show();
        AndroidUtilities.setDialogLayoutParams(context, checkoutDlg);
    }

    private void createTicketGeneratingDlg() {
        generatingDlg = new Dialog(context);
        generatingDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        generatingDlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        generatingDlg.setContentView(R.layout.modal_checkout_generating_layout);

        LinearLayout middleView = (LinearLayout) generatingDlg.findViewById(R.id.checkout_generating_middle_layout);
        TextView checkoutGenCode = (TextView) generatingDlg.findViewById(R.id.checkout_generating_ticketcode_text);
        TextView checkoutGenDescCode = (TextView) generatingDlg.findViewById(R.id.checkout_generating_main_text);

        try {
            middleView.setBackgroundColor(Color.parseColor(checkoutResData.getString("visualIdentifierColor")));
            checkoutGenCode.setText(checkoutResData.getString("visualIdentifierCode"));
            checkoutGenCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(checkoutResData.getString("visualIdentifierColor"))));
            checkoutGenDescCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(checkoutResData.getString("visualIdentifierColor"))));

        } catch (Exception e) {

            Log.e("generating_errror--->", e.getMessage());
        }

        Button cancelBtn = (Button) generatingDlg.findViewById(R.id.checkout_generating_cancel);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiftcardCheckoutStatusGettingService.isStoped = true;
                cancelCheckout(checkoutResData);
                checkoutDlg.dismiss();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), "Item(s) created. Generating item(s)...", Toast.LENGTH_SHORT).show();
            }
        }, 200);
        generatingDlg.show();
        AndroidUtilities.setDialogLayoutParams(context, generatingDlg);
    }

}
