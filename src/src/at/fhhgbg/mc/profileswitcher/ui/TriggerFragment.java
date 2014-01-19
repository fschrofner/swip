package at.fhhgbg.mc.profileswitcher.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.R.id;
import at.fhhgbg.mc.profileswitcher.R.layout;
import at.fhhgbg.mc.profileswitcher.R.menu;

public class TriggerFragment extends Fragment implements
		OnItemLongClickListener {

	@Override
	public void onStart() {
		refreshListView();
		super.onStart();
	}

	List<String> triggerList = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View convertView = inflater.inflate(R.layout.activity_trigger_fragment,
				null);
		setHasOptionsMenu(true);
		return convertView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(getActivity(),
				at.fhhgbg.mc.profileswitcher.trigger.TriggerService.class);
		getActivity().startService(intent);
	}

	/**
	 * Inflates the menu.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflates the menu containing the add triggers button and the settings
		menuInflater.inflate(R.menu.main_menu_trigger, menu);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.new_trigger) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			Editor prefEditor = preferences.edit();

			// loads the default values for a new trigger
			prefEditor.putString("name_trigger", "Insert name");
			prefEditor.putString("profile", "Choose a profile");
			prefEditor.putString("start_time", "Ignored");
			prefEditor.putString("end_time", "Ignored");
			prefEditor.putString("battery_state", "ignored");
			prefEditor.putInt("battery_start_level", -1);
			prefEditor.putInt("battery_end_level", -1);
			prefEditor.putString("headphone", "ignored");
			prefEditor.commit();

			Intent i = new Intent(getActivity(), TriggerEditActivity.class);
			startActivity(i);

		} else if (item.getItemId() == R.id.settings) {
			// if the settings are selected
			Intent i = new Intent(getActivity(), SettingsActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Refreshes the profile list and then the list adapter.
	 */
	private void refreshListView() {
		triggerList.clear();

		ListView v = (ListView) getActivity().findViewById(
				R.id.ListViewTriggers);

		String[] fileList = getActivity().getFilesDir().list();
		StringBuffer sb = new StringBuffer();

		for (String file : fileList) {
			// if (file.contains("_trigger")) {
			// sb.append(file);
			// sb.delete(sb.length() - 12, sb.length());
			// triggerList.add(sb.toString());
			// sb.delete(0, sb.length());
			// }
			// if (file.contains("_tri_dis")) {
			// sb.append(file);
			// sb.delete(sb.length() - 12, sb.length());
			// triggerList.add(sb.toString());
			// sb.delete(0, sb.length());
			// }
			if (file.contains("_tri_dis") || file.contains("_trigger")) {
				triggerList.add(file);
			}
		}

		Collections.sort(triggerList, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
					return 1;
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
					return -1;
				return 0;
			}

		});

		ArrayListAdapterTrigger listAdapter = new ArrayListAdapterTrigger(
				getActivity(), 0, triggerList);
		v.setAdapter(listAdapter);
		v.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> _a, View _v, int _position,
			long arg3) {

		String filename = triggerList.get(_position);
		String[] options;

		if (filename.contains("_trigger")) {
			options = new String[] { "Disable", "Delete" };
		} else {
			options = new String[] { "Enable", "Delete" };
		}
		// used to notify the user of the longpress.
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(
				getActivity().VIBRATOR_SERVICE);
		vibrator.vibrate(25);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setItems(options, new LongPressMenuListener(_a, _position));
		builder.show();
		return false;
	}

	/**
	 * Used to delete profiles after a longpress.
	 * 
	 * @author Florian Schrofner & Dominik Koeltringer
	 * 
	 */
	private class LongPressMenuListener implements OnClickListener {
		AdapterView<?> a;
		int position;

		LongPressMenuListener(AdapterView<?> _a, int _position) {
			a = _a;
			position = _position;
		}

		/**
		 * Deletes a profile after the delete option is pressed.
		 * 
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
		 *      int)
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Boolean enableTrigger = true;
			StringBuilder sb = new StringBuilder();
			sb.append(String.valueOf(getActivity().getFilesDir()) + "/"
					+ a.getItemAtPosition(position));
			if (sb.toString().contains("_trigger")) {
				enableTrigger = false;
			}
			sb.delete(sb.length() - 12, sb.length());

			switch (which) {
			// Disables the trigger
			case 0: {
				File file;
				File newFile;
				
				if (enableTrigger) {
					file = new File(sb.toString() + "_tri_dis.xml");
					newFile = new File(sb.toString() + "_trigger.xml");
					file.renameTo(newFile);
				} else {
					file = new File(sb.toString() + "_trigger.xml");
					newFile = new File(sb.toString() + "_tri_dis.xml");
					file.renameTo(newFile);
				}
				
				refreshListView();
				// refreshes the triggerlist for the service
				Intent intent = new Intent();
				intent.setAction("at.fhhgbg.mc.profileswitcher.trigger.refresh");
				getActivity().sendBroadcast(intent);
			}
				break;
			// Deletes the trigger
			case 1: {
				File file = new File(sb.toString() + "_trigger.xml");
				file.delete();
				file = new File(sb.toString() + "_tri_dis.xml");
				file.delete();
				refreshListView();
				// refreshes the triggerlist for the service
				Intent intent = new Intent();
				intent.setAction("at.fhhgbg.mc.profileswitcher.trigger.refresh");
				getActivity().sendBroadcast(intent);
			}
				break;
			}
		}
	}

}
