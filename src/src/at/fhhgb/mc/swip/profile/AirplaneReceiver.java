package at.fhhgb.mc.swip.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AirplaneReceiver extends BroadcastReceiver{
	Setter setter;
	boolean airplaneModeFinished;
	
	AirplaneReceiver(Setter _setter){
		this.setter = _setter;
		airplaneModeFinished = false;
	}
	
	@Override
	public void onReceive(Context _context, Intent _intent) {
		airplaneModeFinished = true;
		Log.i("AirplaneReceiver", "AirplaneMode completed!");
	}

	public boolean isAirplaneModeFinished() {
		return airplaneModeFinished;
	}
	
	
	
}
