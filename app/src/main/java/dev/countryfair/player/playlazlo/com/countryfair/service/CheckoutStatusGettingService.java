package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by mymac on 3/17/17.
 */

public class CheckoutStatusGettingService extends IntentService {
    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */

    public static boolean isStoped = false;
    public static List<JSONObject> shoppingCartData;

    private ResultReceiver mResultReceiver;
    private ProgressDialog mProgressDialog;
    private JSONObject receivedObj;
    private JSONArray checkoutStatusData;

    private boolean b_runningGetStatus = true;
    private boolean b_ticketGeneratingFlg = false;
    private boolean b_checkoutbtnPressed = false;

    private int i_loopCount = 0;
    private int i_getStatusErrorloopCount = 0;

    private String sessionStr = "";
    private String licenseCypherText = "";

    public CheckoutStatusGettingService() {
        super("HelloIntentService");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String jsonStr = intent.getStringExtra("receivedObj");
        mResultReceiver = intent.getParcelableExtra("receiver");
        try {
            JSONObject receivedObj = new JSONObject(jsonStr);
            checkoutStatusGetting(receivedObj);
        } catch (Exception e) {
            stopSelf();
            Bundle bundle = new Bundle();
            bundle.putInt("resultStatus", Constants.SHOPPING_CART_SERVICE_EXCEPTION);
            bundle.putString("exceptionMessage", e.getMessage());
            mResultReceiver.send(Activity.RESULT_OK, bundle);
            Log.e("jsonparsing--->", e.getMessage());
        }

        b_runningGetStatus = true;
        return super.onStartCommand(intent,flags,startId);
    }

    private void checkoutStatusGetting(JSONObject obj) {

        if (isStoped) {
            stopSelf();
            Bundle bundle = new Bundle();
            bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_CANCELED);
            mResultReceiver.send(Activity.RESULT_OK, bundle);
        } else {
            final JSONObject checkoutRes = obj;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        sessionStr = checkoutRes.getString("checkoutSessionRefId");
                        licenseCypherText = checkoutRes.getString("licenseCypherText");
                        System.out.println(licenseCypherText);
                        //TODO: Push Skip not sure
                        receivedObj = APIInterface.getCheckoutStatusSession(checkoutRes);

                        b_checkoutbtnPressed = false;

                        if (receivedObj != null) {
                            try {
                                JSONArray jsonArr = receivedObj.getJSONArray("data");

                                // check received data length is 0 or not : generating or creating

                                if (jsonArr.length() == 0) {
                                    b_ticketGeneratingFlg = false;

                                    // if the length is 0, the thread will go on until get tickets list

                                    if (i_loopCount < 50) {
                                        Thread.sleep(2000);
                                        checkoutStatusGetting(checkoutRes);
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_LOOP);
                                        mResultReceiver.send(Activity.RESULT_OK, bundle);
                                        i_loopCount++;
                                    } else {
                                        i_loopCount = 0;

                                        JSONArray savingTicketErrorArr = new JSONArray();
                                        for (JSONObject shopObj : shoppingCartData) {
                                            JSONObject checkItem = new JSONObject();
                                            String fDate = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
                                            checkItem.put("gameData", shopObj);
                                            checkItem.put("ticketDownloadDate", fDate);
                                            checkItem.put("checkoutSessionRefId", sessionStr);
                                            checkItem.put("licenseCypherText", licenseCypherText);
                                            checkItem.put("isValid", false);
                                            checkItem.put("isCompleted", false);
                                            checkItem.put("isClaimed", false);
                                            checkItem.put("channelRefId", shopObj.getString("channelRefId"));

                                            savingTicketErrorArr.put(checkItem);
                                        }

                                        stopSelf();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_FAILED);
                                        bundle.putString("resultValueResData", jsonArr.toString());
                                        bundle.putString("resultValueSuccess", "");
                                        bundle.putString("resultValueError", savingTicketErrorArr.toString());
                                        mResultReceiver.send(Activity.RESULT_OK, bundle);
                                    }
                                } else {

                                    // ticket generating is successed but not completed until get SASS url from ticket

                                    if (!b_ticketGeneratingFlg) { // is generating or ticket creating
                                        b_ticketGeneratingFlg = true;
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_SUCCESS);
                                        mResultReceiver.send(Activity.RESULT_OK, bundle);
                                    }

                                    boolean b_sasUriFlg = true;
                                    JSONArray newResData = new JSONArray();
                                    List<JSONObject> resDataList = AppHelper.parseFromJsonList(jsonArr);

                                    for (JSONObject checkItem : resDataList) {

                                        if (!checkItem.isNull("sasUri")) {
                                            b_sasUriFlg = true;
                                        } else {
                                            b_sasUriFlg = false;
                                            break;
                                        }

                                        String strImageName = checkItem.getString("ticketRefId");
                                        switch (checkItem.getInt("ticketTemplateType")) {
                                            case 1:
                                                checkItem.put("fileName", strImageName + ".ticket.lazlo.jpg");
                                                break;
                                            case 2:
                                                checkItem.put("fileName", strImageName + ".ticket.lazlo.mp4");
                                                break;
                                            case 3:
                                                checkItem.put("fileName", strImageName + ".ticket.lazlo.mp4");
                                                break;
                                        }
                                        newResData.put(checkItem);
                                    }
                                    if (b_sasUriFlg) {
                                        JSONArray saveTicketArr = new JSONArray();

                                        for (JSONObject checkItem : resDataList) {
                                            String returnedChannelRefId = checkItem.getString("channelRefId");

                                            for (JSONObject channelGroupItem : Constants.channelGroupGlobalArr) {

                                                List<JSONObject> channelsList = AppHelper.parseFromJsonList(channelGroupItem.getJSONArray("channels"));

                                                for (JSONObject channelItem : channelsList) {
                                                    if (channelItem.getString("channelRefId").equals(returnedChannelRefId)) {
                                                        JSONObject ticketTemplateData = channelItem.getJSONObject("ticketTemplate");
                                                        String gameRefId = channelGroupItem.getString("gameRefId");
                                                        for (JSONObject gameItem : Constants.gameGlobalArr) {
                                                            if (gameItem.getString("gameRefId").equals(gameRefId)) {
                                                                channelItem.put("gameRefId", gameRefId);
                                                                channelItem.put("gameLogoUrl", gameItem.getString("logoUrl"));
                                                                channelItem.put("animatedState", true);
                                                                channelItem.put("tileUrl", ticketTemplateData.getString("tileUrl"));
                                                                channelItem.put("animatedTileUrl", ticketTemplateData.getString("tileAnimatedUrl"));
                                                                break;
                                                            }
                                                        }
                                                        checkItem.put("gameData", channelItem);
                                                    }
                                                }
                                            }

                                            String fDate = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
                                            checkItem.put("ticketDownloadDate", fDate);
                                            checkItem.put("checkoutSessionRefId", sessionStr);
                                            checkItem.put("licenseCypherText", licenseCypherText);
                                            checkItem.put("isValid", false);
                                            checkItem.put("isCompleted", false);
                                            checkItem.put("isClaimed", false);

                                            if (!checkItem.isNull("sasUri")) {
                                                saveTicketArr.put(checkItem);
                                            }
                                        }
                                        stopSelf();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("resultStatus", Constants.SHOPPING_CART_DOWNLOADING_SUCCESS);
                                        bundle.putString("resultValueResData", jsonArr.toString());
                                        bundle.putString("resultValueSuccess", saveTicketArr.toString());
                                        bundle.putString("resultValueError", new JSONArray().toString());
                                        mResultReceiver.send(Activity.RESULT_OK, bundle);

                                    } else {
                                        if (i_loopCount < 15) {
                                            Thread.sleep(2000);
                                            checkoutStatusGetting(checkoutRes);
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_LOOP);
                                            mResultReceiver.send(Activity.RESULT_OK, bundle);
                                            i_loopCount++;
                                        } else {
                                            i_loopCount = 0;

                                            JSONArray saveTicketArr = new JSONArray();
                                            JSONArray saveTicketErrorArr = new JSONArray();

                                            for (JSONObject checkItem : resDataList) {
                                                String returnedChannelRefId = checkItem.getString("channelRefId");

                                                for (JSONObject channelGroupItem : Constants.channelGroupGlobalArr) {

                                                    List<JSONObject> channelsList = AppHelper.parseFromJsonList(channelGroupItem.getJSONArray("channels"));

                                                    for (JSONObject channelItem : channelsList) {
                                                        if (channelItem.getString("channelRefId").equals(returnedChannelRefId)) {
                                                            JSONObject ticketTemplateData = channelItem.getJSONObject("ticketTemplate");
                                                            String gameRefId = channelGroupItem.getString("gameRefId");
                                                            for (JSONObject gameItem : Constants.gameGlobalArr) {
                                                                if (gameItem.getString("gameRefId").equals(gameRefId)) {
                                                                    channelItem.put("gameRefId", gameRefId);
                                                                    channelItem.put("gameLogoUrl", gameItem.getString("logoUrl"));
                                                                    channelItem.put("animatedState", true);
                                                                    channelItem.put("tileUrl", ticketTemplateData.getString("tileUrl"));
                                                                    channelItem.put("animatedTileUrl", ticketTemplateData.getString("tileAnimatedUrl"));
                                                                    break;
                                                                }
                                                            }
                                                            checkItem.put("gameData", channelItem);
                                                        }
                                                    }
                                                }

                                                String fDate = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US).format(new Date());
                                                checkItem.put("ticketDownloadDate", fDate);
                                                checkItem.put("checkoutSessionRefId", sessionStr);
                                                checkItem.put("licenseCypherText", licenseCypherText);
                                                checkItem.put("isValid", false);
                                                checkItem.put("isCompleted", false);
                                                checkItem.put("isClaimed", false);

                                                if (!checkItem.isNull("sasUri")) {
                                                    saveTicketArr.put(checkItem);
                                                } else {
                                                    saveTicketErrorArr.put(checkItem);
                                                }
                                            }
                                            stopSelf();
                                            Bundle bundle = new Bundle();
                                            bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_SUB_FAILED);
                                            bundle.putString("resultValueResData", jsonArr.toString());
                                            bundle.putString("resultValueSuccess", saveTicketArr.toString());
                                            bundle.putString("resultValueError", saveTicketErrorArr.toString());
                                            mResultReceiver.send(Activity.RESULT_OK, bundle);
                                        }
                                    }

                                }
                            } catch (Exception e) {
                                Log.e("getting st error--->", e.getMessage());
                                stopSelf();
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultStatus", Constants.SHOPPING_CART_SERVICE_EXCEPTION);
                                bundle.putString("exceptionMessage", e.getMessage());
                                mResultReceiver.send(Activity.RESULT_OK, bundle);
                            }

                        } else {
                            try {
                                // if the length is 0, the thread will go on until get tickets list
                                if (i_getStatusErrorloopCount < 5) {
                                    Thread.sleep((i_getStatusErrorloopCount + 1) * 1000);
                                    checkoutStatusGetting(checkoutRes);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_LOOP);
                                    mResultReceiver.send(Activity.RESULT_OK, bundle);
                                    i_getStatusErrorloopCount++;
                                } else {
                                    i_getStatusErrorloopCount = 0;

                                    stopSelf();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("resultStatus", Constants.SHOPPING_CART_GENERATING_API_FAILED);
                                    mResultReceiver.send(Activity.RESULT_OK, bundle);
                                }
                            } catch (Exception e) {
                                stopSelf();
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultStatus", Constants.SHOPPING_CART_SERVICE_EXCEPTION);
                                bundle.putString("exceptionMessage", e.getMessage());
                                mResultReceiver.send(Activity.RESULT_OK, bundle);
                                Log.e("getting st error--->", e.getMessage());
                            }
                        }

                    } catch (Exception e) {
                        stopSelf();
                        Bundle bundle = new Bundle();
                        bundle.putInt("resultStatus", Constants.SHOPPING_CART_SERVICE_EXCEPTION);
                        bundle.putString("exceptionMessage", e.getMessage());
                        mResultReceiver.send(Activity.RESULT_OK, bundle);
                        Log.e("getting st error--->", e.getMessage());

                    }
                }
            }).start();
        }

    }

    @Override
    public boolean stopService(Intent name) {
        stopSelf();
        return super.stopService(name);
    }
}
