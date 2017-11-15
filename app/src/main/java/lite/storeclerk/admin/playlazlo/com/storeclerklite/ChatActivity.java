package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter.ChatAdapter;
import project.labs.avviotech.com.chatsdk.CallActivity;
import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.client.Client;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.NearByProtocol;


public class ChatActivity extends AppCompatActivity implements NearByProtocol.DiscoveryProtocol{

    private NearByUtil nearby;
    private StaggeredGridView mGridView;
    private ChatAdapter mAdapter;
    private Button backButton;
    private final int RECORD_AUDIO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
        click();
        populateData();

    }

    public void init()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }

        nearby = NearByUtil.getInstance(this,Build.MANUFACTURER,"clerk");
        nearby.delegate = this;
        nearby.setActivity(this);

        mGridView = (StaggeredGridView) findViewById(R.id.grid_view);
        mAdapter = new ChatAdapter(this, R.id.txt_name);

        mGridView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mAdapter.setNearby(nearby);

        backButton = (Button) findViewById(R.id.chat_back);

        populateData();

    }

    public void click()
    {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                finish();
                startActivity(intent);
            }
        });

    }

    public void populateData()
    {

        HashMap<String,DeviceModel> clientList = nearby.getClientList();
        if(clientList != null)
        {
            Log.i("Clerk", "populateData" + " - " + clientList.size());
            mAdapter.clear();
            Iterator it = clientList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                DeviceModel value = (DeviceModel)pair.getValue();
                HashMap<String,String> map = new HashMap<>();
                map.put("name", value.getName());
                mAdapter.add(value.getName());
            }
            if(clientList.size() > 0)
            {
                nearby.playSound();
            }
            else
            {
                nearby.stopSound();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPeersFound(HashMap<String, DeviceModel> devices) {
        populateData();
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        nearby.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

}
