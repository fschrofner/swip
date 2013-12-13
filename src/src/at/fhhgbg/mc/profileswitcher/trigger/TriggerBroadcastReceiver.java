package at.fhhgbg.mc.profileswitcher.trigger;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class TriggerBroadcastReceiver extends BroadcastReceiver{
	TriggerService triggerservice;
	
	TriggerBroadcastReceiver(TriggerService _service){
		triggerservice = _service;
		IntentFilter filter=new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		_service.registerReceiver(this,filter);
		filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		_service.registerReceiver(this,filter);
	}

	@Override
	public void onReceive(Context _context, Intent _intent) {
		// TODO Auto-generated method stub
		if (_intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
			Log.i("TriggerBroadcastReceiver", "TimeTick");
			int h = Integer.parseInt(String.valueOf(Calendar
					.getInstance().get(Calendar.HOUR_OF_DAY)));
			int m = Integer.parseInt(String.valueOf(Calendar
					.getInstance().get(Calendar.MINUTE)));
			triggerservice.setTime(h, m);
		}
		else if (_intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
			int state = _intent.getIntExtra("state", -1);
            switch (state) {
            case 0:
            	triggerservice.setHeadPhones(false);
                Log.i("TriggerBroadcastReceiver", "Headset unplugged");
                break;
            case 1:
            	triggerservice.setHeadPhones(true);
                Log.i("TriggerBroadcastReceiver", "Headset plugged");
                break;
            }
            
            }
		
		}
	}
