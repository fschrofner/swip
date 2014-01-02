package at.fhhgbg.mc.profileswitcher.trigger;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

public class XmlParserPrefTrigger {
	
	Context context;
	Editor prefEdit;
	String triggerName;
	
	/**
	 * Initializes the xml parser with the given context.
	 * 
	 * @param _context
	 */
	public XmlParserPrefTrigger(Context _context, String _name) {
		context = _context;
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		triggerName = _name;
	}

	/**
	 * Sets up the xml parser for the given inputstream and then hands it over
	 * to the readAndApplyTags method to process the stream.
	 * 
	 * @param _in
	 *            the input stream you want to parse.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void initializeXmlParser(InputStream _in)
			throws XmlPullParserException, IOException {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(_in, null);
			parser.nextTag();
			readAndApplyTags(parser);
		} finally {
			_in.close(); 										// closes the inputstream in the end
		}
	}

	/**
	 * Reads the given input stream.
	 * 
	 * @param _parser
	 *            the parser which should read the tags
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void readAndApplyTags(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		
		prefEdit.putString("name_trigger", triggerName);
		
		_parser.require(XmlPullParser.START_TAG, null, "trigger");

		while (_parser.next() != XmlPullParser.END_TAG) { 		// while the tag is not the closing tag

			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue; 										// skips this turn if the tag is not a start tag
			}

			String name = _parser.getName();
			
			// Starts by looking for the entry tag
			if (name.equals("profile")) {
				setProfile(_parser);
			} else if (name.equals("time")) {
				setTime(_parser);
			} else if (name.equals("battery")) {
				setBattery(_parser);
			} else if (name.equals("headphone")) {
				setHeadphone(_parser);
			} else {
				Log.w("XmlParser", "Skip!"); 					// invalid tag, will be skipped
				_parser.nextTag();
			}
		}
	}

	/**
	 * Sets the profile according to the next tags in the given xml parser.
	 * 
	 * @param _parser
	 *            the xml parser of which you want to apply the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setProfile(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "profile");

		if (_parser.getAttributeValue(null, "name") != null) {
			prefEdit.putString("name", _parser.getAttributeValue(null, "name"));
			Log.i("XmlParserPrefTrigger",
					"Profile: " + _parser.getAttributeValue(null, "name"));
		} else {
			Log.e("XmlParserPrefTrigger", "Profile: Invalid Argument!");
		}
		_parser.nextTag();
	}

	/**
	 * Sets the time according to the tags inside the given xml parser.
	 * 
	 * @param _parser
	 *            the parser of which you want to apply settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setTime(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "time");
		
		int startHours = 0;
		int startMinutes = 0;
		int endHours = 0;
		int endMinutes = 0;

		if (_parser.getAttributeValue(null, "start_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "start_hours")) <= 23) {
				startHours = Integer.parseInt(_parser.getAttributeValue(
						null, "start_hours"));
				Log.i("XmlParserPrefTrigger",
						"start_hours: " + _parser.getAttributeValue(null, "start_hours"));
			} else {
				Log.i("XmlParserPrefTrigger", "start_hours: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "start_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "start_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "start_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_minutes")) <= 59) {
				startMinutes = Integer.parseInt(_parser.getAttributeValue(
						null, "start_minutes"));
				Log.i("XmlParserPrefTrigger",
						"start_minutes: "
								+ _parser.getAttributeValue(null, "start_minutes"));
			} else {
				Log.i("XmlParserPrefTrigger", "start_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "start_minutes: Invalid Argument!");
		}
		
		if (_parser.getAttributeValue(null, "end_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "end_hours")) <= 23) {
				endHours = Integer.parseInt(_parser.getAttributeValue(
						null, "end_hours"));
				Log.i("XmlParserPrefTrigger",
						"end_hours: " + _parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i("XmlParserPrefTrigger", "end_hours: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "end_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_minutes")) <= 59) {
				endMinutes = Integer.parseInt(_parser.getAttributeValue(
						null, "end_minutes"));
				Log.i("XmlParserPrefTrigger",
						"end_minutes: "
								+ _parser.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i("XmlParserPrefTrigger", "end_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "end_minutes: Invalid Argument!");
		}
		
		String startTime;
		String endTime;
		
		if (startHours != -1 && startMinutes != -1) {
			startTime = new String(startHours + ":" + startMinutes);
		} else {
			startTime = new String("Ignored");
		}
		if (endHours != -1 && endMinutes != -1) {
			endTime = new String(endHours + ":" + endMinutes);
		} else {
			endTime = new String("Ignored");
		}
		
		prefEdit.putString("start_time", startTime);
		prefEdit.putString("end_time", endTime);

		_parser.nextTag();
	}

	/**
	 * Applies the Battery settings.
	 * 
	 * @param _parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setBattery(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "battery");

		if (_parser.getAttributeValue(null, "level") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "level")) >= 0
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "level")) <= 100) {
				prefEdit.putInt("battery_level", Integer.parseInt(_parser
						.getAttributeValue(null, "level")));
				Log.i("XmlParserPrefTrigger",
						"BatteryLevel: "
								+ _parser.getAttributeValue(null, "level"));
			} else {
				Log.i("XmlParserPrefTrigger", "BatteryLevel: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "BatteryLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("battery_state", "charging");
				Log.i("XmlParserPrefTrigger", "BatteryState listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("battery_state", "discharging");
				Log.i("XmlParserPrefTrigger", "BatteryState listen off.");
			} else if (_parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("battery_state", "ignored");
				Log.i("XmlParserPrefTrigger", "BatteryState ignored.");
			} else {
				Log.i("XmlParserPrefTrigger", "BateryState: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "BatteryState: Invalid Argument!");
		}

		_parser.nextTag();
	}
	
	
	/**
	 * Applies the Headphone settings.
	 * 
	 * @param _parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setHeadphone(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "headphone");
		
		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("headphone", "plugged_in");
				Log.i("XmlParserPrefTrigger", "Headphones listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("headphone", "unplugged");
				Log.i("XmlParserPrefTrigger", "Headphones listen off.");
			} else if (_parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("headphone", "ignored");
				Log.i("XmlParserPrefTrigger", "Headphones ignored.");
			} else {
				Log.i("XmlParserPrefTrigger", "Headphones: ignore.");
			}
		} else {
			Log.e("XmlParserPrefTrigger", "Headphones: Invalid Argument!");
		}
		
		_parser.nextTag();
	}
}
