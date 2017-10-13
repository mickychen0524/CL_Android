package dev.countryfair.player.playlazlo.com.countryfair;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.squareup.otto.Subscribe;

import net.hockeyapp.android.FeedbackManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dev.countryfair.player.playlazlo.com.countryfair.azuregcm.MyHandler;
import dev.countryfair.player.playlazlo.com.countryfair.azuregcm.NotificationSettings;
import dev.countryfair.player.playlazlo.com.countryfair.event.EventBus;
import dev.countryfair.player.playlazlo.com.countryfair.event.PageChangedEvent;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.fragment.CentralFragment;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppDelegate;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.DbManager;
import dev.countryfair.player.playlazlo.com.countryfair.model.BeaconsItem;
import dev.countryfair.player.playlazlo.com.countryfair.model.db.DiscoveredBeacons;
import dev.countryfair.player.playlazlo.com.countryfair.service.SendBleDataService;
import dev.countryfair.player.playlazlo.com.countryfair.swipeviewer.VerticalPager;

public class MainActivity extends ShakeDetectorActivity implements CentralFragment.OnCentralFragmentInteractionListener,BeaconConsumer{

    private static final int CENTRAL_PAGE_INDEX = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public VerticalPager mVerticalPager;

    private BeaconManager beaconManager;
    private boolean isBond;
    private ArrayList<BeaconsItem> itemsToMonitor = null;
    private Set<BeaconsItem> discoveredItems = new HashSet<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        DbManager.clearExistingDataFlags(getApplicationContext());
        try {
            FeedbackManager.register(MainActivity.this, Constants.HOCKEY_APP_ID);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this.getApplicationContext(), "Hockey App ID seems invalid!", Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.activity_main);
        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        findViews();
        displaySocialScanIfRequired();
        beaconManager.bind(this);
        SendBleDataService.startSend(this);
    }

    private void displaySocialScanIfRequired(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPref.getBoolean("shouldDisplaySocialScan", true)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("shouldDisplaySocialScan", false);
            editor.apply();
            startActivity(new Intent(MainActivity.this,ScanPromptActivity.class));
        }
    }

    private void findViews() {
        mVerticalPager = (VerticalPager) findViewById(R.id.activity_main_vertical_pager);
        initViews();
    }

    private void initViews() {
        snapPageWhenLayoutIsReady(mVerticalPager, CENTRAL_PAGE_INDEX);
    }

    private void snapPageWhenLayoutIsReady(final View pageView, final int page) {
		/*
		 * VerticalPager is not fully initialized at the moment, so we want to snap to the central page only when it
		 * layout and measure all its pages.
		 */
        pageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                mVerticalPager.snapToPage(page, VerticalPager.PAGE_SNAP_DURATION_INSTANT);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                    // recommended removeOnGlobalLayoutListener method is available since API 16 only
                    pageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else
                    removeGlobalOnLayoutListenerForJellyBean(pageView);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private void removeGlobalOnLayoutListenerForJellyBean(final View pageView) {
                pageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    protected void onPause() {
        EventBus.getInstance().unregister(this);
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Subscribe
    public void onLocationChanged(PageChangedEvent event) {
        mVerticalPager.setPagingEnabled(event.hasVerticalNeighbors());
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()){
                AndroidUtilities.hideKeyboard(MainActivity.this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public void startBeaconsDiscover(ArrayList<BeaconsItem> beacons) {
        if (isBond){
            itemsToMonitor = beacons;
            discoveredItems.clear();
            if (beacons==null||beacons.size()==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,R.string.beacons_list_empty,Toast.LENGTH_LONG).show();
                    }
                });

            }
            if (beacons!=null){
                for (BeaconsItem item:beacons){
                    try {
                        beaconManager.startRangingBeaconsInRegion(new Region(item.getBeaconId(),null,null,null));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopBeaconDiscover();
        super.onDestroy();
    }

    private void stopBeaconDiscover() {
        if (isBond) {
            beaconManager.unbind(this);
            isBond = false;
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        isBond = true;
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (itemsToMonitor!=null) {
                    for (Beacon b : collection) {
                        if (b.getId2() != null && b.getId3() != null) {
                            int major = b.getId2().toInt();
                            int minor = b.getId3().toInt();
                            if (BuildConfig.USE_TEMP_VALUES){
                                major = 4;
                                minor = 26;
                            }
                            for (BeaconsItem item:itemsToMonitor){
                                if (item.getMajor()==major&&item.getMinor()==minor){
                                    beaconFound(item,b);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void beaconFound(BeaconsItem item, Beacon b) {
        DiscoveredBeacons beacon = new DiscoveredBeacons(item.getBeaconRefId(),
                item.getMajor(),item.getMinor(),b.getRssi(),b.getDistance(),getProximity(b.getDistance()),System.currentTimeMillis());
        ((AppDelegate)getApplication()).getDaoSession().getDiscoveredBeaconsDao().insert(beacon);
        if (!isFinishing()) {
            if (!discoveredItems.contains(item)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,R.string.beacon_discovered,Toast.LENGTH_SHORT).show();
                    }
                });
                discoveredItems.add(item);
            }
        }
    }

    private String getProximity(double distance) {
        if (distance<0.5d)
            return "immediate";
        else if (distance<3.0d)
            return "near";
        else
            return "far";
    }
}
