package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import dev.countryfair.player.playlazlo.com.countryfair.PushMessagesActivity;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.MarshMallowPermission;

/**
 * Fragment to manage the left page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class NextOverLeftFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{

	private static final int CAMERA_REQUEST = 1888;
	private MarshMallowPermission marshMallowPermission;

    private TextView tvPushMessages;

	private TextView pushHandleCodeTxt;
	private TextView playerLicenseCodeTxt;
	private TextView osVersionCodeTxt;
	private Switch simulateStateSwitch, setting_pin_state, setting_bot_state;
    private EditText etLicenseText,etPinPwd;

	private SharedPreferences sharedPref;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_next_over_left, container, false);
		marshMallowPermission = new MarshMallowPermission(getActivity());

		simulateStateSwitch = (Switch) fragmentView.findViewById(R.id.setting_simulation_state_btn);
        setting_pin_state = (Switch) fragmentView.findViewById(R.id.setting_pin_state);
        setting_bot_state = (Switch) fragmentView.findViewById(R.id.setting_bot_state);

        simulateStateSwitch.setOnCheckedChangeListener(this);
        setting_pin_state.setOnCheckedChangeListener(this);
        setting_bot_state.setOnCheckedChangeListener(this);

        etLicenseText = (EditText) fragmentView.findViewById(R.id.etLicenseText);
        etPinPwd = (EditText) fragmentView.findViewById(R.id.etPinPwd);

        pushHandleCodeTxt = (TextView) fragmentView.findViewById(R.id.setting_push_handle_txt);
		playerLicenseCodeTxt = (TextView) fragmentView.findViewById(R.id.setting_player_code_txt);
		osVersionCodeTxt = (TextView) fragmentView.findViewById(R.id.setting_os_version_code);


        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        simulateStateSwitch.setChecked(sharedPref.getBoolean("isSystemGenerated", false));
        setting_pin_state.setChecked(sharedPref.getBoolean("isPinEnabled", false));
        setting_bot_state.setChecked(sharedPref.getBoolean("isShowBot", false));

        pushHandleCodeTxt.setText(sharedPref.getString("deviceTokenForPush", ""));
        playerLicenseCodeTxt.setText(Constants.PLAYER_TOKEN);
        osVersionCodeTxt.setText(String.valueOf(android.os.Build.VERSION.SDK_INT));

        tvPushMessages = (TextView) fragmentView.findViewById(R.id.tvPushMessages);
        tvPushMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PushMessagesActivity.class));
            }
        });

		return fragmentView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (buttonView.getId()) {
            case R.id.setting_simulation_state_btn:
                editor.putBoolean("isSystemGenerated", isChecked);
                etLicenseText.setVisibility((isChecked) ? View.VISIBLE : View.GONE);
                break;
            case R.id.setting_pin_state:
                editor.putBoolean("isPinEnabled", isChecked);
                etPinPwd.setVisibility((isChecked) ? View.VISIBLE : View.GONE);
                break;
            case R.id.setting_bot_state:
                editor.putBoolean("isShowBot", isChecked);
                break;
        }
        editor.apply();
	}

}
