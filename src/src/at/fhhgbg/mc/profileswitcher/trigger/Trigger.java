package at.fhhgbg.mc.profileswitcher.trigger;

import android.util.Log;

public class Trigger {

	protected enum listen_state {
		listen_off, listen_on, ignore;
	}

	private String name;
	private String profileName;
	private int startHours;
	private int startMinutes;
	private int endHours;
	private int endMinutes;
	private int batteryLevel;
	private listen_state headphones;
	private	listen_state batteryCharging;

	public Trigger(String _name) {
		this.name = _name;
		this.profileName = null;
		this.startHours = -1;
		this.startMinutes = -1;
		this.endHours = -1;
		this.endMinutes = -1;
		this.batteryLevel = -1;
		this.headphones = listen_state.ignore;
		this.batteryCharging = listen_state.ignore;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
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

//	public int getHours() {
//		return startHours;
//	}
//
//	public void setHours(int _hours) {
//		if (_hours < 24 && _hours >= -1) {
//			this.startHours = _hours;
//			this.endHours = -1;
//			Log.i("Trigger", "set exact hours");
//		} else {
//			Log.e("Trigger", "hours not in allowed range!");
//		}
//	}
//
//	public int getMinutes() {
//		return startMinutes;
//	}
//
//	public void setMinutes(int _minutes) {
//		if (_minutes < 60 && _minutes >= -1) {
//			this.startMinutes = _minutes;
//			this.endMinutes = -1;
//			Log.i("Trigger", "set exact minutes");
//		} else {
//			Log.e("Trigger", "minutes not in allowed range!");
//		}
//	}

	public int getStartHours() {
		return startHours;
	}

	public void setStartHours(int _hours) {
		if (_hours < 24 && _hours >= -1) {
			this.startHours = _hours;
			Log.i("Trigger", "set start hours for time range");
		} else {
			Log.e("Trigger", "start hours not in allowed range!");
		}
	}

	public int getStartMinutes() {
		return startMinutes;
	}

	public void setStartMinutes(int _minutes) {
		if (_minutes < 60 && _minutes >= -1) {
			this.startMinutes = _minutes;
			Log.i("Trigger", "set start minutes for time range");
		} else {
			Log.e("Trigger", "start minutes not in allowed range!");
		}
	}

	public int getEndHours() {
		return endHours;
	}

	public void setEndHours(int _hours) {
		if (_hours < 24 && _hours >= -1) {
			this.endHours = _hours;
			Log.i("Trigger", "set end hours for time range");
		} else {
			Log.e("Trigger", "end hours not in allowed range!");
		}
	}

	public int getEndMinutes() {
		return endMinutes;
	}

	public void setEndMinutes(int _minutes) {
		if (_minutes < 60 && _minutes >= -1) {
			this.endMinutes = _minutes;
			Log.i("Trigger", "set end minutes for time range");
		} else {
			Log.e("Trigger", "end minutes not in allowed range!");
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
