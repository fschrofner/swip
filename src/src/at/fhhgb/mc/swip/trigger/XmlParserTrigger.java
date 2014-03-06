package at.fhhgb.mc.swip.trigger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * Class that is used to read an xml input stream and load the triggers into the
 * triggerservice to be compared.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class XmlParserTrigger {

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
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_hours")) <= 23) {
				_trigger.setStartHours(Integer.parseInt(_parser
						.getAttributeValue(null, "start_hours")));
				Log.i("XmlParserTrigger",
						"start_hours: "
								+ _parser
										.getAttributeValue(null, "start_hours"));
			} else {
				Log.i("XmlParserTrigger", "start_hours: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "start_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "start_minutes") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null,
					"start_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_minutes")) <= 59) {
				_trigger.setStartMinutes(Integer.parseInt(_parser
						.getAttributeValue(null, "start_minutes")));
				Log.i("XmlParserTrigger",
						"start_minutes: "
								+ _parser.getAttributeValue(null,
										"start_minutes"));
			} else {
				Log.i("XmlParserTrigger", "start_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "start_minutes: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_hours") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_hours")) <= 23) {
				_trigger.setEndHours(Integer.parseInt(_parser
						.getAttributeValue(null, "end_hours")));
				Log.i("XmlParserTrigger",
						"end_hours: "
								+ _parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i("XmlParserTrigger", "end_hours: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "end_hours: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_minutes") != null) {
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_minutes")) <= 59) {
				_trigger.setEndMinutes(Integer.parseInt(_parser
						.getAttributeValue(null, "end_minutes")));
				Log.i("XmlParserTrigger",
						"end_minutes: "
								+ _parser
										.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i("XmlParserTrigger", "end_minutes: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "end_minutes: Invalid Argument!");
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
				Log.i("XmlParserTrigger",
						"BatteryStartLevel: "
								+ _parser
										.getAttributeValue(null, "start_level"));
			} else {
				Log.i("XmlParserTrigger", "BatteryStartLevel: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "BatteryStartLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_level") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_level")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_level")) <= 100) {
				_trigger.setBatteryEndLevel(Integer.parseInt(_parser
						.getAttributeValue(null, "end_level")));
				Log.i("XmlParserTrigger",
						"BatteryEndLevel: "
								+ _parser.getAttributeValue(null, "end_level"));
			} else {
				Log.i("XmlParserTrigger", "BatteryEndLevel: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "BatteryEndLevel: Invalid Argument!");
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
				Log.i("XmlParserTrigger", "Geofence: " + _trigger.getGeofence());
			} else {
				_trigger.setGeofence(null);
				Log.i("XmlParserTrigger", "Geofence: ignore");
			}
		} else {
			Log.e("XmlParserTrigger", "Geofence: Invalid Argument!");
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
				Log.i("XmlParserTrigger",
						"priority: " + _parser.getAttributeValue(null, "value"));
			} else {
				Log.i("XmlParserTrigger", "priority: ignore.");
			}
		} else {
			Log.e("XmlParserTrigger", "priority: Invalid Argument!");
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

		if (_parser.getAttributeValue(null, "monday") != null) {
			if (_parser.getAttributeValue(null, "monday").equals("true")) {
				weekdays.add("monday");
				Log.i("XmlParserTriggerPref", "weekdays: monday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no monday");
			}
		}
		if (_parser.getAttributeValue(null, "tuesday") != null) {
			if (_parser.getAttributeValue(null, "tuesday").equals("true")) {
				weekdays.add("tuesday");
				Log.i("XmlParserTriggerPref", "weekdays: tuesday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no tuesday");
			}
		}
		if (_parser.getAttributeValue(null, "wednesday") != null) {
			if (_parser.getAttributeValue(null, "wednesday").equals("true")) {
				weekdays.add("wednesday");
				Log.i("XmlParserTriggerPref", "weekdays: wednesday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no wednesday");
			}
		}
		if (_parser.getAttributeValue(null, "thursday") != null) {
			if (_parser.getAttributeValue(null, "thursday").equals("true")) {
				weekdays.add("thursday");
				Log.i("XmlParserTriggerPref", "weekdays: thursday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no thursday");
			}
		}
		if (_parser.getAttributeValue(null, "friday") != null) {
			if (_parser.getAttributeValue(null, "friday").equals("true")) {
				weekdays.add("friday");
				Log.i("XmlParserTriggerPref", "weekdays: friday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no friday");
			}
		}
		if (_parser.getAttributeValue(null, "saturday") != null) {
			if (_parser.getAttributeValue(null, "saturday").equals("true")) {
				weekdays.add("saturday");
				Log.i("XmlParserTriggerPref", "weekdays: saturday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no saturday");
			}
		}
		if (_parser.getAttributeValue(null, "sunday") != null) {
			if (_parser.getAttributeValue(null, "sunday").equals("true")) {
				weekdays.add("sunday");
				Log.i("XmlParserTriggerPref", "weekdays: sunday");
			} else {
				Log.i("XmlParserTriggerPref", "weekdays: no sunday");
			}
		}
		
		_trigger.setWeekdays(weekdays);

		_parser.nextTag();
	}
}