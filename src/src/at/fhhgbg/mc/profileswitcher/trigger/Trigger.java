package at.fhhgbg.mc.profileswitcher.trigger;

import android.util.Log;

public class Trigger {

	protected enum listen_state {
		listen_off, listen_on, ignore;
	}

	private String name;
	private String profileName;
	private int hours;
	private int minutes;
	private int batteryLevel;

	public Trigger(String _name) {
		name = _name;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	private listen_state headphones;
	private	listen_state batteryCharging;
	

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

	public int getHours() {
		return hours;
	}

	public void setHours(int _hours) {
		if (_hours < 24 && _hours >= -1) {
			this.hours = _hours;
		} else {
			Log.e("Trigger", "hours not in allowed range!");
		}
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int _minutes) {
		if (_minutes < 60 && _minutes >= -1) {
			this.minutes = _minutes;
		} else {
			Log.e("Trigger", "minutes not in allowed range!");
		}
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
