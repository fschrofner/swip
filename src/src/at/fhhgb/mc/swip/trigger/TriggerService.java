package at.fhhgb.mc.swip.trigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import at.fhhgb.mc.swip.services.Handler;
import at.flosch.logwrap.Log;

/**
 * Service which manages the triggers.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class TriggerService extends Service{
	final static String TAG = "TriggerService";

	//triggerreceiver, so the broadcastreceiver can register itself dynamically in the constructor
	private TriggerBroadcastReceiver triggerReceiver;
	private int currentHours;
	private int currentMinutes;
	private String currentWeekday;
	private boolean headphones;
	private boolean batteryCharging;
	private int batteryLevel;
	private List<Trigger> triggerList = new ArrayList<Trigger>();
	private List<Trigger> triggerPriorityList = new ArrayList<Trigger>();
	private String[] geofences;

	/**
	 * Set the status of the headphones on initialization and compares the triggers.
	 */
	private void setInitialHeadphones(){
		AudioManager audiomanager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		if(audiomanager.isWiredHeadsetOn()){
			headphones = true;
			Log.i(TAG, "initial headphone value defined as: plugged");
		} else if (!audiomanager.isWiredHeadsetOn()){
			headphones = false;
			Log.i(TAG, "initial headphone value defined as: unplugged");
		}
		compareTriggers();
	}
	
	
	/**
	 * Set the time on initialization and compares the triggers.
	 */
	private void setInitialTime() {
		int h = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.HOUR_OF_DAY)));
		int m = Integer.parseInt(String.valueOf(Calendar
				.getInstance().get(Calendar.MINUTE)));
		setTime(h, m);
		
		compareTriggers();
	}
	
	/**
	 * Set the weekday on initialization and compares the triggers.
	 */
	private void setInitialWeekday() {
		String weekday = "";

	    Calendar c = Calendar.getInstance();
	    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

	    if (Calendar.MONDAY == dayOfWeek) {
	        weekday = "1";
	    } else if (Calendar.TUESDAY == dayOfWeek) {
	        weekday = "2";
	    } else if (Calendar.WEDNESDAY == dayOfWeek) {
	        weekday = "3";
	    } else if (Calendar.THURSDAY == dayOfWeek) {
	        weekday = "4";
	    } else if (Calendar.FRIDAY == dayOfWeek) {
	        weekday = "5";
	    } else if (Calendar.SATURDAY == dayOfWeek) {
	        weekday = "6";
	    } else if (Calendar.SUNDAY == dayOfWeek) {
	        weekday = "7";
	    }
	    
	    setWeekday(weekday);
	    
	    compareTriggers();
	}
	
	/**
	 * Clears the list of currently triggered geofences
	 */
	public void clearGeofences(){
		//should be null, the new list will be initiated by the system
		geofences = null;
		Log.i(TAG, "all geofences cleared!");
	}
	
	/**
	 * Set the battery state on initialization and compares the triggers.
	 */
	protected void setInitialBatteryState(Intent _intent){
		int status = _intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean batteryCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);
		Log.i(TAG, "initial battery state defined as " + batteryCharging);
		int level = _intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = _intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryLevelF = level / (float)scale;
		batteryLevel = (int)(batteryLevelF * 100);
		Log.i(TAG, "initial battery level defined as " + batteryLevel);
		compareTriggers();
	}
	
	public void setBatteryCharging(boolean batteryCharging) {
		this.batteryCharging = batteryCharging;
		Log.i(TAG, "batterystate changed to " + batteryCharging);
		compareTriggers();
	}

	public void setHeadphones(boolean _headphones) {
		this.headphones = _headphones;
		Log.i(TAG, "headphones changed to " + _headphones);
		compareTriggers();
	}

	public void setBatteryLevel(int _batteryLevel) {
		this.batteryLevel = _batteryLevel;
		Log.i(TAG, "batterylevel changed to " + _batteryLevel);
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

	/**
	 * Sets the initial values, initialises & registers the broadcastreceiver and loads the
	 * already existent triggers.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i(TAG, "TriggerService started");

		setInitialTime();
		setInitialWeekday();
		setInitialHeadphones();
		
		// Create a broadcast receiver to handle changes
		triggerReceiver = new TriggerBroadcastReceiver(this);
		
		//register the broadcast receiver for the intents
		IntentFilter filter=new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(triggerReceiver,filter);
		filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(triggerReceiver,filter);
		filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryIntent = registerReceiver(triggerReceiver, filter);
		setInitialBatteryState(batteryIntent);
		filter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		registerReceiver(triggerReceiver, filter);
		filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		registerReceiver(triggerReceiver, filter);
		filter = new IntentFilter("at.fhhgb.mc.swip.trigger.refresh");
		registerReceiver(triggerReceiver, filter);
    	filter = new IntentFilter("at.fhhgb.mc.swip.trigger.location_change");
		registerReceiver(triggerReceiver, filter);
		filter = new IntentFilter("at.fhhgb.mc.swip.trigger.clearGeofences");
		registerReceiver(triggerReceiver, filter);
		
		refreshTriggers();

		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(triggerReceiver);
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
		Log.i(TAG, "current time updated: " + currentHours + ":" + currentMinutes);
		compareTriggers();
	}
	
	/**
	 * Sets the weekday.
	 * 
	 * @param _currenWeekday
	 *            the current weekday.
	 */
	public void setWeekday(String _currentWeekday) {
		currentWeekday = _currentWeekday;
		Log.i(TAG, "current weekday updated: " + currentWeekday);
		compareTriggers();
	}

	/**
	 * Compares the triggers with the actual state.
	 */
	private void compareTriggers() {
		triggerPriorityList.clear();
		
		Log.i(TAG, "compareTriggers called");
		for (Trigger trigger : triggerList) {
			Log.i(TAG, "compare trigger: " + trigger.getName());
			if(compareTime(trigger)){
				Log.i(TAG, "trigger matching time");
				if(compareWeekday(trigger)){
					Log.i(TAG, "trigger matching weekday");
					if(compareHeadphones(trigger)){
						Log.i(TAG, "trigger matching headphones");
						if(compareBatteryCharging(trigger)){
							Log.i(TAG, "trigger matching battery state");
							if(compareBatteryLevel(trigger)){
								Log.i(TAG, "trigger matching battery level");
								if(compareGeofence(trigger)){
									Log.i(TAG,
											"trigger matching geofence");
									Log.i(TAG, "adding trigger to triggerPriorityList: " + trigger.getName());
										
										
									triggerPriorityList.add(trigger);
									Log.i(TAG, "highestPriority add: " + trigger.getName());
								} 
								else {
									Log.i(TAG, trigger.getName()
											+ " does not match geofence");
								}
							}
							else{
								Log.i(TAG, trigger.getName() + " does not match battery level");
							}					
						}
						else{
							Log.i(TAG, trigger.getName() + " does not match battery state");
						}
					}
					else{
						Log.i(TAG, trigger.getName() + " does not match headphones");
					}
				}
				else{
					Log.i(TAG, trigger.getName() + " does not match weekday");
				}
			}
			else{
				Log.i(TAG, trigger.getName() + " does not match time " + trigger.getStartHours() + " " + trigger.getEndHours());
			}
			
		}
		comparePriorities();
	}
	
	/**
	 * Compares the time saved inside the variables of the trigger with the current time received by the broadcast receiver.
	 * @param _trigger the trigger to compare to
	 * @return true = time matches, false = time does not match
	 */
	private boolean compareTime(Trigger _trigger){
		Log.i(TAG, "compare time called!");
		//if no time range is set
		if (_trigger.getStartHours() == -1 && _trigger.getEndHours() == -1){
			Log.i(TAG, "time ignored");
			return true;
		}
		//if the end time is not set (trigger is only activated at a certain time)
		else if (_trigger.getEndHours() == -1){
			Log.i(TAG, "no end time set, only compared to certain time");
			//if the current time matches the time set in the trigger
			if(currentHours == _trigger.getStartHours() && currentMinutes == _trigger.getStartMinutes()){
				Log.i(TAG, "trigger matches exact current time");
				return true;
			}
			else{
				return false;
			}
		}
		//if the start and end hours are on the same day
		else if((_trigger.getStartHours() < _trigger.getEndHours()) || 
				(_trigger.getStartHours() == _trigger.getEndHours() && _trigger.getStartMinutes() < _trigger.getEndMinutes())){
			Log.i(TAG, "time range on same day");
			
			//if the hours are in between the trigger hours
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
			Log.i(TAG, "time range on other day");
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
	
	/**
	 * Compares the weekday saved inside the variables of the trigger with the current weekday received by the broadcast receiver.
	 * 
	 * @param _trigger the trigger to compare to
	 * @return true = weekday matches, false = weekday does not match
	 */
	private boolean compareWeekday(Trigger _trigger) {
		Log.i(TAG, "compare weekday called!");
		//if every weekday is set
		
		if (_trigger.getWeekdays() == null) {
			return true;
		}
		
		if (_trigger.getWeekdays().size() == 7 || _trigger.getWeekdays().size() == 0){
			Log.i(TAG, "every or no weekday is set");
			return true;
		}
		
		if (_trigger.getWeekdays().contains(currentWeekday)){
			return true;
		}
		return false;
	}
	
	/**
	 * Compares the headphone state saved inside the variables of the trigger with the current headphone state received by the broadcast receiver.
	 * @param _trigger the trigger to compare to
	 * @return true = trigger matches headphone state, false = trigger does not match
	 */
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
	
	/**
	 * Compares the battery state saved inside the variables of the trigger with the current battery state received by the broadcast receiver.
	 * @param _trigger the trigger to compare to
	 * @return true = trigger matches battery state, false = trigger does not match
	 */
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
	
	/**
	 * Compares the battery level saved inside the variables of the trigger with the current battery level received by the broadcast receiver.
	 * If the trigger has only the battery start level defined, it needs to match the value exactly.
	 * @param _trigger the trigger to compare to
	 * @return true = trigger matches battery level, false = trigger does not match
	 */
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

	/**
	 * Compares the geofence saved inside the variables of the trigger with the current geofence received by the broadcast receiver.
	 * @param _trigger the trigger to compare to
	 * @return true = trigger matches geofence, false = trigger does not match
	 */
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
					Log.i(TAG, "Trigger found: " + trigger.getName());
					parser.initializeXmlParser(openFileInput(fileList[i]),
							trigger);
					triggerList.add(trigger);
				} else {
					Log.i(TAG, "Not a trigger file");
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		registerExistingGeofences();
		Log.i(TAG, "triggerList: " + triggerList.size());
	}
	
	/**
	 * Registers all the geofences already stored in a trigger.
	 */
	public void registerExistingGeofences(){
		SimpleGeofenceStore store = new SimpleGeofenceStore(getApplicationContext());
		LocationTrigger trigger = new LocationTrigger(getApplicationContext());
		SimpleGeofence geofence;
		
		for(int i=0; i < triggerList.size(); i++){
			if(triggerList.get(i).getGeofence() != null){
				geofence = store.getGeofence(triggerList.get(i).getGeofence());
				trigger.registerGeofence(geofence);
				Log.i(TAG, "Registered existing geofence: " + geofence.getId());
			}
		}
	}
	
	/**
	 * Compares the priorities of triggers, if two or more are matching the current phone state.
	 */
	private void comparePriorities() {
		int highestPriority = -1;
		Trigger highestTrigger = null;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Log.i(TAG, "comparePriorities called");
		
		if (triggerPriorityList.size() > 1) {
			Log.i(TAG, "triggerPriorityList: " + triggerPriorityList.size());
			
			for(Trigger trigger : triggerPriorityList) {
				
				if (trigger.getPriority() > highestPriority) {
					highestTrigger = trigger;
					highestPriority = trigger.getPriority();
				}
			}
			
			if (highestTrigger != null && !highestTrigger.getProfileName().equals(pref.getString("active_profile", "Default"))) {
				Handler handler = new Handler(getApplicationContext());
				handler.applyProfile(highestTrigger.getProfileName());
				Log.i(TAG, "matching trigger found: " + highestTrigger.getName());
			}
			
		} else if (triggerPriorityList.size() == 1 && !triggerPriorityList.get(0).getProfileName().equals(pref.getString("active_profile", "Default"))) {
			Handler handler = new Handler(getApplicationContext());
			handler.applyProfile(triggerPriorityList.get(0).getProfileName());
			Log.i(TAG, "matching trigger found: " + triggerPriorityList.get(0).getName());
		}		
		
	}

}
