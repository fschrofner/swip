package at.fhhgbg.mc.profileswitcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class TriggerFragment extends Fragment {

	//TODO the whole thing 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		View convertView = inflater.inflate(R.layout.activity_trigger_fragment,null);
		setHasOptionsMenu(true);
		return convertView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
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
	    super.onCreateOptionsMenu(menu,menuInflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.new_trigger) {
//			Intent i = new Intent(getActivity(), TriggerEditActivity.class);
//			startActivity(i);
			//TODO start create trigger activity
		} else if (item.getItemId() == R.id.settings) {
			// if the settings are selected
			Intent i = new Intent(getActivity(), SettingsActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

}
