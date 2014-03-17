package at.fhhgb.mc.swip.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.services.Handler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

/**
 * Activity that shows the possible general settings for the application.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	
	/**
	 * Sets up the actionbar and removes the tick for root, if no root access is actually given.
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
	
	@Override
	protected boolean isValidFragment(String fragmentName) {
		if(fragmentName.equals("at.fhhgb.mc.swip.SettingsActivity$GeneralPreferenceFragment")){
			Log.i("SettingsActivity", "valid fragment started");
			return true;
		} else {
			Log.i("SettingsActivity", "invalid fragment started");
			return false;
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

		PreferenceScreen screen = getPreferenceScreen();
		Handler handler = new Handler(this);
		
		if (pref.getBoolean("systemapp", false) || handler.checkSystemapp()) {
			Preference install = findPreference("systemapp");
			screen.removePreference(install);
			Preference removeSystemappPreference = (Preference) super.findPreference("removeSystemapp");
			removeSystemappPreference.setOnPreferenceClickListener(this);
		}
		
		else if (!pref.getBoolean("systemapp", false)) {
			Preference uninstall = findPreference("removeSystemapp");
			screen.removePreference(uninstall);
			Preference systemappPreference = (Preference) super.findPreference("systemapp");
			systemappPreference.setOnPreferenceClickListener(this);
		}
		
		// binds summary to preference
		bindPreferenceSummaryToValue(findPreference("language"));
		
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

	/**
	 * Applies the selected settings: launches the notification if activated or destroys it otherwise.
	 * Also checks if root is available, when the user ticks the root option.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences _pref, String _key) {

		if (_pref.getBoolean("notification", false)) {
			Handler handler = new Handler (this);
			handler.updateNotification();
		} else {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(123);
		}
		
		if(_pref.getBoolean("root", false)){
			if(!RootTools.isAccessGiven()){
				AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle(getResources().getString(R.string.pref_title_noRoot));
				dialog.setMessage(getResources().getString(R.string.pref_text_noRoot));
				dialog.setNeutralButton(getResources().getString(R.string.pref_neutral_button), new DialogInterface.OnClickListener(){

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
		
		setLocale(_pref.getString("language", "en"));
	}

	/**
	 * This code will be executed, when the install as system app preference is clicked.
	 * Displays some information about the installation.
	 */
	@Override
	public boolean onPreferenceClick(Preference _preference) {
		if(_preference.getKey().equals("systemapp")){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			dialog.setTitle(getResources().getString(R.string.pref_headline_systemapp));
			dialog.setMessage(getResources().getString(R.string.pref_text_systemapp));
			dialog.setPositiveButton(getResources().getString(R.string.pref_positive_button), new InstallOnClick(this));
			dialog.setNegativeButton(getResources().getString(R.string.pref_negative_button), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
				
			});
			Log.i("SettingsActivity", "Install as systemapp selected");
			dialog.show();
		}
		else if(_preference.getKey().equals("removeSystemapp")){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			dialog.setTitle(getResources().getString(R.string.pref_headline_removeSystemapp));
			dialog.setMessage(getResources().getString(R.string.pref_text_removeSystemapp));
			dialog.setPositiveButton(getResources().getString(R.string.pref_positive_button), new UninstallOnClick(this));
			dialog.setNegativeButton(getResources().getString(R.string.pref_negative_button), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
				
			});
			Log.i("SettingsActivity", "Uninstall as systemapp selected");
			dialog.show();
		}
		return true;
	}

	
	/**
	 * Inner class used to install the app as system-app, if the user confirms the dialog.
	 * @author Florian Schrofner & Dominik Koeltringer
	 *
	 */
	private class InstallOnClick implements DialogInterface.OnClickListener{
		Context context;
		
		InstallOnClick(Context _context){
			context = _context;
		}

		/**
		 * Copies the apk file to the system app directory of the installed Android version
		 * and displays a dialog.
		 */
		@Override
		public void onClick(DialogInterface _dialog, int _which) {
			if(RootTools.isAccessGiven()){
				//this will copy the apk to the system-apps folder and therefor swip will become a system-app
				CommandCapture command;
				//in kitkat the systemapps were moved to the /system/priv-app folder
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
					command = new CommandCapture(1,"mount -o remount,rw /system", 						//mounts the system partition to be writeable
							"cp /data/app/at.fhhgb.mc.swip-[12].apk /system/priv-app/",					//copies the apk of the app to the system-apps folder
							"chmod 644 /system/priv-app/at.fhhgb.mc.swip-[12].apk",						//fixes the permissions
							"mount -o remount,r /system");												//mounts the system partition to be read-only again
				} else{
					command = new CommandCapture(1,"mount -o remount,rw /system", 						
							"cp /data/app/at.fhhgb.mc.swip-[12].apk /system/app/",
							"chmod 644 /system/app/at.fhhgb.mc.swip-[12].apk",		
							"mount -o remount,r /system");									
				}
				
				try {
					RootTools.getShell(true).add(command);
					RootTools.closeAllShells();
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
					
					Handler handler = new Handler(getApplicationContext());
					

					//saves the fact that it can use system-app possibilities now (there still will be a real check before using the system-app functionality)
					pref.edit().putBoolean("systemapp", true).commit();							
					try {
						ComponentName comp = new ComponentName(context, context.getClass());
						PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
						//puts the versionname of the app into shared preferences for update reasons
						pref.edit().putString("versionname", pinfo.versionName).commit();
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (RootDeniedException e) {
					e.printStackTrace();
				}
				
				AlertDialog.Builder dialog = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle(getResources().getString(R.string.pref_title_systemapp_successful));
				dialog.setMessage(getResources().getString(R.string.pref_text_pleaseRestart));
				dialog.setNeutralButton(getResources().getString(R.string.pref_neutral_button), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
					
				});
				dialog.show();
				
			}
			else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle(getResources().getString(R.string.pref_title_noRoot));
				dialog.setMessage(getResources().getString(R.string.pref_text_noRoot));
				dialog.setNeutralButton(getResources().getString(R.string.pref_neutral_button), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
					
				});
				dialog.show();
			}
			
		}	
	}
	
	/**
	 * Inner class used to uninstall the app as system-app, if the user confirms.
	 * @author Florian Schrofner & Dominik Koeltringer
	 *
	 */
	private class UninstallOnClick implements DialogInterface.OnClickListener{

		Context context;
		
		UninstallOnClick(Context _context){
			context = _context;
		}

		/**
		 * Removes the copied apk from the system app directory and displays a dialog.
		 */
		@Override
		public void onClick(DialogInterface _dialog, int _which) {
			if(RootTools.isAccessGiven()){
				//this will copy the apk to the system-apps folder and therefor swip will become a system-app
				CommandCapture command;
				//the systemapps were moved to the /system/priv-app folder in kitkat
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
					command = new CommandCapture(1,"mount -o remount,rw /system", 						//mounts the system partition to be writeable
							"rm /system/priv-app/at.fhhgb.mc.swip-[12].apk",				//removes the apk inside the system-apps folder
							"mount -o remount,r /system");												//mounts the system partition to be read-only again
				} else {
					command = new CommandCapture(1,"mount -o remount,rw /system", 						
							"rm /system/app/at.fhhgb.mc.swip-[12].apk",						
							"mount -o remount,r /system");	
				}																	
				try {
					RootTools.getShell(true).add(command);
					RootTools.closeAllShells();
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
					pref.edit().putBoolean("systemapp", false).commit();							//saves that the app is not a systemapp anymore
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (RootDeniedException e) {
					e.printStackTrace();
				}
				
				AlertDialog.Builder dialog = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle(getResources().getString(R.string.pref_title_removeSystemapp_successful));
				dialog.setMessage(getResources().getString(R.string.pref_text_pleaseRestart));
				dialog.setNeutralButton(getResources().getString(R.string.pref_neutral_button), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
					
				});
				dialog.show();

				
			}
			else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setTitle(getResources().getString(R.string.pref_title_noRoot));
				dialog.setMessage(getResources().getString(R.string.pref_text_noRoot));
				dialog.setNeutralButton(getResources().getString(R.string.pref_neutral_button), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();					
					}
					
				});
				dialog.show();
			}
			
		}
	}
	
	public void setLocale(String lang) { 
		Log.i("SettingsActivity", "setLocale: " + lang);
		
		Locale myLocale = new Locale(lang); 
		Resources res = getResources(); 
		DisplayMetrics dm = res.getDisplayMetrics(); 
		Configuration conf = res.getConfiguration(); 
		conf.locale = myLocale; 
		res.updateConfiguration(conf, dm); 
		Intent refresh = new Intent(this, MainActivity.class); 
		refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(refresh); 
		} 
}