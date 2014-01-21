package at.fhhgbg.mc.profileswitcher.trigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.profile.XmlParser;
import at.fhhgbg.mc.profileswitcher.services.Handler;

/**
 * Service which manages the triggers.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class TriggerService extends Service{

	private TriggerBroadcastReceiver triggerReceiver;
	private int currentHours;
	private int currentMinutes;
	private boolean headphones;
	private boolean batteryCharging;
	private int batteryLevel;
	private List<Trigger> triggerList = new ArrayList<Trigger>();
	private String[] geofences;

	private void setInitialHeadphones(){
		AudioManager audiomanager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		if(audiomanager.isWiredHeadsetOn()){
			headphones = true;
			Log.i("TriggerService", "initial headphone value defined as: plugged");
		} else if (!audiomanager.isWiredHeadsetOn()){
			headphones = false;
			Log.i("TriggerService", "initial headphone value defined as: unplugged");
		}
		compareTriggers();
	}
	
	private void setInitialTime() {
		int h = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.HOUR_OF_DAY)));
		int m = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.MINUTE)));
		setTime(h, m);
	}
	
	protected void setInitialBatteryState(Intent _intent){
		int status = _intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean batteryCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);
		Log.i("TriggerService", "initial battery state defined as " + batteryCharging);
		int level = _intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = _intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryLevelF = level / (float)scale;
		batteryLevel = (int)(batteryLevelF * 100);
		Log.i("TriggerService", "initial battery level defined as " + batteryLevel);
		compareTriggers();
	}
	
	public void setBatteryCharging(boolean batteryCharging) {
		this.batteryCharging = batteryCharging;
		Log.i("TriggerService", "batterystate changed to " + batteryCharging);
		compareTriggers();
	}

	public void setHeadphones(boolean _headphones) {
		this.headphones = _headphones;
		Log.i("TriggerService", "headphones changed to " + _headphones);
		compareTriggers();
	}

	public void setBatteryLevel(int _batteryLevel) {
		this.batteryLevel = _batteryLevel;
		Log.i("TriggerService", "batterylevel changed to " + _batteryLevel);
		compareTriggers();
	}
	
	public void setGeofences(String[] _geofences) {
		this.geofences = _geofences;
		compareTriggers();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("TriggerService", "TriggerService started");

//		 Trigger test = new Trigger("Test");
//		 test.setProfileName("Test");
//		 test.setStartHours(15);
//		 test.setStartMinutes(26);
//		 test.setEndHours(15);
//		 test.setEndMinutes(28);
//		 test.setHours(19);
//		 test.setMinutes(10);
//		 test.setHeadphones(Trigger.listen_state.listen_off);
//		 test.setBatteryState(Trigger.listen_state.listen_on);
//		 test.setBatteryLevel(96);
//		 triggerList.add(test);
		
//		 Trigger test2 = new Trigger("Test2");
//		 test2.setProfileName("Test4");
//		 test2.setStartHours(19);
//		 test2.setStartMinutes(22);
//		 test2.setEndHours(20);
//		 test2.setEndMinutes(24);
//		 test2.setProfileName("Test2");
//		 test2.setHeadphones(Trigger.listen_state.listen_on);
//		 test2.setBatteryState(Trigger.listen_state.listen_off);
//		 triggerList.add(test2);
		 
		setInitialTime();
		setInitialHeadphones();
		
		// Create a broadcast receiver to handle changes
		triggerReceiver = new TriggerBroadcastReceiver(this);
		refreshTriggers();

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Sets the time.
	 * 
	 * @param _currentHours
	 *            the current number of hours.
	 * @param _currentMinutes
	 *            the current number of minutes.
	 */
	public void setTime(int _currentHours, int _currentMinutes) {
		currentHours = _currentHours;
		currentMinutes = _currentMinutes;
		Log.i("TriggerService", "current time updated: " + currentHours + ":" + currentMinutes);
		compareTriggers();
	}

	/**
	 * Compares the triggers with the actual state.
	 */
	private void compareTriggers() {
		Log.i("TriggerService", "compareTriggers called");
		for (Trigger trigger : triggerList) {
			Log.i("TriggerService", "compare trigger: " + trigger.getName());
			if(compareTime(trigger)){
				Log.i("TriggerService", "trigger matching time");
				if(compareHeadphones(trigger)){
					Log.i("TriggerService", "trigger matching headphones");
					if(compareBatteryCharging(trigger)){
						Log.i("TriggerService", "trigger matching battery state");
						if(compareBatteryLevel(trigger)){
							Log.i("TriggerService", "trigger matching battery level");
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
							if(compareGeofence(trigger)){
								Log.i("TriggerService",
										"trigger matching geofence");
								if (!trigger.getProfileName().equals(pref.getString("active_profile", "Default"))) {
									Log.i("TriggerService", "matching trigger found: " + trigger.getName());
									
									Handler handler = new Handler(getApplicationContext());
									handler.applyProfile(trigger.getProfileName());
								} 						
								else{
									Log.i("TriggerService", trigger.getProfileName() + " is already applied");
								}	
							} 
							else {
								Log.i("TriggerService", trigger.getName()
										+ " does not match geofence");
							}
						}
						else{
							Log.i("TriggerService", trigger.getName() + " does not match battery level");
						}					
					}
					else{
						Log.i("TriggerService", trigger.getName() + " does not match battery state");
					}
				}
				else{
					Log.i("TriggerService", trigger.getName() + " does not match headphones");
				}
			}
			else{
				Log.i("TriggerService", trigger.getName() + " does not match time " + trigger.getStartHours() + " " + trigger.getEndHours());
			}
			
		}
	}
	
	private boolean compareTime(Trigger _trigger){
		Log.i("TriggerService", "compare time called!");
		//if no time range is set
		if (_trigger.getStartHours() == -1 && _trigger.getEndHours() == -1){
			Log.i("TriggerService", "time ignored");
			return true;
		}
		//if the end time is not set (trigger is only activated at a certain time)
		else if (_trigger.getEndHours() == -1){
			Log.i("TriggerService", "no end time set, only compared to certain time");
			//if the current time matches the time set in the trigger
			if(currentHours == _trigger.getStartHours() && currentMinutes == _trigger.getStartMinutes()){
				Log.i("TriggerService", "trigger matches exact current time");
				return true;
			}
			else{
				return false;
			}
		}
		//if the start and end hours are on the same day
		else if((_trigger.getStartHours() < _trigger.getEndHours()) || 
				(_trigger.getStartHours() == _trigger.getEndHours() && _trigger.getStartMinutes() < _trigger.getEndMinutes())){
			Log.i("TriggerService", "time range on same day");
			
			//if the hours are inbetween the trigger hours
			if(currentHours > _trigger.getStartHours() && currentHours < _trigger.getEndHours()){
					return true;			
			}
			//if the start hours are the same as the current hour
			else if(currentHours == _trigger.getStartHours() && currentMinutes >= _trigger.getStartMinutes()){
				if(currentHours < _trigger.getEndHours()){
					return true;
				}
				else if(currentHours == _trigger.getEndHours() && currentMinutes <= _trigger.getEndMinutes()){
					return true;
				}
				else {
					return false;
				}
			}
			//if the end hours are the same as the current hour
			else if(currentHours == _trigger.getEndHours() && currentMinutes <= _trigger.getEndMinutes()){
				if(currentHours > _trigger.getStartHours()){
					return true;
				}
				else if(currentHours == _trigger.getStartHours() && currentMinutes > _trigger.getStartMinutes()){
					return true;
				}
				else {
					return false;
				}
			}
			else{
				return false;
			}
		//if the end time is already on the next day
		} else if (_trigger.getStartHours() > _trigger.getEndHours() || 
				(_trigger.getStartHours() == _trigger.getEndHours() && _trigger.getStartMinutes() > _trigger.getEndMinutes())){
			Log.i("TriggerService", "time range on other day");
			//if the time is after the start time or before the end time
			if(currentHours > _trigger.getStartHours() || currentHours < _trigger.getEndHours()){
				return true;
			}
			//if the hour is the same as the start hour
			else if(currentHours == _trigger.getStartHours() && currentMinutes >= _trigger.getStartMinutes()){
				return true;
			}
			//if the hour is the same as the end hour
			else if(currentHours == _trigger.getEndHours() && currentMinutes <= _trigger.getEndMinutes()){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	private boolean compareHeadphones(Trigger _trigger){
		if(!_trigger.getHeadphones().equals(Trigger.listen_state.ignore)){
			if(headphones && _trigger.getHeadphones().equals(Trigger.listen_state.listen_on)){
				return true;
			}
			if(!headphones && _trigger.getHeadphones().equals(Trigger.listen_state.listen_off)){
				return true;
			}
			else{
				return false;
			}
		}
		else {
			return true;
		}
	}
	
	private boolean compareBatteryCharging(Trigger _trigger){
		if(!_trigger.getBatteryState().equals(Trigger.listen_state.ignore)){
			if(_trigger.getBatteryState().equals(Trigger.listen_state.listen_on) && batteryCharging){
				return true;
			}
			if(_trigger.getBatteryState().equals(Trigger.listen_state.listen_off) && !batteryCharging){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return true;
		}
	}
	
	private boolean compareBatteryLevel(Trigger _trigger){
		if(_trigger.getBatteryStartLevel() == -1 && _trigger.getBatteryEndLevel() == -1){
			return true;
		}
		else if(_trigger.getBatteryEndLevel() == -1 && _trigger.getBatteryStartLevel() == batteryLevel){
				return true;
		} 
		else if(_trigger.getBatteryStartLevel() < batteryLevel && 
				_trigger.getBatteryEndLevel() > batteryLevel){
					return true;
		}
		return false;
	}

	private boolean compareGeofence(Trigger _trigger){
		//if there is no geofence set for the trigger
		if(_trigger.getGeofence() == null){
			return true;
		}
		if(geofences != null){
			for(int i = 0; i < geofences.length; i++){
				//if the geofence set for the trigger is found inside the triggered geofences
				if(geofences[i].equals(_trigger.getGeofence())){
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Refreshes the list of triggers.
	 */
	public void refreshTriggers() {

		triggerList.clear();

		String[] fileList = getFilesDir().list();
		XmlParserTrigger parser = new XmlParserTrigger(this);

		try {
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].contains("_trigger")) {
					Trigger trigger = new Trigger(fileList[i].substring(0,
							fileList[i].length() - 12));
					Log.i("TriggerService", "Trigger found: " + trigger.getName());
					parser.initializeXmlParser(openFileInput(fileList[i]),
							trigger);
					triggerList.add(trigger);
				} else {
					Log.i("TriggerService", "no Trigger found");
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("TriggerService", "triggerList: " + triggerList.size());
	}

}
