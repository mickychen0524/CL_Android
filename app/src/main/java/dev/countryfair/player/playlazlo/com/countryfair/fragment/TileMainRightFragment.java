package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.ShoppingCartListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.TicketListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.ChannelGroupListDataAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.DbManager;
import dev.countryfair.player.playlazlo.com.countryfair.helper.GeoLocationUtil;
import dev.countryfair.player.playlazlo.com.countryfair.helper.GettingJSONFromLocal;
import dev.countryfair.player.playlazlo.com.countryfair.helper.MarshMallowPermission;
import dev.countryfair.player.playlazlo.com.countryfair.service.GettingRetailerListService;
import dev.countryfair.player.playlazlo.com.countryfair.service.ServiceResultReceiver;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to manage the right page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class TileMainRightFragment extends Fragment {

	// -----------------------------------------------------------------------
	//
	// Methods
	//
	// -----------------------------------------------------------------------

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	private static final int CAMERA_REQUEST = 1888;
	MarshMallowPermission marshMallowPermission;

	protected LocationManager locationManager;

	private static final int RESULT_REQUEST_RECORD_AUDIO = 0;
	private static final String TAG = TileMainRightFragment.class.getSimpleName();

	private JSONObject receivedObj;

	private List<JSONObject> gameData = new ArrayList<>();
	private List<JSONObject> brandData = new ArrayList<>();
	private List<JSONObject> prizeData = new ArrayList<>();
	private List<JSONObject> channelGroupData = new ArrayList<>();
	private List<JSONObject> shoppingCartItemArr = new ArrayList<>();

	private static int i_wasBorn = 0;
	private static String str_wasBorn = "";
	private String str_playerRegisterLisence = "";
	private boolean prizeSaveFlg = false;
	private int fullDataCount = 0;
	private int imageFileDownloadCount = 0;

	private ProgressDialog mProgressDialog;
	private TextView lblTicketBadgeCount;
	private TextView lblShoppingCartBadgeCount;
	private RecyclerView mainChannelDataListView;
	private SwipeRefreshLayout swipeContainer;
	private AppHelper helper;

	private ChannelGroupListDataAdapter channelAdapter;
	Timer timer = new Timer();

	private Intent mServiceIntent;
	private ServiceResultReceiver mReceiverForRetailer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_tile_main_right, container, false);
		this.marshMallowPermission = new MarshMallowPermission(getActivity());

		lblTicketBadgeCount = (TextView) fragmentView.findViewById(R.id.badge_ticket_count);
		lblShoppingCartBadgeCount = (TextView) fragmentView.findViewById(R.id.badge_shoppingcart_count);

		helper = new AppHelper(getActivity());

		mainChannelDataListView = (RecyclerView) fragmentView.findViewById(R.id.main_channelgroup_list_view);
		channelAdapter = new ChannelGroupListDataAdapter(getActivity(), new ArrayList<JSONObject>(), new ArrayList<JSONObject>(), new ArrayList<JSONObject>());
		mainChannelDataListView.setAdapter(channelAdapter);
		mainChannelDataListView.setHasFixedSize(true);
		mainChannelDataListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		setupToolbar(fragmentView);

		if (!checkDemoOrRealData()) {
			gettingJsonFromLocal();
		} else {
			getAllGameList();
		}
		setupGeoAndRetailerService();

		return fragmentView;
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
//					setupServiceReceiver();//TODO: error
					mServiceIntent = new Intent(getActivity(), GettingRetailerListService.class);
					mServiceIntent.putExtra("gettingStatus", true);
					mServiceIntent.putExtra("receiver", mReceiverForRetailer);
					getActivity().startService(mServiceIntent);
				}
				else{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getActivity(), "Geo service is not working", Toast.LENGTH_SHORT).show();

						}
					});				}
			}
		};
		if(!geoLocationUtil.getLocation(getActivity(),geoLocationResult)){
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

					switch(resultData.getInt("resultStatus")) {
						case 1:
							break;
						case 2:
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

	private boolean checkDemoOrRealData() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		return sharedPref.getBoolean("realDataFlag", true);
	}

	private boolean checkHelpOverlayState() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		return sharedPref.getBoolean("channelHelpOverlayHasShown", false);
	}

	private void getAllGameList () {

		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mProgressDialog.setMessage("Getting Games...");
		if(!DbManager.hasExistingData(getActivity().getApplicationContext(),"games")){mProgressDialog.show();};
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		new Thread(new Runnable() {
			public void run() {
				try {
					receivedObj = APIInterface.getGames();
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {

							if (mProgressDialog.isShowing()) {
								mProgressDialog.dismiss();
							}

							if (receivedObj != null) {

								try {
									JSONArray jsonArr = receivedObj.getJSONArray("data");
									gameData = cacheAndReplaceGameImageData(AppHelper.parseFromJsonList(jsonArr));
									Constants.gameGlobalArr = gameData;
									getAllBrandslst();
								} catch (JSONException e) {
									Log.d("json_e-->", e.getMessage());
								}
							} else {
								Log.e("game_list--->", "receivedObj is null");
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

						}
					});
				} catch (Exception e) {
					Log.e("game_list--->", e.getMessage());
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
			}
		}).start();
	}

	private void getAllBrandslst() {

		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mProgressDialog.setMessage("Getting Brands...");
		if(!DbManager.hasExistingData(getActivity().getApplicationContext(),"brands")){mProgressDialog.show();};
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		new Thread(new Runnable() {
			public void run() {
				try {
					receivedObj = APIInterface.getChannelAndBrands();
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {

							if (mProgressDialog.isShowing()) {
								mProgressDialog.dismiss();
							}

							if (receivedObj != null) {
								List<JSONObject> subItemsInGroup = new ArrayList<> ();
								try {
									JSONArray jsonArr = receivedObj.getJSONArray("data");
									brandData = AppHelper.parseFromJsonList(jsonArr);

									channelGroupData.clear();
									for (JSONObject obj : brandData) {
										JSONArray subGroupArr = obj.getJSONArray("channelGroups");
										subItemsInGroup = AppHelper.parseFromJsonList(subGroupArr);
										Collections.sort(subItemsInGroup, new Comparator<JSONObject>() {
											@Override
											public int compare(JSONObject o1, JSONObject o2) {
												try {
													String v1 = o1.getString("priority");
													String v2 = o1.getString("priority");
													return v1.compareTo(v2);
												} catch (Exception e) {
													Log.d("json_e-->", e.getMessage());
												}
												return 0;
											}
										});

										for (JSONObject channelObj : subItemsInGroup) {
											String brandRefId = obj.getString("brandRefId");
											channelObj.put("brandRefId", brandRefId);
											channelGroupData.add(channelObj);
										}
									}

									Constants.channelGroupGlobalArr = channelGroupData;

									cacheAndReplaceChannelImageData(channelGroupData);

									if (!checkHelpOverlayState()) {
										createHelpOverlayView();
									}

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

						}
					});
				} catch (Exception e) {
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
			}
		}).start();
	}

	private List<JSONObject> cacheAndReplaceGameImageData(List<JSONObject> jsonArr) {

		String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/";
		if (!(new File(localFilePath).exists())) {
			new File(localFilePath).mkdir();
		}

		List<JSONObject> gameArr = new ArrayList<>();
		for (JSONObject obj : jsonArr) {
			try {
				String fileUrlStr = obj.getString("logoUrl");
				String fileName = fileUrlStr.substring(fileUrlStr.lastIndexOf('/') + 1);
				Ion.with(getActivity())
						.load(fileUrlStr)
						.progress(new ProgressCallback() {
							@Override
							public void onProgress(long downloaded, long total) {
								Log.d(TAG,"" + downloaded + " / " + total);
							}
						})
						.write(new File(localFilePath + fileName))
						.setCallback(new FutureCallback<File>() {
							@Override
							public void onCompleted(Exception e, File file) {
								// download done...
							}
						});

				obj.put("logoUrl", fileName);

				gameArr.add(obj);
			} catch (Exception e) {
				Log.e("MainAct-->", e.getMessage());
			}

		}
		return gameArr;
	}

	// store all images and gifs data onto local storage and replace image url with new one
	private void cacheAndReplaceChannelImageData(List<JSONObject> jsonArr) {
		fullDataCount += jsonArr.size();
		for (JSONObject channelGroupItem : jsonArr) {
			try {
				String fileUrlStr = channelGroupItem.getString("channelGroupLogoUrl");
				String fileName = fileUrlStr.substring(fileUrlStr.lastIndexOf('/') + 1);
				downloadFileFromServer(fileUrlStr, new JSONObject(), channelGroupItem, fileName, 1);

				List<JSONObject> channelData = new ArrayList<>();
				channelData = AppHelper.parseFromJsonList(channelGroupItem.getJSONArray("channels"));

				for (JSONObject channelDataItem : channelData){
					fullDataCount++;
					JSONObject ticketTemplate = channelDataItem.getJSONObject("ticketTemplate");
					String ticketTemplateTileUrl = ticketTemplate.getString("tileUrl");
					String ticketTemplateFileName = ticketTemplateTileUrl.substring(ticketTemplateTileUrl.lastIndexOf('/') + 1);
					downloadFileFromServer(ticketTemplateTileUrl, channelDataItem, new JSONObject(), ticketTemplateFileName, 2);
				}

			} catch (Exception e) {
				Log.e("MainAct-->", e.getMessage());
			}

		}

		for (JSONObject obj : jsonArr) {
			try {
				List<JSONObject> channelData = new ArrayList<>();
				channelData = AppHelper.parseFromJsonList(obj.getJSONArray("channels"));

				for (JSONObject channelDataItem : channelData){
					fullDataCount++;
					JSONObject ticketTemplate = channelDataItem.getJSONObject("ticketTemplate");
					String ticketTemplateTileAnimatedUrl = ticketTemplate.getString("tileAnimatedUrl");
					String ticketTemplateAnimatedFileName = ticketTemplateTileAnimatedUrl.substring(ticketTemplateTileAnimatedUrl.lastIndexOf('/') + 1);
					downloadFileFromServer(ticketTemplateTileAnimatedUrl, channelDataItem, new JSONObject(), ticketTemplateAnimatedFileName, 3);
				}

			} catch (Exception e) {
				Log.e("MainAct-->", e.getMessage());
			}

		}
	}

	// image download process include all logo, gif, brand and game images from remote to "Download" foler of android device

	/**
	 *
	 * @param fileUrl : image file url from remote
	 * @param chItem : channel item JSONObject include this image
	 * @param chGroupItem : channelGroupItem item JSONObject include this image
	 * @param fileName : file name will be downloaded from server
	 * @param type : image type(1: game logo image, 2: static image, 3 : gif image)
	 */
	private void downloadFileFromServer(String fileUrl, final JSONObject chItem, final JSONObject chGroupItem, final String fileName, final int type) {

		String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/";
		if (!(new File(localFilePath).exists())) {
			new File(localFilePath).mkdir();
		}
		Ion.with(getActivity())
				.load(fileUrl)
				.progress(new ProgressCallback() {
					@Override
					public void onProgress(long downloaded, long total) {
						Log.d(TAG, "" + downloaded + " / " + total);
					}
				})
				.write(new File(localFilePath + fileName))
				.setCallback(new FutureCallback<File>() {
					@Override
					public void onCompleted(Exception e, File file) {
						// download done...
						imageFileDownloadCount++;
						switch (type) {
							case 1:
								List<JSONObject> newChannelGroupData = new ArrayList<>();
								for (JSONObject obj : channelGroupData) {
									try {
										if (chGroupItem.getString("channelGroupRefId").equals(obj.get("channelGroupRefId"))) {
											obj.put("channelGroupLogoUrl", fileName);
											obj.put("localSavingState", true);
										}
									} catch (Exception jsonE) {
										Log.e("MainAct-->", jsonE.getMessage());
									}
									newChannelGroupData.add(obj);
								}
								channelGroupData = new ArrayList<>();
								channelGroupData = newChannelGroupData;
								Constants.channelGroupGlobalArr = newChannelGroupData;

								break;
							case 2:
								List<JSONObject> newChannelG2Data = new ArrayList<>();
								List<JSONObject> channel2Data = new ArrayList<>();

								for (JSONObject obj : channelGroupData) {
									try {
										channel2Data = AppHelper.parseFromJsonList(obj.getJSONArray("channels"));
										for (JSONObject objChannel : channel2Data) {

											JSONObject ticketTemplate = objChannel.getJSONObject("ticketTemplate");
											if (chItem.getString("channelRefId").equals(objChannel.get("channelRefId"))) {
												ticketTemplate.put("tileUrl", fileName);
												objChannel.put("ticketTemplate", ticketTemplate);
												objChannel.put("localSavingStateStatic", true);
											}
										}

									} catch (Exception jsonE) {
										Log.e("MainAct-->", jsonE.getMessage());
									}
									newChannelG2Data.add(obj);
								}
								channelGroupData = new ArrayList<>();
								channelGroupData = newChannelG2Data;
								Constants.channelGroupGlobalArr = newChannelG2Data;
								break;
							case 3:
								List<JSONObject> newChannelG3Data = new ArrayList<>();
								List<JSONObject> channel3Data = new ArrayList<>();

								for (JSONObject obj : channelGroupData) {
									try {
										channel3Data = AppHelper.parseFromJsonList(obj.getJSONArray("channels"));
										for (JSONObject objChannel : channel3Data) {

											JSONObject ticketTemplate = objChannel.getJSONObject("ticketTemplate");
											if (chItem.getString("channelRefId").equals(objChannel.get("channelRefId"))) {
												ticketTemplate.put("tileAnimatedUrl", fileName);
												objChannel.put("ticketTemplate", ticketTemplate);
												objChannel.put("localSavingState", true);
											}
										}

									} catch (Exception jsonE) {
										Log.e("MainAct-->", jsonE.getMessage());
									}
									newChannelG3Data.add(obj);
								}
								channelGroupData = new ArrayList<>();
								channelGroupData = newChannelG3Data;
								Constants.channelGroupGlobalArr = newChannelG3Data;
								break;
							default:
								break;
						}

						if (imageFileDownloadCount == fullDataCount) {
							imageFileDownloadCount = 0;
							fullDataCount = 0;
							channelAdapter = new ChannelGroupListDataAdapter(getActivity(), gameData, brandData, channelGroupData);
							mainChannelDataListView.setAdapter(channelAdapter);
							channelAdapter.notifyDataSetChanged();
						}

					}
				});
	}

	private void setupToolbar(View fragmentView) {

		ImageButton btnShoppingCart = (ImageButton) fragmentView.findViewById(R.id.shopping_cart_btn);
		btnShoppingCart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (new AppHelper(getActivity()).getShoppingCartDataFromLocal().length() == 0) {
					Toast.makeText(getActivity(), "Add items to your shopping cart!", Toast.LENGTH_SHORT).show();
				} else {
					Intent i = new Intent(getActivity(), ShoppingCartListActivity.class);
					startActivity(i);
				}

			}
		});

		ImageButton btnHelpOverlay = (ImageButton) fragmentView.findViewById(R.id.help_overlay_btn);
		btnHelpOverlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				createHelpOverlayView();

			}
		});

		ImageButton btnTicketList = (ImageButton) fragmentView.findViewById(R.id.ticket_list_btn);
		btnTicketList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (new AppHelper(getActivity()).getTicketDataFromLocal().length() == 0) {
					Toast.makeText(getActivity(), "Oops! You don't have any tickets to view.", Toast.LENGTH_SHORT).show();
				} else {
					Intent i = new Intent(getActivity(), TicketListActivity.class);
					startActivity(i);
				}
			}
		});
	}

	private void gettingJsonFromLocal() {
		String jsonStr = GettingJSONFromLocal.loadJSONFromAsset(getActivity(), "gameData.json");
		try {
			JSONObject obj = new JSONObject(jsonStr);
			JSONArray m_jArry = obj.getJSONArray("data");
			ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> m_li;

			for (int i = 0; i < m_jArry.length(); i++) {
				JSONObject jo_inside = m_jArry.getJSONObject(i);
				Log.d("Details-->", jo_inside.getString("gameName"));
				String formula_value = jo_inside.getString("gameName");
				String url_value = jo_inside.getString("logoUrl");

				//Add your values in your `ArrayList` as below:
				m_li = new HashMap<String, String>();
				m_li.put("formule", formula_value);
				m_li.put("url", url_value);

				formList.add(m_li);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// create help overlay view
	private void createHelpOverlayView () {
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

	// android timer to confirm badge count
	private void setBadgeCount() {
		timer = new Timer();
		timer.schedule(new BadgeCountSetTask(), 0, 500);
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
		if (helper.getShoppingCartDataFromLocal().length() == 0) {
			lblShoppingCartBadgeCount.setVisibility(View.GONE);
		} else {
			lblShoppingCartBadgeCount.setVisibility(View.VISIBLE);
			int i_totalCount = 0;
			JSONArray shopArr = helper.getShoppingCartDataFromLocal();
			List<JSONObject> shopDataList = AppHelper.parseFromJsonList(shopArr);
			for (JSONObject obj : shopDataList) {
				try {
					i_totalCount += obj.getInt("panelCount");
					lblShoppingCartBadgeCount.setText(String.valueOf(i_totalCount));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		if (helper.getTicketDataFromLocal().length() == 0) {
			lblTicketBadgeCount.setVisibility(View.GONE);
		} else {
			lblTicketBadgeCount.setVisibility(View.VISIBLE);
			lblTicketBadgeCount.setText(String.valueOf(helper.getTicketDataFromLocal().length()));
		}
	}
}
