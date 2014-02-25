package at.fhhgb.mc.swip.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.profile.Profile;
import at.fhhgb.mc.swip.profile.Setter;
import at.fhhgb.mc.swip.profile.XmlCreator;
import at.fhhgb.mc.swip.profile.XmlParser;
import at.fhhgb.mc.swip.ui.ListDialogActivity;

/**
 * Provides often used methods.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class Handler {
	private Context context;
	SharedPreferences pref;

	public Handler(Context _context) {
		context = _context;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Applies a profile, updates the notification and shows a toast.
	 * 
	 * @param _name
	 *            The name of the profile
	 */
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

		// updates the notification
		if (pref.getBoolean("notification", false)) {
			updateNotification();
		}

		// shows the toast
		Toast toast = Toast.makeText(context, _name + " " + context.getResources().getString(R.string.profileApplied),
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Applies a profile-object directly
	 * 
	 * @param _profile
	 *            The profile-object.
	 */
	public void applyProfile(Profile _profile) {

		Setter setter = new Setter();

		setter.setRingerMode(context, _profile.getRingerMode());

		// alarm volume
		if (_profile.getAlarmVolume() != -1) {
			setter.setAlarmVolume(context, _profile.getAlarmVolume());
		}

		// media volume
		if (_profile.getMediaVolume() != -1) {
			setter.setMediaVolume(context, _profile.getMediaVolume());
		}

		// ringtone volume
		if (_profile.getRingtoneVolume() != -1) {
			setter.setRingtoneVolume(context, _profile.getRingtoneVolume());
		}

		// nfc
		if (_profile.getNfc() == Profile.state.enabled) {
			setter.setNfc(context, true);
		} else if (_profile.getNfc() == Profile.state.disabled) {
			setter.setNfc(context, false);
		}

		// bluetooth
		if (_profile.getBluetooth() == Profile.state.enabled) {
			setter.setBluetooth(context, true);
		} else if (_profile.getBluetooth() == Profile.state.disabled) {
			setter.setBluetooth(context, false);
		}

		// wifi
		if (_profile.getWifi() == Profile.state.enabled) {
			setter.setWifi(context, true);
		} else if (_profile.getWifi() == Profile.state.disabled) {
			setter.setWifi(context, false);
		}

		// mobile data
		if (_profile.getMobileData() == Profile.state.enabled) {
			setter.setMobileData(context, true);
		} else if (_profile.getMobileData() == Profile.state.disabled) {
			setter.setMobileData(context, false);
		}

		// gps
		if (_profile.getGps() == Profile.state.enabled) {
			setter.setGps(context, true);
		} else if (_profile.getGps() == Profile.state.disabled) {
			setter.setGps(context, false);
		}

		// airplane mode
		if (_profile.getAirplane_mode() == Profile.state.enabled) {
			setter.setAirplaneMode(context, true);
		} else if (_profile.getAirplane_mode() == Profile.state.disabled) {
			setter.setAirplaneMode(context, false);
		}

		// lockscreen
		if (_profile.getLockscreen() == Profile.state.enabled) {
			setter.setLockscreen(context, true);
		} else if (_profile.getLockscreen() == Profile.state.disabled) {
			setter.setLockscreen(context, false);
		}

		// screen brightness
		if (_profile.getScreenBrightness() != -1) {
			setter.setScreenBrightness(context, _profile.getScreenBrightness());
		}

		// screen brightness automode
		if (_profile.getScreenBrightnessAutoMode() == Profile.state.enabled) {
			setter.setScreenBrightnessMode(context, true);
		} else if (_profile.getScreenBrightnessAutoMode() == Profile.state.disabled) {
			setter.setScreenBrightnessMode(context, false);
		}

		// screen timeout
		if (_profile.getScreenTimeOut() != -1) {
			setter.setScreenTimeout(context, _profile.getScreenTimeOut());
		}

		// saves the active profile into the shared preferences
		pref.edit().putString("active_profile", _profile.getName()).commit();

		if (pref.getBoolean("notification", false)) {
			updateNotification();
		}

		Toast toast = Toast.makeText(context, _profile.getName()
				+ " was applied!", Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Updates the notification
	 */
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

	/**
	 * Checks if the app is installed as systemapp.
	 * It does so by checking for the write to secure settings permission.
	 * @param _context the context of your activity
	 * @return true = app is installed as systemapp, false = it is not
	 */
	public boolean checkSystemapp(){
	    String permission = "android.permission.WRITE_SECURE_SETTINGS";
	    int res = context.checkCallingOrSelfPermission(permission);
	    return (res == PackageManager.PERMISSION_GRANTED);            
	}
	
	/**
	 * If the app is installed as system app and an upgrade took place, the systemapp will be upgraded here
	 */
	public void updateSystemApp(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean("systemapp", false) && checkSystemapp()){
		    try {
				ComponentName comp = new ComponentName(context, context.getClass());
				PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
				if(!pinfo.versionName.equals(pref.getString("versionname", ""))){
					CommandCapture command;
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
						pref.edit().putString("versionname", pinfo.versionName).commit();
						Log.i("Handler", "updated systemapp!");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					} catch (RootDeniedException e) {
						e.printStackTrace();
					}
				} else {
					Log.i("Handler", "systemapp up to date!");
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Log.i("Handler", "app not installed as systemapp");
		}
	}
	
	/**
	 * Creates the default profiles
	 */
	public void createDefaultProfiles() {
		Profile pDefault = new Profile(context.getResources().getString(R.string.default_profile_default));
		pDefault.setRingerMode(Profile.mode.normal);
		pDefault.setGps(Profile.state.disabled);
		pDefault.setMobileData(Profile.state.enabled);
		pDefault.setWifi(Profile.state.disabled);
		pDefault.setBluetooth(Profile.state.disabled);
		pDefault.setScreenBrightnessAutoMode(Profile.state.enabled);

		Profile pHome = new Profile(context.getResources().getString(R.string.default_profile_home));
		pHome.setRingerMode(Profile.mode.normal);
		pHome.setGps(Profile.state.disabled);
		pHome.setMobileData(Profile.state.disabled);
		pHome.setWifi(Profile.state.enabled);
		pHome.setBluetooth(Profile.state.disabled);
		pHome.setScreenBrightnessAutoMode(Profile.state.enabled);

		Profile pMeeting = new Profile(context.getResources().getString(R.string.default_profile_meeting));
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
					pDefault.getName() + "_profile.xml", Context.MODE_PRIVATE);
			output.write(creator.create(pDefault).getBytes());
			output.close();

			output = context.openFileOutput(pHome.getName() + "_profile.xml",
					Context.MODE_PRIVATE);
			output.write(creator.create(pHome).getBytes());
			output.close();

			output = context.openFileOutput(
					pMeeting.getName() + "_profile.xml", Context.MODE_PRIVATE);
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
