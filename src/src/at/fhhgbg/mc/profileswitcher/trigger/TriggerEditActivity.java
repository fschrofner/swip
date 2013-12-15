package at.fhhgbg.mc.profileswitcher.trigger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import at.fhhgbg.mc.profileswitcher.NfcWriterActivity;
import at.fhhgbg.mc.profileswitcher.Profile;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.XmlCreator;
import at.fhhgbg.mc.profileswitcher.R.string;
import at.fhhgbg.mc.profileswitcher.R.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
	private String previousName; 	// saves the previous profile name for the case the profile gets renamed
									// (so the previous file of this profile can be deleted)

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
		
		if (item.getItemId() == R.id.save_trigger) {
			this.saveTrigger();
			this.finish();
		} else if (item.getItemId() == R.id.cancel_trigger) {
			this.finish();
		} else if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
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

		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		
		// Add 'Time' preferences, and a corresponding header.
		fakeHeader.setTitle(R.string.pref_header_time);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_trigger_time);
		
//		// Add 'Sound' preferences, and a corresponding header.
//		fakeHeader = new PreferenceCategory(this);
//		fakeHeader.setTitle(R.string.pref_header_profile);
//		getPreferenceScreen().addPreference(fakeHeader);
//		addPreferencesFromResource(R.xml.pref_trigger_profile);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("name_trigger"));
		bindPreferenceSummaryToValue(findPreference("time"));
//		bindPreferenceSummaryToValue(findPreference("profile"));
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

		String name = pref.getString("name_trigger", "Insert name");

		Trigger trigger = new Trigger(name);

		trigger.setHours(pref.getInt("hours", -1));
		trigger.setMinutes(pref.getInt("minutes", -1));

//		if (!findPreference("time").isEnabled()) {
//			trigger.setHours(-1);
//			trigger.setMinutes(-1);
//		} else {
			trigger.setHours(Integer.parseInt(pref.getString("time", "00:00").split(":")[0]));
			trigger.setMinutes(Integer.parseInt(pref.getString("time", "00:00").split(":")[1]));
//		}

		
		XmlCreatorTrigger creator = new XmlCreatorTrigger();
		try {
			FileOutputStream output = openFileOutput(
					trigger.getName() + "_trigger.xml", Context.MODE_PRIVATE);
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}
}
