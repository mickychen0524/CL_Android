package dev.countryfair.player.playlazlo.com.countryfair;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.lib.colordialog.ColorDialog;
import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.ShoppingCartGiftListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.service.GiftcardCheckoutStatusGettingService;
import dev.countryfair.player.playlazlo.com.countryfair.service.ServiceResultReceiver;


public class ShoppingCartGiftListActivity extends AppCompatActivity {

    private float f_totalPrice = 0.0f;
    private List<JSONObject> shoppingCartData = new ArrayList<>();
    private TextView totalPriceTxt;
    private Button checkoutBtn;
    private Button shoppingCartCountBtn;
    private ProgressDialog mProgressDialog;
    private Dialog checkoutDlg;
    private Dialog generatingDlg;

    private JSONObject checkoutResData = new JSONObject();
    private JSONObject receivedObj = new JSONObject();
    private JSONArray shoppingCartArr = new JSONArray();

    private boolean b_cancelBtnPressed = false;

    private Intent mServiceIntent;
    private ServiceResultReceiver mReceiverForCheckStatus;
    private ShoppingCartGiftListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_cart_list_activity);


        mProgressDialog = new ProgressDialog(ShoppingCartGiftListActivity.this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        shoppingCartCountBtn = (Button) findViewById(R.id.shopping_cart_list_checkout_count_btn);
        shoppingCartCountBtn.setText("0");

        totalPriceTxt = (TextView) this.findViewById(R.id.shopping_cart_list_totalprice_txt);
        ListView mListView = (ListView) findViewById(R.id.shopping_cart_list);
        mAdapter = new ShoppingCartGiftListAdapter(this, loadAllShoppingCartData());
        mListView.setAdapter(mAdapter);

        checkoutBtn = (Button) findViewById(R.id.shopping_cart_list_checkout);
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkoutShoppingCartWithAPI(view);
            }
        });

        if (!checkRetailerExistState()) {
            checkoutBtn.setEnabled(true);
        } else {
            checkoutBtn.setEnabled(true);
        }

        mAdapter.setOnDataChangeListener(new ShoppingCartGiftListAdapter.OnDataChangeListener() {
            public void onDataChanged(int size){
                mAdapter.notifyDataSetChanged();
                loadAllShoppingCartData();
            }
        });
        setupServiceReceiver();
    }


    public void checkoutShoppingCartWithAPI(View view) {
        final SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        b_cancelBtnPressed = false;
        checkoutBtn.setEnabled(false);
        mProgressDialog.setMessage("Checkout...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    int simulationType = sharePref.getBoolean("isSystemGenerated", true) ? 9 : 0;
                    String uuid = AndroidUtilities.getUUID(ShoppingCartGiftListActivity.this);
                    //TODO: Push Skip
                    receivedObj = APIInterface.checkoutWithShoppingGift(shoppingCartData,simulationType,uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            checkoutBtn.setEnabled(true);
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    if (jsonData != null) {
                                        checkoutResData = jsonData;
                                        createCheckoutDlg(jsonData);
                                    } else {
                                        new PromptDialog(ShoppingCartGiftListActivity.this)
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
                                new PromptDialog(ShoppingCartGiftListActivity.this)
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkoutBtn.setEnabled(true);
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();

                            }
                            new PromptDialog(ShoppingCartGiftListActivity.this)
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

    private void getCheckoutStatus(JSONObject obj) {
        if (!b_cancelBtnPressed) {

            mServiceIntent = new Intent(ShoppingCartGiftListActivity.this, GiftcardCheckoutStatusGettingService.class);
            GiftcardCheckoutStatusGettingService.shoppingCartData = shoppingCartData;
            try {
                mServiceIntent.putExtra("receivedObj", obj.toString());
            } catch (Exception e) {
                Log.e("jsonparsing--->", e.getMessage());
            }

            mServiceIntent.putExtra("receiver", mReceiverForCheckStatus);
            GiftcardCheckoutStatusGettingService.isStoped = false;
            ShoppingCartGiftListActivity.this.startService(mServiceIntent);
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
                if (resultCode == RESULT_OK) {

                    try{

                        JSONArray ticketSuccessArr = new JSONArray();
                        JSONArray ticketErrorArr = new JSONArray();
                        JSONArray resDataArr = new JSONArray();
                        Intent i = new Intent(ShoppingCartGiftListActivity.this, GiftcardCheckoutFileDownloadActivity.class);
                        switch (resultData.getInt("resultStatus")) {
                            case Constants.SHOPPING_CART_DOWNLOADING_SUCCESS:
                                generatingDlg.dismiss();
                                ticketSuccessArr = new JSONArray(resultData.getString("resultValueSuccess"));
                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));
//                                addNotification("Hi Dear, the checkout file download was completed.");
                                Toast.makeText(getApplicationContext(), "Generating completed", Toast.LENGTH_SHORT).show();

                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
                                i.putExtra("resDataArr", resDataArr.toString());
                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
                                startActivity(i);
                                finish();

                                break;
                            case Constants.SHOPPING_CART_GENERATING_SUCCESS:
                                checkoutDlg.dismiss();
                                createTicketGeneratingDlg();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_SUB_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if(generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                ticketSuccessArr = new JSONArray(resultData.getString("resultValueSuccess"));
                                ticketErrorArr = new JSONArray(resultData.getString("resultValueError"));
                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));

                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
                                i.putExtra("resDataArr", resDataArr.toString());
                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
                                startActivity(i);
                                finish();
                                Toast.makeText(getApplicationContext(), "Generating sub failed", Toast.LENGTH_SHORT).show();

                                break;
                            case Constants.SHOPPING_CART_GENERATING_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if(generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), "Generating failed", Toast.LENGTH_SHORT).show();
//                                ticketErrorArr = new JSONArray(resultData.getString("resultValueError"));
//                                resDataArr = new JSONArray(resultData.getString("resultValueResData"));
//                                i.putExtra("ticketSuccessArr", ticketSuccessArr.toString());
//                                i.putExtra("ticketErrorArr", ticketErrorArr.toString());
//                                i.putExtra("resDataArr", resDataArr.toString());
//                                i.putExtra("shoppingCartArr", shoppingCartArr.toString());
//                                startActivity(i);
//                                finish();

                                Toast.makeText(getApplicationContext(), "Generating failed", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_API_FAILED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if(generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                new PromptDialog(ShoppingCartGiftListActivity.this)
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
                                    Toast toast =  Toast.makeText(getApplicationContext(), "Item(s) creating", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else if(generatingDlg.isShowing()) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Generating item(s)", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();
                                }
                                break;
                            case Constants.SHOPPING_CART_SERVICE_EXCEPTION:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if(generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), resultData.getString("exceptionMessage"), Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.SHOPPING_CART_GENERATING_CANCELED:
                                if (checkoutDlg.isShowing()) {
                                    checkoutDlg.dismiss();
                                } else if(generatingDlg.isShowing()) {
                                    generatingDlg.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), "Checkout service canceled", Toast.LENGTH_SHORT).show();
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
        stopService(mServiceIntent);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Checkout cancel...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(ShoppingCartGiftListActivity.this);
                    receivedObj = APIInterface.checkoutCancel(obj,uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            checkoutBtn.setEnabled(true);
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {

                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    if (jsonData != null) {

                                        new PromptDialog(ShoppingCartGiftListActivity.this)
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
                                        ColorDialog dialog = new ColorDialog(getApplicationContext());
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
                                ColorDialog dialog = new ColorDialog(ShoppingCartGiftListActivity.this);
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

                    checkoutBtn.setEnabled(true);
                    Log.e("register error--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            ColorDialog dialog = new ColorDialog(ShoppingCartGiftListActivity.this);
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
    private void createCheckoutDlg(final JSONObject obj) {

        checkoutDlg = new Dialog(this);
        checkoutDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkoutDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkoutDlg.setContentView(R.layout.modal_checkout_layout);

        LinearLayout middleView = (LinearLayout) checkoutDlg.findViewById(R.id.checkout_middle_layout);
        TextView checkoutCode = (TextView) checkoutDlg.findViewById(R.id.checkout_ticketcode_text);
        TextView checkoutDescCode = (TextView) checkoutDlg.findViewById(R.id.checkout_main_text);
        ImageView qrcodeImg = (ImageView) checkoutDlg.findViewById(R.id.checkout_qrcode_img);
         MultiFormatWriter writer = new MultiFormatWriter();

        try {
            middleView.setBackgroundColor(Color.parseColor(obj.getString("visualIdentifierColor")));
            checkoutCode.setText(obj.getString("visualIdentifierCode"));
            checkoutCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(obj.getString("visualIdentifierColor"))));
            checkoutDescCode.setTextColor(AppHelper.getContrastColor(Color.parseColor(obj.getString("visualIdentifierColor"))));
            String finaldata = obj.getString("licenseCypherText");

            BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE,350, 350);
            Bitmap ImageBitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < 350; i++) {//width
                for (int j = 0; j < 350; j++) {//height
                    ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }

            if (ImageBitmap != null) {
                qrcodeImg.setImageBitmap(ImageBitmap);
            } else {
                Toast.makeText(getApplicationContext(), "QRCode image generating error", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(getApplicationContext(), "Checkout completed. Creating item(s)...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCheckoutStatus(obj);
            }
        }, 500);

        checkoutDlg.show();
        AndroidUtilities.setDialogLayoutParams(ShoppingCartGiftListActivity.this,checkoutDlg);
    }

    private void createTicketGeneratingDlg() {
        generatingDlg = new Dialog(this);
        generatingDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        generatingDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                generatingDlg.dismiss();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Item(s) created. Generating item(s)...", Toast.LENGTH_SHORT).show();
            }
        }, 200);
        generatingDlg.show();
        AndroidUtilities.setDialogLayoutParams(ShoppingCartGiftListActivity.this,generatingDlg);
    }

    private List<JSONObject> loadAllShoppingCartData() {
        f_totalPrice = 0.0f;
        shoppingCartArr = new AppHelper(this).getShoppingCartDataFromLocalGift();
        shoppingCartData = AppHelper.parseFromJsonList(shoppingCartArr);

        for (JSONObject obj : shoppingCartData) {
            try {
                double d_amount = obj.getDouble("Value");

                f_totalPrice += d_amount;

            } catch (Exception e) {
                Log.e("ShoppingCartGiftList-->", e.getMessage());
            }
        }
        totalPriceTxt.setText(String.format("Total : $%.2f", f_totalPrice));
        shoppingCartCountBtn.setText(String.valueOf(shoppingCartData.size()));

        if (!checkHelpOverlayState()) {
            createHelpOverlayView();
        }

        return shoppingCartData;
    }

    private boolean checkHelpOverlayState() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPref.getBoolean("shoppingCartHelpOverlayHasShown", false);
    }

    private boolean checkRetailerExistState() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPref.getBoolean("retailerExistFlag", false);
    }

    // create help overlay view
    private void createHelpOverlayView () {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_help_shoppingcart_layout);

        TextView helpTxt = (TextView) dialog.findViewById(R.id.modal_help_overlay);
        helpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ShoppingCartGiftListActivity.this.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("shoppingCartHelpOverlayHasShown", true);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addNotification(String message) {
        /*********** Create notification ***********/

        final NotificationManager mgr=
                (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification note=new Notification(R.mipmap.ic_launcher,
                message,
                System.currentTimeMillis());

        // This pending intent will open after notification click
        PendingIntent i=PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class),
                0);

        //After uncomment this line you will see number of notification arrived
        //note.number=2;
        mgr.notify(1337, note);
    }
}

