package at.fhhgb.mc.swip.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.profile.Profile;
import at.fhhgb.mc.swip.trigger.LocationTrigger;
import at.fhhgb.mc.swip.trigger.SimpleGeofence;
import at.fhhgb.mc.swip.trigger.Trigger;
import at.fhhgb.mc.swip.trigger.XmlCreatorTrigger;
import at.fhhgb.mc.swip.trigger.Trigger.listen_state;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.android.gms.location.Geofence;

/**
 * Activity used to edit the different settings of a trigger.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class TriggerEditActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	private boolean preferencesChanged = false;
	private CharSequence[] profileArray;
	private String previousName; // saves the previous profile name for the case
									// the profile gets renamed
									// (so the previous file of this profile can
									// be deleted)

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
		getMenuInflater().inflate(R.menu.trigger_edit, menu);
		return true;
	}

	/**
	 * If one of the items on the actionbar is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Show dialogs if the user wants to save and something is wrong
		if (item.getItemId() == R.id.save_trigger) {
			if (pref.getString(
					"name_trigger",
					getResources()
							.getString(R.string.pref_trigger_name_default))
					.equals(getResources().getString(
							R.string.pref_trigger_name_default))
					|| pref.getString(
							"name_trigger",
							getResources().getString(
									R.string.pref_trigger_name_default))
							.equals("")
					|| pref.getString(
							"profile",
							getResources().getString(
									R.string.pref_profile_default)).equals(
							getResources().getString(
									R.string.pref_profile_default))
					|| (pref.getInt("battery_start_level", -1) >= pref.getInt(
							"battery_end_level", -1) && pref.getInt(
							"battery_end_level", -1) != -1)) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this,
						AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				dialog.setIcon(R.drawable.alerts_and_states_warning);
				if (pref.getInt("battery_start_level", -1) > pref.getInt(
						"battery_end_level", -1)
						&& pref.getInt("battery_end_level", -1) != -1) {
					// Battery low-level is smaller than battery high-level
					dialog.setTitle(getResources().getString(
							R.string.alert_battery_title));
					dialog.setMessage(getResources().getString(
							R.string.alert_battery_text));
				} else if (pref.getInt("battery_start_level", -1) == pref
						.getInt("battery_end_level", -1)
						&& pref.getInt("battery_end_level", -1) != -1) {
					// Battery low-level == battery high-level
					dialog.setTitle(getResources().getString(
							R.string.alert_battery_title));
					dialog.setMessage(getResources().getString(
							R.string.alert_battery_exact_text));
				} else if (pref.getString(
						"name_trigger",
						getResources().getString(
								R.string.pref_trigger_name_default)).equals(
						getResources().getString(
								R.string.pref_trigger_name_default))) {
					// The name is the default name
					dialog.setTitle(getResources().getString(
							R.string.alert_name_title));
					dialog.setMessage(getResources().getString(
							R.string.alert_name_text));
				} else if (pref.getString(
						"name_trigger",
						getResources().getString(
								R.string.pref_trigger_name_default)).equals("")) {
					// the name is empty
					dialog.setTitle(getResources().getString(
							R.string.alert_name_title));
					dialog.setMessage(getResources().getString(
							R.string.alert_name_text));
				} else if (pref
						.getString(
								"profile",
								getResources().getString(
										R.string.pref_profile_default)).equals(
								getResources().getString(
										R.string.pref_profile_default))) {
					// no profile is set
					dialog.setTitle(getResources().getString(
							R.string.alert_profile_title));
					dialog.setMessage(getResources().getString(
							R.string.alert_profile_text));
				}
				dialog.setNegativeButton(
						getResources().getString(R.string.alert_button),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				dialog.show();
			} else {
				this.saveTrigger();
				this.finish();
			}
		} else if (item.getItemId() == R.id.cancel_trigger) {
			this.finish();
		} else if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {

		if (preferencesChanged) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK);

			dialog.setTitle(getResources().getString(
					R.string.alert_discard_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_discard_text));
			dialog.setIcon(R.drawable.alerts_and_states_warning);

			dialog.setPositiveButton(
					getResources().getString(R.string.alert_discard_yes),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});

			dialog.setNegativeButton(
					getResources().getString(R.string.alert_discard_no),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.show();
		} else {
			finish();
		}
	}

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

		previousName = pref.getString("name_trigger", "default name");

		pref.registerOnSharedPreferenceChangeListener(this);

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_trigger_general);
		refreshProfileArray();
		ListPreference lp = (ListPreference) findPreference("profile");
		lp.setEntries(profileArray);
		lp.setEntryValues(profileArray);

		PreferenceCategory fakeHeader = new PreferenceCategory(this);

		// Add 'Location preferences, and a corresponding header.
		fakeHeader.setTitle(R.string.pref_header_location);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_trigger_location);

		// Add 'Time' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_time);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_trigger_time);

		// Add 'Battery' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_battery);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_trigger_battery);

		// Add 'Headphone' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_headphone);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_trigger_headphone);

		// Bind the summaries of EditText/List/Dialog preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("name_trigger"));
		bindPreferenceSummaryToValue(findPreference("start_time"));
		bindPreferenceSummaryToValue(findPreference("end_time"));
		bindPreferenceSummaryToValue(findPreference("profile"));
		bindPreferenceSummaryToValue(findPreference("battery_state"));
		bindPreferenceSummaryToValue(findPreference("headphone"));

		// binds the summary to the location-preference
		if (pref.getFloat("geofence_lat", -1F) > -1
				&& pref.getFloat("geofence_lng", -1F) > -1
				&& pref.getInt("geofence_radius", 50) > 0) {
			findPreference("location").setSummary(
					"N: " + pref.getFloat("geofence_lat", -1F) + ", E: "
							+ pref.getFloat("geofence_lng", -1F) + ", Radius: "
							+ pref.getInt("geofence_radius", 50));
		} else {
			findPreference("location").setSummary(R.string.ignored);
		}

		if (pref.getString("start_time", "Ignored").equals("Ignored")) {
			findPreference("end_time").setEnabled(false);
			pref.edit().putString("end_time", "Ignored").commit();
		}

		if (pref.getInt("battery_start_level", -1) == -1) {
			findPreference("battery_end_level").setEnabled(false);
			pref.edit().putInt("battery_end_level", -1).commit();
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Saves the current settings of the activity to a profile object and lets
	 * it be written by the XmlCreator.
	 */
	public void saveTrigger() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		LocationTrigger locTrig = new LocationTrigger(this);

		String name = pref.getString("name_trigger",
				getResources().getString(R.string.pref_trigger_name_default));

		Trigger trigger = new Trigger(name);

		if (pref.getString("start_time", "Ignored").equals("Ignored")) {
			trigger.setStartHours(-1);
			trigger.setStartMinutes(-1);
		} else {
			trigger.setStartHours(Integer.parseInt(pref.getString("start_time",
					"00:00").split(":")[0]));
			trigger.setStartMinutes(Integer.parseInt(pref.getString(
					"start_time", "00:00").split(":")[1]));
		}

		if (pref.getString("end_time", "Ignored").equals("Ignored")) {
			trigger.setEndHours(-1);
			trigger.setEndMinutes(-1);
		} else {
			trigger.setEndHours(Integer.parseInt(pref.getString("end_time",
					"00:00").split(":")[0]));
			trigger.setEndMinutes(Integer.parseInt(pref.getString("end_time",
					"00:00").split(":")[1]));
		}

		trigger.setProfileName(pref.getString("profile", getResources()
				.getString(R.string.pref_profile_default)));

		trigger.setBatteryStartLevel(pref.getInt("battery_start_level", -1));

		trigger.setBatteryEndLevel(pref.getInt("battery_end_level", -1));

		if (pref.getString("battery_state", "ignored").equals("charging")) {
			trigger.setBatteryState(Trigger.listen_state.listen_on);
		} else if (pref.getString("battery_state", "ignored").equals(
				"discharging")) {
			trigger.setBatteryState(Trigger.listen_state.listen_off);
		} else {
			trigger.setBatteryState(Trigger.listen_state.ignore);
		}

		if (pref.getString("headphone", "ignored").equals("plugged_in")) {
			trigger.setHeadphones(Trigger.listen_state.listen_on);
		} else if (pref.getString("headphone", "ignored").equals("unplugged")) {
			trigger.setHeadphones(Trigger.listen_state.listen_off);
		} else {
			trigger.setHeadphones(Trigger.listen_state.ignore);
		}

		if (pref.getFloat("geofence_lat", -1) > -1
				&& pref.getFloat("geofence_lng", -1) > -1
				&& pref.getInt("geofence_radius", 50) > 0) {

			// geofence that registers if you enter the area
			SimpleGeofence simple = new SimpleGeofence(name, pref.getFloat(
					"geofence_lat", -1F), pref.getFloat("geofence_lng", -1F),
					pref.getInt("geofence_radius", 0), Geofence.NEVER_EXPIRE,
					Geofence.GEOFENCE_TRANSITION_ENTER);
			locTrig.registerGeofence(simple);

			// geofence that registers if you leave the area
			simple = new SimpleGeofence(name + "_exit", pref.getFloat(
					"geofence_lat", -1F), pref.getFloat("geofence_lng", -1F),
					pref.getInt("geofence_radius", 0), Geofence.NEVER_EXPIRE,
					Geofence.GEOFENCE_TRANSITION_EXIT);
			locTrig.registerGeofence(simple);

			// sets the geofence of the trigger to the enter event
			trigger.setGeofence(name);
		} else {
			locTrig.unregisterGeofence(name);
			locTrig.unregisterGeofence(name + "_exit");
			trigger.setGeofence(null);
		}

		// Creates the xml
		XmlCreatorTrigger creator = new XmlCreatorTrigger();
		try {
			FileOutputStream output = openFileOutput(trigger.getName()
					+ "_trigger.xml", Context.MODE_PRIVATE);
			output.write(creator.create(trigger).getBytes());
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
					+ previousName + "_trigger.xml");
			file.delete();

		}

		Intent intent = new Intent();
		intent.setAction("at.fhhgb.mc.swip.trigger.refresh");
		sendBroadcast(intent);
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

	/** {@inheritDoc} */
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
	public void onSharedPreferenceChanged(SharedPreferences _pref, String key) {
		// dis- and enables the endtime if the start time is changed
		if (key.equals("start_time")
				&& _pref.getString("start_time", "Ignored").equals("Ignored")) {
			_pref.edit().putString("end_time", "Ignored").commit();
			findPreference("end_time").setEnabled(false);
			findPreference("end_time").setSummary("Ignored");
		} else if (key.equals("start_time")
				&& !_pref.getString("start_time", "Ignored").equals("Ignored")) {
			findPreference("end_time").setEnabled(true);
		}

		// dis- and enables the end battery level if the start battery level is
		// changed
		if (key.equals("battery_start_level")
				&& _pref.getInt("battery_start_level", -1) == -1) {
			_pref.edit().putInt("battery_end_level", -1).commit();
			findPreference("battery_end_level").setEnabled(false);
			findPreference("battery_end_level").setSummary("Ignored");
		} else if (key.equals("battery_start_level")
				&& _pref.getInt("battery_start_level", -1) != -1) {
			findPreference("battery_end_level").setEnabled(true);
		}

		// Binds the summary of the location
		if (key.equals("geofence_lat") || key.equals("geofence_lng")
				|| key.equals("geofence_radius")) {

			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);

			if (pref.getFloat("geofence_lat", -1F) > -1
					&& pref.getFloat("geofence_lng", -1F) > -1
					&& pref.getInt("geofence_radius", 50) > 0) {
				findPreference("location").setSummary(
						"N: " + pref.getFloat("geofence_lat", -1F) + ", E: "
								+ pref.getFloat("geofence_lng", -1F)
								+ ", Radius: "
								+ pref.getInt("geofence_radius", 50));
			} else {
				findPreference("location").setSummary(R.string.ignored);
			}
		}

		preferencesChanged = true;
	}

	/**
	 * Refreshes the profile array.
	 */
	private void refreshProfileArray() {
		List<String> profileList = new ArrayList<String>();

		String[] fileList = getFilesDir().list();
		StringBuffer sb = new StringBuffer();

		for (String file : fileList) {
			if (file.contains("_profile")) {
				sb.append(file);
				sb.delete(sb.length() - 12, sb.length());
				profileList.add(sb.toString());
				sb.delete(0, sb.length());
			}
		}

		profileArray = new CharSequence[profileList.size() + 1];
		profileArray[0] = getResources().getString(
				R.string.pref_profile_default);

		for (int i = 0; i < profileArray.length - 1; i++) {
			profileArray[i + 1] = profileList.get(i);
		}
	}

}
