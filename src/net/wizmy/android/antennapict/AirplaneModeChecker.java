package net.wizmy.android.antennapict;

import android.content.Context;
import android.os.Build;

public class AirplaneModeChecker {
	public static boolean isAirplaneModeOn(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return AirplaneModeCheckerJBMR1.isAirplaneModeOn(context);
		} else {
			return AirplaneModeCheckerJB.isAirplaneModeOn(context);
		}
	}
}
