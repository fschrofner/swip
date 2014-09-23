package at.fhhgb.mc.swip.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.services.Handler;
import at.fhhgb.mc.swip.widgets.ListWidget;

/**
 * Fragment, where the profiles are listed.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class ProfileFragment extends Fragment implements OnItemClickListener,
		OnItemLongClickListener {

	List<String> profileList = new ArrayList<String>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		View convertView = inflater.inflate(R.layout.activity_profile_fragment,null);
		return convertView;
	}
	

	/**
	 * Sets up the layout and the notification.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		boolean firstRun = false;
		setHasOptionsMenu(true);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		firstRun = pref.getBoolean("FIRST_RUN", false);
		
		// if the application is run for the first time
		if (!firstRun) {

			Handler handler = new Handler(getActivity());
			handler.createDefaultProfiles();
			
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("FIRST_RUN", true);
			editor.commit();
		}
		
		//updates the systemapp if there has been an update
		Handler handler = new Handler(getActivity());
		handler.updateSystemApp();
		
		// starts the permanent notification if it is activated
		if (pref.getBoolean("notification", false)) {
			handler.updateNotification();
		} else {
			// deactivates the notification otherwise
			NotificationManager notificationManager = (NotificationManager) getActivity()
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(123);
		}
	}

	/**
	 * Inflates the menu.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflates the menu containing the add profiles button and the settings
		menuInflater.inflate(R.menu.main_menu_profile, menu);
	    super.onCreateOptionsMenu(menu,menuInflater);
	}

	/**
	 * Will be called if the add- or settings-button is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// if a new profile should be created
		if (item.getItemId() == R.id.new_profile) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			Editor prefEditor = preferences.edit();

			// loads the default values into the shared preferences
			prefEditor.putString("name", getResources().getString(R.string.pref_default_name));
			prefEditor.putString("ringer_mode", "unchanged");
			prefEditor.putInt("alarm_volume", -1);
			prefEditor.putInt("media_volume", -1);
			prefEditor.putInt("ringtone_volume", -1);
			prefEditor.putString("bluetooth", "unchanged");
			prefEditor.putString("wifi", "unchanged");
			prefEditor.putString("mobile_data", "unchanged");
			prefEditor.putString("gps", "unchanged");
			prefEditor.putInt("display_brightness", -1);
			prefEditor.putString("display_auto_mode", "unchanged");
			prefEditor.putString("display_time_out", "-1");
			prefEditor.putString("lockscreen", "unchanged");
			prefEditor.putString("nfc", "unchanged");
			prefEditor.putString("airplane_mode", "unchanged");

			prefEditor.commit();

			Intent i = new Intent(getActivity(), ProfileEditActivity.class);
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
		profileList.clear();

		ListView v = (ListView) getActivity().findViewById(R.id.ListViewProfiles);

		String[] fileList = getActivity().getFilesDir().list();
		StringBuffer sb = new StringBuffer();

		for (String file : fileList) {
			if (file.contains("_profile")) {
				sb.append(file);
				sb.delete(sb.length() - 12, sb.length());
				profileList.add(sb.toString());
				sb.delete(0, sb.length());
			}
		}

		Collections.sort(profileList, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
					return 1;
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
					return -1;
				return 0;
			}

		});

		ArrayListAdapter listAdapter = new ArrayListAdapter(getActivity(), 0, profileList);
		v.setAdapter(listAdapter);
		v.setOnItemClickListener(this);
		v.setOnItemLongClickListener(this);
	}

	/**
	 * Simply refreshes the list view every time the activity is started.
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		refreshListView();
	}

	/**
	 * Updates the widgets if the activity is stopped.
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();

		Intent intent = new Intent(getActivity(), ListWidget.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		int[] ids = { R.xml.widget_list };
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		getActivity().sendBroadcast(intent);

	}

	/**
	 * Applies a profile if clicked on it.
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> _a, View v, int _position, long arg3) {
		Handler handler = new Handler(getActivity());
		handler.applyProfile((String) _a.getItemAtPosition(_position));
	}

	/**
	 * Shows the delete option for a profile if it is longpressed.
	 * 
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> _a, View _v, int _position,
			long arg3) {
		
		String[] options;
		
		//if nfc is available
		if (checkNfc()) {
			options = new String[] { getResources().getString(R.string.long_press_nfc) , getResources().getString(R.string.delete)};
		}
		else {
			options = new String[] {getResources().getString(R.string.delete)};
		}
		
		// used to notify the user of the longpress.
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
		vibrator.vibrate(25);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setItems(options, new LongPressMenuListener(_a, _position));
		builder.show();
		return true;
	}
	
	
	/**
	 * Checks if a nfc adapter is available
	 * @return true = nfc adapter is available, false = no nfc adapter available
	 */
	private boolean checkNfc(){
		NfcManager manager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
		NfcAdapter adapter = manager.getDefaultAdapter();
		if(adapter != null){
			return true;
		} else {
			return false;
		}
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
			//nfc available
			if(checkNfc()){
				switch (which) {
				case 0: {
					NfcManager manager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
					NfcAdapter adapter = manager.getDefaultAdapter();
					
					//checks if nfc is enabled
					if(adapter.isEnabled()){
						Intent intent = new Intent(getActivity(), NfcWriterActivity.class);
						intent.putExtra("fileName", a.getItemAtPosition(position).toString());
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);
					} else {
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(),
								AlertDialog.THEME_DEVICE_DEFAULT_DARK);
						alertDialog.setIcon(R.drawable.alerts_and_states_warning);
						alertDialog.setTitle(getResources().getString(R.string.nfcNotEnabledTitle));
						alertDialog.setMessage(getResources().getString(R.string.nfcNotEnabledDialog1) + 
								"\n" + getResources().getString(R.string.nfcNotEnabledDialog2));
						alertDialog.setNeutralButton(getResources().getString(R.string.DialogNeutral), new OnClickListener(){

							@Override
							public void onClick(DialogInterface _dialog, int arg1) {
								_dialog.dismiss();
							}
							
						});
						alertDialog.show();
					}

					break;
				}
				case 1: {
					File file = new File(String.valueOf(getActivity().getFilesDir()) + "/"
							+ a.getItemAtPosition(position) + "_profile.xml");
					file.delete();
					refreshListView();
				}
					break;
				}
			}
			//no nfc available
			else {
				File file = new File(String.valueOf(getActivity().getFilesDir()) + "/"
						+ a.getItemAtPosition(position) + "_profile.xml");
				file.delete();
				refreshListView();
			}

		}
	}

}
