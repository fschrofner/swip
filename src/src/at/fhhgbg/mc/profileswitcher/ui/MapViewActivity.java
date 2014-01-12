package at.fhhgbg.mc.profileswitcher.ui;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.UiSettings;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.support.v4.app.NavUtils;
import at.fhhgbg.mc.profileswitcher.R;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity extends Activity implements
		GoogleMap.OnMapLongClickListener {

	private GoogleMap mMap;
	private LatLng point;
	private int radius;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		// Show the Up button in the action bar.
		setupActionBar();

		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
		}

		// Check if we were successful in obtaining the map.
		if (mMap != null) {
			Log.i("MapViewActivity", "mMap != null");
			// The Map is verified. It is now safe to manipulate the map.

			// options = new GoogleMapOptions();
			// options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
			// .compassEnabled(true);

			// UiSettings uiSettings = mMap.getUiSettings();
			// uiSettings.setAllGesturesEnabled(true);
			// uiSettings.setCompassEnabled(true);
			// uiSettings.setMyLocationButtonEnabled(true);

			mMap.setOnMapLongClickListener(this);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			// This ID represents the Home or Up button. In the case of this
//			// activity, the Up button is shown. Use NavUtils to allow users
//			// to navigate up one level in the application structure. For
//			// more details, see the Navigation pattern on Android Design:
//			//
//			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
//			//
//			NavUtils.navigateUpFromSameTask(this);
//			return true;
//		}
		
		if(item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
		} else if (item.getItemId() == R.id.save_location) { 
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);
			
			if (point == null) {
				pref.edit().putFloat("geofence_lat", -1F).commit();
				pref.edit().putFloat("geofence_lng", -1F).commit();
				pref.edit().putInt("geofence_radius", -1).commit();
			} else {
				pref.edit().putFloat("geofence_lat", (float) point.latitude).commit();
				pref.edit().putFloat("geofence_lng", (float) point.longitude).commit();
				pref.edit().putInt("geofence_radius", radius).commit();
			}
			
			this.finish();
		} else if (item.getItemId() == R.id.cancel_location) {
			this.finish();
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMapLongClick(LatLng _point) {
		Log.i("MapViewActivity", "Longpress");
		
		point = _point;
		EditText editRadius = (EditText) findViewById(R.id.editTextRadius);

		if (editRadius.getText().toString().isEmpty()) {
			radius = 0;
		} else {
			radius = Integer.parseInt(editRadius.getText().toString());
		}

		if (mMap != null) {
			mMap.clear();
			
			mMap.addMarker(new MarkerOptions().position(point).title(
					"Hello world"));

			if (radius > 0) {
				mMap.addCircle(new CircleOptions().radius(radius)
						.center(point).fillColor(0x5533B5E5).strokeColor(0xEE33B5E5).strokeWidth(3));
			}
		}
	}

}
