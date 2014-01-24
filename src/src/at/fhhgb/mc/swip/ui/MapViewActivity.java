package at.fhhgb.mc.swip.ui;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;
import at.fhhgb.mc.swip.R;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This Activity shows the map view
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class MapViewActivity extends Activity implements
		GoogleMap.OnMapLongClickListener, OnClickListener {

	private GoogleMap mMap;
	private LatLng point;
	private int radius;
	private boolean preferencesChanged = false;
	private boolean satView = false;

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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
			// The Map is verified. It is now safe to manipulate the map.

			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);

			mMap.setPadding(0, 100, 0, 0);
			mMap.setMyLocationEnabled(true);
			mMap.setOnMapLongClickListener(this);
			Button clear = (Button) findViewById(R.id.buttonClearMap);
			clear.setOnClickListener(this);
			EditText editRadius = (EditText) findViewById(R.id.editTextRadius);
			editRadius.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				/**
				 * The Radius is changed.
				 * 
				 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence,
				 *      int, int, int)
				 */
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

					if (s.length() == 0) {
						radius = 50;
					} else {
						radius = Integer.parseInt(s.toString());
					}

					if (point != null && radius > 0) {
						mMap.clear();

						mMap.addMarker(new MarkerOptions().position(point));

						mMap.addCircle(new CircleOptions().radius(radius)
								.center(point).fillColor(0x5533B5E5)
								.strokeColor(0xEE33B5E5).strokeWidth(2));
					}

					preferencesChanged = true;

				}
			});

			// A geofence is already defined
			if (pref.getFloat("geofence_lat", -1F) > -1
					&& pref.getFloat("geofence_lng", -1F) > -1) {
				point = new LatLng(pref.getFloat("geofence_lat", 0),
						pref.getFloat("geofence_lng", 0));
				radius = pref.getInt("geofence_radius", 50);
				editRadius.setText(String.valueOf(radius));
				preferencesChanged = false;

				mMap.clear();
				mMap.addMarker(new MarkerOptions().position(point));

				if (radius > 0) {
					mMap.addCircle(new CircleOptions().radius(radius)
							.center(point).fillColor(0x5533B5E5)
							.strokeColor(0xEE33B5E5).strokeWidth(2));
				}

				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(point.latitude, point.longitude), 15));

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(point.latitude, point.longitude))
						.zoom(15).build();
				mMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			} else { // a new geofence is created
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();

				Location location = locationManager
						.getLastKnownLocation(locationManager.getBestProvider(
								criteria, false));
				if (location != null) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(), location
									.getLongitude()), 15));

					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(new LatLng(location.getLatitude(), location
									.getLongitude())).zoom(15).build();
					mMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));

				}
			}
		}
	}

	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemSat = menu.findItem(R.id.change_map_sat);
		MenuItem itemNormal = menu.findItem(R.id.change_map_normal);

		// The current view is the satellite view
		if (satView) {
			itemSat.setVisible(false);
			itemNormal.setVisible(true);
		} else {
			// The current view is the normal view
			itemSat.setVisible(true);
			itemNormal.setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_view, menu);
		return true;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
		} else if (item.getItemId() == R.id.save_location) {
			// Saving

			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);

			// No geofence defined
			if (point == null) {
				pref.edit().putFloat("geofence_lat", -1F).commit();
				pref.edit().putFloat("geofence_lng", -1F).commit();
				pref.edit().putInt("geofence_radius", -1).commit();
			} else {
				// new geofence defined

				pref.edit().putFloat("geofence_lat", (float) point.latitude)
						.commit();
				pref.edit().putFloat("geofence_lng", (float) point.longitude)
						.commit();
				pref.edit().putInt("geofence_radius", radius).commit();
			}

			this.finish();
		} else if (item.getItemId() == R.id.cancel_location) {
			this.finish();
		} else if (item.getItemId() == R.id.change_map_sat) {
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			satView = true;
		} else if (item.getItemId() == R.id.change_map_normal) {
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			satView = false;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * @see com.google.android.gms.maps.GoogleMap.OnMapLongClickListener#onMapLongClick(com.google.android.gms.maps.model.LatLng)
	 */
	@Override
	public void onMapLongClick(LatLng _point) {
		Log.i("MapViewActivity", "Longpress");

		point = _point;
		EditText editRadius = (EditText) findViewById(R.id.editTextRadius);

		if (editRadius.getText().toString().isEmpty()) {
			radius = 50;
		} else {
			radius = Integer.parseInt(editRadius.getText().toString());
		}

		if (mMap != null) {
			mMap.clear();

			// draw the marker
			mMap.addMarker(new MarkerOptions().position(point));

			// draw the radius
			if (radius > 0) {
				mMap.addCircle(new CircleOptions().radius(radius).center(point)
						.fillColor(0x5533B5E5).strokeColor(0xEE33B5E5)
						.strokeWidth(2));
			}
		}

		preferencesChanged = true;
	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		// clears the map
		if (v.equals(findViewById(R.id.buttonClearMap))) {
			mMap.clear();
			point = null;
			radius = -1;

			Log.i("MapViewActivity", "Cleared map.");

			preferencesChanged = true;
		}
	}

	/**
	 * Implemented a Dialog, if the user presses back and there were changes
	 * made.
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (preferencesChanged) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK);

			dialog.setTitle(getResources().getString(
					R.string.alert_discard_title));
			dialog.setMessage(getResources().getString(
					R.string.alert_discard_text));
			dialog.setIcon(R.drawable.alerts_and_states_warning);

			dialog.setPositiveButton(
					getResources().getString(R.string.alert_discard_yes),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});

			dialog.setNegativeButton(
					getResources().getString(R.string.alert_discard_no),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.show();
		} else {
			finish();
		}

	}

}
