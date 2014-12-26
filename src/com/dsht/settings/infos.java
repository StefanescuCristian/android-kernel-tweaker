package com.dsht.settings;

import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class infos extends PreferenceFragment implements OnPreferenceClickListener {

	private String KEY_DSHT = "key_dsht";
	private String KEY_CESCO = "key_cesco";
	private String KEY_BIG_BUM = "key_big-bum";
	private String KEY_SOLLYX = "key_sollyx";
	private String KEY_SLIDINGMENU = "key_slidingmenu";

	private Preference mDsht, 
	mCesco,
	mBig_Bum,
	mSollyx,
	mSlidingMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.infos);

		mDsht = findPreference(KEY_DSHT);
		mCesco = findPreference(KEY_CESCO);
		mBig_Bum = findPreference(KEY_BIG_BUM);
		mSollyx = findPreference(KEY_SOLLYX);
		mSlidingMenu = findPreference(KEY_SLIDINGMENU);

		PackageInfo pInfo = null;
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = pInfo.versionName;

		mDsht.setTitle(R.string.app_name);
		mDsht.setIcon(R.drawable.ic_launcher);
		mDsht.setSummary("Version: "+version);

		mCesco.setIcon(R.drawable.cesco);
		mSollyx.setIcon(R.drawable.sollyx_google);
		mBig_Bum.setIcon(R.drawable.big_bum);

		mSlidingMenu.setIcon(R.drawable.github);

		mDsht.setOnPreferenceClickListener(this);
		mCesco.setOnPreferenceClickListener(this);
		mSollyx.setOnPreferenceClickListener(this);
		mBig_Bum.setOnPreferenceClickListener(this);
		mSlidingMenu.setOnPreferenceClickListener(this);

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
	}


	@Override
	public boolean onPreferenceClick(Preference pref) {
		// TODO Auto-generated method stub
		String url = "";
		if(pref == mDsht) {
			url = "https://play.google.com/store/apps/developer?id=DSHT";
		}
		if(pref == mCesco) {
			url = "https://plus.google.com/u/0/+FrancescoRigamonti/posts";
		}
		if(pref == mSollyx) {
			url = "https://plus.google.com/u/0/116757450567339042397/posts";
		}
		if(pref == mBig_Bum) {
			url = "https://plus.google.com/+Cristian%C8%98tef%C4%83nescu/posts";
		}
		
		if(pref == mSlidingMenu) {
			url = "https://github.com/jfeinstein10/slidingmenu";
		}
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);

		return false;
	}



}
