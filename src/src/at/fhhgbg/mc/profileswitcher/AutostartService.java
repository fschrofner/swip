package at.fhhgbg.mc.profileswitcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Service used to start the notification, if the permanent notification option
 * is activated.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class AutostartService extends Service {

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
		Log.i("AutostartService", "checking if notification is enabled");

		// checks if the permanent notification option is enabled
		if (pref.getBoolean("notification", false)) {
			Intent resultIntent = new Intent(this, ListDialogActivity.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
					0, resultIntent, 0);

			// builds the notification
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					this);
			nBuilder.setSmallIcon(R.drawable.profile_switcher_notification_icon);
			nBuilder.setContentText(getResources().getString(
					R.string.textNotificationContentText));
			nBuilder.setContentTitle(getResources().getString(
					R.string.textNotificationTitle) + " " + pref.getString("active_profile", getResources().getString(
							R.string.textNotificationNoProfile)));
			nBuilder.setContentIntent(resultPendingIntent);
			nBuilder.setOngoing(true);
			nBuilder.setWhen(0);
			nBuilder.setPriority(1);

			Notification notification = nBuilder.build();
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(123, notification);
		}
		
		Intent triggerIntent = new Intent(getApplicationContext(), at.fhhgbg.mc.profileswitcher.trigger.TriggerService.class);
		startService(triggerIntent);
		return super.onStartCommand(intent, flags, startId);
	}

}