package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by Android Developer on 20/08/17.
 */

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.ItemHolder> implements Filterable{

    private Context context;
    private List<JSONObject> storeListOrig = new ArrayList<JSONObject>();
    private List<JSONObject> storeList = new ArrayList<JSONObject>();


    public class ItemHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvAddress;
        public TextView tvMiles;

        public ItemHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
            tvMiles = (TextView) itemView.findViewById(R.id.tvMiles);
        }
    }


    public StoreListAdapter() {
    }

    public StoreListAdapter(Activity activity, List<JSONObject> storeList) {
        this.context = activity;
        this.storeListOrig = storeList;
        this.storeList = storeList;

    }

    public List<JSONObject> getStoreList() {
        return storeListOrig;
    }

    public void setStoreList(List<JSONObject> storeList) {
        this.storeListOrig = storeList;
        this.storeList = storeList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_retailer, parent, false);
        ItemHolder holder = new ItemHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, final int position) {
        final JSONObject store = storeList.get(position);
        try {
            holder.tvName.setText(store.getString("retailerName"));
            holder.tvAddress.setText(getAddress(store));
            holder.tvMiles.setText(getDistance(store));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public String getAddress(JSONObject store){

        String address = "";
        try {
            String addressLine1 = store.getString("addressLine1");
            String addressLine2 = store.getString("addressLine2");
            String addressCity = store.getString("addressCity");
            String addressStateProvince = store.getString("addressStateProvince");
            String addressCounty = store.getString("addressCounty");
            String addressZipPostalCode = store.getString("addressZipPostalCode");
            String addressCountryCode = store.getString("addressCountryCode");

            address = addIfNotEmpty(address,addressLine1);
            address = addIfNotEmpty(address,addressLine2);
            address = addIfNotEmpty(address,addressCity);
            address = addIfNotEmpty(address,addressStateProvince);
            address = addIfNotEmpty(address,addressCounty);
            address = addIfNotEmpty(address,addressZipPostalCode);
            address = addIfNotEmpty(address,addressCountryCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(address.endsWith(", ")){
            address = address.substring(0,address.length()-2);
        }

        return address;
    }

    public String addIfNotEmpty(String originalValue,String extraValue){
        if(extraValue!=null && extraValue!="null"){
            originalValue = originalValue + extraValue + ", ";
        }
        return originalValue;
    }

    public String getDistance(JSONObject store){

        String distance = "";
        try {
            JSONObject addressLocation = store.getJSONObject("addressLocation");
            JSONArray cordinates = addressLocation.getJSONArray("coordinates");
            String storeName = store.getString("retailerName");
            LatLng latLng = new LatLng(cordinates.getDouble(1),cordinates.getDouble(0));
            Location currentLocation = new Location("gps");
            currentLocation.setLatitude(Double.parseDouble(Constants.GEO_LATITUDE));
            currentLocation.setLongitude(Double.parseDouble(Constants.GEO_LONGITUDE));
            Location storeLocation = new Location("gps");
            storeLocation.setLatitude(cordinates.getDouble(1));
            storeLocation.setLongitude(cordinates.getDouble(0));
            double miles = AndroidUtilities.getMiles(currentLocation.distanceTo(storeLocation));
            distance = context.getResources().getString(R.string.count_miles,miles);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distance;

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                storeList = (List<JSONObject>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<JSONObject> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = storeListOrig;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }


    private List<JSONObject> getFilteredResults(String constraint) {
        List<JSONObject> results = new ArrayList<>();

        for (JSONObject item : storeListOrig) {
            String retailerName = "";
            try {
                retailerName = item.getString("retailerName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (retailerName.toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }
        return results;
    }

}
