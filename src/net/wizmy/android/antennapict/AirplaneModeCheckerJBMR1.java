package net.wizmy.android.antennapict;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class AirplaneModeCheckerJBMR1 {

	public static boolean isAirplaneModeOn(Context context) {
		return Settings.Global.getInt(context.getContentResolver(),
				Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}
}
