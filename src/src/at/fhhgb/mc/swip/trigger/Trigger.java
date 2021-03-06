package at.fhhgb.mc.swip.trigger;

import java.util.Set;

import at.flosch.logwrap.Log;

/**
 * Container class used to transfer the settings between activities and methods.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class Trigger {
	final static String TAG = "Trigger";

	public enum listen_state {
		listen_off, listen_on, ignore;
	}

	private String name;
	private String profileName;
	private int startHours;
	private int startMinutes;
	private int endHours;
	private int endMinutes;
	private int batteryStartLevel;
	private int batteryEndLevel;
	private listen_state headphones;
	private listen_state batteryCharging;
	private String geofence;
	private int priority;
	private Set<String> weekdays;

	public Set<String> getWeekdays() {
		return weekdays;
	}

	public void setWeekdays(Set<String> weekdays) {
		this.weekdays = weekdays;
	}

	public Trigger(String _name) {
		this.name = _name;
		this.profileName = null;
		this.startHours = -1;
		this.startMinutes = -1;
		this.endHours = -1;
		this.endMinutes = -1;
		this.batteryStartLevel = -1;
		this.batteryEndLevel = -1;
		this.headphones = listen_state.ignore;
		this.batteryCharging = listen_state.ignore;
		this.geofence = null;
		this.priority = 0;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getGeofence() {
		return geofence;
	}

	public void setGeofence(String geofence) {
		this.geofence = geofence;
	}

	public int getBatteryStartLevel() {
		return batteryStartLevel;
	}

	public void setBatteryStartLevel(int batteryStartLevel) {
		this.batteryStartLevel = batteryStartLevel;
	}

	public int getBatteryEndLevel() {
		return batteryEndLevel;
	}

	public void setBatteryEndLevel(int batteryEndLevel) {
		this.batteryEndLevel = batteryEndLevel;
	}

	public listen_state getBatteryState() {
		return batteryCharging;
	}

	public void setBatteryState(listen_state batteryState) {
		this.batteryCharging = batteryState;
	}

	public listen_state getHeadphones() {
		return headphones;
	}

	public void setHeadphones(listen_state headphones) {
		this.headphones = headphones;
	}

	public int getStartHours() {
		return startHours;
	}

	public void setStartHours(int _hours) {
		if (_hours < 24 && _hours >= -1) {
			this.startHours = _hours;
			Log.i(TAG, "set start hours for time range");
		} else {
			Log.e(TAG, "start hours not in allowed range!");
		}
	}

	public int getStartMinutes() {
		return startMinutes;
	}

	public void setStartMinutes(int _minutes) {
		if (_minutes < 60 && _minutes >= -1) {
			this.startMinutes = _minutes;
			Log.i(TAG, "set start minutes for time range");
		} else {
			Log.e(TAG, "start minutes not in allowed range!");
		}
	}

	public int getEndHours() {
		return endHours;
	}

	public void setEndHours(int _hours) {
		if (_hours < 24 && _hours >= -1) {
			this.endHours = _hours;
			Log.i(TAG, "set end hours for time range");
		} else {
			Log.e(TAG, "end hours not in allowed range!");
		}
	}

	public int getEndMinutes() {
		return endMinutes;
	}

	public void setEndMinutes(int _minutes) {
		if (_minutes < 60 && _minutes >= -1) {
			this.endMinutes = _minutes;
			Log.i(TAG, "set end minutes for time range");
		} else {
			Log.e(TAG, "end minutes not in allowed range!");
		}
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String _profileName) {
		this.profileName = _profileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
