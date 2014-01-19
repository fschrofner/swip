package at.fhhgbg.mc.profileswitcher.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.profile.Profile;
import at.fhhgbg.mc.profileswitcher.profile.XmlCreator;
import at.fhhgbg.mc.profileswitcher.profile.XmlParser;
import at.fhhgbg.mc.profileswitcher.ui.ListDialogActivity;

public class Handler {
	private Context context;
	SharedPreferences pref;
	
	public Handler(Context _context) {
		context = _context;
		pref = PreferenceManager
				.getDefaultSharedPreferences(context);
	}
	
	public void applyProfile(String _name) {
		XmlParser parser = new XmlParser(context);
		try {
			// applies the profile.
			parser.initializeXmlParser(context.openFileInput(_name
					+ "_profile.xml"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// saves the active profile into the shared preferences
		pref.edit().putString("active_profile", _name).commit();

		if (pref.getBoolean("notification", false)) {
			updateNotification();
		}

		Toast toast = Toast.makeText(context, _name + " was applied!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	public void updateNotification() {
		Intent resultIntent = new Intent(context, ListDialogActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, resultIntent, 0);

		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
				context);
		nBuilder.setSmallIcon(R.drawable.profile_switcher_notification_icon);
		nBuilder.setContentText(context.getResources().getString(
				R.string.textNotificationContentText));
		nBuilder.setContentTitle(context.getResources().getString(
				R.string.textNotificationTitle)
				+ " "
				+ pref.getString("active_profile", context.getResources()
						.getString(R.string.textNotificationNoProfile)));
		nBuilder.setContentIntent(resultPendingIntent);
		nBuilder.setOngoing(true);
		nBuilder.setWhen(0);
		nBuilder.setPriority(1);

		Notification notification = nBuilder.build();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(123, notification);
	}
	
	public void createStandardProfiles() {
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
			output = context.openFileOutput(
					pDefault.getName() + "_profile.xml",
					Context.MODE_PRIVATE);
			output.write(creator.create(pDefault).getBytes());
			output.close();

			output = context.openFileOutput(
					pHome.getName() + "_profile.xml", Context.MODE_PRIVATE);
			output.write(creator.create(pHome).getBytes());
			output.close();

			output = context.openFileOutput(
					pMeeting.getName() + "_profile.xml",
					Context.MODE_PRIVATE);
			output.write(creator.create(pMeeting).getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

}
