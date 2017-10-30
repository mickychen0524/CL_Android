package lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.etsy.android.grid.util.DynamicHeightTextView;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.R;
import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;

public class ChatAdapter extends ArrayAdapter<String> {

    private static final String TAG = "SampleAdapter";
    private NearByUtil nearby;

    static class ViewHolder {
        DynamicHeightTextView txtLineOne;
        Button btnGo;
    }

    private final LayoutInflater mLayoutInflater;
    private final Random mRandom;


    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    public ChatAdapter(final Context context, final int textViewResourceId) {
        super(context, textViewResourceId);
        mLayoutInflater = LayoutInflater.from(context);
        mRandom = new Random();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.chat_list, parent, false);
            vh = new ViewHolder();
            vh.txtLineOne = (DynamicHeightTextView) convertView.findViewById(R.id.txt_name);
            vh.btnGo = (Button) convertView.findViewById(R.id.user_icon);

            vh.btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Object[] keys = nearby.getPeerList().keySet().toArray();
                            String d = nearby.getPeerList().get(keys[position]).getAddress();
                            nearby.call(d);
                        }
                    });
                }
            });

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder) convertView.getTag();
        }

        double positionHeight = getPositionRatio(position);
        Log.d(TAG, "getView position:" + position + " h:" + positionHeight);
        vh.txtLineOne.setText(getItem(position));



        return convertView;
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
            Log.d(TAG, "getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5 the width
    }

    public NearByUtil getNearby() {
        return nearby;
    }

    public void setNearby(NearByUtil nearby) {
        this.nearby = nearby;
    }
}
