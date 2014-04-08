package at.fhhgb.mc.swip.profile;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * Class that is used to read an xml input stream and apply the settings in it using the Setter class.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class XmlParser {

	Context context;							//the context is needed to be handed over to the setter
	Setter setter = new Setter();				//a setter is needed to apply the settings
	
	/**
	 * Initializes the xml parser with the given context.
	 * @param _context
	 */
	public XmlParser(Context _context) {
		context = _context;
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
			} else if (name.equals("airplane_mode")){
				setAirplaneMode(_parser);
			} else if (name.equals("nfc")){
				setNfc(_parser);
			} else if (name.equals("bluetooth")) {
				setBluetooth(_parser);
			} else if (name.equals("gps")) {
				setGps(_parser);
			} else if (name.equals("mobile_data")) {
				setMobileData(_parser);
			} else if (name.equals("wifi")) {
				setWifi(_parser);
			} else if (name.equals("display")) {
				setDisplay(_parser);
			} else if (name.equals("lockscreen")) {
				setLockscreen(_parser);
			} else {
				Log.w("XmlParser", "Skip!");							//invalid tag, will be skipped
				_parser.nextTag();
			}
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
				setter.setRingerMode(context, Profile.mode.normal);
				Log.i("XmlParser", "RingerMode: normal");
			} else if (_parser.getAttributeValue(null, "mode").equals("silent")) {//if the mode is defined as silent
				setter.setRingerMode(context, Profile.mode.silent);
				Log.i("XmlParser", "RingerMode: silent");
			} else if (_parser.getAttributeValue(null, "mode")					//if the mode is defined as vibrate
					.equals("vibrate")) {
				setter.setRingerMode(context, Profile.mode.vibrate);
				Log.i("XmlParser", "RingerMode: vibrate");
			} else {															//for log messages only
				Log.i("XmlParser", "RingerMode: No change.");
			}
		} else {
			Log.e("XmlParser", "RingerMode: Invalid Argument!");
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
			if (Integer.parseInt(_parser.getAttributeValue(null, "media")) >= 0		//checks if the media-volume is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "media")) <= 15) {
				setter.setMediaVolume(context, Integer.parseInt(_parser				//sets the media-volume to the given value
						.getAttributeValue(null, "media")));
				Log.i("XmlParser",
						"MediaVolume: "
								+ _parser.getAttributeValue(null, "media"));
			} else {																//if the media volume is set to an invalid value
				Log.i("XmlParser", "MediaVolume: No change.");
			}
		} else {
			Log.e("XmlParser", "MediaVolume: Invalid Argument!");					//if the media attribute is not set
		}

		if (_parser.getAttributeValue(null, "alarm") != null) {						//if the alarm attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "alarm")) >= 0		//checks if the value assigned would be a valid one
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "alarm")) <= 7) {
				setter.setAlarmVolume(context, Integer.parseInt(_parser				//sets the alarm-volume to the given value
						.getAttributeValue(null, "alarm")));
				Log.i("XmlParser",
						"AlarmVolume: "
								+ _parser.getAttributeValue(null, "alarm"));
			} else {																//if the alarm volume is set to an invalid value
				Log.i("XmlParser", "AlarmVolume: No change.");
			}
		} else {
			Log.e("XmlParser", "AlarmVolume: Invalid Argument!");					//if the alarm attribute is not set
		}

		if (_parser.getAttributeValue(null, "ringtone") != null) {					//if the ringtone attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "ringtone")) >= 0
					&& Integer.parseInt(_parser.getAttributeValue(null,
							"ringtone")) <= 7) {									//checks if the value is set to a valid one
				setter.setRingtoneVolume(context, Integer.parseInt(_parser
						.getAttributeValue(null, "ringtone")));						//sets the ringtone-volume
				Log.i("XmlParser",
						"Ringtone-Volume: "
								+ _parser.getAttributeValue(null, "ringtone"));
			} else {
				Log.i("XmlParser", "RingtoneVolume: No change.");					//if the value would be invalid
			}
		} else {
			Log.e("XmlParser", "RingtoneVolume: Invalid Argument!");				//if the ringtone attribute was not set
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
				setter.setGps(context, true);
				Log.i("XmlParser", "GPS on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {	//if it is set to disabled
				setter.setGps(context, false);
				Log.i("XmlParser", "GPS off.");
			} else {
				Log.i("XmlParser", "GPS: No change.");						//if it is set to an invalid value
			}
		} else {															//if the enabled attribute is not set
			Log.e("XmlParser", "GPS: Invalid Argument!");
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
			if (Integer.parseInt(_parser.getAttributeValue(null, "brightness")) >= 1	//checks if the brightness is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "brightness")) <= 255) {
				setter.setScreenBrightness(context, Integer.parseInt(_parser				//sets the brightness to the given value
						.getAttributeValue(null, "brightness")));
				Log.i("XmlParser",
						"ScreenBrightness: "
								+ _parser.getAttributeValue(null, "brightness"));
			} else {																	//if the brightness is set to an invalid value
				Log.i("XmlParser", "ScreenBrightness: No change.");	
			}
		} else {
			Log.e("XmlParser", "ScreenBrightness: Invalid Argument!");					//if the brightness attribute is not set
		}
		
		if (_parser.getAttributeValue(null, "auto_mode_enabled") != null) {
			if (_parser.getAttributeValue(null, "auto_mode_enabled").equals("1")) {	//if autoMode is set enabled
				setter.setScreenBrightnessMode(context, true);
				Log.i("XmlParser", "ScreenBrightnessAutoMode on.");
			} else if (_parser.getAttributeValue(null, "auto_mode_enabled").equals("0")) {	//if it is set to disabled
				setter.setScreenBrightnessMode(context, false);
				Log.i("XmlParser", "ScreenBrightnessAutoMode off.");
			} else {
				Log.i("XmlParser", "ScreenBrightnessAutoMode: No change.");				
			}
		} else {																			//if the enabled attribute is not set
			Log.e("XmlParser", "ScreenBrightnessAutoMode: Invalid Argument!");				
		}
		
		if (_parser.getAttributeValue(null, "time_out") != null) {				//if the timeOut-attribute is set
			if (Integer.parseInt(_parser.getAttributeValue(null, "time_out")) >= 0	//checks if the timeOut is set to a valid value
					&& Integer.parseInt(_parser
							.getAttributeValue(null, "time_out")) <= 6) {
				setter.setScreenTimeout(context, Integer.parseInt(_parser				//sets the timeOut to the given value
						.getAttributeValue(null, "time_out")));
				Log.i("XmlParser",
						"TimeOut: "
								+ _parser.getAttributeValue(null, "time_out"));
			} else {																//if the timeOut is set to an invalid value
				Log.i("XmlParser", "TimeOut: No change.");
			}
		} else {
			Log.e("XmlParser", "TimeOut: Invalid Argument!");						//if the timeOut attribute is not set
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
				setter.setBluetooth(context, true);
				Log.i("XmlParser", "Bluetooth on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {//disables bluetooth
				setter.setBluetooth(context, false);
				Log.i("XmlParser", "Bluetooth off.");
			} else {															//invalid value for the attribute
				Log.i("XmlParser", "Bluetooth: No change.");
			}
		} else {																//enabled not set
			Log.e("XmlParser", "Bluetooth: Invalid Argument!");
		}
		_parser.nextTag();
	}

	/**
	 * Applies the nfc state according to the next attributes inside the given parser.
	 * @param _parser the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setNfc(XmlPullParser _parser)
			throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG, null, "nfc");
		if (_parser.getAttributeValue(null, "enabled") != null) {				//if the right attribute is here
			if (_parser.getAttributeValue(null, "enabled").equals("1")) {		//enables nfc
				setter.setNfc(context, true);
				Log.i("XmlParser", "NFC on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {//disables nfc
				setter.setNfc(context, false);
				Log.i("XmlParser", "NFC off.");
			} else {															//invalid value for the attribute
				Log.i("XmlParser", "NFC: No change.");
			}
		} else {																//enabled not set
			Log.e("XmlParser", "NFC: Invalid Argument!");
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
					setter.setMobileData(context, true);
				Log.i("XmlParser", "MobileData on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {
					setter.setMobileData(context, false);
				Log.i("XmlParser", "MobileData off.");
			} else {
				Log.i("XmlParser", "MobileData: No change.");
				
			}
		} else {
			Log.e("XmlParser", "MobileData: Invalid Argument!");
		}
		_parser.nextTag();
	}
	
	/**
	 * Sets the airplane mode according to the next attributes inside the given parser.
	 * @param _parser the parser of which you want to read the settings.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setAirplaneMode(XmlPullParser _parser) throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG,  null, "airplane_mode");
		if(_parser.getAttributeValue(null, "enabled") != null){
			if(_parser.getAttributeValue(null, "enabled").equals("1")){
				setter.setAirplaneMode(context, true);
				Log.i("XmlParser", "Airplane Mode on.");
			} else if(_parser.getAttributeValue(null, "enabled").equals("0")){
				setter.setAirplaneMode(context,false);
				Log.i("XmlParser","Airplane Mode off");
			} else {
				Log.i("XmlParser", "Airplane Mode: No change.");
			}
		} else {
			Log.e("XmlParser","Airplane Mode: Invalid Argument!");
		}
		_parser.nextTag();
	}

	
	/**
	 * Sets the lockscreen according to the next attributes inside the given parser.
	 * @param _parser the parser of which you want to apply the settings
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void setLockscreen(XmlPullParser _parser) throws XmlPullParserException, IOException {
		_parser.require(XmlPullParser.START_TAG,  null, "lockscreen");
		if(_parser.getAttributeValue(null, "enabled") != null){
			if(_parser.getAttributeValue(null, "enabled").equals("1")){
				setter.setLockscreen(context, true);
				Log.i("XmlParser", "Lockscreen on.");
			} else if(_parser.getAttributeValue(null, "enabled").equals("0")){
				setter.setLockscreen(context,false);
				Log.i("XmlParser","Lockscreen off");
			} else {
				Log.i("XmlParser", "Lockscreen: No change.");
			}
		} else {
			Log.e("XmlParser","Lockscreen: Invalid Argument!");
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
				setter.setWifi(context, true);
				Log.i("XmlParser", "WiFi on.");
			} else if (_parser.getAttributeValue(null, "enabled").equals("0")) {	//if it is set to disabled
				setter.setWifi(context, false);
				Log.i("XmlParser", "WiFi off.");
			} else {																//if there is not a valid value
				Log.i("XmlParser", "WiFi: No change.");
			}
		} else {
			Log.e("XmlParser", "WiFi: Invalid Argument!");							//if the enabled attribute is not there
		}
		_parser.nextTag();
	}
}