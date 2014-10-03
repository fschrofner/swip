package at.fhhgb.mc.swip.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import at.flosch.logwrap.Log;

/**
 * Service used to start the notification, if the permanent notification option
 * is activated.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class AutostartService extends Service {
	final static String TAG = "AutostartService";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * Builds and shows the notification, when the service is started and the
	 * permanent notification is enabled inside the settings.
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Log.i(TAG, "checking if notification is enabled");

		// checks if the permanent notification option is enabled
		if (pref.getBoolean("notification", false)) {
			Handler handler = new Handler(this);
			handler.updateNotification();
		}
		// starts the trigger service
		Intent triggerIntent = new Intent(getApplicationContext(), at.fhhgb.mc.swip.trigger.TriggerService.class);
		startService(triggerIntent);
		return super.onStartCommand(intent, flags, startId);
	}

}