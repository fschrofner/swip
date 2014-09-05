package at.fhhgb.mc.swip.widgets;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import at.fhhgb.mc.swip.services.Handler;

/**
 * Transparent activity used to apply a profile without showing anything.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class WidgetActivity extends Activity {

	/**
	 * Applies the profile inside the intent.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String fileName = getIntent().getStringExtra("fileName");

		Handler handler = new Handler(this);
		handler.applyProfile(fileName);
		
		Log.i("widget", fileName);
		this.finish();
	}

}