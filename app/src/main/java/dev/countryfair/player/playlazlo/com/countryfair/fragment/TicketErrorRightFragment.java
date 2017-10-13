package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dev.countryfair.player.playlazlo.com.countryfair.R;

/**
 * Fragment to manage the left page of the 5 pages application navigation (top, center, bottom, left, right).
 */
public class TicketErrorRightFragment extends Fragment {

	// -----------------------------------------------------------------------
	//
	// Methods
	//
	// -----------------------------------------------------------------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_ticket_error_right, container, false);
		return fragmentView;
	}

}
