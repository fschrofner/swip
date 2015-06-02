package at.fhhgb.mc.swip.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import at.flosch.logwrap.Log;

/**
 * BootCompletedReceiver which starts the AutostartService to show the permanent
 * notification on reboot.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	final static String TAG = "BootCompletedReceiver";

	/**
	 * Starts the AutostartService when a it receives a boot completed message.
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 *      android.content.Intent)
	 */
	@Override
	public void onReceive(Context _context, Intent _intent) {
		Log.i(TAG, "boot completed");
		
		Intent intent = new Intent(_context, AutostartService.class);
		_context.startService(intent);
	}
}