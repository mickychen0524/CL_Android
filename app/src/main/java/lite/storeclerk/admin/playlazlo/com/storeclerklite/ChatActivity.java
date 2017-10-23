package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.NearByProtocol;


public class ChatActivity extends AppCompatActivity implements NearByProtocol.DiscoveryProtocol{

    private NearByUtil nearby;
    private ListView listView;
    private ArrayList<HashMap<String,String>> arrayList;
    private SimpleAdapter simpleAdapter;

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
        nearby = NearByUtil.getInstance();
        //nearby.init(this, Build.MANUFACTURER,"clerk");
        nearby.delegate = this;

        String[] from={"name"};//string array
        int[] to={R.id.textView};//int array of views id's
        listView = (ListView)findViewById(R.id.client_list);
        arrayList=new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,arrayList, project.labs.avviotech.com.chatsdk.R.layout.list_view_items,from,to);
        listView.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();




    }

    public void click()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Object[] keys = nearby.getPeerList().keySet().toArray();
                        String d = nearby.getPeerList().get(keys[i]).getAddress();
                        nearby.call(d);
                    }
                });

            }
        });
    }

    public void populateData()
    {

        HashMap<String,DeviceModel> clientList = nearby.getClientList();
        Log.i("Clerk", "populateData" + " - " + clientList.size());
        arrayList.clear();
        Iterator it = clientList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DeviceModel value = (DeviceModel)pair.getValue();
            HashMap<String,String> map = new HashMap<>();
            map.put("name", value.getName());
            arrayList.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
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
        nearby.stop();
    }

}
