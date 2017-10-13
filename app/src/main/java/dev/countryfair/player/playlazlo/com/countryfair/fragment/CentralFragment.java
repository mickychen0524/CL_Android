package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import net.hockeyapp.android.FeedbackManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.LoyaltyActivity;
import dev.countryfair.player.playlazlo.com.countryfair.MainActivity;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.StoreLocatorActivity;
import dev.countryfair.player.playlazlo.com.countryfair.azuregcm.RegistrationService;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.ApiClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppDelegate;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.GeoLocationUtil;
import dev.countryfair.player.playlazlo.com.countryfair.model.BeaconsItem;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptData;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptOCRBody;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptOCRProduct;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptRequestBody;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptResponse;
import dev.countryfair.player.playlazlo.com.countryfair.service.GettingRetailerListService;
import dev.countryfair.player.playlazlo.com.countryfair.service.ServiceResultReceiver;
import mobi.windfall.receipt.IntentUtils;
import mobi.windfall.receipt.Media;
import mobi.windfall.receipt.Product;
import mobi.windfall.receipt.Retailer;
import mobi.windfall.receipt.ScanOptions;
import mobi.windfall.receipt.ScanResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to manage the central page of the 5 pages application navigation (top, center, bottom, left, right).
 */

public class CentralFragment extends Fragment {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = CentralFragment.class.getSimpleName();
    private ImageView ivLoyalty, ivStoreLocator;

    private TextView tvFeedback;
    private WebView homePage;

    private ViewPager pager;
    private MainActivity mCenterActivity;
    private ProgressDialog mProgressDialog;
    private Intent mServiceIntent;
    private ServiceResultReceiver mReceiverForRetailer;

    boolean loadingFinished = true;
    boolean redirect = false;

    private static int i_wasBorn = 30;
    private static String str_wasBorn = "";
    private String str_playerRegisterLisence = "";
    private JSONObject receivedObj;
    private FloatingActionButton mScanButton;
    private static final int SCAN_REQUEST_CODE = 777;
    private int currentRegister = 0;
    private int maxRegisterNumber = 5;
    private OnCentralFragmentInteractionListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_central, container, false);

        pager = (ViewPager) container;
        mCenterActivity = (MainActivity) getActivity();

        tvFeedback = (TextView) fragmentView.findViewById(R.id.tvFeedback);
        ivLoyalty = (ImageView) fragmentView.findViewById(R.id.ivLoyalty);
        ivStoreLocator = (ImageView) fragmentView.findViewById(R.id.ivStoreLocator);
        homePage = (WebView) fragmentView.findViewById(R.id.webview);
        mScanButton = (FloatingActionButton) fragmentView.findViewById(R.id.scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanActivity();
            }
        });


        homePage.getSettings().setUseWideViewPort(true);
        homePage.getSettings().setLoadWithOverviewMode(true);

        tvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedbackManager.showFeedbackActivity(getActivity());
            }
        });

        homePage.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                view.loadUrl(urlNewString);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mProgressDialog.setMessage("Home page loading...");
//				mProgressDialog.show();
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                loadingFinished = false;
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    loadingFinished = true;
                    checkUserRegisterState();
                }

                if (loadingFinished && !redirect) {
                    //HIDE LOADING IT HAS FINISHED
                } else {
                    redirect = false;
                }

            }
        });
        homePage.loadUrl("http://common.content.32point6.com/templates/cofairapplanding.html");

        ivLoyalty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoyaltyActivity.class));
            }
        });

        ivStoreLocator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), StoreLocatorActivity.class));
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (!sharedPref.getBoolean("socialConnectionImageDownload", false)) {
            downloadSocialConnectionImage();
        }

        if (checkUserAgeCertification()) {
            new Constants(getActivity());

            setupGeoAndRetailerService();

        } else {

            new Constants(getActivity());
        }

        return fragmentView;
    }

    private void setupGeoAndRetailerService() {
        // getting location service part
        final GeoLocationUtil geoLocationUtil = new GeoLocationUtil();
        final GeoLocationUtil.LocationResult geoLocationResult = new GeoLocationUtil.LocationResult() {
            @Override
            public void gotLocation(final Location location) {
                new Thread()
                {
                    public void run()
                    {
                        mCenterActivity.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                //Do your UI operations like dialog opening or Toast here
                                if (location != null) {
                                    Constants.GEO_LATITUDE = String.valueOf(location.getLatitude());
                                    Constants.GEO_LONGITUDE = String.valueOf(location.getLongitude());
                                    setupServiceReceiver();
                                    mServiceIntent = new Intent(getActivity(), GettingRetailerListService.class);
                                    mServiceIntent.putExtra("gettingStatus", true);
                                    mServiceIntent.putExtra("receiver", mReceiverForRetailer);
                                    getActivity().startService(mServiceIntent);
                                } else {
//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
                                            Toast.makeText(getActivity(), "Geo service is not working", Toast.LENGTH_SHORT).show();

//                                        }
//                                    });

                                }
                            }
                        });
                    }
                }.start();
            }
        };

        if (!geoLocationUtil.getLocation(getActivity(), geoLocationResult)) {
            Toast.makeText(getActivity(), "Geo service is not working", Toast.LENGTH_SHORT).show();
        }


    }

    private void setupServiceReceiver() {
        mReceiverForRetailer = new ServiceResultReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        mReceiverForRetailer.setReceiver(new ServiceResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {

                    switch (resultData.getInt("resultStatus")) {
                        case 1:
                            String retailerAddress = resultData.getString("retailerAddress");
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getActivity())
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("Country Fair")
                                            .setContentText("Play now at " + retailerAddress);
                            if (isAdded()) {
//                                Toast.makeText(getActivity(), "Play now at " + retailerAddress, Toast.LENGTH_SHORT).show();
                            }
                            Log.d("address", retailerAddress);
                            if (mListener != null) {
                                ArrayList<BeaconsItem> beacons = resultData.getParcelableArrayList(GettingRetailerListService.EXTRA_BEACONS);
                                mListener.startBeaconsDiscover(beacons);
                            }
                            break;
                        case 2:
                            if (isAdded()) {
//                                Toast.makeText(getActivity(), "retailer getting failed", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 33:
                            break;
                    }
                }
            }
        });
    }

    private boolean checkPlayerTokenIsEmpty() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (sharedPref.getString("playerLicenseCode", "").equals("")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkUserAgeCertification() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        return sharedPref.getBoolean("ageCertificationHasOccured", false);
    }

    private boolean checkUserSelfieCertification() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        return sharedPref.getBoolean("selfieHasBeenTaken", false);
    }

    private void checkUserRegisterState() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (sharedPref.getBoolean("userRegisterState", false)) {
            registerWithNotificationHubs();
        } else {
            Log.i("devoloTest", "checkUserRegisterState");
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Registering...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            addUnregisteredOverlay();
            registerUserWithSelfie("", mProgressDialog);
        }
    }

    // register user with user selfie or without user image
    private void registerUserWithSelfie(final String userSelfie, final ProgressDialog mProgressDialog) {
        Log.i("devoloTest", "registerUserWithSelfie");

        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(getActivity());
                    receivedObj = APIInterface.registerWithImage(i_wasBorn, userSelfie, uuid);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            if (receivedObj != null) {
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    str_playerRegisterLisence = jsonData.getString("playerLicenseCode");
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();

                                    editor.putString("playerLicenseCode", str_playerRegisterLisence);
                                    editor.putBoolean("ageCertificationHasOccured", true);
                                    editor.putString("age", String.valueOf(i_wasBorn));
                                    editor.putBoolean("userRegisterState", true);
                                    editor.putBoolean("selfieHasBeenTaken", true);
                                    editor.putString("userSelfieImage", userSelfie);
                                    editor.apply();

                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }

                                    new Constants(getActivity());
                                    registerWithNotificationHubs();
//									registerNotification();
                                    downloadSocialConnectionImage();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                Log.i("devoloTest", "else");
                                Log.i("devoloTest", currentRegister + " - " + maxRegisterNumber);
                                if (currentRegister < maxRegisterNumber) {
                                    Log.i("devoloTest", "currentRegister < maxRegisterNumber");
                                    registerUserWithSelfie(userSelfie, mProgressDialog);
                                    currentRegister++;
                                } else {
                                    Log.i("devoloTest", "currentRegister else maxRegisterNumber");
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("ageCertificationHasOccured", false);
                                    editor.putString("age", "");
                                    editor.putString("wasBorn", "");
                                    editor.apply();
                                    PromptDialog promptDialog = new PromptDialog(getActivity())
                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                            .setAnimationEnable(true)
                                            .setTitleText(Constants.ERROR_TITLE)
                                            .setContentText(Constants.ERROR_REGISTRATION)
                                            .setPositiveListener("Try again", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    mProgressDialog.show();
                                                    registerUserWithSelfie(userSelfie, mProgressDialog);
                                                    currentRegister = 0;
                                                }
                                            });
                                    promptDialog.setCancelable(false);
                                    promptDialog.setCanceledOnTouchOutside(false);
                                    promptDialog.show();
                                }
                            }

                        }
                    });
                } catch (final Exception e) {
                    Log.e("register error--->", e.getMessage());
                    Log.i("devoloTest", "error");

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("devoloTest", currentRegister + " - " + maxRegisterNumber);

                            if (currentRegister < maxRegisterNumber) {
                                Log.i("devoloTest", "currentRegister < maxRegisterNumber");

                                registerUserWithSelfie(userSelfie, mProgressDialog);
                                currentRegister++;
                            } else {
                                Log.i("devoloTest", "currentRegister else maxRegisterNumber");

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                String app_name = getActivity().getString(R.string.app_name);
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("ageCertificationHasOccured", false);
                                editor.putString("age", "");
                                editor.putString("wasBorn", "");
                                editor.apply();
                                PromptDialog promptDialog = new PromptDialog(getActivity())
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText(Constants.ERROR_TITLE)
                                        .setContentText(Constants.ERROR_MESSAGE)
                                        .setPositiveListener("Try again", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                mProgressDialog.show();

                                                registerUserWithSelfie(userSelfie, mProgressDialog);
                                                currentRegister = 0;
                                            }
                                        });
                                promptDialog.setCancelable(false);
                                promptDialog.setCanceledOnTouchOutside(false);
                                promptDialog.show();
                            }
                        }
                    });

                }
            }
        }).start();
    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
            Intent intent = new Intent(getActivity(), RegistrationService.class);
            getActivity().startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(getActivity(), "This device is not supported by Google Play Services.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    // register device notification on GCM
    private void registerNotification() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Notification Registering...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    FirebaseApp.initializeApp(getActivity());
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("deviceTokenForPush", refreshedToken);
                    editor.apply();
                    String uuid = AndroidUtilities.getUUID(getActivity());
                    receivedObj = APIInterface.registerPushNotification(refreshedToken, uuid);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    Toast.makeText(getActivity(), "Push Notification Registered", Toast.LENGTH_SHORT).show();
                                    downloadSocialConnectionImage();

                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), "Notification register error", Toast.LENGTH_SHORT).show();
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                Toast.makeText(getActivity(), "Notification register error", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "Notification register error", Toast.LENGTH_SHORT).show();
                            downloadSocialConnectionImage();
                        }
                    });
                }
            }
        }).start();
    }

    private void downloadSocialConnectionImage() {
        new Thread(new Runnable() {
            public void run() {
                try {

                    final Bitmap tempBmp = APIInterface.downloadSocialImage();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("socialConnectionImageDownload", true);
                            editor.apply();
                            if (tempBmp != null) {
                                writeSocialImageFile(tempBmp);
                                createSocialDialog(tempBmp);
                            } else {
                                Toast.makeText(getActivity(), "Social connection image is null", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Social connection image download failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    private void writeSocialImageFile(Bitmap bitmap) {
        try {
            final String localFilePathForSocialImage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + Constants.DIR_ROOT + "/" + Constants.FILE_SOCIALIMAGE + "/";
            FileOutputStream fOut = new FileOutputStream(new File(localFilePathForSocialImage));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createSocialDialog(Bitmap bmp) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_socialconnection_download_layout);

        Button doneBtn = (Button) dialog.findViewById(R.id.download_done);

        ImageView imgView = (ImageView) dialog.findViewById(R.id.social_connection_image);
        imgView.setImageBitmap(bmp);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addUnregisteredOverlay() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_help_mainview_layout);

        TextView helpTxt = (TextView) dialog.findViewById(R.id.modal_help_overlay);
        helpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("channelHelpOverlayHasShown", true);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void startScanActivity() {
        ScanOptions scanOptions = ScanOptions.newBuilder()
                .retailer(Retailer.UNKNOWN)
                .storeFrames(true)
                .useExternalStorage(true)
                .build();

        startActivityForResult(IntentUtils.cameraScan(getActivity(),
                scanOptions), SCAN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "SCAN DONE!");

                if (data.hasExtra(IntentUtils.MEDIA_EXTRA) && data.hasExtra(IntentUtils.DATA_EXTRA)) {
                    Media media = data.getParcelableExtra(IntentUtils.MEDIA_EXTRA);
                    ScanResults results = data.getParcelableExtra(IntentUtils.DATA_EXTRA);

                    if (media != null && results != null) {
                        getReceiptUploadUrl(media, results);
                    } else {
                        Toast.makeText(getContext(), "Failed to scan receipt. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                    Log.d(TAG, media.toString());
                }
            }
        }
    }

    private void getReceiptUploadUrl(Media media, final ScanResults results) {
        if (media != null && media.items().size() > 0) {
            final File file = media.items().get(0);
            ApiClient.getInstance(getContext()).getUploadUrl().enqueue(new Callback<ReceiptResponse>() {
                @Override
                public void onResponse(Call<ReceiptResponse> call, Response<ReceiptResponse> response) {
                    if (response.isSuccessful()) {
                        ReceiptData data = response.body().getData();
                        uploadImageToServer(file, response.body(), results);
                    }
                }

                @Override
                public void onFailure(Call<ReceiptResponse> call, Throwable t) {

                }
            });

        }
    }

    private void uploadImageToServer(File file, final ReceiptResponse receiptResponse, final ScanResults scanResults) {
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpg"), file);
        MultipartBody.Part body = MultipartBody.Part.create(reqFile);

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Lazlo-CorrelationRefId", receiptResponse.getCorrelationRefId());
        headersMap.put("Accept-Type", "application/json");

        String lat;
        String lng;

        //ReceiptRequestBody requestBody = new ReceiptRequestBody(receiptResponse.getData().getReceiptRefId(), scanResults, receiptResponse.getCorrelationRefId(), "", "");

        ApiClient.getInstance(getContext()).uploadReceiptImage(receiptResponse.getData().getSasUri(), headersMap, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "uploadImageToServer - onResponse - " + response.isSuccessful());
                uploadReceiptOCR(receiptResponse, scanResults);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "uploadImageToServer - onFailure - ");
                Toast.makeText(getContext(), "Failed to upload receipt. Please try again.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void uploadReceiptOCR(ReceiptResponse response, ScanResults results) {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Lazlo-CorrelationRefId", response.getCorrelationRefId());
        headersMap.put("Accept-Type", "application/json");

        Location location = AppDelegate.getInstance().getLocation();

        ReceiptOCRBody ocrBody = new ReceiptOCRBody();
        List<ReceiptOCRProduct> productList = new ArrayList<>();
        if (results.products() != null) {
            for (Product product : results.products()) {
                productList.add(new ReceiptOCRProduct(product.productDescription(), Math.abs(product.productQuantity()), Math.abs(product.productPrice())));
            }
        }
        ocrBody.setLineItems(productList);

        if (!"UNKNOWN".equals(results.retailerId().name())) {
            ocrBody.setRetailerId(results.retailerId().name());
        }
        ocrBody.setBrandId(results.registerId());
        ocrBody.setBrandName(results.storeName());
        if (results.purchaseDate() != null) {
            ocrBody.setCreatedOn(results.purchaseDate().toString());
        }

        ocrBody.setOcrRaw(null);

        if (location != null) {
            ReceiptRequestBody body = new ReceiptRequestBody(ocrBody, response.getCorrelationRefId(), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

            ApiClient.getInstance(getContext()).putReceiptOCR(headersMap, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "uploadReceiptOCR - onResponse - " + response.isSuccessful());
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Receipt uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "uploadReceiptOCR - onFailure - ");
                    Toast.makeText(getContext(), "Failed to upload receipt. Please try again.", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } else {
            Toast.makeText(getContext(), "Failed to upload receipt. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    public interface OnCentralFragmentInteractionListener {
        void startBeaconsDiscover(ArrayList<BeaconsItem> beacons);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCentralFragmentInteractionListener) {
            mListener = (OnCentralFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCentralFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
