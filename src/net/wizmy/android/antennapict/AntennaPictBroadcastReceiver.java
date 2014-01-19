package net.wizmy.android.antennapict;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AntennaPictBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (sp.getBoolean(context.getString(R.string.key_auto_start_at_boot),
				false)) {
			Intent serviceIntent = new Intent(context, AntennaPictService.class);
			context.startService(serviceIntent);
		}
	}
}
