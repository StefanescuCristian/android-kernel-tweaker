package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.util.List;

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

import com.dsht.kerneltweaker.CustomCheckBoxPreference;
import com.dsht.kerneltweaker.CustomListPreference;
import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.bb.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;

public class CpuPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
	private Context mContext;
	private SharedPreferences mPrefs;
	private CustomListPreference mCpuMaxFreq;
	private CustomListPreference mCpuMinFreq;
	private CustomListPreference mCpuBoostFreq;
	private CustomListPreference mCpuSync;
	private CustomListPreference mCpuGovernor;
	private CustomPreference mAdvancedGovernor;
	private CustomPreference mCpuTemp;
	private CustomPreference mCpuBoostMs;
	private CustomPreference mInputBoostMs;
	private PreferenceCategory mAdvancedCategory;
	private PreferenceCategory mCpuBoostCategory;
	private PreferenceScreen mRoot;
	private CustomCheckBoxPreference mCpuBoostHp;
	private CustomCheckBoxPreference mCpuBoostLbs;
	private static final String MAX_FREQ_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	private static final String GOVERNOR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	private static final String MIN_FREQ_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	private static final String TEMP_FILE = "/sys/module/msm_thermal/parameters/temp_threshold";
	private static final String CPU_BOOST_HP = "/sys/module/cpu_boost/parameters/hotplug_boost";
	private static final String CPU_BOOST_LBS = "/sys/module/cpu_boost/parameters/load_based_syncs";
	private static final String CPU_BOOST_MS = "/sys/module/cpu_boost/parameters/boost_ms";
	private static final String CPU_BOOST_FREQ = "/sys/module/cpu_boost/parameters/input_boost_freq";
	private static final String INPUT_BOOST_MS = "/sys/module/cpu_boost/parameters/input_boost_ms";
	private static final String SYNC_THRESHOLD = "/sys/module/cpu_boost/parameters/sync_threshold";
	private static final String category = "cpu";
	private DatabaseHandler db = MainActivity.db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_cpu);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle();
		}
		RootTools.isRootAvailable();

		Helpers.setPermissions(MAX_FREQ_FILE);
		Helpers.setPermissions(MIN_FREQ_FILE);
		Helpers.setPermissions(GOVERNOR_FILE);
		Helpers.setPermissions(TEMP_FILE);
		Helpers.setPermissions(CPU_BOOST_HP);
		Helpers.setPermissions(CPU_BOOST_LBS);
		Helpers.setPermissions(CPU_BOOST_MS);
		Helpers.setPermissions(INPUT_BOOST_MS);
		Helpers.setPermissions(CPU_BOOST_FREQ);
		Helpers.setPermissions(SYNC_THRESHOLD);



		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mCpuMaxFreq = (CustomListPreference) findPreference("key_cpu_max");
		mCpuMinFreq = (CustomListPreference) findPreference("key_cpu_min");
		mCpuBoostFreq = (CustomListPreference) findPreference("key_boost_freq");
		mCpuSync = (CustomListPreference) findPreference("key_sync_freq");
		mCpuGovernor = (CustomListPreference) findPreference("key_cpu_governor");
		mCpuTemp = (CustomPreference) findPreference("key_cpu_temp");
		mCpuBoostMs = (CustomPreference) findPreference("key_cpu_boost_ms");
		mInputBoostMs = (CustomPreference) findPreference("key_input_boost_ms");
		mAdvancedGovernor = (CustomPreference) findPreference("key_advanced_governor");
		mAdvancedCategory = (PreferenceCategory) findPreference("key_advanced");
		mCpuBoostCategory = (PreferenceCategory) findPreference("key_boost_category");
		mAdvancedGovernor.setOnPreferenceClickListener(this);
		mCpuBoostHp = (CustomCheckBoxPreference) findPreference("key_cpu_boost_hp");
		mCpuBoostLbs = (CustomCheckBoxPreference) findPreference("key_cpu_boost_lbs");

		String color = "";
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			color = "#"+Integer.toHexString(col);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_CPU, Color.parseColor("#ff0099cc"));
			color = "#"+Integer.toHexString(col);
		}
		else {
			String col = mContext.getResources().getStringArray(R.array.menu_colors)[2];
			color = col;
		}
		mCpuMaxFreq.setTitleColor(color);
		mCpuMinFreq.setTitleColor(color);
		mCpuBoostFreq.setTitleColor(color);
		mCpuSync.setTitleColor(color);
		mCpuGovernor.setTitleColor(color);
		mAdvancedGovernor.setTitleColor(color);
		mCpuTemp.setTitleColor(color);
		mCpuBoostHp.setTitleColor(color);
		mCpuBoostLbs.setTitleColor(color);
		mCpuBoostMs.setTitleColor(color);
		mInputBoostMs.setTitleColor(color);

		mCpuMaxFreq.setCategory(category);
		mCpuMinFreq.setCategory(category);
		mCpuBoostFreq.setCategory(category);
		mCpuGovernor.setCategory(category);
		mCpuTemp.setCategory(category);
		mCpuBoostHp.setCategory(category);
		mCpuBoostLbs.setCategory(category);
		mCpuBoostMs.setCategory(category);
		mInputBoostMs.setCategory(category);
		mCpuSync.setCategory(category);

		mCpuMaxFreq.setKey(MAX_FREQ_FILE);
		mCpuMinFreq.setKey(MIN_FREQ_FILE);
		mCpuBoostFreq.setKey(CPU_BOOST_FREQ);
		mCpuGovernor.setKey(GOVERNOR_FILE);
		mCpuTemp.setKey(TEMP_FILE);
		mCpuBoostHp.setKey(CPU_BOOST_HP);
		mCpuBoostLbs.setKey(CPU_BOOST_LBS);
		mCpuBoostMs.setKey(CPU_BOOST_MS);
		mInputBoostMs.setKey(INPUT_BOOST_MS);
		mCpuSync.setKey(SYNC_THRESHOLD);
		
		String[] frequencies = Helpers.getFrequencies();
		String[] governors = Helpers.getGovernors();
		String[] names = Helpers.getFrequenciesNames();

		mCpuMaxFreq.setEntries(names);
		mCpuMaxFreq.setEntryValues(frequencies);
		
		mCpuMinFreq.setEntries(names);
		mCpuMinFreq.setEntryValues(frequencies);
		
		mCpuBoostFreq.setEntries(names);
		mCpuBoostFreq.setEntryValues(frequencies);
		
		mCpuSync.setEntries(names);
		mCpuSync.setEntryValues(frequencies);
		
		mCpuGovernor.setEntries(governors);
		mCpuGovernor.setEntryValues(governors);


		mCpuMaxFreq.setSummary(Helpers.readOneLine(MAX_FREQ_FILE));
		mCpuMaxFreq.setValue(mCpuMaxFreq.getSummary().toString());

		mCpuMinFreq.setSummary(Helpers.readOneLine(MIN_FREQ_FILE));
		mCpuMinFreq.setValue(mCpuMinFreq.getSummary().toString());
		
		mCpuBoostFreq.setSummary(Helpers.readOneLine(CPU_BOOST_FREQ));
		mCpuBoostFreq.setValue(mCpuBoostFreq.getSummary().toString());
		
		mCpuSync.setSummary(Helpers.readOneLine(SYNC_THRESHOLD));
		mCpuSync.setValue(mCpuSync.getSummary().toString());
		
		Helpers.runRootCommand("chmod 655 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
		mCpuGovernor.setSummary(Helpers.getCurrentGovernor());
		mCpuGovernor.setValue(mCpuGovernor.getSummary().toString());
		mCpuTemp.setSummary(Helpers.getFileContent(new File(TEMP_FILE)));
		mCpuBoostMs.setSummary(Helpers.getFileContent(new File(CPU_BOOST_MS)));
		mInputBoostMs.setSummary(Helpers.getFileContent(new File(INPUT_BOOST_MS)));

		mCpuMaxFreq.setOnPreferenceChangeListener(this);
		mCpuMinFreq.setOnPreferenceChangeListener(this);
		mCpuBoostFreq.setOnPreferenceChangeListener(this);
		mCpuSync.setOnPreferenceChangeListener(this);
		mCpuGovernor.setOnPreferenceChangeListener(this);
		mCpuTemp.setOnPreferenceClickListener(this);
		mCpuBoostMs.setOnPreferenceClickListener(this);
		mInputBoostMs.setOnPreferenceClickListener(this);
		mAdvancedGovernor.excludeFromDialog(true);

		mAdvancedGovernor.hideBoot(true);
		setRetainInstance(true);
		
		mCpuBoostHp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+CPU_BOOST_HP;
					value = "Y";
				} else {
					cmd = "echo N > "+CPU_BOOST_HP;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});
		
		String HpBoostState = Helpers.getFileContent(new File(CPU_BOOST_HP));
		if(HpBoostState.equals("Y")) {
			mCpuBoostHp.setChecked(true);
			mCpuBoostHp.setValue("Y");
		}else if(HpBoostState.equals("N")) {
			mCpuBoostHp.setChecked(false);
			mCpuBoostHp.setValue("N");
		}
		
		mCpuBoostLbs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String cmd = null;
				String value = null;
				if (newValue.toString().equals("true")) {
					cmd = "echo Y > "+CPU_BOOST_LBS;
					value = "Y";
				} else {
					cmd = "echo N > "+CPU_BOOST_LBS;
					value = "N";
				}
				CMDProcessor.runSuCommand(cmd);
				updateListDb(preference, value, ((CustomCheckBoxPreference) preference).isBootChecked());
				return true;
			}
		});
		
		String LbsState = Helpers.getFileContent(new File(CPU_BOOST_LBS));
		if(LbsState.equals("Y")) {
			mCpuBoostLbs.setChecked(true);
			mCpuBoostLbs.setValue("Y");
		}else if(LbsState.equals("N")) {
			mCpuBoostLbs.setChecked(false);
			mCpuBoostLbs.setValue("N");
		}
		
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
		if(pref == mCpuMaxFreq) {
			String value = (String) newValue;
			mCpuMaxFreq.setSummary(value);
			mCpuMaxFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MAX_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuMinFreq) {
			String value = (String) newValue;
			mCpuMinFreq.setSummary(value);
			mCpuMinFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MIN_FREQ_FILE);
			updateListDb(pref, value,((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuGovernor) {
			String value = ((String)newValue).trim().replaceAll(" ", "").replaceAll("\n", "");
			mCpuGovernor.setSummary(value);
			mCpuGovernor.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+GOVERNOR_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());

		}
		if(pref == mCpuBoostFreq) {
			String value = (String) newValue;
			mCpuBoostFreq.setSummary(value);
			mCpuBoostFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+CPU_BOOST_FREQ);
			updateListDb(pref, value,((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuSync) {
			String value = (String) newValue;
			mCpuSync.setSummary(value);
			mCpuSync.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+SYNC_THRESHOLD);
			updateListDb(pref, value,((CustomListPreference) pref).isBootChecked());
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(final Preference pref) {
		// TODO Auto-generated method stub
		if(pref == mAdvancedGovernor) {
			Fragment f = new CpuGovernorPreferenceFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			// This adds the newly created Preference fragment to my main layout, shown below
			ft.replace(R.id.activity_container,f);
			// By hiding the main fragment, transparency isn't an issue
			ft.addToBackStack("TAG");
			ft.commit();
		}
		if(pref == mCpuTemp) {
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
					updateListDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		if(pref == mCpuBoostMs) {
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
					updateListDb(pref, value, ((CustomPreference) pref).isBootChecked());
				}
			} );
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
			Window window = dialog.getWindow();
			window.setLayout(800, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		if(pref == mInputBoostMs) {
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
					updateListDb(pref, value, ((CustomPreference) pref).isBootChecked());
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


	private void createPreference(PreferenceCategory mCategory, File file, String color) {
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
				et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				et.setGravity(Gravity.CENTER_HORIZONTAL);
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo "+value+" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
						updateListDb(pref, value, pref.isBootChecked());
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

	private void updateListDb(final Preference p, final String value, final boolean isChecked) {

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
