package at.fhhgbg.mc.profileswitcher;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

/**
 * 
 * Activity that shows the possible general settings for the application.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener,OnPreferenceClickListener,OnClickListener {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;

	/**
	 * Sets up the actionbar.
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref.getBoolean("root", false) && !RootTools.isAccessGiven()){
			Editor editor = pref.edit();
			editor.putBoolean("root", false);
			editor.commit();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * If the home button on the actionbar is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
		Preference systemappPreference = (Preference) super.findPreference("systemapp");
		systemappPreference.setOnPreferenceClickListener(this);
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		pref.registerOnSharedPreferenceChangeListener(this);

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

	}

	/**
	 * When the device uses a multipane layout.
	 * 
	 * @see android.preference.PreferenceActivity#onIsMultiPane()
	 */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/**
	 * @see android.preference.PreferenceActivity#onBuildHeaders(java.util.List)
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences _pref, String _key) {

		if (_pref.getBoolean("notification", false)) {
			Intent resultIntent = new Intent(this, ListDialogActivity.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
					0, resultIntent, 0);

			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					this);
			nBuilder.setSmallIcon(R.drawable.profile_switcher_notification_icon);
			nBuilder.setContentText(getResources().getString(
					R.string.textNotificationContentText));
			nBuilder.setContentTitle(getResources().getString(
					R.string.textNotificationTitle));
			nBuilder.setContentIntent(resultPendingIntent);
			nBuilder.setOngoing(true);
			nBuilder.setWhen(0);
			nBuilder.setPriority(1);

			Notification notification = nBuilder.build();
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(123, notification);
		} else {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(123);
		}
		
		if(_pref.getBoolean("root", false)){
			if(!RootTools.isAccessGiven()){
				AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle("Root access not granted");
				dialog.setMessage("Please check if your device is rooted and you have a superuser app installed!");
				dialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
					
				});
				dialog.show();
				Editor editor = _pref.edit();
				editor.putBoolean("root", false);
				editor.commit();
				CheckBoxPreference checkBox = (CheckBoxPreference) super.findPreference("root");
				checkBox.setChecked(false);
			}
		}
	}

	/**
	 * This code will be executed, when the
	 */
	@Override
	public boolean onPreferenceClick(Preference arg0) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		dialog.setTitle("Install SwiP as system-app?");
		dialog.setMessage("Please understand that this will prevent the app from being uninstalled, make sure to get back to these settings to make it uninstallable again.");
		dialog.setPositiveButton("Ok", this);
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();					
			}
			
		});
		dialog.show();
		return true;
	}

	/**
	 * When the user decides to install the app as system-app this will be called
	 */
	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if(RootTools.isAccessGiven()){
			//this will copy the apk to the system-apps folder and therefor swip will become a system-app
			CommandCapture command;
			command = new CommandCapture(1,"mount -o remount,rw /system", 						//mounts the system partition to be writeable
					"cp /data/app/at.fhhgbg.mc.profileswitcher-[12].apk /system/app/",			//copies the apk of the app to the system-apps folder
					"chmod 644 /system/app/at.fhhgbg.mc.profileswitcher-[12].apk",				//fixes the permissions
					"mount -o remount,r /system");												//mounts the system partition to be read-only again
			
			try {
				RootTools.getShell(true).add(command);
				RootTools.closeAllShells();
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				pref.edit().putBoolean("systemapp", true).commit();					//saves the fact that it can use system-app possibilities now
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RootDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			dialog.setTitle("Install Successful!");
			dialog.setMessage("Please restart your phone for the changes to take effect.");
			dialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
				
			});
			dialog.show();

			
		}
		else{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			dialog.setTitle("Root access not granted");
			dialog.setMessage("Please check if your device is rooted and you have a superuser app installed!");
			dialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
				
			});
			dialog.show();
		}
	}
}