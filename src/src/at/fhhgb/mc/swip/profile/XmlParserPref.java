package at.fhhgb.mc.swip.profile;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import at.flosch.logwrap.Log;
import android.util.Xml;

/**
 * Class used to put the values saved in a xml file into the shared preferences
 * (to load them into the profile edit activity).
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class XmlParserPref {
	final static String TAG = "XmlParserPref";

	Context context;	 //the context is needed to be handed over to the setter
	Editor prefEdit;
	String profileName;

	/**
	 * Initializes the xml parser with the given context.
	 * @param _context
	 */
	public XmlParserPref(Context _context, String _name) {
		context = _context;
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		profileName = _name;
	}

	/**
	 * Sets up the xml parser for the given inputstream and then hands it over
	 * to the readAndApplyTags method to process the stream.
	 * @param _in the input stream you want to parse.
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
			_in.close();					//closes the inputstream in the end
		}
	}

	/**
	 * Reads the given input stream and applies the settings inside
	 * using the corresponding setter methods.
	 * @param _parser the parser which should read the tags
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void readAndApplyTags(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		
		prefEdit.putString("name", profileName);
		
		_parser.require(XmlPullParser.START_TAG, null, "resources");
		while (_parser.next() != XmlPullParser.END_TAG) {				//while the tag is not the closing tag
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;												//skips this turn if the tag is not a start tag
			}
			String name = _parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("ringer_mode")) {
				setRingerMode(_parser);
			} else if (name.equals("volume")) {	
				setVolume(_parser);
			} else if(name.equals("nfc")){
				setNfc(_parser);
			} else if (name.equals("bluetooth")) {
				setBluetooth(_parser);
			} else if (name.equals("gps")) {
				setGps(_parser);
			} else if (name.equals("mobile_data")) {
				setMobileData(_parser);
			} else if (name.equals("wifi")) {
				setWifi(_parser);
			} else if (name.equals("airplane_mode")) {
				setAirplaneMode(_parser);
			} else if (name.equals("display")) {
				setDisplay(_parser);
			} else if(name.equals("lockscreen")){
				setLockscreen(_parser);
			} else {
				Log.i("XmlParser", "Skip!");							//invalid tag, will be skipped
				_parser.nextTag();
			}
			prefEdit.commit();
		}
	}

	
	/**
	 * Sets the ringermode according to the next tags in the given xml parser.
	 * @param _parser the xml parser of which you want to apply the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setRingerMode(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "ringer_mode");			//the start-tag is ringer_mode

		if (_parser.getAttributeValue(null, "mode") != null) {					//if there is a mode attribute inside(otherwise it would not be a valid setting)
			if (_parser.getAttributeValue(null, "mode").equals("normal")) {		//if the mode is defined as normal
				prefEdit.putString("ringer_mode", "normal");
				Log.i(TAG, "RingerMode: normal");
			} else if (_parser.getAttributeValue(null, "mode").equals("silent")) {//if the mode is defined as silent
				prefEdit.putString("ringer_mode", "silent");
				Log.i(TAG, "RingerMode: silent");
			} else if (_parser.getAttributeValue(null, "mode")					//if the mode is defined as vibrate
					.equals("vibrate")) {
				prefEdit.putString("ringer_mode", "vibrate");
				Log.i(TAG, "RingerMode: vibrate");
			} else if (_parser.getAttributeValue(null, "mode")					//if the mode is defined as unchanged
					.equals("unchanged")) {
				prefEdit.putString("ringer_mode", "unchanged");
				Log.i(TAG, "RingerMode: unchanged");
			} else {															//for log messages only
				Log.e(TAG, "RingerMode: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "RingerMode: No change.");
		}
		
		_parser.nextTag();														//goes to the next tag (otherwise the readAndApplyTags method would not continue)
	}

	
	/**
	 * Sets the volume according to the tags inside the given xml parser.
	 * @param _parser the parser of which you want to apply settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setVolume(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "volume");	

		if (_parser.getAttributeValue(null, "media") != null) {						//if the media-attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "media")) >= -1	//checks if the media-volume is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "media")) <= 15) {
				prefEdit.putInt("media_volume", Integer.parseInt(_parser
						.getAttributeValue(null, "media")));
				Log.i(TAG,
						"MediaVolume: "
								+ _parser.getAttributeValue(null, "media"));
			} else {																//if the media volume is set to an invalid value
				Log.e(TAG, "MediaVolume: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "MediaVolume: No change.");							//if the media attribute is not set
		}

		if (_parser.getAttributeValue(null, "alarm") != null) {						//if the alarm attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "alarm")) >= -1		//checks if the value assigned would be a valid one
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "alarm")) <= 7) {
				prefEdit.putInt("alarm_volume", Integer.parseInt(_parser
						.getAttributeValue(null, "alarm")));
				Log.i(TAG,
						"AlarmVolume: "
								+ _parser.getAttributeValue(null, "alarm"));
			} else {																//if the alarm volume is set to an invalid value
				Log.e(TAG, "AlarmVolume: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "AlarmVolume: No change.");							//if the alarm attribute is not set
		}

		if (_parser.getAttributeValue(null, "ringtone") != null) {					//if the ringtone attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "ringtone")) >= -1
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"ringtone")) <= 7) {									//checks if the value is set to a valid one
				prefEdit.putInt("ringtone_volume", Integer.parseInt(_parser
						.getAttributeValue(null, "ringtone")));					//sets the ringtone-volume
				Log.i(TAG,
						"Ringtone-Volume: "
								+ _parser.getAttributeValue(null, "ringtone"));
			} else {
				Log.e(TAG, "RingtoneVolume: Invalid Argument!");			//if the value would be invalid
			}
		} else {
			Log.i(TAG, "RingtoneVolume: No change.");						//if the ringtone attribute was not set
		}

		_parser.nextTag();
	}

	
	/**
	 * Applies the gps state according to the attributes.
	 * @param _parser the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setGps(XmlPullParser _parser) throws XmlPullParserException,
			IOException {
		_parser.require(XmlPullParser.START_TAG, null, "gps");
		if (_parser.getAttributeValue(null, "enabled") != null) {
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {	//if gps is set enabled
				prefEdit.putString("gps", "enabled");
				Log.i(TAG, "GPS on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {	//if it is set to disabled
				prefEdit.putString("gps", "disabled");
				Log.i(TAG, "GPS off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {	//if it is set to unchanged
				prefEdit.putString("gps", "unchanged");
				Log.i(TAG, "GPS unchanged.");
			} else {
				Log.e(TAG, "GPS: Invalid Argument!");				//if it is set to an invalid value
			}
		} else {															//if the enabled attribute is not set
			Log.i(TAG, "GPS: No change.");
		}
		_parser.nextTag();
	}
	
	/** Applies the display settings.
	 * @param _parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setDisplay(XmlPullParser _parser) throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "display");	

		if (_parser.getAttributeValue(null, "brightness") != null) {				//if the brightness-attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "brightness")) == -1
					|| (Integer.parseInt(_parser.getAttributeValue(null, "brightness")) >= 1	//checks if the brightness is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "brightness")) <= 255)) {
				prefEdit.putInt("display_brightness", Integer.parseInt(_parser
						.getAttributeValue(null, "brightness")));	
				Log.i(TAG,
						"ScreenBrightness: "
								+ _parser.getAttributeValue(null, "brightness"));
			} else {																//if the brightness is set to an invalid value
				Log.e(TAG, "ScreenBrightness: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "ScreenBrightness: No change.");							//if the brightness attribute is not set
		}
		
		if (_parser.getAttributeValue(null, "auto_mode_enabled") != null) {
			if (_parser.getAttributeValue(null, "auto_mode_enabled").equals("1")) {	//if autoMode is set enabled
				prefEdit.putString("display_auto_mode", "enabled");
				Log.i(TAG, "ScreenBrightnessAutoMode on.");
			} else if (_parser.getAttributeValue(null, "auto_mode_enabled").equals("0")) {	//if it is set to disabled
				prefEdit.putString("display_auto_mode", "disabled");
				Log.i(TAG, "ScreenBrightnessAutoMode off.");
			} else if (_parser.getAttributeValue(null, "auto_mode_enabled").equals("-1")) {	//if it is set to unchanged
				prefEdit.putString("display_auto_mode", "unchanged");
				Log.i(TAG, "ScreenBrightnessAutoMode unchanged.");
			} else {
				Log.e(TAG, "ScreenBrightnessAutoMode: Invalid Argument!");				//if it is set to an invalid value
			}
		} else {															//if the enabled attribute is not set
			Log.i(TAG, "ScreenBrightnessAutoMode: No change.");
		}
		
		if (_parser.getAttributeValue(null, "time_out") != null) {				//if the timeOut-attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "time_out")) >= -1	//checks if the timeOut is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "time_out")) <= 6) {
				prefEdit.putString("display_time_out", _parser
						.getAttributeValue(null, "time_out"));	
				Log.i(TAG,
						"TimeOut: "
								+ _parser.getAttributeValue(null, "time_out"));
			} else {																//if the timeOut is set to an invalid value
				Log.e(TAG, "TimeOut: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "TimeOut: No change.");							//if the timeOut attribute is not set
		}
		
		_parser.nextTag();
	}

	
	/**
	 * Sets the lockscreen state according to the next attributes.
	 * @param _parser the xmlparser containing the next attributes
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setLockscreen(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "lockscreen");
		if (_parser.getAttributeValue(null, "enabled") != null) {				//if the right attribute is here
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {		//enables lockscreen
				prefEdit.putString("lockscreen", "enabled");
				Log.i(TAG, "Lockscreen on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {//disables lockscreen
				prefEdit.putString("lockscreen", "disabled");
				Log.i(TAG, "Lockscreen off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {//lockscreen unchanged
				prefEdit.putString("lockscreen", "unchanged");
				Log.i(TAG, "Lockscreen unchanged.");
			} else {															//invalid value for the attribute
				Log.e(TAG, "Lockscreen: Invalid Argument!");
			}
		} else {																//enabled not set
			Log.i(TAG, "Lockscreen: No change.");
		}
		_parser.nextTag();
	}
	/**
	 * Sets the bluetooth state according to the next attributes.
	 * @param _parser the parser of which you want to read the attributes.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setBluetooth(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "bluetooth");
		if (_parser.getAttributeValue(null, "enabled") != null) {				//if the right attribute is here
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {		//enabled bluetooth
				prefEdit.putString("bluetooth", "enabled");
				Log.i(TAG, "Bluetooth on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {//disables bluetooth
				prefEdit.putString("bluetooth", "disabled");
				Log.i(TAG, "Bluetooth off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {//bluetooth unchanged
				prefEdit.putString("bluetooth", "unchanged");
				Log.i(TAG, "Bluetooth unchanged.");
			} else {															//invalid value for the attribute
				Log.e(TAG, "Bluetooth: Invalid Argument!");
			}
		} else {																//enabled not set
			Log.i(TAG, "Bluetooth: No change.");
		}
		_parser.nextTag();
	}
	
	/**
	 * Sets the nfc state according to the next attributes.
	 * @param _parser the parser of which you want to read the attributes.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setNfc(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "nfc");
		if (_parser.getAttributeValue(null, "enabled") != null) {				//if the right attribute is here
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {		//enables nfc
				prefEdit.putString("nfc", "enabled");
				Log.i(TAG, "NFC on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {//disables nfc
				prefEdit.putString("nfc", "disabled");
				Log.i(TAG, "NFC off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {//nfc unchanged
				prefEdit.putString("nfc", "unchanged");
				Log.i(TAG, "NFC unchanged.");
			} else {															//invalid value for the attribute
				Log.e(TAG, "NFC: Invalid Argument!");
			}
		} else {																//enabled not set
			Log.i(TAG, "NFC: No change.");
		}
		_parser.nextTag();
	}
	
	/**
	 * Sets the mobile-data state according to the next attributes inside the given parser.
	 * @param _parser the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setMobileData(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "mobile_data");
		if (_parser.getAttributeValue(null, "enabled") != null) {
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {
				prefEdit.putString("mobile_data", "enabled");
				Log.i(TAG, "MobileData on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {
				prefEdit.putString("mobile_data", "disabled");
				Log.i(TAG, "MobileData off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {
				prefEdit.putString("mobile_data", "unchanged");
				Log.i(TAG, "MobileData unchanged.");
			} else {
				Log.e(TAG, "MobileData: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "MobileData: No change.");
		}
		_parser.nextTag();
	}
	
	/**
	 * Sets the airplane mode state according to the next attributes inside the given parser.
	 * @param _parser the parser of which you want to read the strings
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setAirplaneMode(XmlPullParser _parser) throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "airplane_mode");
		if (_parser.getAttributeValue(null, "enabled") != null) {
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {
				prefEdit.putString("airplane_mode", "enabled");
				Log.i(TAG, "Airplane Mode on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {
				prefEdit.putString("airplane_mode", "disabled");
				Log.i(TAG, "Airplane Mode off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {
				prefEdit.putString("airplane_mode", "unchanged");
				Log.i(TAG, "Airplane Mode unchanged.");
			} else {
				Log.e(TAG, "Airplane Mode: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "Airplane Mode: No change.");
		}
		_parser.nextTag();
	}

	/**
	 * Sets the wifi state according to the next attributes of the given parser.
	 * @param _parser the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setWifi(XmlPullParser _parser) throws XmlPullParserException,
			IOException {
		_parser.require(XmlPullParser.START_TAG, null, "wifi");
		if (_parser.getAttributeValue(null, "enabled") != null) {
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {			//if wifi is set to enabled
				prefEdit.putString("wifi", "enabled");
				Log.i(TAG, "WiFi on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {	//if it is set to disabled
				prefEdit.putString("wifi", "disabled");
				Log.i(TAG, "WiFi off.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("-1")) {	//if it is set to unchanged
				prefEdit.putString("wifi", "unchanged");
				Log.i(TAG, "WiFi unchanged.");
			} else {																//if there is not a valid value
				Log.e(TAG, "WiFi: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "WiFi: No change.");									//if the enabled attribute is not there
		}
		_parser.nextTag();
	}

}