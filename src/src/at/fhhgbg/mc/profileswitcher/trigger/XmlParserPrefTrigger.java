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
	/**
	 * Initializes the xml parser with the given context.
	 * 
	 * @param _context
	 */
	public XmlParserPrefTrigger(Context _context) {
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		context = _context;
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
	public void initializeXmlParser(InputStream _in, Trigger _trigger)
			throws XmlPullParserException, IOException {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(_in, null);
			parser.nextTag();
			readAndApplyTags(parser, _trigger);
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
	private void readAndApplyTags(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "trigger");

		while (_parser.next() != XmlPullParser.END_TAG) { 		// while the tag is not the closing tag

			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue; 										// skips this turn if the tag is not a start tag
			}

			String name = _parser.getName();
			
			// Starts by looking for the entry tag
			if (name.equals("profile")) {
				setProfile(_parser, _trigger);
			} else if (name.equals("time")) {
				setTime(_parser, _trigger);
			} else if (name.equals("battery")) {
				setBattery(_parser, _trigger);
			} else if (name.equals("headphone")) {
				setHeadphone(_parser, _trigger);
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
	private void setProfile(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "profile");

		if (_parser.getAttributeValue(null, "name") != null) {
			_trigger.setProfileName(_parser.getAttributeValue(null, "name"));
			Log.i("XmlParserTrigger",
					"Profile: " + _parser.getAttributeValue(null, "name"));
		} else {
			Log.e("XmlParserTrigger", "Profile: Invalid Argument!");
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
	private void setTime(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "time");

		if (_parser.getAttributeValue(null, "start_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "start_hours")) <= 23) {
				_trigger.setStartHours(Integer.parseInt(_parser.getAttributeValue(
						null, "start_hours")));
				Log.i("XmlParserTrigger",
						"start_hours: " + _parser.getAttributeValue(null, "start_hours"));
			} else {
				Log.i("XmlParserTrigger", "start_hours: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "start_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "start_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "start_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_minutes")) <= 59) {
				_trigger.setStartMinutes(Integer.parseInt(_parser.getAttributeValue(
						null, "start_minutes")));
				Log.i("XmlParserTrigger",
						"start_minutes: "
								+ _parser.getAttributeValue(null, "start_minutes"));
			} else {
				Log.i("XmlParserTrigger", "start_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "start_minutes: Invalid Argument!");
		}
		
		if (_parser.getAttributeValue(null, "end_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "end_hours")) <= 23) {
				_trigger.setEndHours(Integer.parseInt(_parser.getAttributeValue(
						null, "end_hours")));
				Log.i("XmlParserTrigger",
						"end_hours: " + _parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i("XmlParserTrigger", "end_hours: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "end_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_minutes")) <= 59) {
				_trigger.setEndMinutes(Integer.parseInt(_parser.getAttributeValue(
						null, "end_minutes")));
				Log.i("XmlParserTrigger",
						"end_minutes: "
								+ _parser.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i("XmlParserTrigger", "end_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "end_minutes: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Applies the Battery settings.
	 * 
	 * @param _parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setBattery(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "battery");

		if (_parser.getAttributeValue(null, "level") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "level")) >= 0
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "level")) <= 100) {
				_trigger.setBatteryLevel(Integer.parseInt(_parser
						.getAttributeValue(null, "level")));
				Log.i("XmlParserTrigger",
						"BatteryLevel: "
								+ _parser.getAttributeValue(null, "level"));
			} else {
				Log.i("XmlParserTrigger", "BatteryLevel: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "BatteryLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				_trigger.setBatteryState(Trigger.listen_state.listen_on);
				Log.i("XmlParserTrigger", "BatteryState listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				_trigger.setBatteryState(Trigger.listen_state.listen_off);
				Log.i("XmlParserTrigger", "BatteryState listen off.");
			} else {
				Log.i("XmlParserTrigger", "BateryState: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "BatteryState: Invalid Argument!");
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
	private void setHeadphone(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "headphone");
		
		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				_trigger.setHeadphones(Trigger.listen_state.listen_on);
				Log.i("XmlParserTrigger", "Headphones listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				_trigger.setHeadphones(Trigger.listen_state.listen_off);
				Log.i("XmlParserTrigger", "Headphones listen off.");
			} else {
				Log.i("XmlParserTrigger", "Headphones: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "Headphones: Invalid Argument!");
		}
		
		_parser.nextTag();
	}
}
