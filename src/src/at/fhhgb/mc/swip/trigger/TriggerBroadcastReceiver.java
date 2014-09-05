package at.fhhgb.mc.swip.trigger;

import java.util.Calendar;
import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Receives several broadcasts and sets the according variables in the trigger service.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class TriggerBroadcastReceiver extends BroadcastReceiver{
	TriggerService triggerservice;
	
	TriggerBroadcastReceiver(TriggerService _service){
		triggerservice = _service;
	}

	/**
	 * Receives the broadcasts registered in the constructor and sets the information
	 * in the triggerservice.
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context _context, Intent _intent) {
		
		Log.i("TriggerBroadcastReceiver", "Broadcast received: " + _intent.getAction());
		
		if (_intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
			
			int h = Integer.parseInt(String.valueOf(Calendar
					.getInstance().get(Calendar.HOUR_OF_DAY)));
			int m = Integer.parseInt(String.valueOf(Calendar
					.getInstance().get(Calendar.MINUTE)));
			triggerservice.setTime(h, m);
			Log.i("TriggerBroadcastReceiver", "TimeTick: " + h + ":" + m);
		}
		else if (_intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
			int state = _intent.getIntExtra("state", -1);
            switch (state) {
            case 0:
            	triggerservice.setHeadphones(false);
                Log.i("TriggerBroadcastReceiver", "Headset unplugged");
                break;
            case 1:
            	triggerservice.setHeadphones(true);
                Log.i("TriggerBroadcastReceiver", "Headset plugged");
                break;
            }
         } 
		if(_intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
        	triggerservice.setBatteryCharging(true);

        } else if (_intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
        	triggerservice.setBatteryCharging(false);
        }
		if(_intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
			int level = _intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = _intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			float batteryLevel = level / (float)scale;
			batteryLevel = batteryLevel * 100;
			triggerservice.setBatteryLevel((int)batteryLevel);
		}
		if(_intent.getAction().equals("at.fhhgb.mc.swip.trigger.refresh")){
			triggerservice.refreshTriggers();
		}
		if(_intent.getAction().equals("at.fhhgb.mc.swip.trigger.clearGeofences")){
			triggerservice.clearGeofences();
		}
		if(_intent.getAction().equals("at.fhhgb.mc.swip.trigger.location_change")){
			Log.i("TriggerBroadcastReceiver", "Location change detected - action");
			// First check for errors
			if (LocationClient.hasError(_intent)) {
				// Get the error code with a static method
				int errorCode = LocationClient.getErrorCode(_intent);
				// Log the error
				Log.e("ReceiveTransitionsIntentService",
						"Location Services error: " + Integer.toString(errorCode));
				
				 // if there's no error, get the transition type and the IDs of the
				 // geofence or geofences that triggered the transition
				
			} else {
				// Get the type of transition (entry or exit)
				int transitionType = LocationClient.getGeofenceTransition(_intent);
				// Test that a valid transition was reported
				if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
						|| (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
					List<Geofence> triggerList = LocationClient
							.getTriggeringGeofences(_intent);

					String[] triggerIds = new String[triggerList.size()];
					for (int i = 0; i < triggerIds.length; i++) {
						// Store the Id of each geofence
						triggerIds[i] = triggerList.get(i).getRequestId();
						Log.i("TriggerBroadcastReceiver", "matching geofence: " + triggerIds[i]);
					}
					
					triggerservice.setGeofences(triggerIds);

				// An invalid transition was reported
				} else {
					Log.e("ReceiveTransitionsIntentService",
							"Geofence transition error: "
									+ Integer.toString(transitionType));
				}
			}
		}
	}
}
