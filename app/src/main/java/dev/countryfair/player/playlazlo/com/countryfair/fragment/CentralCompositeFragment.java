package dev.countryfair.player.playlazlo.com.countryfair.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.CirclePageIndicator;
import java.util.ArrayList;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.FragmentsClassesPagerAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.event.EventBus;
import dev.countryfair.player.playlazlo.com.countryfair.event.PageChangedEvent;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;


/**
 * Fragment to manage the horizontal pages (left, central, right) of the 5 pages application navigation (top, center,
 * bottom, left, right).
 */
public class CentralCompositeFragment extends Fragment  {

	private static final String TAG = CentralCompositeFragment.class.getSimpleName();

	// -----------------------------------------------------------------------
	//
	// Fields
	//
	// -----------------------------------------------------------------------
	public ViewPager mHorizontalPager;
	public CirclePageIndicator cpiIndicator;

	private int mCentralPageIndex = 0;

	private Handler mHandler = new Handler();
	private Runnable mIndicatorShowcase = new Runnable() {
		@Override
		public void run() {
			AndroidUtilities.fadeoutView(cpiIndicator);
		}
	};

	private OnPageChangeListener mPagerChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			Log.d(TAG, "onPageSelected: "+position);
			resetIndicatorShowcase();
			EventBus.getInstance().post(new PageChangedEvent(mCentralPageIndex == position));
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	// -----------------------------------------------------------------------
	//
	// Methods
	//
	// -----------------------------------------------------------------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_composite_central, container, false);
		findViews(fragmentView);
		return fragmentView;
	}

	private void findViews(View fragmentView) {
		mHorizontalPager = (ViewPager) fragmentView.findViewById(R.id.fragment_composite_central_pager);
		cpiIndicator = (CirclePageIndicator) fragmentView.findViewById(R.id.cpiIndicator);
		initViews();
	}

	private void initViews() {
		populateHozizontalPager();
		cpiIndicator.setCurrentItem(mCentralPageIndex);
		cpiIndicator.setOnPageChangeListener(mPagerChangeListener);
	}

	private void populateHozizontalPager() {
		ArrayList<Class<? extends Fragment>> pages = new ArrayList<Class<? extends Fragment>>();
		pages.add(NextOverLeftFragment.class);		// for settitng views to make the simulation view
		pages.add(OverLeftFragment.class);			// for whole gift card view display

		if (((new AppHelper(getActivity()).getCouponJSONDataFromLocal()).length() > 0) || ((new AppHelper(getActivity()).getRedeemCardJSONDataFromLocal()).length() > 0)){
			pages.add(LeftFragment.class);			// for downloaded gift card view
		}
		pages.add(CentralFragment.class); 			// for main home page view
//		pages.add(RightFragment.class);				// for bot chat view
		if ((new AppHelper(getActivity()).getTicketDataFromLocal()).length() > 0){
			pages.add(OverRightFragment.class); 	// for downloaded ticket list view
		}
		pages.add(TileMainRightFragment.class); 	// for main tile view to display game and brands
		mCentralPageIndex = pages.indexOf(CentralFragment.class);
		mHorizontalPager.setAdapter(new FragmentsClassesPagerAdapter(getChildFragmentManager(), getActivity(), pages));
		cpiIndicator.setViewPager(mHorizontalPager);
	}

	@Override
	public void onResume() {
		super.onResume();
		resetIndicatorShowcase();
	}

	private void resetIndicatorShowcase(){
		mHandler.removeCallbacks(mIndicatorShowcase);
		cpiIndicator.setVisibility(View.VISIBLE);
		mHandler.postDelayed(mIndicatorShowcase, Constants.INDICATOR_DURATION);
	}
}
