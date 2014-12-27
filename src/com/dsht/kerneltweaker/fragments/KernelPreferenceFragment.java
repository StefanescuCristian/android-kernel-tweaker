package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.util.List;
import com.dsht.kerneltweaker.CustomCheckBoxPreference;
import com.dsht.kerneltweaker.CustomListPreference;
import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

public class KernelPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private CustomCheckBoxPreference mKernelFsync;
	private CustomCheckBoxPreference mKernelKSM;
	private CustomCheckBoxPreference mKernelFcharge;
	private CustomCheckBoxPreference mDoubleTap;
	private CustomCheckBoxPreference mSweep2wake;
	private CustomCheckBoxPreference mSweep2sleep;
	private CustomPreference mVibration;
	private CustomPreference mAdvancedScheduler;
	private CustomCheckBoxPreference mKernelKSMTimer;
	private CustomPreference mKernelKSMPages;
	private CustomPreference mKernelKSMSleep;
	private CustomPreference mKernelKSMMB;
	private CustomPreference mKernelKSMShared;
	private CustomListPreference mCpuScheduler;
	private CustomListPreference mCpuReadAhead;
	private PreferenceCategory mSoundCategory;
	private PreferenceCategory mKSMCategory;
	private PreferenceCategory mWakeCategory;
	private CustomPreference mSoundInfo;
	private SharedPreferences mPrefs;
	private PreferenceCategory mKernelCategory;
	private Context mContext; 
	private PreferenceScreen mRootScreen;
	private PreferenceCategory mSchedCategory;

	private static final String FSYNC_FILE = "/sys/module/sync/parameters/fsync_enabled";
	private static final String SCHEDULER_FILE = "/sys/block/mmcblk0/queue/scheduler";
	private static final String READ_AHEAD_FILE = "/sys/block/mmcblk0/queue/read_ahead_kb";
	private static final String FCHARGE_FILE = "/sys/kernel/fast_charge/force_fast_charge";

	private static final String HEADSET_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/headset_boost";
	private static final String MIC_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/mic_boost";
	private static final String SPEAKER_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/speaker_boost";
	private static final String VOLUME_BOOST_FILE = "/sys/devices/virtual/misc/soundcontrol/volume_boost";
	private static final String category = "kernel";
	private static final String TCP_OPTIONS = "sysctl net.ipv4.tcp_available_congestion_control";
	private static final String TCP_CURRENT = "sysctl net.ipv4.tcp_congestion_control";
	private static final String DT2W_FILE = "/sys/android_touch/doubletap2wake";
	private static final String S2W_FILE = "/sys/android_touch/sweep2wake";
	private static final String S2W_SLEEPONLY_FILE = "/sys/android_touch/sweep2sleep";
	private static final String VIBRATION_FILE = "/sys/class/timed_output/vibrator/amp";
	private static final String KSM_RUN_PATH = "/sys/kernel/mm/ksm/run";
    private static final String KSM_SLEEP_PATH = "/sys/kernel/mm/ksm/sleep_millisecs";
    private static final String KSM_PAGESTOSCAN_PATH = "/sys/kernel/mm/ksm/pages_to_scan";
    private static final String KSM_DEFERRED_TIMER = "/sys/kernel/mm/ksm/deferred_timer";
    private static final String KSM_SAVED_MB = "/sys/kernel/mm/ksm/pages_sharing";
    private static final String KSM_SHARED_MB = "/sys/kernel/mm/ksm/pages_shared";
	
	
	private String color;
	private DatabaseHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_kernel);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle();
		}
		
		Helpers.setPermissions(DT2W_FILE);
		Helpers.setPermissions(FCHARGE_FILE);
		Helpers.setPermissions(FSYNC_FILE);
		Helpers.setPermissions(HEADSET_BOOST_FILE);
		Helpers.setPermissions(MIC_BOOST_FILE);
		Helpers.setPermissions(READ_AHEAD_FILE);
		Helpers.setPermissions(S2W_FILE);
		Helpers.setPermissions(S2W_SLEEPONLY_FILE);
		Helpers.setPermissions(SCHEDULER_FILE);
		Helpers.setPermissions(SPEAKER_BOOST_FILE);
		Helpers.setPermissions(VIBRATION_FILE);
		Helpers.setPermissions(VOLUME_BOOST_FILE);
		Helpers.setPermissions(TCP_CURRENT);
		Helpers.setPermissions(TCP_OPTIONS);
		Helpers.setPermissions(KSM_RUN_PATH);
		Helpers.setPermissions(KSM_SLEEP_PATH);
		Helpers.setPermissions(KSM_PAGESTOSCAN_PATH);
		Helpers.setPermissions(KSM_DEFERRED_TIMER);

		

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mSoundCategory = (PreferenceCategory) findPreference("key_sound_category");
		mKSMCategory = (PreferenceCategory) findPreference("key_ksm_category");
		mWakeCategory = (PreferenceCategory) findPreference("key_wake_category");
		mSchedCategory = (PreferenceCategory) findPreference("key_sched_cat");
		mKernelFsync = (CustomCheckBoxPreference) findPreference("key_fsync_switch");
		mKernelKSM = (CustomCheckBoxPreference) findPreference("key_ksm_switch");
		mKernelKSMTimer = (CustomCheckBoxPreference) findPreference("key_ksm_timer");
		mKernelKSMPages = (CustomPreference) findPreference("key_ksm_pages");
		mKernelKSMSleep = (CustomPreference) findPreference("key_ksm_sleep");
		mKernelKSMMB = (CustomPreference) findPreference("key_ksm_mb");
		mKernelKSMShared = (CustomPreference) findPreference("key_ksm_share");
		mKernelFcharge = (CustomCheckBoxPreference) findPreference("key_fcharge_switch");
		mCpuScheduler = (CustomListPreference) findPreference("key_cpu_sched");
		mAdvancedScheduler = (CustomPreference) findPreference("key_advanced_scheduler");
		mCpuReadAhead = (CustomListPreference) findPreference("key_cpu_readahead");
		mSoundInfo = (CustomPreference) findPreference("key_kernel_info");
		mKernelCategory = (PreferenceCategory) findPreference("key_kernel_tweaks");
		mRootScreen = (PreferenceScreen) findPreference("key_pref_screen");
		mDoubleTap = (CustomCheckBoxPreference) findPreference("key_dt2w_switch");
		mSweep2wake = (CustomCheckBoxPreference) findPreference("key_s2w_switch");
		mSweep2sleep = (CustomCheckBoxPreference) findPreference("key_s2ws_switch");

		mVibration = (CustomPreference) findPreference("key_vibration");
		db = MainActivity.db;

		mKernelFsync.setKey(FSYNC_FILE);
		mKernelFcharge.setKey(FCHARGE_FILE);
		mCpuScheduler.setKey(SCHEDULER_FILE);
		mCpuReadAhead.setKey(READ_AHEAD_FILE);
		mDoubleTap.setKey(DT2W_FILE);
		mSweep2wake.setKey(S2W_FILE);
		mSweep2sleep.setKey(S2W_SLEEPONLY_FILE);
		mVibration.setKey(VIBRATION_FILE);
		mKernelKSM.setKey(KSM_RUN_PATH);
		mKernelKSMTimer.setKey(KSM_DEFERRED_TIMER);
		mKernelKSMSleep.setKey(KSM_SLEEP_PATH);
		mKernelKSMPages.setKey(KSM_PAGESTOSCAN_PATH);

		mCpuScheduler.setKey(SCHEDULER_FILE);
		mCpuReadAhead.setKey(READ_AHEAD_FILE);

		mCpuScheduler.setCategory(category);
		mCpuReadAhead.setCategory(category);
		mKernelFsync.setCategory(category);
		mKernelFcharge.setCategory(category);
		mDoubleTap.setCategory(category);
		mSweep2wake.setCategory(category);
		mSweep2sleep.setCategory(category);
		mVibration.setCategory(category);
		mKernelKSM.setCategory(category);
		mKernelKSMTimer.setCategory(category);
		mKernelKSMSleep.setCategory(category);
		mKernelKSMPages.setCategory(category);
		mKernelKSMMB.setCategory(category);
		mKernelKSMShared.setCategory(category);
		
		String[] schedulers = Helpers.getAvailableSchedulers();
		String[] readAheadKb = {"128","256","384","512","640","768","896","1024","1152",
				"1280","1408","1536","1664","1792","1920","2048", "2176", "2304", "2432", "2560", 
				"2688", "2816", "2944", "3072", "3200", "3328", "3456", "3584", "3712", "3840", "3968", "4096"};
		color = "";
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			color = "#"+Integer.toHexString(col);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_KERNEL, Color.parseColor("#ff0099cc"));
			color = "#"+Integer.toHexString(col);
		} 
		else {
			color = getResources().getStringArray(R.array.menu_colors)[5];
		}

		mKernelFsync.setTitleColor(color);
		mKernelFcharge.setTitleColor(color);
		mCpuScheduler.setTitleColor(color);
		mAdvancedScheduler.setTitleColor(color);
		mCpuReadAhead.setTitleColor(color);
		mSoundInfo.setTitleColor("#ff4444");
		mSoundInfo.setSummaryColor("#ff4444");
		mSoundInfo.excludeFromDialog(true);
		mSoundInfo.hideBoot(true);
		mAdvancedScheduler.excludeFromDialog(true);
		mAdvancedScheduler.hideBoot(true);
		mDoubleTap.setTitleColor(color);
		mSweep2wake.setTitleColor(color);
		mSweep2sleep.setTitleColor(color);
		mVibration.setTitleColor(color);
		mKernelKSM.setTitleColor(color);
		mKernelKSMTimer.setTitleColor(color);
		mKernelKSMSleep.setTitleColor(color);
		mKernelKSMPages.setTitleColor(color);
		mKernelKSMMB.setTitleColor(color);
		mKernelKSMShared.setTitleColor(color);
		
		mCpuScheduler.setEntries(schedulers);
		mCpuScheduler.setEntryValues(schedulers);
		mCpuReadAhead.setEntries(readAheadKb);
		mCpuReadAhead.setEntryValues(readAheadKb);

		mCpuScheduler.setSummary(Helpers.getCurrentScheduler());
		mCpuReadAhead.setSummary(Helpers.getFileContent(new File(READ_AHEAD_FILE)));

		mAdvancedScheduler.setOnPreferenceClickListener(this);
		mCpuScheduler.setOnPreferenceChangeListener(this);
		mCpuReadAhead.setOnPreferenceChangeListener(this);
		mVibration.setOnPreferenceClickListener(this);
		mKernelKSMPages.setOnPreferenceClickListener(this);
		mKernelKSMSleep.setOnPreferenceClickListener(this);
		
		mKernelKSMMB.hideBoot(true);
		mKernelKSMShared.hideBoot(true);

		mKernelFsync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+FSYNC_FILE;
					value = "Y";
				} else {
					cmd = "echo N > "+FSYNC_FILE;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				
				return true;
			}
		});

		mDoubleTap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+DT2W_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+DT2W_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mSweep2wake.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+S2W_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+S2W_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mSweep2sleep.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+S2W_SLEEPONLY_FILE;
					value ="1";
				} else {
					cmd = "echo 0 > "+S2W_SLEEPONLY_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});
		
		mKernelKSM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+KSM_RUN_PATH;
					value = "1";
				} else {
					cmd = "echo 0 > "+KSM_RUN_PATH;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mKernelKSMTimer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+KSM_DEFERRED_TIMER;
					value = "1";
				} else {
					cmd = "echo 0 > "+KSM_DEFERRED_TIMER;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});

		mKernelFcharge.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo 1 > "+FCHARGE_FILE;
					value = "1";
				} else {
					cmd = "echo 0 > "+FCHARGE_FILE;
					value = "0";
				}
				CMDProcessor.runSuCommand(cmd);
				updateDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});
//remove not needed checks
		
		String fsyncState = Helpers.getFileContent(new File(FSYNC_FILE));
		if(fsyncState.equals("Y")) {
			mKernelFsync.setChecked(true);
			mKernelFsync.setValue("Y");
		}else if(fsyncState.equals("N")) {
			mKernelFsync.setChecked(false);
			mKernelFsync.setValue("N");
		}
		
		String dtState = Helpers.getFileContent(new File(DT2W_FILE));
		if(dtState.equals("1")) {
			mDoubleTap.setChecked(true);
			mDoubleTap.setValue("1");
		}else if(dtState.equals("0")) {
			mDoubleTap.setChecked(false);
			mDoubleTap.setValue("0");
		}


		String s2wState = Helpers.getFileContent(new File(S2W_FILE));
		if(s2wState.equals("1")) {
			mSweep2wake.setChecked(true);
			mSweep2wake.setValue("1");
		}else if(s2wState.equals("0")) {
			mSweep2wake.setChecked(false);
			mSweep2wake.setValue("0");
		}


		String ksmState = Helpers.getFileContent(new File(KSM_RUN_PATH));
		if(ksmState.equals("1")) {
			mKernelKSM.setChecked(true);
			mKernelKSM.setValue("1");
		}else if(ksmState.equals("0")) {
			mKernelKSM.setChecked(false);
			mKernelKSM.setValue("0");
		}
		
		String ksmdState = Helpers.getFileContent(new File(KSM_DEFERRED_TIMER));
		if(ksmdState.equals("1")) {
			mKernelKSMTimer.setChecked(true);
			mKernelKSMTimer.setValue("1");
		}else if(ksmdState.equals("0")) {
			mKernelKSMTimer.setChecked(false);
			mKernelKSMTimer.setValue("0");
		}

		String s2wsState = Helpers.getFileContent(new File(S2W_SLEEPONLY_FILE));
		if(s2wsState.equals("1")) {
			mSweep2sleep.setChecked(true);
			mSweep2sleep.setValue("1");
		}else if(s2wsState.equals("0")) {
			mSweep2sleep.setChecked(false);
			mSweep2sleep.setValue("0");
		}

		String fchargeState = Helpers.getFileContent(new File(FCHARGE_FILE));
		if(fchargeState.equals("0")) {
			mKernelFcharge.setChecked(false);
			mKernelFcharge.setValue("0");
		} else if(fchargeState.equals("1")) {
			mKernelFcharge.setChecked(true);
			mKernelFcharge.setValue("1");
		}

		File f = new File(HEADSET_BOOST_FILE);
		File f1 = new File(MIC_BOOST_FILE);
		File f2 = new File(SPEAKER_BOOST_FILE);
		File f3 = new File(VOLUME_BOOST_FILE);
		
		createPreference(mSoundCategory,f, color, true);
		createPreference(mSoundCategory,f1, color, true);
		createPreference(mSoundCategory,f2, color, true);
		createPreference(mSoundCategory,f3, color, true);

		
		mVibration.setSummary(Helpers.getFileContent(new File(VIBRATION_FILE)));
		mKernelKSMPages.setSummary(Helpers.getFileContent(new File(KSM_PAGESTOSCAN_PATH)));
		mKernelKSMSleep.setSummary(Helpers.getFileContent(new File(KSM_SLEEP_PATH)));
		String savedMB;
		savedMB = new String(Integer.toString(Integer.parseInt(Helpers.getFileContent(new File(KSM_SAVED_MB))) * 4096 / 1024 / 1024));
		mKernelKSMMB.setSummary(savedMB);
		String sharedMB;
		sharedMB = new String(Integer.toString(Integer.parseInt(Helpers.getFileContent(new File(KSM_SHARED_MB))) * 4096 / 1024 / 1024));
		mKernelKSMShared.setSummary(sharedMB);
		
		if(RootTools.isBusyboxAvailable()) {
			String[] availTCP = Helpers.readCommandStrdOut(TCP_OPTIONS, false).replaceAll("net.ipv4.tcp_available_congestion_control = ", "").replaceAll("\n", "").split(" ");
			CustomListPreference pref = new CustomListPreference(mContext, category);
			pref.setTitle("TCP Congestion control");
			pref.setTitleColor(color);
			pref.setEntries(availTCP);
			pref.setEntryValues(availTCP);
			pref.setSummary(Helpers.readCommandStrdOut(TCP_CURRENT, false).replaceAll("net.ipv4.tcp_congestion_control = ","").replaceAll("\n", ""));
			String key = "sysctl -w net.ipv4.tcp_congestion_control=" +pref.getSummary();
			pref.setKey(key);
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					// TODO Auto-generated method stub
					String value = (String) newValue;
					String command = "sysctl -w net.ipv4.tcp_congestion_control=" +value;
					pref.setKey(command);
					pref.setSummary(value);
					CMDProcessor.runSuCommand(command);
					return true;
				}

			});

			mSchedCategory.addPreference(pref);
		}
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);

		return v;
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		if(pref == mCpuScheduler) {
			String value = (String) newValue;
			mCpuScheduler.setSummary(value);
			mCpuScheduler.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+SCHEDULER_FILE);
			updateDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuReadAhead) {
			String value = (String) newValue;
			mCpuReadAhead.setSummary(value);
			mCpuReadAhead.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+READ_AHEAD_FILE);
			updateDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		// TODO Auto-generated method stub
		if(pref == mAdvancedScheduler) {
			Fragment f = new CpuSchedulerPreferenceFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			// This adds the newly created Preference fragment to my main layout, shown below
			ft.replace(R.id.activity_container,f);
			// By hiding the main fragment, transparency isn't an issue
			ft.addToBackStack("TAG");
			ft.commit();
		}
		if(pref == mVibration) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		if(pref == mKernelKSMPages) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		if(pref == mKernelKSMSleep) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.dialog_layout, null, false);
			final EditText et = (EditText) v.findViewById(R.id.et);
			String val = pref.getSummary().toString();
			et.setText(val);
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			et.setGravity(Gravity.CENTER_HORIZONTAL);
			List<DataItem> items = db.getAllItems();
			builder.setView(v);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String value = et.getText().toString();
					pref.setSummary(value);
					CMDProcessor.runSuCommand("echo \""+value+"\" > "+pref.getKey());
					updateDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		return false;
	}

	private void createPreference(PreferenceCategory mCategory, File file, String color,final boolean numbers) {
		String fileName = file.getName();
		String filePath = file.getAbsolutePath();
		final String fileContent = Helpers.getFileContent(file);
		final CustomPreference pref = new CustomPreference(mContext, false, category);
		pref.setTitle(fileName);
		pref.setTitleColor(color);
		pref.setSummary(fileContent);
		pref.setKey(filePath);
		Log.d("CONTENT", fileContent);
		mCategory.addPreference(pref);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(final Preference p) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_layout, null, false);
				final EditText et = (EditText) v.findViewById(R.id.et);
				String val = p.getSummary().toString();
				et.setText(val);
				if(numbers) {
					et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				}
				et.setGravity(Gravity.CENTER_HORIZONTAL);
				List<DataItem> items = db.getAllItems();
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo \""+value+"\" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo \""+value+"\" > "+p.getKey());
						updateDb(p, value, pref.isBootChecked());
					}
				} );
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
				Window window = dialog.getWindow();
				window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				return true;
			}

		});
	}



	private void updateDb(final Preference p, final String value,final boolean isChecked) {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				if(isChecked) {
					List<DataItem> items = db.getAllItems();
					for(DataItem item : items) {
						if(item.getName().equals("'"+p.getKey()+"'")) {
							db.deleteItemByName("'"+p.getKey()+"'");
						}
					}
					db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
				} else {
					if(db.getContactsCount() != 0) {
						db.deleteItemByName("'"+p.getKey()+"'");
					}
				}

				return "Executed";
			}
			@Override
			protected void onPostExecute(String result) {

			}
		}
		new LongOperation().execute();
	}

}
