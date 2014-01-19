package at.fhhgbg.mc.profileswitcher.widgets;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.profile.Profile;
import at.fhhgbg.mc.profileswitcher.profile.XmlParser;
import at.fhhgbg.mc.profileswitcher.services.Handler;

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

//		XmlParser parser = new XmlParser(this);
//		try {
//			parser.initializeXmlParser(openFileInput(fileName + "_profile.xml"));
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		} catch (XmlPullParserException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		//saves the active profile into the shared preferences
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		pref.edit().putString("active_profile", fileName).commit();
//
//		Toast toast = Toast.makeText(this, fileName + " was applied!",
//				Toast.LENGTH_SHORT);
//		toast.show();
		
		Handler handler = new Handler(this);
		handler.applyProfile(fileName);
		
		Log.i("widget", fileName);
		this.finish();
	}

}