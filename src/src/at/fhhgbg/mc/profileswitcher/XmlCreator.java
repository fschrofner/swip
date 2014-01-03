package at.fhhgbg.mc.profileswitcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;

/**
 * Class used to convert a profile into a string containing a xml, that is
 * correspondent to the guidelines of this app.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class XmlCreator {
	DocumentBuilderFactory buildFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	TransformerFactory transFactory = TransformerFactory.newInstance();
	Transformer transformer;

	/**
	 * Creates and returns a string containing all the options specified in the
	 * given profile object as xml.
	 * 
	 * @param _profile
	 *            the profile of which you want to create the xml
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 */
	public String create(Profile _profile) throws ParserConfigurationException,
			TransformerException, IOException {
		builder = buildFactory.newDocumentBuilder();
		Document xmlProfile = builder.newDocument();

		// this is the root tag for the xml file
		Element rootElement = xmlProfile.createElement("resources");
		xmlProfile.appendChild(rootElement);

		// specifies the final output
		Properties outputProperties = new Properties();
		outputProperties.setProperty(OutputKeys.INDENT, "yes");
		outputProperties.setProperty(OutputKeys.METHOD, "xml");
		outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outputProperties.setProperty(OutputKeys.VERSION, "1.0");
		outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");

		// writes the ringer mode change
		Element ringerModeElement = xmlProfile.createElement("ringer_mode");
		ringerModeElement.setAttribute("mode",
				String.format("%s", _profile.getRingerMode()));
		rootElement.appendChild(ringerModeElement);
		Log.i("XMLCreator",
				String.format("vibration was defined as %s",
						_profile.getRingerMode()));

		// writes volume changes
		if (_profile.getAlarmVolume() >= -1 || _profile.getMediaVolume() >= -1
				|| _profile.getRingtoneVolume() >= -1) {
			Element volumeElement = xmlProfile.createElement("volume");
			
			if (_profile.getAlarmVolume() >= -1)
				volumeElement.setAttribute("alarm",
						String.format("%d", _profile.getAlarmVolume()));
			
			if (_profile.getMediaVolume() >= -1)
				volumeElement.setAttribute("media",
						String.format("%d", _profile.getMediaVolume()));
			
			if (_profile.getRingtoneVolume() >= -1)
				volumeElement.setAttribute("ringtone",
						String.format("%d", _profile.getRingtoneVolume()));
			
			Log.i("XMLCreator",
					String.format(
							"volume changes were defined as follows: alarm: %s media: %s ringtone: %s",
							_profile.getAlarmVolume(),
							_profile.getMediaVolume(),
							_profile.getRingtoneVolume()));
			rootElement.appendChild(volumeElement);
		}

		//writes nfc change
		Element nfcElement = xmlProfile.createElement("nfc");
		
		if (_profile.getNfc() != Profile.state.unchanged) {
			nfcElement.setAttribute("enabled",
					String.format("%s", _profile.getNfc().ordinal()));
		} else {
			nfcElement.setAttribute("enabled", String.format("%s", -1));
		}
		Log.i("XMLCreator",
				String.format("nfc was defined as %s",
						_profile.getNfc()));
		rootElement.appendChild(nfcElement);
		
		// writes bluetooth change
		Element bluetoothElement = xmlProfile.createElement("bluetooth");

		if (_profile.getBluetooth() != Profile.state.unchanged) {
			bluetoothElement.setAttribute("enabled",
					String.format("%s", _profile.getBluetooth().ordinal()));
		} else {
			bluetoothElement.setAttribute("enabled", String.format("%s", -1));
		}
		Log.i("XMLCreator",
				String.format("bluetooth was defined as %s",
						_profile.getBluetooth()));
		rootElement.appendChild(bluetoothElement);

		// writes wifi change
		Element wifiElement = xmlProfile.createElement("wifi");

		if (_profile.getWifi() != Profile.state.unchanged) {
			wifiElement.setAttribute("enabled",
					String.format("%s", _profile.getWifi().ordinal()));
		} else {
			wifiElement.setAttribute("enabled", String.format("%s", -1));
		}
		rootElement.appendChild(wifiElement);
		Log.i("XMLCreator",
				String.format("wifi was defined as %s", _profile.getWifi()));

		// writes mobile data change
		Element dataElement = xmlProfile.createElement("mobile_data");

		if (_profile.getMobileData() != Profile.state.unchanged) {
			dataElement.setAttribute("enabled",
					String.format("%s", _profile.getMobileData().ordinal()));
		} else {
			dataElement.setAttribute("enabled", String.format("%s", -1));
		}
		rootElement.appendChild(dataElement);
		Log.i("XMLCreator",
				String.format("mobile-data was defined as %s",
						_profile.getMobileData()));

		// writes gps change
		Element gpsElement = xmlProfile.createElement("gps");

		if (_profile.getGps() != Profile.state.unchanged) {
			gpsElement.setAttribute("enabled",
					String.format("%s", _profile.getGps().ordinal()));
		} else {
			gpsElement.setAttribute("enabled", String.format("%s", -1));
		}
		rootElement.appendChild(gpsElement);
		Log.i("XMLCreator",
				String.format("gps was defined as %s", _profile.getGps()));
		
		// writes airplane mode change
		Element airplaneElement = xmlProfile.createElement("airplane_mode");
		
		if(_profile.getAirplane_mode() != Profile.state.unchanged){
			airplaneElement.setAttribute("enabled", String.format("%s", _profile.getAirplane_mode().ordinal()));
		} else {
			airplaneElement.setAttribute("enabled", String.format("%s", -1));
		}
		rootElement.appendChild(airplaneElement);
		Log.i("XMLCreator", String.format("airplane mode was defined as %s", _profile.getAirplane_mode()));

		// writes display changes
		if (_profile.getScreenBrightness() >= -1
				&& _profile.getScreenBrightnessAutoMode() != null
				&& _profile.getScreenTimeOut() >= -1) {
			Element displayElement = xmlProfile.createElement("display");
			
			if (_profile.getScreenBrightness() >= -1)
				displayElement.setAttribute("brightness",
						String.format("%d", _profile.getScreenBrightness()));
			
			if (_profile.getScreenBrightnessAutoMode() != Profile.state.unchanged)
				displayElement
						.setAttribute("auto_mode_enabled", String.format("%d",
								_profile.getScreenBrightnessAutoMode()
										.ordinal()));
			
			else
				displayElement.setAttribute("auto_mode_enabled",
						String.format("%d", -1));
			
			if (_profile.getScreenTimeOut() >= -1)
				displayElement.setAttribute("time_out",
						String.format("%d", _profile.getScreenTimeOut()));
			
			Log.i("XMLCreator",
					String.format(
							"display changes were defined as follows: brightness: %s autoMode: %s timeOut: %s",
							_profile.getScreenBrightness(),
							_profile.getScreenBrightnessAutoMode(),
							_profile.getScreenTimeOut()));
			rootElement.appendChild(displayElement);
		}
		
		// writes lockscreen change
		Element lockscreenElement = xmlProfile.createElement("lockscreen");
		
		if(_profile.getLockscreen() != Profile.state.unchanged){
			lockscreenElement.setAttribute("enabled", String.format("%s", _profile.getLockscreen().ordinal()));
		} else {
			lockscreenElement.setAttribute("enabled", String.format("%s", -1));
		}
		rootElement.appendChild(lockscreenElement);
		Log.i("XMLCreator", String.format("lockscreen was defined as %s", _profile.getLockscreen()));

		
		//writes the complete xml file
		transformer = transFactory.newTransformer();
		transformer.setOutputProperties(outputProperties);
		DOMSource domSource = new DOMSource(xmlProfile.getDocumentElement());

		OutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(domSource, result);
		String xmlString = output.toString();

		return xmlString;
	}
}