package net.wizmy.android.antennapict;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AntennaState extends PhoneStateListener {

	private static final String TAG = AntennaState.class.getSimpleName();

	public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
	public static final int SIGNAL_STRENGTH_POOR = 1;
	public static final int SIGNAL_STRENGTH_MODERATE = 2;
	public static final int SIGNAL_STRENGTH_GOOD = 3;
	public static final int SIGNAL_STRENGTH_GREAT = 4;

	private int mDataConnectionState;
	private int mSignalLevel;

	interface AntennaStateChangedListener {

		public void onAntennaLevelChanged(int state, int level);

		public void onAntennaConnectionStateChanged(int state, int level);
	}

	private AntennaStateChangedListener listener;

	public AntennaState(AntennaStateChangedListener listener) {
		super();
		this.listener = listener;
		mDataConnectionState = TelephonyManager.DATA_DISCONNECTED;
		mSignalLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		int level = getLevel(signalStrength);
		if (level != mSignalLevel) {
			mSignalLevel = level;
			listener.onAntennaLevelChanged(mDataConnectionState, mSignalLevel);
		}
		super.onSignalStrengthsChanged(signalStrength);
	}

	@Override
	public void onDataConnectionStateChanged(int state) {
		if (state != mDataConnectionState) {
			mDataConnectionState = state;
			listener.onAntennaConnectionStateChanged(mDataConnectionState,
					mSignalLevel);
		}
		super.onDataConnectionStateChanged(state);
	}

	/* ref. frameworks/base/telephony/java/android/telephony/SignalStrength.java */
	public int getLevel(SignalStrength signalStrength) {
		int level;
		if (signalStrength.isGsm()) {
			level = getGsmLevel(signalStrength);
		} else {
			int cdmaLevel = getCdmaLevel(signalStrength);
			int evdoLevel = getEvdoLevel(signalStrength);
			if (evdoLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
				/* We don't know evdo, use cdma */
				level = cdmaLevel;
			} else if (cdmaLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
				/* We don't know cdma, use evdo */
				level = evdoLevel;
			} else {
				/* We know both, use the lowest level */
				level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
			}
		}
		return level;
	}

	private int getGsmLevel(SignalStrength signalStrength) {
		int level;

		int asu = signalStrength.getGsmSignalStrength();
		if (asu <= 2 || asu == 99) {
			level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		} else if (asu >= 12) {
			level = SIGNAL_STRENGTH_GREAT;
		} else if (asu >= 8) {
			level = SIGNAL_STRENGTH_GOOD;
		} else if (asu >= 5) {
			level = SIGNAL_STRENGTH_MODERATE;
		} else {
			level = SIGNAL_STRENGTH_POOR;
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "GSM: strength = " + asu);
		}
		return level;
	}

	private int getCdmaLevel(SignalStrength signalStrength) {
		int cdmaDbm = signalStrength.getCdmaDbm();
		int cdmaEcio = signalStrength.getCdmaEcio();
		int levelDbm;
		int levelEcio;

		if (cdmaDbm >= -75) {
			levelDbm = SIGNAL_STRENGTH_GREAT;
		} else if (cdmaDbm >= -85) {
			levelDbm = SIGNAL_STRENGTH_GOOD;
		} else if (cdmaDbm >= -95) {
			levelDbm = SIGNAL_STRENGTH_MODERATE;
		} else if (cdmaDbm >= -100) {
			levelDbm = SIGNAL_STRENGTH_POOR;
		} else {
			levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		}

		if (cdmaEcio >= -90) {
			levelEcio = SIGNAL_STRENGTH_GREAT;
		} else if (cdmaEcio >= -110) {
			levelEcio = SIGNAL_STRENGTH_GOOD;
		} else if (cdmaEcio >= -130) {
			levelEcio = SIGNAL_STRENGTH_MODERATE;
		} else if (cdmaEcio >= -150) {
			levelEcio = SIGNAL_STRENGTH_POOR;
		} else {
			levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		}

		int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "CDMA: dbm = " + cdmaDbm + ", ecio = " + cdmaEcio);
		}
		return level;
	}

	private int getEvdoLevel(SignalStrength signalStrength) {
		int evdoDbm = signalStrength.getEvdoDbm();
		int evdoSnr = signalStrength.getEvdoSnr();
		int levelEvdoDbm;
		int levelEvdoSnr;

		if (evdoDbm >= -65) {
			levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
		} else if (evdoDbm >= -75) {
			levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
		} else if (evdoDbm >= -90) {
			levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
		} else if (evdoDbm >= -105) {
			levelEvdoDbm = SIGNAL_STRENGTH_POOR;
		} else {
			levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		}

		if (evdoSnr >= 7) {
			levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
		} else if (evdoSnr >= 5) {
			levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
		} else if (evdoSnr >= 3) {
			levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
		} else if (evdoSnr >= 1) {
			levelEvdoSnr = SIGNAL_STRENGTH_POOR;
		} else {
			levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		}

		int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "EVDO: dbm = " + evdoDbm + ", snr = " + evdoSnr);
		}
		return level;
	}
}
