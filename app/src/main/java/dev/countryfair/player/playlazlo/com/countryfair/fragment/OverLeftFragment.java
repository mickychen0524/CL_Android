package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.CardsListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.ShoppingCartGiftListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.ShoppingCartListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.CardsListDataAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.GiftcardsListDataAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.DbManager;

/**
 * Fragment to manage the left page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class OverLeftFragment extends Fragment {

	private static final String TAG = "OverLeftFragment";

	private String claimLicenseCodeStr = "";
	private JSONObject claimTicketItem = new JSONObject();
	private JSONObject receivedObj = new JSONObject();

	private List<JSONObject> cardsDataList = new ArrayList<>();

	private GiftcardsListDataAdapter mAdapter;
	private SharedPreferences mSharedPref;
	private ProgressDialog mProgressDialog;
	private ListView mListView;
	private SwipeRefreshLayout swipe_container;

	private TextView lblShoppingCartBadgeCount;
	private AppHelper helper;
	Timer timer = new Timer();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_over_left, container, false);


		lblShoppingCartBadgeCount = (TextView) fragmentView.findViewById(R.id.badge_shoppingcart_count);
		swipe_container = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_container);
		mListView = (ListView) fragmentView.findViewById(R.id.giftcard_list_listview);
		mAdapter = new GiftcardsListDataAdapter(getActivity(), new ArrayList<JSONObject>());
		mListView.setAdapter(mAdapter);
		setupPullToRefresh();
//		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				JSONObject cardItem = cardsDataList.get(position);
//				createTermsDialog(cardItem);
//			}
//		});

		helper = new AppHelper(getActivity().getApplicationContext());
		setupToolbar(fragmentView);
		getAllCardsList();
		return fragmentView;
	}

	private void setupPullToRefresh(){
		swipe_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
												 @Override
												 public void onRefresh() {
													 Log.d(TAG, "onRefresh: ");
													 getAllCardsList();
												 }
											 });

		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition =
						(mListView == null || mListView.getChildCount() == 0) ?
								0 : mListView.getChildAt(0).getTop();
					swipe_container.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
			}
		});
	}

	private void setupToolbar(View fragmentView) {
		ImageButton btnShoppingCart = (ImageButton) fragmentView.findViewById(R.id.shopping_cart_btn);
		btnShoppingCart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (helper.getShoppingCartDataFromLocalGift().length() == 0) {
					Toast.makeText(getActivity(), "Add items to your shopping cart!", Toast.LENGTH_SHORT).show();
				} else {
					Intent i = new Intent(getActivity(), ShoppingCartGiftListActivity.class);
					startActivity(i);
				}

			}
		});

	}


	private void getAllCardsList() {
		try {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mProgressDialog.setMessage("Getting cards...");
			if(!DbManager.hasExistingData(getActivity().getApplicationContext(),"cards")){mProgressDialog.show();}
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			new Thread(new Runnable() {
				public void run() {
					try {
						receivedObj = APIInterface.getAllMerchandies();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {

								if (mProgressDialog.isShowing()) {
									mProgressDialog.dismiss();
								}

								if (receivedObj != null) {
									try {
										JSONObject jsonData = receivedObj.getJSONObject("data");
										cardsDataList = AppHelper.parseFromJsonList(jsonData.getJSONArray("merchandise"));
										mAdapter = new GiftcardsListDataAdapter(getActivity(), cardsDataList);
										mListView.setAdapter(mAdapter);
										mAdapter.notifyDataSetChanged();
									} catch (Exception e) {
										Log.d("json_e-->", e.getMessage());
									}

								} else {
									new PromptDialog(getActivity())
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
								swipe_container.setRefreshing(false);
							}
						});
					} catch (final Exception e) {
						Log.e("register error--->", e.getMessage());
						if(isAdded()){
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (mProgressDialog.isShowing()) {
										mProgressDialog.dismiss();
									}
									new PromptDialog(getActivity())
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
						swipe_container.setRefreshing(false);
					}
				}
			}).start();


		} catch (Exception e) {

			Log.e("json_error-->", e.getMessage());
		}
	}

	// android timer to confirm badge count
	private void setBadgeCount() {
		timer = new Timer();
		timer.schedule(new OverLeftFragment.BadgeCountSetTask(), 0, 500);
	}

	// badge set timer class
	//tells handler to send a message
	class BadgeCountSetTask extends TimerTask {

		@Override
		public void run() {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
				updateCount();
				}
			});
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		setBadgeCount();
	}

	@Override
	public void onPause() {
		super.onPause();
		timer.cancel();
		timer.purge();
	}

	private void updateCount(){
		if (helper.getShoppingCartDataFromLocalGift().length() == 0) {
			lblShoppingCartBadgeCount.setVisibility(View.GONE);
		} else {
			lblShoppingCartBadgeCount.setVisibility(View.VISIBLE);
			JSONArray shopArr = helper.getShoppingCartDataFromLocalGift();
			List<JSONObject> shopDataList = AppHelper.parseFromJsonList(shopArr);
			lblShoppingCartBadgeCount.setText(String.valueOf(shopDataList.size()));
		}
	}



}
