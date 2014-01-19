package net.wizmy.android.antennapict;

import android.content.Context;
import android.provider.Settings;

public class AirplaneModeCheckerJB {

	@SuppressWarnings("deprecation")
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
}
