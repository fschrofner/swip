package at.fhhgb.mc.swip.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.TimePicker;
import android.widget.Toast;

import at.fhhgb.mc.swip.constants.IntentConstants;
import at.fhhgb.mc.swip.constants.SharedPrefConstants;
import at.fhhgb.mc.swip.ui.ListDialog;
import at.flosch.logwrap.Log;
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
	final static String TAG = "Handler";
	
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
		if (pref.getBoolean(SharedPrefConstants.NOTIFICATION, false)) {
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

		if (pref.getBoolean(SharedPrefConstants.NOTIFICATION, false)) {
			updateNotification();
		}

		Toast toast = Toast.makeText(context, _profile.getName()
				+ " was applied!", Toast.LENGTH_SHORT);
		toast.show();
	}

    /**
     * Displays a timepicker dialog which lets the user decide how long all triggers should be overriden,
     * when selecting a manual profile.
     * Should only be shown, when the user did not select a timeout inside the app settings.
     * @param _profilename the name of the profile you want to apply afterwards
     * @return the time to timeout triggers in milliseconds
     */
    public void displayTimeoutDialog(final String _profilename){
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker _timePicker, int _hour, int _minute) {
                //this if is needed, because onTimeSet gets called twice because of an android bug
                if(_timePicker.isShown()){
                    long timeoutInMs = calculateAndShowTimeDifference(_hour, _minute);
                    Log.i(TAG, "selected timeout time: " + _hour + ":" + _minute);
                    setTriggerTimeout(timeoutInMs);
                    applyProfile(_profilename);
                }
            }

        }, hour, minute, true);//Yes 24 hour time
        timePickerDialog.setTitle(context.getString(R.string.timeout_dialog_title));
        //TODO: write description about dialog
        timePickerDialog.show();
    }

    private long calculateAndShowTimeDifference(int _hours, int _minutes){
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        int hourDiff = 0;
        int minuteDiff = 0;

        //more than 12 hours
        if(_hours < hour || (_hours == hour && _minutes < minute)){
            Log.d(TAG, "more than 12 hours");
            if(_hours != hour){
                hourDiff = 24 - hour + _hours - 1;
                minuteDiff = 60 - minute + _minutes;
                if(minuteDiff >= 60){
                    minuteDiff -= 60;
                    hourDiff++;
                }
            } else {
                hourDiff = 23;
                minuteDiff = 60 - minute + _minutes;
            }
        //less than 12 hours
        } else if (_hours > hour || (_hours == hour && _minutes > minute)){
            Log.d(TAG, "less than 12 hours");
            if(_hours != hour){
                hourDiff = _hours - hour - 1;
                minuteDiff = 60 - minute + _minutes;
                if(minuteDiff >= 60){
                    minuteDiff -= 60;
                    hourDiff++;
                }
            } else {
                hourDiff = 0;
                minuteDiff = _minutes - minute;
            }
        } else {
            //error: exact same time
            Log.e(TAG, "user selected same time");
        }

        String hourString = "";
        String minuteString = "";
        String message = "";

        if(hourDiff > 1){
            hourString = String.format(context.getString(R.string.timeout_hours), hourDiff);
        } else{
            hourString = String.format(context.getString(R.string.timeout_hour), hourDiff);
        }

        if(minuteDiff > 1){
            minuteString = String.format(context.getString(R.string.timeout_minutes), minuteDiff);
        } else {
            minuteString = String.format(context.getString(R.string.timeout_minute), minuteDiff);
        }

        if(hourDiff != 0 && minuteDiff != 0){
            message = String.format(context.getString(R.string.timeout_toast_message_multiple), hourString, minuteString);
        } else if(hourDiff != 0 && minuteDiff == 0){
            message = String.format(context.getString(R.string.timeout_toast_message_single), hourString);
        } else if(hourDiff == 0 && minuteDiff != 0){
            message = String.format(context.getString(R.string.timeout_toast_message_single), minuteString);
        }

        //only show the message when the times not set to the current time
        if(hourDiff > 0 || minuteDiff > 0){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        return (hourDiff *  3600000 + minuteDiff * 60000);
    }

    /**
     * Tells the trigger service to stop comparing triggers for the given amount of time
     * @param _timeoutInMs the timeout in milliseconds
     */
    public void setTriggerTimeout(long _timeoutInMs){
        Intent intent = new Intent();
        intent.setAction(IntentConstants.TIMEOUT);
        intent.putExtra(IntentConstants.TIMEOUT_EXTRA, _timeoutInMs);
        context.sendBroadcast(intent);
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
		nBuilder.setPriority(-1);

		Notification notification = nBuilder.build();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(123, notification);
	}

	/**
	 * Checks if the app is installed as systemapp.
	 * It does so by checking for the write to secure settings permission.
	 * @return true = app is installed as systemapp, false = it is not
	 */
	public boolean checkSystemApp(){
	    String permission = "android.permission.WRITE_SECURE_SETTINGS";
	    int res = context.checkCallingOrSelfPermission(permission);
	    return (res == PackageManager.PERMISSION_GRANTED);            
	}
	
	/**
	 * If the app is installed as system app and an upgrade took place, the systemapp will be upgraded here
	 */
	public void updateSystemApp(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean(SharedPrefConstants.SYSTEM_APP, false) && checkSystemApp()){
		    try {
				ComponentName comp = new ComponentName(context, context.getClass());
				PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
				if(!pinfo.versionName.equals(pref.getString("versionname", ""))){
					Command command;
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
						command = new Command(1,"mount -o remount,rw /system", 						        //mounts the system partition to be writeable
								"rm /system/priv-app/at.fhhgb.mc.swip-[12].apk",							//removes the old systemapp
								"cp /data/app/at.fhhgb.mc.swip-[12].apk /system/priv-app/",					//copies the apk of the app to the system-apps folder
								"chmod 644 /system/priv-app/at.fhhgb.mc.swip-[12].apk",						//fixes the permissions
								"mount -o remount,r /system");												//mounts the system partition to be read-only again
					} else{
						command = new Command(1,"mount -o remount,rw /system",
								"rm /system/app/at.fhhgb.mc.swip-[12].apk",
								"cp /data/app/at.fhhgb.mc.swip-[12].apk /system/app/",
								"chmod 644 /system/app/at.fhhgb.mc.swip-[12].apk",		
								"mount -o remount,r /system");									
					}
					
					try {
						RootTools.getShell(true).add(command);
						RootTools.closeAllShells();
						pref.edit().putString("versionname", pinfo.versionName).commit();
						Log.i(TAG, "updated systemapp!");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					} catch (RootDeniedException e) {
						e.printStackTrace();
					}
				} else {
					Log.i(TAG, "systemapp up to date!");
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "app not installed as systemapp");
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
