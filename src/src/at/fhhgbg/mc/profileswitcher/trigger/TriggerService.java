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
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.XmlParser;

public class TriggerService extends Service {
		
	private TriggerBroadcastReceiver triggerReceiver;
	private int currentHours;
	private int currentMinutes;

	public void setHeadPhones(boolean headPhones) {
		this.headPhones = headPhones;
		Log.i("TriggerService", "Headphones changed");
		compareTriggers();
	}

	private boolean headPhones;
	private List<Trigger> triggerList = new ArrayList<Trigger>();
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("TriggerService", "TriggerService started");

//		 Trigger test = new Trigger();
//		 test.setProfileName("Test");
//		 test.setHours(19);
//		 test.setMinutes(10);
//		 test.setHeadphones(Trigger.listen_state.listen_off);
//		 triggerList.add(test);
//		
//		 Trigger test2 = new Trigger();
//		 test2.setProfileName("Test2");
//		 test2.setHeadphones(Trigger.listen_state.listen_on);
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
	
	private void setInitialHeadphones(){
		AudioManager audiomanager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		if(audiomanager.isWiredHeadsetOn()){
			headPhones = true;
			Log.i("TriggerService", "initial headphone value defined as: plugged");
		} else if (!audiomanager.isWiredHeadsetOn()){
			headPhones = false;
			Log.i("TriggerService", "initial headphone value defined as: unplugged");
		}
	}

	private void compareTriggers() {
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
			if(trigger.getHeadphones() == Trigger.listen_state.listen_on && headPhones || 
					trigger.getHeadphones() == Trigger.listen_state.listen_off && !headPhones){
				Log.i("TriggerService", "matching headphone trigger found");

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

