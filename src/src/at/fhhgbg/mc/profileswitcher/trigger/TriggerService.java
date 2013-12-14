package at.fhhgbg.mc.profileswitcher.trigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.XmlParser;

public class TriggerService extends Service {
		
	private TriggerBroadcastReceiver triggerReceiver;
	private int currentHours;
	private int currentMinutes;
	private boolean headphones;
	private boolean batteryCharging;
	private int batteryLevel;
		private List<Trigger> triggerList = new ArrayList<Trigger>();

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

	public void setHeadPhones(boolean _headphones) {
		this.headphones = _headphones;
		Log.i("TriggerService", "headphones changed to " + _headphones);
		compareTriggers();
	}
	
	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
		Log.i("TriggerService", "batterylevel changed to " + batteryLevel);
		compareTriggers();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("TriggerService", "TriggerService started");

		 Trigger test = new Trigger();
		 test.setProfileName("Test");
//		 test.setHours(19);
//		 test.setMinutes(10);
//		 test.setHeadphones(Trigger.listen_state.listen_off);
//		 test.setBatteryState(Trigger.listen_state.listen_on);
		 test.setBatteryLevel(96);
		 triggerList.add(test);
		
//		 Trigger test2 = new Trigger();
//		 test2.setProfileName("Test2");
//		 test2.setHeadphones(Trigger.listen_state.listen_on);
//		 test2.setBatteryState(Trigger.listen_state.listen_off);
//		 triggerList.add(test2);
		 
		setInitialHeadphones();
		
		// Create a broadcast receiver to handle changes
		triggerReceiver = new TriggerBroadcastReceiver(this);

		return super.onStartCommand(intent, flags, startId);
	}

	public void setTime(int _currentHours, int _currentMinutes) {
		currentHours = _currentHours;
		currentMinutes = _currentMinutes;
		Log.i("TriggerService", "current time updated");
		compareTriggers();
	}

	private void compareTriggers() {
		Log.i("TriggerService", "compareTriggers called");
		for (Trigger trigger : triggerList) {
//			if (trigger.getHours() == currentHours
//					&& trigger.getMinutes() == currentMinutes) {
//				Log.i("TriggerService", "matching trigger found");
//
//				XmlParser parser = new XmlParser(getApplicationContext());
//				try {
//					// applies the profile.
//					parser.initializeXmlParser(openFileInput(trigger
//							.getProfileName() + ".xml"));
//				} catch (NotFoundException e) {
//					e.printStackTrace();
//				} catch (XmlPullParserException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				Log.i("TriggerService", "profile applied");
//			}
//			if((trigger.getHeadphones() == Trigger.listen_state.listen_on && headphones) || 
//					(trigger.getHeadphones() == Trigger.listen_state.listen_off && !headphones)){
//				Log.i("TriggerService", "matching headphone trigger found");
//
//				XmlParser parser = new XmlParser(getApplicationContext());
//				try {
//					// applies the profile.
//					parser.initializeXmlParser(openFileInput(trigger
//							.getProfileName() + ".xml"));
//					Toast.makeText(getApplicationContext(), trigger.getProfileName() + " was applied!", Toast.LENGTH_SHORT).show();
//				} catch (NotFoundException e) {
//					e.printStackTrace();
//				} catch (XmlPullParserException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if((trigger.getBatteryState() == Trigger.listen_state.listen_on && batteryCharging) ||
//					(trigger.getBatteryState() == Trigger.listen_state.listen_off && !batteryCharging)){
//				Log.i("TriggerService", "matching batterystate trigger found");
//
//				XmlParser parser = new XmlParser(getApplicationContext());
//				try {
//					// applies the profile.
//					parser.initializeXmlParser(openFileInput(trigger
//							.getProfileName() + ".xml"));
//					Toast.makeText(getApplicationContext(), trigger.getProfileName() + " was applied!", Toast.LENGTH_SHORT).show();
//				} catch (NotFoundException e) {
//					e.printStackTrace();
//				} catch (XmlPullParserException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
			if(trigger.getBatteryLevel() == batteryLevel){
				Log.i("TriggerService", "matching batterylevel trigger found");

				XmlParser parser = new XmlParser(getApplicationContext());
				try {
					// applies the profile.
					parser.initializeXmlParser(openFileInput(trigger
							.getProfileName() + ".xml"));
					Toast.makeText(getApplicationContext(), trigger.getProfileName() + " was applied!", Toast.LENGTH_SHORT).show();
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	private void refreshTriggers() {
//
//		String[] fileList = getFilesDir().list();
//		XmlParser parser = new XmlParser(this);
//
//		try {
//			for (int i = 0; i < fileList.length; i++) {
//				parser.initializeXmlParser(openFileInput(fileList[i]));
//			}
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		} catch (XmlPullParserException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
		
}

