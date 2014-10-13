package at.fhhgb.mc.swip.trigger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import at.flosch.logwrap.Log;
import android.util.Xml;
import at.fhhgb.mc.swip.R;

/**
 * Class used to put the values saved in a xml file into the shared preferences
 * (to load them into the trigger edit activity).
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class XmlParserPrefTrigger {

	final static String TAG = "XmlParserPrefTrigger";
	
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
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
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
			_in.close(); // closes the inputstream in the end
		}
	}

	/**
	 * Reads the given input stream and saves the read values.
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

		while (_parser.next() != XmlPullParser.END_TAG) {
			// while the tag is not the closing tag

			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue; // skips this turn if the tag is not a start tag
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
			} else if (name.equals("geofence")) {
				setGeofence(_parser);
			} else if (name.equals("priority")) {
				setPriority(_parser);
			} else if (name.equals("weekdays")) {
				setWeekdays(_parser);
			} else {
				Log.w("XmlParser", "Skip!"); // invalid tag, will be skipped
				_parser.nextTag();
			}
			prefEdit.commit();
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
			prefEdit.putString("profile",
					_parser.getAttributeValue(null, "name"));
			Log.i(TAG,
					"Profile: " + _parser.getAttributeValue(null, "profile"));
		} else {
			Log.e(TAG, "Profile: Invalid Argument!");
		}
		_parser.nextTag();
	}

	/**
	 * Sets the time according to the tags inside the given xml parser.
	 * 
	 * @param _parser
	 *            the parser of which you want to set settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setTime(XmlPullParser _parser) throws XmlPullParserException,
			IOException {
		_parser.require(XmlPullParser.START_TAG, null, "time");

		int startHours = -1;
		int startMinutes = -1;
		int endHours = -1;
		int endMinutes = -1;

		if (_parser.getAttributeValue(null, "start_hours") != null) {
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_hours")) <= 23) {
				startHours = Integer.parseInt(_parser.getAttributeValue(null,
						"start_hours"));
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
				startMinutes = Integer.parseInt(_parser.getAttributeValue(null,
						"start_minutes"));
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
				endHours = Integer.parseInt(_parser.getAttributeValue(null,
						"end_hours"));
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
				endMinutes = Integer.parseInt(_parser.getAttributeValue(null,
						"end_minutes"));
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

		String startTime;
		String endTime;

		if (startHours != -1 && startMinutes != -1) {
			startTime = new String(String.format("%02d", startHours) + ":"
					+ String.format("%02d", startMinutes));
		} else {
			startTime = new String(context.getString(R.string.ignored));
		}
		if (endHours != -1 && endMinutes != -1) {
			endTime = new String(String.format("%02d", endHours) + ":"
					+ String.format("%02d", endMinutes));
		} else {
			endTime = new String(context.getString(R.string.ignored));
		}

		prefEdit.putString("start_time", startTime);
		prefEdit.putString("end_time", endTime);

		_parser.nextTag();
	}

	/**
	 * Sets the according battery settings.
	 * 
	 * @param _parser
	 *            the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setBattery(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "battery");

		if (_parser.getAttributeValue(null, "start_level") != null) {
			if (Integer
					.parseInt(_parser.getAttributeValue(null, "start_level")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"start_level")) <= 100) {
				prefEdit.putInt("battery_start_level", Integer.parseInt(_parser
						.getAttributeValue(null, "start_level")));
				Log.i(TAG,
						"BatteryStartLevel: "
								+ _parser
										.getAttributeValue(null, "start_level"));
			} else {
				prefEdit.putInt("battery_start_level", -1);
				Log.i(TAG, "BatteryStartLevel: ignore.");
			}
		} else {
			Log.e(TAG,
					"BatteryStartLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "end_level") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "end_level")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"end_level")) <= 100) {
				prefEdit.putInt("battery_end_level", Integer.parseInt(_parser
						.getAttributeValue(null, "end_level")));
				Log.i(TAG,
						"BatteryEndLevel: "
								+ _parser.getAttributeValue(null, "end_level"));
			} else {
				prefEdit.putInt("battery_end_level", -1);
				Log.i(TAG, "BatteryEndLevel: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryEndLevel: Invalid Argument!");
		}

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("battery_state", "charging");
				Log.i(TAG, "BatteryState listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("battery_state", "discharging");
				Log.i(TAG, "BatteryState listen off.");
			} else if (_parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("battery_state", "ignored");
				Log.i(TAG, "BatteryState ignored.");
			} else {
				Log.i(TAG, "BateryState: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryState: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the headphone settings.
	 * 
	 * @param _parser
	 *            the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setHeadphone(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "headphone");

		if (_parser.getAttributeValue(null, "state") != null) {
			if (_parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("headphone", "plugged_in");
				Log.i(TAG, "Headphones listen on.");
			} else if (_parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("headphone", "unplugged");
				Log.i(TAG, "Headphones listen off.");
			} else if (_parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("headphone", "ignored");
				Log.i(TAG, "Headphones ignored.");
			} else {
				Log.i(TAG, "Headphones: ignore.");
			}
		} else {
			Log.e(TAG, "Headphones: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the location settings.
	 * 
	 * @param _parser
	 *            the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setGeofence(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "geofence");

		if (_parser.getAttributeValue(null, "id") != null) {
			if (!_parser.getAttributeValue(null, "id").equals("")) {
				SimpleGeofenceStore store = new SimpleGeofenceStore(context);
				SimpleGeofence simple = store.getGeofence(_parser
						.getAttributeValue(null, "id"));
				prefEdit.putFloat("geofence_lat", (float) simple.getLatitude());
				prefEdit.putFloat("geofence_lng", (float) simple.getLongitude());
				prefEdit.putInt("geofence_radius", (int) simple.getRadius());
				Log.i(TAG, "Geofence loaded");
			} else {
				prefEdit.putFloat("geofence_lat", -1F);
				prefEdit.putFloat("geofence_lng", -1F);
				prefEdit.putInt("geofence_radius", -1);
				Log.i(TAG, "Geofence: ignore");
			}
		} else {
			Log.e(TAG, "Geofence: Invalid Argument!");
		}

		_parser.nextTag();
	}

	/**
	 * Sets the priority settings.
	 * 
	 * @param _parser
	 *            the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setPriority(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "priority");
		if (_parser.getAttributeValue(null, "value") != null) {
			if (Integer.parseInt(_parser.getAttributeValue(null, "value")) >= 0
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "value")) <= 99) {
				prefEdit.putString("priority",
						_parser.getAttributeValue(null, "value"));
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
	 * Sets the weekdays settings.
	 * 
	 * @param _parser
	 *            the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setWeekdays(XmlPullParser _parser)
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
		
		prefEdit.putStringSet("weekdays", weekdays);

		_parser.nextTag();
	}
}
