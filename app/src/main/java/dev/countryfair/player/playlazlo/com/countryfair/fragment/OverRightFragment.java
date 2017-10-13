package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.refactor.lib.colordialog.ColorDialog;
import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.TicketListActivity;
import dev.countryfair.player.playlazlo.com.countryfair.TicketValidationActivity;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.TicketListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AeSimpleSHA1;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppStringHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Fragment to manage the left page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class OverRightFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{

	static private final int TICKET_CLAIM_STATE  = 1;
	static private final int TICKET_DELETE_STATE = 2;

	private List<JSONObject> ticketDataList = new ArrayList<>();
	private List<JSONObject> subTicketDataList = new ArrayList<>();
	private JSONArray ticketDataArr = new JSONArray();
	private JSONObject receivedObj = new JSONObject();

	private TicketListAdapter mAdapter;
	private SharedPreferences mSharedPref;
	private SegmentedGroup segmented;
	private ProgressDialog mProgressDialog;
	private ListView mListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_over_right, container, false);
		mListView = (ListView) fragmentView.findViewById(R.id.ticket_list_listview);
		mAdapter = new TicketListAdapter(getActivity(), loadAllTicketData());
		mListView.setAdapter(mAdapter);

		mAdapter.setOnTicketClaimListener(new TicketListAdapter.OnTicketClaimListener() {
			@Override
			public void onTicketClaimed(int position) {

				JSONObject ticketItem = ticketDataList.get(position);
				validationTicketWithData(ticketItem, TICKET_CLAIM_STATE);
			}
		});

		mAdapter.setOnTicketDeleteListener(new TicketListAdapter.OnTicketDeleteListener() {
			@Override
			public void onTicketDeleted(int position) {

				JSONObject ticketItem = ticketDataList.get(position);
				validationTicketWithData(ticketItem, TICKET_DELETE_STATE);
			}
		});

		mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putBoolean("ticketRecentFlag", true);
		editor.apply();

		segmented = (SegmentedGroup) fragmentView.findViewById(R.id.ticket_list_segmented);
		segmented.setOnCheckedChangeListener(this);
		mListView.setAdapter(mAdapter);
		return fragmentView;
	}
	private List<JSONObject> loadAllTicketData() {
		ticketDataArr = new AppHelper(getActivity()).getTicketDataFromLocal();
		ticketDataList = AppHelper.parseFromJsonList(ticketDataArr);
		if (ticketDataList.size() > 5) {
			subTicketDataList = ticketDataList.subList(0, 5);
			return subTicketDataList;
		} else {
			subTicketDataList = ticketDataList;
			return ticketDataList;
		}
	}
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {

			case R.id.ticket_seg_all:
				SharedPreferences.Editor editorAll = mSharedPref.edit();
				editorAll.putBoolean("ticketRecentFlag", false);
				editorAll.apply();
				mAdapter = new TicketListAdapter(getActivity(), ticketDataList);
				mAdapter.setOnTicketClaimListener(new TicketListAdapter.OnTicketClaimListener() {
					@Override
					public void onTicketClaimed(int position) {

						JSONObject ticketItem = ticketDataList.get(position);
						validationTicketWithData(ticketItem, TICKET_CLAIM_STATE);
					}
				});
				mAdapter.setOnTicketDeleteListener(new TicketListAdapter.OnTicketDeleteListener() {
					@Override
					public void onTicketDeleted(int position) {

						JSONObject ticketItem = ticketDataList.get(position);
						validationTicketWithData(ticketItem, TICKET_DELETE_STATE);
					}
				});
				mAdapter.notifyDataSetChanged();
				mListView.setAdapter(mAdapter);

				break;
			case R.id.ticket_seg_recent:
				SharedPreferences.Editor editorRecent = mSharedPref.edit();
				editorRecent.putBoolean("ticketRecentFlag", true);
				editorRecent.apply();
				mAdapter = new TicketListAdapter(getActivity(), subTicketDataList);
				mAdapter.setOnTicketClaimListener(new TicketListAdapter.OnTicketClaimListener() {
					@Override
					public void onTicketClaimed(int position) {

						JSONObject ticketItem = ticketDataList.get(position);
						validationTicketWithData(ticketItem, TICKET_CLAIM_STATE);
					}
				});
				mAdapter.setOnTicketDeleteListener(new TicketListAdapter.OnTicketDeleteListener() {
					@Override
					public void onTicketDeleted(int position) {

						JSONObject ticketItem = ticketDataList.get(position);
						validationTicketWithData(ticketItem, TICKET_DELETE_STATE);
					}
				});
				mAdapter.notifyDataSetChanged();
				mListView.setAdapter(mAdapter);
				break;
			default:
				// Nothing to do
		}
	}

	private void validationTicketWithData(final JSONObject ticketItem, final int type) {
		try {
			boolean validFlag = ticketItem.getBoolean("isValid");

			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mProgressDialog.setMessage("Validating...");
			mProgressDialog.show();
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			new Thread(new Runnable() {
				public void run() {
					try {
						String uuid = AndroidUtilities.getUUID(getActivity());
						receivedObj = APIInterface.ticketValidate(ticketItem,uuid);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {

								if (mProgressDialog.isShowing()) {
									mProgressDialog.dismiss();
								}

								if (receivedObj != null) {
									try {

										JSONObject jsonData = receivedObj.getJSONObject("data");
										String claimLicenseCode = jsonData.getString("claimLicenseCode");
										JSONArray awardsArr = jsonData.getJSONArray("awards");

										List<JSONObject> awardsDataList = AppHelper.parseFromJsonList(awardsArr);

										switch (type) {
											case TICKET_CLAIM_STATE:
												try {
													if (awardsDataList.size() == 0 && jsonData.getInt("nonDrawnPanels") == 0) {
														new PromptDialog(getActivity())
																.setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
																.setAnimationEnable(true)
																.setTitleText("No Winner!")
																.setContentText("Sorry your ticket is not a winner this time, try again.")
																.setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
																	@Override
																	public void onClick(PromptDialog dialog) {
																		dialog.dismiss();
																	}
																}).show();
													} else if (awardsDataList.size() == 0 && jsonData.getInt("nonDrawnPanels") > 0) {
														new PromptDialog(getActivity())
																.setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
																.setAnimationEnable(true)
																.setTitleText("Warning!")
																.setContentText("Oops! Some Draws have not completed yet! Try back after they are completed.")
																.setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
																	@Override
																	public void onClick(PromptDialog dialog) {
																		dialog.dismiss();
																	}
																}).show();
													} else if (awardsDataList.size() > 0) {
														float f_totalAmount = 0.0f;
														for (JSONObject awardItem : awardsDataList) {
															f_totalAmount += awardItem.getDouble("prizeAwardAmount");
//                                                            f_totalAmount += awardItem.getDouble("amountDueFromPlayer");
														}

														if (jsonData.getInt("nonDrawnPanels") == 0) {
															ticketItem.put("", true);
														}

														Intent i = new Intent(getActivity(), TicketValidationActivity.class);
														i.putExtra("totalAmount", f_totalAmount);
														i.putExtra("claimLicenseCode", claimLicenseCode);
														i.putExtra("claimTicketData", ticketItem.toString());
														startActivity(i);
														getActivity().finish();
													}
												} catch (Exception e) {
													Log.d("json_e-->", e.getMessage());
												}

												break;
											case TICKET_DELETE_STATE:
												try {
													if (awardsDataList.size() > 0 || jsonData.getInt("nonDrawnPanels") > 0) {
														new PromptDialog(getActivity())
																.setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
																.setAnimationEnable(true)
																.setTitleText("Warning!")
																.setContentText("Oops! You have unclaimed awards! Please Claim Ticket.")
																.setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
																	@Override
																	public void onClick(PromptDialog dialog) {
																		dialog.dismiss();
																	}
																}).show();
													} else if (awardsDataList.size() == 0 && jsonData.getInt("nonDrawnPanels") == 0) {
														ColorDialog dialog = new ColorDialog(getActivity());
														dialog.setTitle("Warning!");
														dialog.setContentText("We have confirmed you have no awards on this Ticket, are you sure you want to delete it? You will not be able to recover it.");
														dialog.setContentImage(ContextCompat.getDrawable(getActivity(), R.drawable.background_green_rect));
														dialog.setPositiveListener("Ok", new ColorDialog.OnPositiveListener() {
															@Override
															public void onClick(final ColorDialog dialog) {
																new AppHelper(getActivity()).deleteOneTicketDataItem(ticketItem);
																mAdapter = new TicketListAdapter(getActivity(), loadAllTicketData());
																mAdapter.setOnTicketClaimListener(new TicketListAdapter.OnTicketClaimListener() {
																	@Override
																	public void onTicketClaimed(int position) {

																		JSONObject ticketItem = ticketDataList.get(position);
																		validationTicketWithData(ticketItem, TICKET_CLAIM_STATE);
																	}
																});
																mAdapter.setOnTicketDeleteListener(new TicketListAdapter.OnTicketDeleteListener() {
																	@Override
																	public void onTicketDeleted(int position) {

																		JSONObject ticketItem = ticketDataList.get(position);
																		validationTicketWithData(ticketItem, TICKET_DELETE_STATE);
																	}
																});
																mAdapter.notifyDataSetChanged();
																mListView.setAdapter(mAdapter);

																dialog.dismiss();

															}
														})
																.setNegativeListener("Cancel", new ColorDialog.OnNegativeListener() {
																	@Override
																	public void onClick(ColorDialog dialog) {
																		dialog.dismiss();
																	}
																}).show();
													}
												} catch (Exception e) {
													Log.d("json_e-->", e.getMessage());
												}
												break;
											default:
												break;
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
					} catch (final Exception e) {
						Log.e("register error--->", e.getMessage());
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

		} catch (Exception e) {

			Log.e("json_error-->", e.getMessage());
		}
	}
}
