package at.fhhgb.mc.swip.trigger;

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

import at.flosch.logwrap.Log;

/**
 * Class used to convert a trigger into a string containing a xml, that is
 * correspondent to the guidelines of this app.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class XmlCreatorTrigger {
	final static String TAG = "XmlCreatorTrigger";
	
	DocumentBuilderFactory buildFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	TransformerFactory transFactory = TransformerFactory.newInstance();
	Transformer transformer;

	/**
	 * Creates and returns a string containing all the options specified in the
	 * given trigger object as xml.
	 * 
	 * @param _trigger
	 *            the trigger of which you want to create the xml
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

		// writes the name of the selected profile
		Element profile = xmlProfile.createElement("profile");
		profile.setAttribute("name",
				String.format("%s", _trigger.getProfileName()));
		rootElement.appendChild(profile);
		Log.i(TAG,
				String.format("Profile was selected: %s",
						_trigger.getProfileName()));

		// writes the priority
		Element priority = xmlProfile.createElement("priority");
		priority.setAttribute("value", String.valueOf(_trigger.getPriority()));
		rootElement.appendChild(priority);
		Log.i(TAG,
				String.format("Priority was selected: %s",
						_trigger.getPriority()));

		// writes time changes
		if (_trigger.getStartMinutes() >= -1 && _trigger.getStartHours() >= -1) {
			Element timeElement = xmlProfile.createElement("time");

			if (_trigger.getStartHours() >= -1)
				timeElement.setAttribute("start_hours",
						String.format("%d", _trigger.getStartHours()));
			if (_trigger.getStartMinutes() >= -1)
				timeElement.setAttribute("start_minutes",
						String.format("%d", _trigger.getStartMinutes()));
			if (_trigger.getEndHours() >= -1)
				timeElement.setAttribute("end_hours",
						String.format("%d", _trigger.getEndHours()));
			if (_trigger.getEndMinutes() >= -1)
				timeElement.setAttribute("end_minutes",
						String.format("%d", _trigger.getEndMinutes()));

			Log.i(TAG,
					String.format(
							"trigger changes were defined as follows: start_hours: %s start_minutes: %s end_minutes: %s end_hours: %s",
							_trigger.getStartHours(),
							_trigger.getStartMinutes(), _trigger.getEndHours(),
							_trigger.getEndMinutes()));
			rootElement.appendChild(timeElement);
		}

		// writes battery changes
		if (_trigger.getBatteryStartLevel() >= -1
				&& _trigger.getBatteryEndLevel() >= -1
				&& _trigger.getBatteryState() != null) {
			Element batteryElement = xmlProfile.createElement("battery");

			if (_trigger.getBatteryState() != Trigger.listen_state.ignore) {
				batteryElement.setAttribute("state", String.format("%d",
						_trigger.getBatteryState().ordinal()));
			} else {
				batteryElement.setAttribute("state", String.format("%d", -1));
			}

			if (_trigger.getBatteryStartLevel() >= -1) {
				batteryElement.setAttribute("start_level",
						String.format("%d", _trigger.getBatteryStartLevel()));
			}

			if (_trigger.getBatteryEndLevel() >= -1) {
				batteryElement.setAttribute("end_level",
						String.format("%d", _trigger.getBatteryEndLevel()));
			}

			Log.i(TAG,
					String.format(
							"trigger changes were defined as follows: batter_start_level: %s, batter_end_level: %s, battery_state: %s",
							_trigger.getBatteryStartLevel(),
							_trigger.getBatteryEndLevel(),
							_trigger.getBatteryState()));
			rootElement.appendChild(batteryElement);
		}

		// writes headphone changes
		if (_trigger.getHeadphones() != null) {
			Element headphoneElement = xmlProfile.createElement("headphone");

			if (_trigger.getHeadphones() != Trigger.listen_state.ignore) {
				headphoneElement
						.setAttribute("state", String.format("%d", _trigger
								.getHeadphones().ordinal()));
			} else {
				headphoneElement.setAttribute("state", String.format("%d", -1));
			}

			Log.i(TAG,
					String.format(
							"trigger changes were defined as follows: headphone_state: %s",
							_trigger.getHeadphones()));
			rootElement.appendChild(headphoneElement);
		}

		// writes geofence changes
		Element geofenceElement = xmlProfile.createElement("geofence");

		if (_trigger.getGeofence() != null) {
			geofenceElement.setAttribute("id", _trigger.getGeofence());
		} else {
			geofenceElement.setAttribute("id", "");
		}

		Log.i(TAG, String.format(
				"trigger changes were defined as follows: trigger_name: %s",
				_trigger.getGeofence()));
		rootElement.appendChild(geofenceElement);

		// writes weekday changes
		if (_trigger.getWeekdays() != null) {
			Element weekdayElement = xmlProfile.createElement("weekdays");

			if (_trigger.getWeekdays().contains("1")) {
				weekdayElement
						.setAttribute("mon", "true");
			} else {
				weekdayElement.setAttribute("mon", "false");
			}
			if (_trigger.getWeekdays().contains("2")) {
				weekdayElement
						.setAttribute("tue", "true");
			} else {
				weekdayElement.setAttribute("tue", "false");
			}
			if (_trigger.getWeekdays().contains("3")) {
				weekdayElement
						.setAttribute("wed", "true");
			} else {
				weekdayElement.setAttribute("wed", "false");
			}
			if (_trigger.getWeekdays().contains("4")) {
				weekdayElement
						.setAttribute("thur", "true");
			} else {
				weekdayElement.setAttribute("thur", "false");
			}
			if (_trigger.getWeekdays().contains("5")) {
				weekdayElement
						.setAttribute("fri", "true");
			} else {
				weekdayElement.setAttribute("fri", "false");
			}
			if (_trigger.getWeekdays().contains("6")) {
				weekdayElement
						.setAttribute("sat", "true");
			} else {
				weekdayElement.setAttribute("sat", "false");
			}
			if (_trigger.getWeekdays().contains("7")) {
				weekdayElement
						.setAttribute("sun", "true");
			} else {
				weekdayElement.setAttribute("sun", "false");
			}

			Log.i(TAG,
					String.format(
							"trigger changes were defined as follows: weekday number: %s",
							_trigger.getWeekdays().size()));
			rootElement.appendChild(weekdayElement);
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