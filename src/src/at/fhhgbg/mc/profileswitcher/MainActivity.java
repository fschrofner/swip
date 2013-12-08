package at.fhhgbg.mc.profileswitcher;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

/**
 * Main Activity which shows the already created profiles and allows applying
 * and editing them or creating new ones. Also provides a settings activity,
 * allowing one to activate the permanent notification.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class MainActivity extends Activity{
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	android.app.FragmentManager fragmentManager = getFragmentManager();
	android.app.Fragment profileFragment = new ProfileFragment();

	boolean titleShowing = true;
	boolean homeShowing = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		actionbar.setDisplayHomeAsUpEnabled(true);
//		actionbar.setHomeButtonEnabled(true);
		Tab tab = actionbar.newTab();
		tab.setText("Profiles");
		tab.setTag(1);
		tab.setTabListener(new TabSwitcher());
		actionbar.addTab(tab);
		tab = actionbar.newTab();
		tab.setText("Triggers");
		tab.setTag(2);
		tab.setTabListener(new TabSwitcher());
		actionbar.addTab(tab);
	}

	@Override
	protected void onDestroy() {
		Log.i("Test", "Activity destroyed!");
		super.onDestroy();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

//	@Override
//	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//	switch(itemPosition){
//	case(0) : {
//		fragmentTransaction.addToBackStack(null);
//		fragmentTransaction.replace(R.id.FragmentFrame1, profileFragment);
//	}
//	break;
//	}
//	fragmentTransaction.commit();
//	return true;
//
//	}
}