package at.fhhgbg.mc.profileswitcher.trigger;

import android.util.Log;

public class Trigger {

	protected enum state {
		ignore, enabled
	};

	private String name;
	private int hours;
	private int minutes;
	private String profileName;

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
