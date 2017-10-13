package dev.countryfair.player.playlazlo.com.countryfair;

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
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.CardsListDataAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppStringHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by mymac on 3/21/17.
 */

public class CardsListActivity extends AppCompatActivity {

    private String termsStr = "";
    private float totalAmount = 0.0f;
    private String claimLicenseCodeStr = "";
    private JSONObject claimTicketItem = new JSONObject();
    private JSONObject receivedObj = new JSONObject();

    private List<JSONObject> cardsDataList = new ArrayList<>();

    private CardsListDataAdapter mAdapter;
    private SharedPreferences mSharedPref;
    private ProgressDialog mProgressDialog;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        totalAmount = getIntent().getFloatExtra("totalAmount", 0.0f);
        claimLicenseCodeStr = getIntent().getStringExtra("claimLicenseCode");
        try {
            claimTicketItem = new JSONObject(getIntent().getStringExtra("claimTicketData"));
        }catch (Exception e) {
            Log.e("json_error-->", e.getMessage());
        }

        setContentView(R.layout.cards_list_activity);
        TextView amountTxt = (TextView) findViewById(R.id.cards_list_amount_txt);
        amountTxt.setText(String.format(Locale.US, "Amount : $%.2f", totalAmount));

        mListView = (ListView) findViewById(R.id.cards_list_listview);
        mAdapter = new CardsListDataAdapter(CardsListActivity.this, new ArrayList<JSONObject>());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject cardItem = cardsDataList.get(position);
                createTermsDialog(cardItem);
            }
        });

        getAllCardsList();
    }

    public void back(View view) {
        goToTicketView();
    }

    private void goToTicketView() {
        Intent i = new Intent(this, TicketListActivity.class);
        startActivity(i);
        finish();
    }

    private void getAllCardsList() {
        try {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Getting cards...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        receivedObj = APIInterface.getAllMerchandies();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (receivedObj != null) {
                                    try {
                                        JSONObject jsonData = receivedObj.getJSONObject("data");
                                        cardsDataList = AppHelper.parseFromJsonList(jsonData.getJSONArray("merchandise"));
                                        mAdapter = new CardsListDataAdapter(CardsListActivity.this, cardsDataList);
                                        mListView.setAdapter(mAdapter);
                                        mAdapter.notifyDataSetChanged();
                                        getTermsStringFromServer();
                                    } catch (Exception e) {
                                        Log.d("json_e-->", e.getMessage());
                                    }

                                } else {
                                    new PromptDialog(CardsListActivity.this)
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
                    } catch (final Exception e) {
                        Log.e("register error--->", e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                new PromptDialog(CardsListActivity.this)
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

        } catch (Exception e) {

            Log.e("json_error-->", e.getMessage());
        }
    }

    private void getTermsStringFromServer() {
        try {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Getting terms...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        receivedObj = APIInterface.getAllTerms();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (receivedObj != null) {
                                    try {
                                        JSONObject jsonData = receivedObj.getJSONObject("data");
                                        termsStr = jsonData.getString("terms");
                                    } catch (Exception e) {
                                        Log.d("json_e-->", e.getMessage());
                                    }

                                } else {
                                    new PromptDialog(CardsListActivity.this)
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
                    } catch (final Exception e) {
                        Log.e("register error--->", e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                new PromptDialog(CardsListActivity.this)
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

        } catch (Exception e) {

            Log.e("json_error-->", e.getMessage());
        }
    }

    private void completeClaimWithMerchandiesData(final JSONObject merchandObj) {
        try {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Ticket claim...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String uuid = AndroidUtilities.getUUID(CardsListActivity.this);
                        receivedObj = APIInterface.ticketClaimComplete(merchandObj,totalAmount,claimLicenseCodeStr,uuid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (receivedObj != null) {
                                    try {

                                        JSONArray jsonData = receivedObj.getJSONArray("data");
                                        List<JSONObject> jsonDataList = AppHelper.parseFromJsonList(jsonData);
                                        if (jsonDataList.size() != 0) {
                                            createClaimDialog(jsonDataList.get(0));
                                        } else {
                                            createClaimDialog(new JSONObject());
                                        }
                                        if (!claimTicketItem.getBoolean("isClaimed")) {
                                            claimTicketItem.put("isClaimed", true);
                                            new AppHelper(CardsListActivity.this).updateOneTicketDataToLocal(claimTicketItem, 3);
                                        }

                                    } catch (Exception e) {
                                        Log.d("json_e-->", e.getMessage());
                                    }

                                } else {
                                    new PromptDialog(CardsListActivity.this)
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
                    } catch (final Exception e) {
                        Log.e("register error--->", e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                new PromptDialog(CardsListActivity.this)
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

        } catch (Exception e) {

            Log.e("json_error-->", e.getMessage());
        }
    }

    private void createTermsDialog(final JSONObject merchandiesData) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_validatioin_terms_layout);

        TextView termsTxt = (TextView) dialog.findViewById(R.id.validation_terms_modal_text);
        termsTxt.setMovementMethod(new ScrollingMovementMethod());
        final ImageView termsImage = (ImageView) dialog.findViewById(R.id.validation_terms_modal_image);

        try {
            termsTxt.setText(termsStr);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String urlStr = merchandiesData.getString("merchandiseImageUrl");
                        urlStr.replace("https://demolngcdn.blob.core.windows.net", "https://dev2lngmstrasia.blob.core.windows.net");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtilities.loadImage(termsImage,urlStr);
                            }

                        });
                    } catch (Exception e) {
                        Log.e("bmp_gettingerr-->", e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {

            Log.e("generating_errror--->", e.getMessage());
        }

        Button okBtn = (Button) dialog.findViewById(R.id.validation_terms_modal_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeClaimWithMerchandiesData(merchandiesData);
                dialog.dismiss();
            }
        });
        Button cancelBtn = (Button) dialog.findViewById(R.id.validation_terms_modal_cancel);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTicketView();
                dialog.dismiss();
            }
        });
        dialog.show();
        AndroidUtilities.setDialogLayoutParams(CardsListActivity.this,dialog);

    }

    private void createClaimDialog(final JSONObject merchandiesData) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_validatioin_claim_layout);

        TextView numberTxt = (TextView) dialog.findViewById(R.id.validation_claim_number_text);
        ImageView claimImage = (ImageView) dialog.findViewById(R.id.validation_claim_modal_image);

        try {
            Bitmap claimBmp = AppStringHelper.decodeBase64(merchandiesData.getString("merchandiseImageBase64").split(",")[1]);
            claimImage.setImageBitmap(claimBmp);
            numberTxt.setText(merchandiesData.getString("merchandiseCode"));
        } catch (Exception e) {

            Log.e("generating_errror--->", e.getMessage());
        }


        Button okBtn = (Button) dialog.findViewById(R.id.validation_claim_modal_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!claimTicketItem.getBoolean("isCompleted")) {
                        claimTicketItem.put("isCompleted", true);
                        new AppHelper(CardsListActivity.this).updateOneTicketDataToLocal(claimTicketItem, 1);
                    }
                } catch (Exception e) {
                    Log.e("json_error-->", e.getMessage());
                }
                goToTicketView();
                dialog.dismiss();
            }
        });
        dialog.show();
        AndroidUtilities.setDialogLayoutParams(CardsListActivity.this,dialog);
    }
}
