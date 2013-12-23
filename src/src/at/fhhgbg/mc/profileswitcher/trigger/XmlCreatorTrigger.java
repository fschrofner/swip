package at.fhhgbg.mc.profileswitcher.trigger;

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
public class XmlCreatorTrigger {
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
	public String create(Trigger _trigger) throws ParserConfigurationException,
			TransformerException, IOException {
		builder = buildFactory.newDocumentBuilder();
		Document xmlProfile = builder.newDocument();

		// this is the root tag for the xml file
		Element rootElement = xmlProfile.createElement("trigger");
		xmlProfile.appendChild(rootElement);

		// specifies the final output
		Properties outputProperties = new Properties();
		outputProperties.setProperty(OutputKeys.INDENT, "yes");
		outputProperties.setProperty(OutputKeys.METHOD, "xml");
		outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outputProperties.setProperty(OutputKeys.VERSION, "1.0");
		outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");

		// writes the selected profile
		Element profile = xmlProfile.createElement("profile");
		profile.setAttribute("name",
				String.format("%s", _trigger.getProfileName()));
		rootElement.appendChild(profile);
		Log.i("XMLCreatorTrigger",
				String.format("Profile was selected: %s",
						_trigger.getProfileName()));

		// writes time changes
		if (_trigger.getMinutes() >= -1 && _trigger.getHours() >= -1) {
			Element timeElement = xmlProfile.createElement("time");

			if (_trigger.getHours() >= -1)
				timeElement.setAttribute("hours",
						String.format("%d", _trigger.getHours()));
			if (_trigger.getMinutes() >= -1)
				timeElement.setAttribute("minutes",
						String.format("%d", _trigger.getMinutes()));

			Log.i("XMLCreatorTrigger",
					String.format(
							"trigger changes were defined as follows: hours: %s minutes: %s",
							_trigger.getHours(), _trigger.getMinutes()));
			rootElement.appendChild(timeElement);
		}

		// writes battery changes
		if (_trigger.getBatteryLevel() >= -1
				|| _trigger.getBatteryState() != Trigger.listen_state.ignore) {
			Element batteryElement = xmlProfile.createElement("battery");

			if (_trigger.getBatteryState() != Trigger.listen_state.ignore) {
				batteryElement.setAttribute("state", String.format("%d",
						_trigger.getBatteryState().ordinal()));
			} else {
				batteryElement.setAttribute("state", String.format("%d", -1));
			}

			if (_trigger.getBatteryLevel() >= -1) {
				batteryElement.setAttribute("level",
						String.format("%d", _trigger.getBatteryLevel()));
			}

			Log.i("XMLCreatorTrigger",
					String.format(
							"trigger changes were defined as follows: batter_level: %s battery_state: %s",
							_trigger.getBatteryLevel(),
							_trigger.getBatteryState()));
			rootElement.appendChild(batteryElement);
		}

		// writes the complete xml file
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