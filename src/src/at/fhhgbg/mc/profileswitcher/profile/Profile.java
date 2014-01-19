package at.fhhgbg.mc.profileswitcher.profile;

import java.io.FileInputStream;
import java.io.IOException;

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
import at.fhhgbg.mc.profileswitcher.ui.ListDialogActivity;

/**
 * Container class used to transfer the settings between activities and methods.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class Profile {

	// IMPORTANT:
	// do not change the position of dis- and enabled!
	public enum state {
		disabled, enabled, unchanged
	};

	public enum mode {
		silent, vibrate, normal, unchanged
	};

	// these are the default states of a profile,
	// where every value is set to unchanged or -1
	// (which is equal to unchanged for options with numerical states)
	private String name;
	private mode ringerMode = mode.unchanged;
	private int alarmVolume = -1;
	private int mediaVolume = -1;
	private int ringtoneVolume = -1;
	private state nfc = state.unchanged;
	private state bluetooth = state.unchanged;
	private state wifi = state.unchanged;
	private state mobileData = state.unchanged;
	private state gps = state.unchanged;
	private state airplane_mode = state.unchanged;
	private state lockscreen = state.unchanged;
	private int screenBrightness = -1;
	private state screenBrightnessAutoMode = state.unchanged;
	private int screenTimeOut = -1;

	public Profile(String name) {
		this.name = name;
	}
	
	public state getNfc() {
		return nfc;
	}

	public void setNfc(state nfc) {
		this.nfc = nfc;
	}

	public state getLockscreen() {
		return lockscreen;
	}

	public void setLockscreen(state lockscreen) {
		this.lockscreen = lockscreen;
	}

	public state getAirplane_mode() {
		return airplane_mode;
	}

	public void setAirplane_mode(state airplane_mode) {
		this.airplane_mode = airplane_mode;
	}

	public int getScreenTimeOut() {
		return screenTimeOut;
	}

	public void setScreenTimeOut(int screenTimeOut) {
		this.screenTimeOut = screenTimeOut;
	}

	public int getScreenBrightness() {
		return screenBrightness;
	}

	public void setScreenBrightness(int screenBrightness) {
		this.screenBrightness = screenBrightness;
	}

	public state getScreenBrightnessAutoMode() {
		return screenBrightnessAutoMode;
	}

	public void setScreenBrightnessAutoMode(state screenBrightnessAutoMode) {
		this.screenBrightnessAutoMode = screenBrightnessAutoMode;
	}

	public mode getRingerMode() {
		return ringerMode;
	}

	public void setRingerMode(mode ringerMode) {
		this.ringerMode = ringerMode;
	}

	public int getAlarmVolume() {
		return alarmVolume;
	}

	public void setAlarmVolume(int alarmVolume) {
		this.alarmVolume = alarmVolume;
	}

	public int getMediaVolume() {
		return mediaVolume;
	}

	public void setMediaVolume(int mediaVolume) {
		this.mediaVolume = mediaVolume;
	}

	public int getRingtoneVolume() {
		return ringtoneVolume;
	}

	public void setRingtoneVolume(int ringtoneVolume) {
		this.ringtoneVolume = ringtoneVolume;
	}

	public state getBluetooth() {
		return bluetooth;
	}

	public void setBluetooth(state bluetooth) {
		this.bluetooth = bluetooth;
	}

	public state getWifi() {
		return wifi;
	}

	public void setWifi(state wifi) {
		this.wifi = wifi;
	}

	public state getMobileData() {
		return mobileData;
	}

	public void setMobileData(state mobileData) {
		this.mobileData = mobileData;
	}

	public state getGps() {
		return gps;
	}

	public void setGps(state gps) {
		this.gps = gps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}