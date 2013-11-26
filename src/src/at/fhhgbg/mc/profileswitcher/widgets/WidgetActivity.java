package at.fhhgbg.mc.profileswitcher.widgets;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.XmlParser;

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

		XmlParser parser = new XmlParser(this);
		try {
			parser.initializeXmlParser(openFileInput(fileName + ".xml"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast toast = Toast.makeText(this, fileName + " was applied!",
				Toast.LENGTH_SHORT);
		toast.show();
		Log.i("widget", fileName);
		this.finish();
	}

}