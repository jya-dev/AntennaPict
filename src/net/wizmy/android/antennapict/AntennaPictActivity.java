package net.wizmy.android.antennapict;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

public class AntennaPictActivity extends PreferenceActivity implements
		OnCheckedChangeListener, OnPreferenceChangeListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			addPreferencesFromResource(R.xml.main);
		} catch (ClassCastException e) {
			// Settings was corrupt. Reset to default values.
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			sp.edit().clear().commit();
			PreferenceManager.setDefaultValues(this, R.xml.main, true);
			addPreferencesFromResource(R.xml.main);
			Toast.makeText(this, R.string.toast_warning_preference_corrupted,
					Toast.LENGTH_LONG).show();
		}

		ListPreference iconColorList = (ListPreference) findPreference(getString(R.string.key_icon_color));
		iconColorList.setSummary(iconColorList.getEntry());
		iconColorList.setOnPreferenceChangeListener(this);

		ListPreference notificationPriorityList = (ListPreference) findPreference(getString(R.string.key_notification_priority));
		/* notificationPriorityList == null if platform is ICS or earlier */
		if (notificationPriorityList != null) {
			notificationPriorityList.setSummary(notificationPriorityList.getEntry());
			notificationPriorityList.setOnPreferenceChangeListener(this);
		}

		setupCheckBoxPreference(R.string.key_auto_start_at_boot);
		setupCheckBoxPreference(R.string.key_always_highlight_icon);
		setupCheckBoxPreference(R.string.key_notify_connection_state_change);
		setupCheckBoxPreference(R.string.key_vibrate_on_notify);

		Preference versionPreference = findPreference(getString(R.string.key_application_version));
		versionPreference.setSummary(getVersionName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.actions, menu);

		setupActionItem(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean checked) {
		Intent intent = new Intent(this, AntennaPictService.class);
		if (checked) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if (getString(R.string.key_icon_color).equals(key)) {
			int index = ((ListPreference) preference)
					.findIndexOfValue((String) newValue);
			CharSequence[] entries = ((ListPreference) preference).getEntries();
			preference.setSummary(entries[index]);
			restartServiceIfRunning();
			return true;
		} else if (getString(R.string.key_notification_priority).equals(key)) {
			int index = ((ListPreference) preference)
					.findIndexOfValue((String) newValue);
			CharSequence[] entries = ((ListPreference) preference).getEntries();
			preference.setSummary(entries[index]);
			restartServiceIfRunning();
			return true;
		} else if (getString(R.string.key_always_highlight_icon).equals(key)
				|| getString(R.string.key_notify_connection_state_change).equals(key)
				|| getString(R.string.key_vibrate_on_notify).equals(key)) {
			restartServiceIfRunning();
			return true;
		}
		return true;
	}

	private void setupCheckBoxPreference(final int key) {
		CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(getString(key));
		checkBox.setOnPreferenceChangeListener(this);
	}

	private void setupActionItem(Menu menu) {
		Switch serviceSwitch = (Switch) menu.findItem(R.id.menu_switch)
				.getActionView().findViewById(R.id.actionbar_switch);

		serviceSwitch.setChecked(isServiceRunning(this,
				AntennaPictService.class));
		serviceSwitch.setOnCheckedChangeListener(this);
	}

	private String getVersionName() {
		String versionName;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			versionName = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			versionName = getString(R.string.unknown);
		}
		return versionName;
	}

	private void restartService() {
		Intent intent = new Intent(this, AntennaPictService.class);
		stopService(intent);
		startService(intent);
	}

	private void restartServiceIfRunning() {
		if (isServiceRunning(this, AntennaPictService.class)) {
			restartService();
		}
	}

	public boolean isServiceRunning(Context c, Class<?> cls) {
		ActivityManager activityManager = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : runningServices) {
			if (cls.getName().equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
