package at.fhhgbg.mc.profileswitcher.trigger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.IBinder;
import android.util.Log;
import at.fhhgbg.mc.profileswitcher.XmlCreator;
import at.fhhgbg.mc.profileswitcher.XmlParser;

/**
 * Service which manages the triggers.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class TriggerService extends Service {

	private static BroadcastReceiver tickReceiver;
	private int currentHours;
	private int currentMinutes;
	private List<Trigger> triggerList = new ArrayList<Trigger>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("TriggerService", "TriggerService started");

		Trigger test = new Trigger("test");
		test.setHours(20);
		test.setMinutes(8);
		test.setProfileName("Home");

		XmlCreatorTrigger creator = new XmlCreatorTrigger();
		try {
			FileOutputStream output = openFileOutput(test.getName()
					+ "_trigger.xml", Context.MODE_PRIVATE);
			output.write(creator.create(test).getBytes());
			output.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		refreshTriggers();

		// Create a broadcast receiver to handle change in time
		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context _context, Intent _intent) {
				Log.i("TriggerService", "TimeTick");

				if (_intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
					int h = Integer.parseInt(String.valueOf(Calendar
							.getInstance().get(Calendar.HOUR_OF_DAY)));
					int m = Integer.parseInt(String.valueOf(Calendar
							.getInstance().get(Calendar.MINUTE)));
					setTime(h, m);
				}
			}
		};

		// Register the broadcast receiver to receive TIME_TICK
		registerReceiver(tickReceiver,
				new IntentFilter(Intent.ACTION_TIME_TICK));

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * sets the time.
	 * 
	 * @param _currentHours
	 *            the current number of hours.
	 * @param _currentMinutes
	 *            the current number of minutes.
	 */
	private void setTime(int _currentHours, int _currentMinutes) {
		Log.i("TriggerService", "current time updated");

		currentHours = _currentHours;
		currentMinutes = _currentMinutes;
		compareTriggers();
	}

	/**
	 * compares the triggers with the actual state.
	 */
	private void compareTriggers() {
		for (Trigger trigger : triggerList) {
			if (trigger.getHours() == currentHours
					&& trigger.getMinutes() == currentMinutes) {
				Log.i("TriggerService", "matching trigger found");

				XmlParser parser = new XmlParser(getApplicationContext());
				try {
					// applies the profile.
					parser.initializeXmlParser(openFileInput(trigger
							.getProfileName() + ".xml"));
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				Log.i("TriggerService", "profile applied");
			}
		}
	}

	/**
	 * refreshes the list of triggers.
	 */
	private void refreshTriggers() {

		triggerList.clear();

		String[] fileList = getFilesDir().list();
		XmlParserTrigger parser = new XmlParserTrigger(this);

		try {
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].contains("_trigger")) {
					Trigger trigger = new Trigger(fileList[i].substring(0,
							fileList[i].length() - 12));
					Log.i("TriggerService", "Trigger found: " + trigger.getName());
					parser.initializeXmlParser(openFileInput(fileList[i]),
							trigger);
					triggerList.add(trigger);
				} else {
					Log.i("TriggerService", "no Trigger found");
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("TriggerService", "triggerList: " + triggerList.size());
	}

}
