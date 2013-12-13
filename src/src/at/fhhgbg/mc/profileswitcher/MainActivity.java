package at.fhhgbg.mc.profileswitcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.trigger.TriggerService;
import at.fhhgbg.mc.profileswitcher.widgets.ListWidget;

/**
 * Main Activity which shows the already created profiles and allows applying
 * and editing them or creating new ones. Also provides a settings activity,
 * allowing one to activate the permanent notification.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class MainActivity extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	List<String> profileList = new ArrayList<String>();

	/**
	 * Sets up the layout and the notification.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		boolean mboolean = false;
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		mboolean = pref.getBoolean("FIRST_RUN", false);

		// if the application is run for the first time
		if (!mboolean) {
			Profile pDefault = new Profile("Default");
			pDefault.setRingerMode(Profile.mode.normal);
			pDefault.setGps(Profile.state.disabled);
			pDefault.setMobileData(Profile.state.enabled);
			pDefault.setWifi(Profile.state.disabled);
			pDefault.setBluetooth(Profile.state.disabled);
			pDefault.setScreenBrightnessAutoMode(Profile.state.enabled);

			Profile pHome = new Profile("Home");
			pHome.setRingerMode(Profile.mode.normal);
			pHome.setGps(Profile.state.disabled);
			pHome.setMobileData(Profile.state.disabled);
			pHome.setWifi(Profile.state.enabled);
			pHome.setBluetooth(Profile.state.disabled);
			pHome.setScreenBrightnessAutoMode(Profile.state.enabled);

			Profile pMeeting = new Profile("Meeting");
			pMeeting.setRingerMode(Profile.mode.vibrate);
			pMeeting.setGps(Profile.state.disabled);
			pMeeting.setMobileData(Profile.state.enabled);
			pMeeting.setWifi(Profile.state.disabled);
			pMeeting.setBluetooth(Profile.state.disabled);
			pMeeting.setScreenBrightnessAutoMode(Profile.state.enabled);

			XmlCreator creator = new XmlCreator();
			FileOutputStream output;
			try {
				output = openFileOutput(pDefault.getName() + "_profile.xml",
						Context.MODE_PRIVATE);
				output.write(creator.create(pDefault).getBytes());
				output.close();

				output = openFileOutput(pHome.getName() + "_profile.xml",
						Context.MODE_PRIVATE);
				output.write(creator.create(pHome).getBytes());
				output.close();

				output = openFileOutput(pMeeting.getName() + "_profile.xml",
						Context.MODE_PRIVATE);
				output.write(creator.create(pMeeting).getBytes());
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}

			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("FIRST_RUN", true);
			editor.commit();
		}

		// starts the permanent notification if it is activated
		if (pref.getBoolean("notification", false)) {
			Intent resultIntent = new Intent(this, ListDialogActivity.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
					0, resultIntent, 0);

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

		} else {
			// deactivates the notification otherwise
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(123);
		}
		
		Intent intent = new Intent(this, at.fhhgbg.mc.profileswitcher.trigger.TriggerService.class);
		startService(intent);
		Log.i("MainActivity", "started TriggerService");
	}

	/**
	 * Inflates the menu.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflates the menu containing the add profiles button and the settings
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Will be called if the add- or settings-button is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// if a new profile should be created
		if (item.getItemId() == R.id.new_profile) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			Editor prefEditor = preferences.edit();

			// loads the default values into the shared preferences
			prefEditor.putString("name", "Insert name");
			prefEditor.putString("ringer_mode", "unchanged");
			prefEditor.putInt("alarm_volume", -1);
			prefEditor.putInt("media_volume", -1);
			prefEditor.putInt("ringtone_volume", -1);
			prefEditor.putString("bluetooth", "unchanged");
			prefEditor.putString("wifi", "unchanged");
			prefEditor.putString("mobile_data", "unchanged");
			prefEditor.putString("gps", "unchanged");
			prefEditor.putInt("display_brightness", -1);
			prefEditor.putString("display_auto_mode", "unchanged");
			prefEditor.putString("display_time_out", "-1");

			prefEditor.commit();

			Intent i = new Intent(this, ProfileEditActivity.class);
			startActivity(i);
		} else if (item.getItemId() == R.id.settings) {
			// if the settings are selected
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Refreshes the profile list and then the list adapter.
	 */
	private void refreshListView() {
		profileList.clear();

		ListView v = (ListView) findViewById(R.id.ListViewProfiles);

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

		Collections.sort(profileList, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
					return 1;
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
					return -1;
				return 0;
			}

		});

		ArrayListAdapter listAdapter = new ArrayListAdapter(this, 0,
				profileList);
		v.setAdapter(listAdapter);
		v.setOnItemClickListener(this);
		v.setOnItemLongClickListener(this);
	}

	/**
	 * Simply refreshes the list view every time the activity is started.
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		refreshListView();
	}

	/**
	 * Updates the widgets if the activity is stopped.
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		Intent intent = new Intent(this, ListWidget.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		int[] ids = { R.xml.widget_list };
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		sendBroadcast(intent);

	}

	/**
	 * Applies a profile if clicked on it.
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> _a, View v, int _position, long arg3) {

		XmlParser parser = new XmlParser(this);
		try {
			// applies the profile.
			parser.initializeXmlParser(openFileInput(_a
					.getItemAtPosition(_position) + "_profile.xml"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//saves the active profile into the shared preferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.edit().putString("active_profile", (String)_a.getItemAtPosition(_position)).commit();
		
		Toast toast = Toast.makeText(this, _a.getItemAtPosition(_position)
				+ " was applied!", Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Shows the delete option for a profile if it is longpressed.
	 * 
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> _a, View _v, int _position,
			long arg3) {
		String[] options = new String[] { "delete" };

		// used to notify the user of the longpress.
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(25);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(options, new LongPressMenuListener(_a, _position));
		builder.show();
		return false;
	}

	/**
	 * Used to delete profiles after a longpress.
	 * 
	 * @author Florian Schrofner & Dominik Koeltringer
	 * 
	 */
	private class LongPressMenuListener implements OnClickListener {
		AdapterView<?> a;
		int position;

		LongPressMenuListener(AdapterView<?> _a, int _position) {
			a = _a;
			position = _position;
		}

		/**
		 * Deletes a profile after the delete option is pressed.
		 * 
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
		 *      int)
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0: {
				File file = new File(String.valueOf(getFilesDir()) + "/"
						+ a.getItemAtPosition(position) + "_profile.xml");
				file.delete();
				refreshListView();
			}
				break;
			}
		}
	}
}