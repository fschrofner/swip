package at.fhhgbg.mc.profileswitcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.stericson.RootTools.RootTools;

/**
 * Activity used to edit the different settings of a profile.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ProfileEditActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	private String previousName; 	// saves the previous profile name for the case the profile gets renamed
									// (so the previous file of this profile can be deleted)

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
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
	 * Adds the buttons to the actionbar (apply, cancel and write to tag).
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_edit, menu);
		return true;
	}

	/**
	 * Sets up the actionbar.
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	/**
	 * If one of the items on the actionbar is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.save_profile) {
			this.saveProfile();
			this.finish();
		} else if (item.getItemId() == R.id.cancel_profile) {
			this.finish();
		} else if (item.getItemId() == R.id.write_to_tag) {
			this.saveProfile();
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);

			Intent intent = new Intent(this, NfcWriterActivity.class);
			intent.putExtra("fileName", pref.getString("name", "default"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		} else if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
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

		previousName = pref.getString("name", "default name");

		pref.registerOnSharedPreferenceChangeListener(this);

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_edit_general);

		PreferenceCategory fakeHeader;

		// Add 'Sound' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_sound);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_edit_sound);

		// Add 'Connectivity' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_connectivity);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_edit_connectivity);

		// Add 'Display' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_display);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_edit_display);

		// Binds the summaries of preferences to their values.
		// When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("name"));
		bindPreferenceSummaryToValue(findPreference("ringer_mode"));
		bindPreferenceSummaryToValue(findPreference("gps"));
		bindPreferenceSummaryToValue(findPreference("mobile_data"));
		bindPreferenceSummaryToValue(findPreference("wifi"));
		bindPreferenceSummaryToValue(findPreference("bluetooth"));
		bindPreferenceSummaryToValue(findPreference("airplane_mode"));
		bindPreferenceSummaryToValue(findPreference("display_auto_mode"));
		bindPreferenceSummaryToValue(findPreference("display_time_out"));

		
		//disables the option to set the exact display brightness, when auto-brightness is enabled
		if (pref.getString("display_auto_mode", "unchanged").equals("enabled")) {
			findPreference("display_brightness").setEnabled(false);
		}

		//disables the option to set the ringtone volume, when either vibrate or silent ring mode is selected
		if (pref.getString("ringer_mode", "unchanged").equals("vibrate")
				|| pref.getString("ringer_mode", "unchanged").equals("silent")) {
			findPreference("ringtone_volume").setEnabled(false);
		}
		
		//disables all other connectivity settings, when airplane mode is enabled
		if(pref.getString("airplane_mode", "unchanged").equals("enabled")){
			findPreference("gps").setEnabled(false);
			findPreference("mobile_data").setEnabled(false);
			findPreference("wifi").setEnabled(false);
			findPreference("bluetooth").setEnabled(false);
		}
		
		if (!pref.getBoolean("root", false)){
			findPreference("gps").setEnabled(true);
			findPreference("mobile_data").setEnabled(true);
			findPreference("wifi").setEnabled(true);
			findPreference("bluetooth").setEnabled(true);
		}
		
		//checks if the root access is disabled inside the settings and disables all root settings in this case
		PreferenceScreen screen = getPreferenceScreen();
		
		if(!pref.getBoolean("root", false)){
			Preference airplane_mode = findPreference("airplane_mode");
			screen.removePreference(airplane_mode);
		}
		//if root is enabled, it checks if the app really has root permissions and disables all root settings otherwise
		else if(pref.getBoolean("root", false)){
			if(!RootTools.isAccessGiven()){
				
				//if no root access is given anymore, airplane mode gets set to unchanged and all other settings that may be block get enabled again.
				Preference airplane_mode = findPreference("airplane_mode");
				pref.edit().putString("airplane_mode", "unchanged").commit();
				findPreference("gps").setEnabled(true);
				findPreference("mobile_data").setEnabled(true);
				findPreference("wifi").setEnabled(true);
				findPreference("bluetooth").setEnabled(true);
				screen.removePreference(airplane_mode);
			}
		}

	}

	/**
	 * If the target device is on a multipane layout.
	 */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Saves the current settings of the activity to a profile object and lets
	 * it be written by the XmlCreator.
	 */
	public void saveProfile() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		String name = pref.getString("name", "Insert name");

		Profile profile = new Profile(name);

		if (pref.getString("ringer_mode", "unchanged").equals("silent")) {
			profile.setRingerMode(Profile.mode.silent);
		} else if (pref.getString("ringer_mode", "unchanged").equals("vibrate")) {
			profile.setRingerMode(Profile.mode.vibrate);
		} else if (pref.getString("ringer_mode", "unchanged").equals("normal")) {
			profile.setRingerMode(Profile.mode.normal);
		} else {
			profile.setRingerMode(Profile.mode.unchanged);
		}

		profile.setMediaVolume(pref.getInt("media_volume", -1));
		profile.setAlarmVolume(pref.getInt("alarm_volume", -1));

		if (!findPreference("ringtone_volume").isEnabled()) {
			profile.setRingtoneVolume(-1);
		} else {
			profile.setRingtoneVolume(pref.getInt("ringtone_volume", -1));
		}

		if (pref.getString("gps", "unchanged").equals("enabled")) {
			profile.setGps(Profile.state.enabled);
		} else if (pref.getString("gps", "unchanged").equals("disabled")) {
			profile.setGps(Profile.state.disabled);
		} else {
			profile.setGps(Profile.state.unchanged);
		}

		if (pref.getString("mobile_data", "unchanged").equals("enabled")) {
			profile.setMobileData(Profile.state.enabled);
		} else if (pref.getString("mobile_data", "unchanged")
				.equals("disabled")) {
			profile.setMobileData(Profile.state.disabled);
		} else {
			profile.setMobileData(Profile.state.unchanged);
		}

		if (pref.getString("wifi", "unchanged").equals("enabled")) {
			profile.setWifi(Profile.state.enabled);
		} else if (pref.getString("wifi", "unchanged").equals("disabled")) {
			profile.setWifi(Profile.state.disabled);
		} else {
			profile.setWifi(Profile.state.unchanged);
		}

		if (pref.getString("bluetooth", "unchanged").equals("enabled")) {
			profile.setBluetooth(Profile.state.enabled);
		} else if (pref.getString("bluetooth", "unchanged").equals("disabled")) {
			profile.setBluetooth(Profile.state.disabled);
		} else {
			profile.setBluetooth(Profile.state.unchanged);
		}
		
		if(pref.getString("airplane_mode", "unchanged").equals("enabled")){
			profile.setAirplane_mode(Profile.state.enabled);
		} else if (pref.getString("airplane_mode", "unchanged").equals("disabled")){
			profile.setAirplane_mode(Profile.state.disabled);
		} else {
			profile.setAirplane_mode(Profile.state.unchanged);
		}

		if (pref.getString("display_auto_mode", "unchanged").equals("enabled")) {
			profile.setScreenBrightnessAutoMode(Profile.state.enabled);
		} else if (pref.getString("display_auto_mode", "unchanged").equals(
				"disabled")) {
			profile.setScreenBrightnessAutoMode(Profile.state.disabled);
		} else {
			profile.setScreenBrightnessAutoMode(Profile.state.unchanged);
		}

		if (!findPreference("display_brightness").isEnabled()) {
			profile.setScreenBrightness(-1);
		} else {
			profile.setScreenBrightness(pref.getInt("display_brightness", -1));
		}

		profile.setScreenTimeOut(Integer.parseInt(pref.getString(
				"display_time_out", "-1")));

		XmlCreator creator = new XmlCreator();
		try {
			FileOutputStream output = openFileOutput(
					profile.getName() + ".xml", Context.MODE_PRIVATE);
			output.write(creator.create(profile).getBytes());
			output.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		if (!(name.equals(previousName))) {
			File file = new File(String.valueOf(getFilesDir()) + "/"
					+ previousName + ".xml");
			file.delete();
		}
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
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
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
						preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * If the shared preferences change, the values in the settings activity
	 * change too.
	 * 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
	 *      java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences _pref, String key) {

		if (key.equals("display_auto_mode")
				&& _pref.getString("display_auto_mode", "unchanged").equals(
						"enabled")) {
			findPreference("display_brightness").setEnabled(false);
		} else if (key.equals("display_auto_mode")
				&& (_pref.getString("display_auto_mode", "unchanged").equals(
						"disabled") || _pref.getString("display_auto_mode",
						"unchanged").equals("unchanged"))) {
			findPreference("display_brightness").setEnabled(true);
		}

		if (key.equals("ringer_mode")
				&& (_pref.getString("ringer_mode", "unchanged").equals(
						"vibrate") || _pref.getString("ringer_mode",
						"unchanged").equals("silent"))) {
			findPreference("ringtone_volume").setEnabled(false);

		} else if (key.equals("ringer_mode")
				&& (_pref.getString("ringer_mode", "unchanged")
						.equals("normal") || _pref.getString("ringer_mode",
						"unchanged").equals("unchanged"))) {
			findPreference("ringtone_volume").setEnabled(true);
		}
		
		if (key.equals("airplane_mode")
				&& (_pref.getString("airplane_mode", "unchanged")
						.equals("enabled"))){
			findPreference("gps").setEnabled(false);
			findPreference("mobile_data").setEnabled(false);
			findPreference("wifi").setEnabled(false);
			findPreference("bluetooth").setEnabled(false);
		} else if (key.equals("airplane_mode")
				&& (_pref.getString("airplane_mode", "unchanged")
						.equals("disabled") || _pref.getString("airplane_mode", "unchanged").equals("unchanged"))){
			findPreference("gps").setEnabled(true);
			findPreference("mobile_data").setEnabled(true);
			findPreference("wifi").setEnabled(true);
			findPreference("bluetooth").setEnabled(true);
		}
	}
}
