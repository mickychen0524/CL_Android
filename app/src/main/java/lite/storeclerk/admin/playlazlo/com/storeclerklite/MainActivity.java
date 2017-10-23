package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import net.hockeyapp.android.FeedbackManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter.MainGameListAdapter;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.azuregcm.MyHandler;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.azuregcm.NotificationSettings;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.azuregcm.RegistrationService;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AppHelper;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.Constants;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.GeoLocationUtil;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.service.GettingRetailerListService;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.service.ServiceResultReceiver;
import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.NearByProtocol;

public class MainActivity extends AppCompatActivity implements NearByProtocol.DiscoveryProtocol{
    private int APP_REQUEST_CODE = 99;
    private ProgressDialog mProgressDialog;
    private JSONObject receivedObj;
    private List<JSONObject> gameData = new ArrayList<JSONObject>();
    private MainGameListAdapter mAdapter;
    private ListView gameListView;
    private Button registerBtn;
    private Button reportBottomBtn;
    private Button checkoutBottomBtn;
    private Button claimBottomBtn;
    private Button refundBottomBtn;
    private Button chatBottomBtn;
    private Button activateBtn;
    private SwipeRefreshLayout swipeContainer;
    private NearByUtil nearby;

    /* put this into your activity class */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    // service section for get retailer and beacon stuff
    private Intent mServiceIntent;
    private ServiceResultReceiver mReceiverForRetailer;

    // fingerprint identify section
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();

        GeoLocationUtil geoLocationUtil = new GeoLocationUtil();
        GeoLocationUtil.LocationResult geoLocationResult = new GeoLocationUtil.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if(location!=null){
                    Constants.GEO_LATITUDE = String.valueOf(location.getLatitude());
                    Constants.GEO_LONGITUDE = String.valueOf(location.getLongitude());
                }
                else{
                    Toast.makeText(MainActivity.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
                }
            }
        };
        if(!geoLocationUtil.getLocation(MainActivity.this,geoLocationResult)){
            Toast.makeText(MainActivity.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
        }
        try {
            FeedbackManager.register(MainActivity.this, Constants.HOCKEY_APP_ID);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this.getApplicationContext(), "Hockey App ID seems invalid!", Toast.LENGTH_SHORT).show();
        }

        gameListView = (ListView) findViewById(R.id.main_view_list);
        mAdapter = new MainGameListAdapter(MainActivity.this, gameData);
        gameListView.setAdapter(mAdapter);

        registerBtn = (Button) findViewById(R.id.main_register_btn);
        activateBtn = (Button) findViewById(R.id.main_activate_btn);
        reportBottomBtn = (Button) findViewById(R.id.main_bottom_barchart_btn);
        checkoutBottomBtn = (Button) findViewById(R.id.main_bottom_checkout_btn);
        claimBottomBtn = (Button) findViewById(R.id.main_bottom_claim_btn);
        refundBottomBtn = (Button) findViewById(R.id.main_bottom_refund_btn);
        chatBottomBtn = (Button) findViewById(R.id.main_bottom_chat_btn);

        init();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ActivateActivity.class);
                startActivity(i);
                finish();
            }
        });

        chatBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                startActivity(intent);
            }
        });

        reportBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ReportChartActivity.class);
                startActivity(i);
                finish();
            }
        });

        checkoutBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
                // or higher before executing any fingerprint-related code

                Intent i = new Intent(MainActivity.this, CheckoutActivity.class);
                startActivity(i);
                finish();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    //Get an instance of KeyguardManager and FingerprintManager//
//                    keyguardManager =
//                            (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//                    fingerprintManager =
//                            (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
//
//                    //Check whether the device has a fingerprint sensor//
//                    if (!fingerprintManager.isHardwareDetected()) {
//                        // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
//                        Intent i = new Intent(MainActivity.this, CheckoutActivity.class);
//                        startActivity(i);
//                        finish();
//                    }
//                    //Check whether the user has granted your app the USE_FINGERPRINT permission//
//                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
//                        // If your app doesn't have this permission, then display the following text//
//                        Toast.makeText(MainActivity.this, "Please enable the fingerprint permission", Toast.LENGTH_SHORT).show();
//                    }
//
//                    //Check that the user has registered at least one fingerprint//
//                    if (!fingerprintManager.hasEnrolledFingerprints()) {
//                        // If the user hasn’t configured any fingerprints, then display the following message//
//                        Toast.makeText(MainActivity.this, "No fingerprint configured. Please register at least one fingerprint in your device's Settings", Toast.LENGTH_SHORT).show();
//                        Intent i = new Intent(MainActivity.this, CheckoutActivity.class);
//                        startActivity(i);
//                        finish();
//                    }
//                }
            }
        });

        claimBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
                // or higher before executing any fingerprint-related code

                Intent i = new Intent(MainActivity.this, ClaimActivity.class);
                startActivity(i);
                finish();
            }
        });

        refundBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
                // or higher before executing any fingerprint-related code

                Intent i = new Intent(MainActivity.this, RefundActivity.class);
                startActivity(i);
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
                if (sharedPref.getBoolean("registerState", false)) {
                    if (sharedPref.getBoolean("activateState", false)) {
                        if (sharedPref.getBoolean("loginState", false)) {
                            AccountKit.logOut();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("loginState", false);
                            editor.apply();
                            initStates();
                        } else {
                            Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
                            AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                                    AccountKitActivity.ResponseType.CODE);
                            intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
                            startActivityForResult(intent, APP_REQUEST_CODE);
                        }
                    }
                } else {
                    if (sharedPref.getBoolean("loginState", false)) {
                        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
                        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                                AccountKitActivity.ResponseType.CODE);
                        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
                        startActivityForResult(intent, APP_REQUEST_CODE);
                    }
                }
            }
        });

        initStates();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_refresh_view);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                gameData = new ArrayList<>();
                swipeContainer.setRefreshing(false);
                getAllGameList();

            }
        });
        setupGeoAndRetailerService();
        getAllGameList();



    }

    private void initStates() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        if (sharedPref.getBoolean("activateState", false)) {
            activateBtn.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
            if (sharedPref.getBoolean("loginState", false) && sharedPref.getBoolean("registerState", false)) {
                registerBtn.setText("Sign Out");
            } else {
                if (sharedPref.getBoolean("registerState", false)) {
                    registerBtn.setText("Sign In");
                } else {
                    registerBtn.setText("Register");
                }
            }
        } else {
            if (sharedPref.getBoolean("loginState", false) && sharedPref.getBoolean("registerState", false)) {
                activateBtn.setVisibility(View.VISIBLE);
                registerBtn.setVisibility(View.GONE);
            } else {
                activateBtn.setVisibility(View.GONE);
                registerBtn.setVisibility(View.VISIBLE);
            }
            if (sharedPref.getBoolean("registerState", false)) {
                registerBtn.setText("Sign In");
            } else {
                registerBtn.setText("Register");
            }
        }

        if (sharedPref.getBoolean("activateState", false) && sharedPref.getBoolean("loginState", false) && sharedPref.getBoolean("registerState", false)) {
            checkoutBottomBtn.setVisibility(View.VISIBLE);
            claimBottomBtn.setVisibility(View.VISIBLE);
            reportBottomBtn.setVisibility(View.VISIBLE);
            refundBottomBtn.setVisibility(View.VISIBLE);
        } else {
            checkoutBottomBtn.setVisibility(View.GONE);
            claimBottomBtn.setVisibility(View.GONE);
            reportBottomBtn.setVisibility(View.GONE);
            refundBottomBtn.setVisibility(View.GONE);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    private void setupGeoAndRetailerService() {
        // getting location service part

        GeoLocationUtil geoLocationUtil = new GeoLocationUtil();
        GeoLocationUtil.LocationResult geoLocationResult = new GeoLocationUtil.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if(location!=null){
                    Constants.GEO_LATITUDE = String.valueOf(location.getLatitude());
                    Constants.GEO_LONGITUDE = String.valueOf(location.getLongitude());
                    setupServiceReceiver();
                    mServiceIntent = new Intent(MainActivity.this, GettingRetailerListService.class);
                    mServiceIntent.putExtra("gettingStatus", true);
                    mServiceIntent.putExtra("receiver", mReceiverForRetailer);
                    MainActivity.this.startService(mServiceIntent);
                }
                else{
                    Toast.makeText(MainActivity.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
                }
            }
        };
        if(!geoLocationUtil.getLocation(MainActivity.this,geoLocationResult)){
            Toast.makeText(MainActivity.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupServiceReceiver() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceiverForRetailer = new ServiceResultReceiver(new Handler());
                // This is where we specify what happens when data is received from the service
                mReceiverForRetailer.setReceiver(new ServiceResultReceiver.Receiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == RESULT_OK) {

                            switch(resultData.getInt("resultStatus")) {
                                case 1:
                                    String retailerAddress = resultData.getString("retailerAddress");
                                    NotificationCompat.Builder mBuilder =
                                            new NotificationCompat.Builder(MainActivity.this)
                                                    .setSmallIcon(R.mipmap.ic_launcher)
                                                    .setContentTitle("Store Clerk Lite")
                                                    .setContentText("Play now at " + retailerAddress);
                                    Toast.makeText(MainActivity.this, "Play now at " + retailerAddress, Toast.LENGTH_SHORT).show();
                                    Log.d("address", retailerAddress);
                                    break;
                                case 2:
                                    Toast.makeText(MainActivity.this, "retailer getting failed", Toast.LENGTH_SHORT).show();

                                    break;
                                case 33:
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }
    private void getAllGameList () {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Getting Games...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    receivedObj = APIInterface.getAllGameList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {

                                try {
                                    JSONArray jsonArr = receivedObj.getJSONArray("data");
                                    gameData = AppHelper.parseFromJsonList(jsonArr);
                                    Constants.gameGlobalArr = gameData;

                                    mAdapter = new MainGameListAdapter(MainActivity.this, gameData);
                                    gameListView.setAdapter(mAdapter);
                                    mAdapter.notifyDataSetChanged();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }
                            } else {
                                Log.e("game_list--->", "receivedObj is null");
                                new PromptDialog(MainActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Get Game Error")
                                        .setContentText("Oops! Our services are not responding, try back later.")
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
                    Log.e("game_list--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            new PromptDialog(MainActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Get Game Error")
                                    .setContentText("Oops! Our services are not responding, try back later.")
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
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage = "";
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                this.verifyFBAuthCode(loginResult.getAuthorizationCode());
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void verifyFBAuthCode(final String authCode) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Login...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {

                    receivedObj = APIInterface.verifyFBCode(authCode);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                Log.i("DevoloTest", "receivedObj:" + receivedObj);

                                try {
                                    String token = receivedObj.getString("data");
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("loginState", true);
                                    editor.putString("fbAuthenticationCode",authCode);
                                    editor.putString("fbAccessToken",token);
//                                    editor.putString("hockeyAppIdAndroid", )
                                    editor.apply();


                                    if (!sharedPref.getBoolean("registerState", false)) {
                                        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                                        startActivity(i);
                                        finish();
                                    }

                                    initStates();

                                } catch (JSONException e) {
                                    Log.d("json_e-->", e.getMessage());
                                }
                            } else {
                                Log.e("login error--->", "receivedObj is null");
                                new PromptDialog(MainActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Login Error")
                                        .setContentText("Oops! Our services are not responding, try back later.")
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
                    Log.e("login error--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            new PromptDialog(MainActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Login Error")
                                    .setContentText("Oops! Our services are not responding, try back later.")
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

    public void clerkImageBtnAction(View v) {
        // createInsertLicenseCodeDlg()
        Intent i = new Intent(MainActivity.this, InactiveUsersActivity.class);
        startActivity(i);
        finish();
    }

    // create insert license code view
    private void createInsertLicenseCodeDlg () {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_main_activity_license_layout);

        final EditText helpTxt = (EditText) dialog.findViewById(R.id.main_modal_license_txt);
        Button saveBtn = (Button) dialog.findViewById(R.id.main_modal_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("playerToken",helpTxt.getText().toString());
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter
            if (mAccel > 12) {
                Intent i = new Intent(MainActivity.this, CheckoutActivity.class);
                startActivity(i);
                finish();
            }

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationService.class);
            startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(this, "This device is not supported by Google Play Services.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        nearby.start();
    }

    public void init()
    {
        nearby = NearByUtil.getInstance();
        nearby.init(this, getPhoneName(),"clerk");
        nearby.delegate = this;
    }

    @Override
    public void onPeersFound(HashMap<String, DeviceModel> devices) {

    }

    @Override
    public void onDisconnect() {

    }
    public String getPhoneName()
    {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        return deviceName;
    }


}
