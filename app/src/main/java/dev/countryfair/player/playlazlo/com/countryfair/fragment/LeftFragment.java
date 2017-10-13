package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.GiftcardDownloadedListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.TicketListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.event.CouponListInvalidateEvent;
import dev.countryfair.player.playlazlo.com.countryfair.event.EventBus;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Fragment to manage the left page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class LeftFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{

	private static final String TAG = "LeftFragment";

	private List<JSONObject> couponDataList = new ArrayList<>();
	private List<JSONObject> giftcardDataList = new ArrayList<>();
	private JSONArray couponDataArr = new JSONArray();
	private JSONArray giftcardDataArr = new JSONArray();
	private JSONObject receivedObj = new JSONObject();

	private GiftcardDownloadedListAdapter mAdapter;
	private SharedPreferences mSharedPref;
	private SegmentedGroup segmented;
	private ProgressDialog mProgressDialog;
	private ListView mListView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_left, container, false);
		mListView = (ListView) fragmentView.findViewById(R.id.giftcard_list_listview);
		mAdapter = new GiftcardDownloadedListAdapter(getActivity(), loadAllGiftData());
		mListView.setAdapter(mAdapter);

		mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putBoolean("ticketRecentFlag", true);
		editor.apply();

		segmented = (SegmentedGroup) fragmentView.findViewById(R.id.giftcard_list_segmented);
		segmented.setOnCheckedChangeListener(this);
		mListView.setAdapter(mAdapter);
		return fragmentView;
	}
	private List<JSONObject> loadAllGiftData() {
		giftcardDataList = new ArrayList<>();
		giftcardDataArr = new AppHelper(getActivity()).getRedeemCardJSONDataFromLocal();
		giftcardDataList = AppHelper.parseFromJsonList(giftcardDataArr);
		return giftcardDataList;
	}

	private List<JSONObject> loadAllCouponData() {
		couponDataList = new ArrayList<>();
		couponDataArr = new AppHelper(getActivity()).getCouponJSONDataFromLocal();
		couponDataList = AppHelper.parseFromJsonList(couponDataArr);
		return couponDataList;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.giftcard_seg_gift:
				mAdapter = new GiftcardDownloadedListAdapter(getActivity(), loadAllGiftData());
				mAdapter.notifyDataSetChanged();
				mListView.setAdapter(mAdapter);
				break;
			case R.id.giftcard_seg_coupons:
				mAdapter = new GiftcardDownloadedListAdapter(getActivity(), loadAllCouponData());
				mAdapter.notifyDataSetChanged();
				mListView.setAdapter(mAdapter);
				break;
			default:
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		EventBus.getInstance().unregister(this);
	}

	@Subscribe
	public void onInvalidateCoupons(CouponListInvalidateEvent event){
		Log.d(TAG, "onInvalidateCoupons: ");
		if(segmented.getCheckedRadioButtonId()==R.id.giftcard_seg_coupons){
			mAdapter = new GiftcardDownloadedListAdapter(getActivity(), loadAllCouponData());
			mAdapter.notifyDataSetChanged();
			mListView.setAdapter(mAdapter);
		}
	}
}
