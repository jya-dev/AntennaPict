package net.wizmy.android.antennapict;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AntennaPictService extends Service implements
		AntennaState.AntennaStateChangedListener {

	private static final String TAG = AntennaPictService.class.getSimpleName();

	/* switch to enable testing features */
	private static final boolean FEATURE_STOP_LISTENER_WHILE_SCREEN_OFF = false;
	private static final boolean FEATURE_LISTEN_AIRPLANE_MODE_CHANGE = true;

	private static final int NOTIFICATION_ID = 1;
	private static final long[] VIBRATE_PATTERN = { 0, 10, 100, 10 };

	/* see 'notification_priority_value' in res/values/array.xml */
	private static final int PRIORITY_MAX = 2;
	private static final int PRIORITY_HIGH = 1;
	private static final int PRIORITY_DEFAULT = 0;
	private static final int PRIORITY_LOW = -1;

	private static final String PREF_DATA_CONNECTION_STATE = "data_connection_state";

	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private boolean mDataConnected;
	private boolean mIsAirplaneMode;
	private int mIconColor;
	private int mNotificationPriority;
	private boolean mUseHighlightIcon;
	private boolean mNotifyConnStateChange;
	private boolean mVibrateOnNotify;
	private NotificationManager mNotificationManager = null;
	private TelephonyManager mTelephonyManager = null;

	private AntennaState mAntennaState;

	private BroadcastReceiver mScreenOnOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "SCREEN_ON");
				}
				startAntennaStateListener();
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "SCREEN_OFF");
				}
				stopAntennaStateListener();
			}
		}
	};

	private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
				boolean state = intent.getBooleanExtra("state", false);
				mIsAirplaneMode = state;
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "AIRPLANE_MODE_CHANGED: "
							+ (state ? "ON" : "OFF"));
				}
				if (mIsAirplaneMode) {
					stopAntennaStateListener();
					sendAntennaNotification(TelephonyManager.DATA_DISCONNECTED, 0, true);
				} else {
					sendAntennaNotification(TelephonyManager.DATA_DISCONNECTED, 0, true);
					startAntennaStateListener();
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = mSharedPreferences.edit();

		mAntennaState = new AntennaState(this);

		mIsAirplaneMode = AirplaneModeChecker.isAirplaneModeOn(this);
		mDataConnected = mSharedPreferences.getBoolean(
				PREF_DATA_CONNECTION_STATE, false);
		mIconColor = Integer.valueOf(mSharedPreferences.getString(
				getString(R.string.key_icon_color), "0"));
		mNotificationPriority = Integer.valueOf(mSharedPreferences.getString(
				getString(R.string.key_notification_priority), "0"));
		mUseHighlightIcon = mSharedPreferences.getBoolean(
				getString(R.string.key_always_highlight_icon), false);
		mNotifyConnStateChange = mSharedPreferences.getBoolean(
				getString(R.string.key_notify_connection_state_change), false);
		mVibrateOnNotify = mSharedPreferences.getBoolean(
				getString(R.string.key_vibrate_on_notify), false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG,
					"onStartCommand(): intent="
							+ (intent != null ? intent.toString() : "(null)")
							+ ", flags=" + flags + ", startId=" + startId);
		}

		if (intent != null) {
			sendAntennaNotification(getTelephonyManager().getDataState(),
					AntennaState.SIGNAL_STRENGTH_NONE_OR_UNKNOWN, false);
		}

		startAntennaStateListener();

		if (FEATURE_STOP_LISTENER_WHILE_SCREEN_OFF) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			registerReceiver(mScreenOnOffReceiver, filter);
		}

		if (FEATURE_LISTEN_AIRPLANE_MODE_CHANGE) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			registerReceiver(mAirplaneModeReceiver, filter);
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}

		stopAntennaStateListener();

		if (FEATURE_STOP_LISTENER_WHILE_SCREEN_OFF) {
			unregisterReceiver(mScreenOnOffReceiver);
		}

		if (FEATURE_LISTEN_AIRPLANE_MODE_CHANGE) {
			unregisterReceiver(mAirplaneModeReceiver);
		}
		
		mEditor.commit();
	}

	private TelephonyManager getTelephonyManager() {
		if (mTelephonyManager == null) {
			mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		}
		return mTelephonyManager;
	}

	private void startAntennaStateListener() {
		getTelephonyManager().listen(
				mAntennaState,
				AntennaState.LISTEN_SIGNAL_STRENGTHS
						| AntennaState.LISTEN_DATA_CONNECTION_STATE);
	}

	private void stopAntennaStateListener() {
		getTelephonyManager().listen(mAntennaState, AntennaState.LISTEN_NONE);
	}

	private void sendAntennaNotification(int state, int level, boolean useTicker) {
		int isFully = (mUseHighlightIcon || state == TelephonyManager.DATA_CONNECTED) ? 1 : 0;
		int icon = (mIsAirplaneMode) ? AntennaPictConstants.Icon.AIRPLANE_MODE[mIconColor]
				: AntennaPictConstants.Icon.SIGNAL_STRENGTH[mIconColor][isFully][level];
		String title = (mIsAirplaneMode) ? getString(R.string.data_disconnected_airplane_mode)
				: (state == TelephonyManager.DATA_CONNECTED) ? getString(R.string.data_connected)
						: getString(R.string.data_disconnected);
		String text = getString(R.string.signal_level, level);
		String ticker = (useTicker && mNotifyConnStateChange) ? title : null;
		boolean vib = (useTicker && mNotifyConnStateChange && mVibrateOnNotify) ? true : false;
		int pri = getNotificationCompatPriority(mNotificationPriority);
		sendOngoingNotification(icon, title, text, ticker, vib, pri);
	}

	private void sendOngoingNotification(int icon, String title, String text,
			String ticker, boolean vibrate, int priority) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(this, AntennaPictActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(icon)
		       .setContentTitle(title)
		       .setContentText(text)
		       .setTicker(ticker)
		       .setPriority(priority)
		       .setOngoing(true)
		       .setWhen(0) // do not show timestamp
		       .setContentIntent(contentIntent);
		if (vibrate) {
			builder.setVibrate(VIBRATE_PATTERN);
		}

		Notification notification = builder.build();
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private int getNotificationCompatPriority(int value) {
		if (value == PRIORITY_MAX) {
			return NotificationCompat.PRIORITY_MAX;
		} else if (value == PRIORITY_HIGH) {
			return NotificationCompat.PRIORITY_HIGH;
		} else if (value == PRIORITY_DEFAULT) {
			return NotificationCompat.PRIORITY_DEFAULT;
		} else if (value == PRIORITY_LOW) {
			return NotificationCompat.PRIORITY_LOW;
		} else {
			return NotificationCompat.PRIORITY_DEFAULT;
		}
	}

	@Override
	public void onAntennaLevelChanged(int state, int level) {
		sendAntennaNotification(state, level, false);
	};

	@Override
	public void onAntennaConnectionStateChanged(int state, int level) {
		boolean connectionState = (state == TelephonyManager.DATA_CONNECTED);
		boolean preConnectionState = mDataConnected;

		if (connectionState != preConnectionState) {
			sendAntennaNotification(state, level, true);
		}

		// save data connection state to restore when service is restarted.
		mDataConnected = connectionState;
		mEditor.putBoolean(PREF_DATA_CONNECTION_STATE, mDataConnected);
	};
}
