package at.fhhgb.mc.swip.trigger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import at.flosch.logwrap.Log;
import android.util.Xml;

/**
 * Class that is used to read an xml input stream and load the triggers into the
 * triggerservice to be compared.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class XmlParserTrigger {
	final static String TAG = "XmlParserTrigger";

	Context context;

	/**
	 * Initializes the xml parser with the given context.
	 * 
	 * @param _context
	 */
	public XmlParserTrigger(Context _context) {
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
			_in.close(); // closes the inputstream in the end
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

		while (_parser.next() != XmlPullParser.END_TAG) { // while the tag is
															// not the closing
															// tag

			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue; // skips this turn if the tag is not a start tag
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
			} else if (name.equals("geofence")) {
				setGeofence(_parser, _trigger);
			} else if (name.equals("priority")) {
				setPriority(_parser, _trigger);
			} else if (name.equals("weekdays")) {
				setWeekdays(_parser, _trigger);
			} else {
				Log.w("XmlParser", "Skip!"); // invalid tag, will be skipped
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
			Log.i(TAG,
					"Profile: " + _parser.getAttributeValue(null, "name"));
		} else {
			Log.e(TAG, "Profile: Invalid Argument!");
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
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_hours")) <= 23) {
				_trigger.setStartHours(Integer.parseInt(_parser
						.getAttributeValue(null, "start_hours")));
				Log.i(TAG,
						"start_hours: "
								+ _parser
										.getAttributeValue(null, "start_hours"));
			} else {
				Log.i(TAG, "start_hours: ignore.");
			}
		} else {
			Log.e(TAG, "start_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "start_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null,
					"start_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_minutes")) <= 59) {
				_trigger.setStartMinutes(Integer.parseInt(_parser
						.getAttributeValue(null, "start_minutes")));
				Log.i(TAG,
						"start_minutes: "
								+ _parser.getAttributeValue(null,
										"start_minutes"));
			} else {
				Log.i(TAG, "start_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "start_minutes: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_hours")) <= 23) {
				_trigger.setEndHours(Integer.parseInt(_parser
						.getAttributeValue(null, "end_hours")));
				Log.i(TAG,
						"end_hours: "
								+ _parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i(TAG, "end_hours: ignore.");
			}
		} else {
			Log.e(TAG, "end_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_minutes") != null) {
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_minutes")) <= 59) {
				_trigger.setEndMinutes(Integer.parseInt(_parser
						.getAttributeValue(null, "end_minutes")));
				Log.i(TAG,
						"end_minutes: "
								+ _parser
										.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i(TAG, "end_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "end_minutes: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the battery according to the tags inside the given xml parser.
	 * 
	 * @param _parser
	 *            the parser of which you want to apply settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setBattery(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "battery");

		if (_parser.getAttributeValue(null, "start_level") != null) {
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "start_level")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_level")) <= 100) {
				_trigger.setBatteryStartLevel(Integer.parseInt(_parser
						.getAttributeValue(null, "start_level")));
				Log.i(TAG,
						"BatteryStartLevel: "
								+ _parser
										.getAttributeValue(null, "start_level"));
			} else {
				Log.i(TAG, "BatteryStartLevel: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryStartLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_level") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_level")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_level")) <= 100) {
				_trigger.setBatteryEndLevel(Integer.parseInt(_parser
						.getAttributeValue(null, "end_level")));
				Log.i(TAG,
						"BatteryEndLevel: "
								+ _parser.getAttributeValue(null, "end_level"));
			} else {
				Log.i(TAG, "BatteryEndLevel: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryEndLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				_trigger.setBatteryState(Trigger.listen_state.listen_on);
				Log.i(TAG, "BatteryState listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				_trigger.setBatteryState(Trigger.listen_state.listen_off);
				Log.i(TAG, "BatteryState listen off.");
			} else {
				Log.i(TAG, "BateryState: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryState: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the headphone state according to the tags inside the given xml parser.
	 * 
	 * @param _parser
	 *            the parser of which you want to apply settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setHeadphone(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "headphone");

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				_trigger.setHeadphones(Trigger.listen_state.listen_on);
				Log.i(TAG, "Headphones listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				_trigger.setHeadphones(Trigger.listen_state.listen_off);
				Log.i(TAG, "Headphones listen off.");
			} else {
				Log.i(TAG, "Headphones: ignore.");
			}
		} else {
			Log.e(TAG, "Headphones: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the geofence of the trigger to the geofence specified in the xml.
	 * 
	 * @param _parser
	 * @param _trigger
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private void setGeofence(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "geofence");
		if (_parser.getAttributeValue(null, "id") != null) {
			if (!_parser.getAttributeValue(null, "id").equals("")) {
				_trigger.setGeofence(_parser.getAttributeValue(null, "id"));
				Log.i(TAG, "Geofence: " + _trigger.getGeofence());
			} else {
				_trigger.setGeofence(null);
				Log.i(TAG, "Geofence: ignore");
			}
		} else {
			Log.e(TAG, "Geofence: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the priority of the trigger to the priority specified in the xml.
	 * 
	 * @param _parser
	 * @param _trigger
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setPriority(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "priority");
		if (_parser.getAttributeValue(null, "value") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "value")) >= 0
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "value")) <= 99) {
				_trigger.setPriority(Integer.parseInt(_parser
						.getAttributeValue(null, "value")));
				Log.i(TAG,
						"priority: " + _parser.getAttributeValue(null, "value"));
			} else {
				Log.i(TAG, "priority: ignore.");
			}
		} else {
			Log.e(TAG, "priority: Invalid Argument!");
		}

		_parser.nextTag();
	}
	
	/**
	 * Sets the weekdays of the trigger to the weekdays specified in the xml.
	 * 
	 * @param _parser
	 * @param _trigger
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setWeekdays(XmlPullParser _parser, Trigger _trigger)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "weekdays");
		Set<String> weekdays = new HashSet<String>();

		if (_parser.getAttributeValue(null, "mon") != null) {
			if (_parser.getAttributeValue(null, "mon").equals("true")) {
				weekdays.add("1");
				Log.i(TAG, "weekdays: monday");
			} else {
				Log.i(TAG, "weekdays: no monday");
			}
		}
		if (_parser.getAttributeValue(null, "tue") != null) {
			if (_parser.getAttributeValue(null, "tue").equals("true")) {
				weekdays.add("2");
				Log.i(TAG, "weekdays: tuesday");
			} else {
				Log.i(TAG, "weekdays: no tuesday");
			}
		}
		if (_parser.getAttributeValue(null, "wed") != null) {
			if (_parser.getAttributeValue(null, "wed").equals("true")) {
				weekdays.add("3");
				Log.i(TAG, "weekdays: wednesday");
			} else {
				Log.i(TAG, "weekdays: no wednesday");
			}
		}
		if (_parser.getAttributeValue(null, "thur") != null) {
			if (_parser.getAttributeValue(null, "thur").equals("true")) {
				weekdays.add("4");
				Log.i(TAG, "weekdays: thursday");
			} else {
				Log.i(TAG, "weekdays: no thursday");
			}
		}
		if (_parser.getAttributeValue(null, "fri") != null) {
			if (_parser.getAttributeValue(null, "fri").equals("true")) {
				weekdays.add("5");
				Log.i(TAG, "weekdays: friday");
			} else {
				Log.i(TAG, "weekdays: no friday");
			}
		}
		if (_parser.getAttributeValue(null, "sat") != null) {
			if (_parser.getAttributeValue(null, "sat").equals("true")) {
				weekdays.add("6");
				Log.i(TAG, "weekdays: saturday");
			} else {
				Log.i(TAG, "weekdays: no saturday");
			}
		}
		if (_parser.getAttributeValue(null, "sun") != null) {
			if (_parser.getAttributeValue(null, "sun").equals("true")) {
				weekdays.add("7");
				Log.i(TAG, "weekdays: sunday");
			} else {
				Log.i(TAG, "weekdays: no sunday");
			}
		}
		
		_trigger.setWeekdays(weekdays);

		_parser.nextTag();
	}
}