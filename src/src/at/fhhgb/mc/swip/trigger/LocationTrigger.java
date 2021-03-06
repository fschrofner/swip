package at.fhhgb.mc.swip.trigger;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import at.fhhgb.mc.swip.constants.IntentConstants;
import at.flosch.logwrap.Log;
import at.fhhgb.mc.swip.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

/**
 * Connects to the Google Location API and is used for geofence handling.
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class LocationTrigger implements ConnectionCallbacks,
		OnConnectionFailedListener, OnAddGeofencesResultListener,
		OnRemoveGeofencesResultListener {
	
	final static String TAG = "LocationTrigger";

	private Context context;
	private PendingIntent pendingIntent;

	// Holds the location client
	private LocationClient locationClient;
	// Flag that indicates if a request is underway.
	private boolean inProgress;
	// Internal List of Geofence objects
	List<Geofence> geofenceList;
	List<String> removeList;
	// Persistent storage for geofences
	private SimpleGeofenceStore geofenceStorage;

	public LocationTrigger(Context _context) {
		this.context = _context;

		// Instantiate a new geofence storage area
		geofenceStorage = new SimpleGeofenceStore(context);

		// Instantiate the current List of geofences
		geofenceList = new ArrayList<Geofence>();

		locationClient = new LocationClient(context, this, this);

	}

	/**
	 * Verifies that Google Play services is available before making a request and
	 * displays a dialog if not.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	public boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Geofence Detection", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(context,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK);
			dialog.setTitle(context.getResources().getString(
					R.string.alert_profile_title));
			dialog.setMessage(context.getResources().getString(
					R.string.alert_profile_text));
			dialog.setNegativeButton(
					context.getResources().getString(R.string.alert_button),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			dialog.show();
			return false;
		}
	}

	/**
	 * Creates a pending intent for location changes.
	 * 
	 * @return The pending intent.
	 */
	private PendingIntent getPendingIntent() {
		Log.i(TAG, "Creating pending intent");
		// Create an explicit Intent
		Intent intent = new Intent();
		intent.setAction(IntentConstants.LOCATION_CHANGE);

		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Registers a geofence at the system and saves it in the store.
	 * 
	 * @param _geofence
	 *            The geofence, which should be registered.
	 */
	public void registerGeofence(SimpleGeofence _geofence) {
		Log.i(TAG, "Started registerGeofence");
		geofenceList.add(_geofence.toGeofence());
		geofenceStorage.setGeofence(_geofence.getId(), _geofence);
		refreshGeofences();
	}

	/**
	 * Unregisters a geofence
	 * 
	 * @param _id
	 *            The id of the geofence, which should be unregistered.
	 */
	public void unregisterGeofence(String _id) {
		Log.i(TAG, "Started unregisterGeofence");
		// locationClient = new LocationClient(context, this, this);
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(_id);
		removeList = ids;
		refreshGeofences();
	}

	/**
	 * Test for Google Play services after setting the request type. If Google
	 * Play services isn't present, the proper request can be restarted.
	 */
	private void refreshGeofences() {
		// Start a request to add geofences

		Log.i(TAG, "Started addGeofences");
		if (!servicesConnected()) {
			Log.e(TAG, "Google Play Services not connected");
			return;
		}

		// If a request is not already underway
		if (!inProgress) {
			// Indicate that a request is underway
			inProgress = true;
			// Request a connection from the client to Location Services
			locationClient.connect();
			Log.i(TAG, "Location Client connected");
		} else {
			Log.e(TAG,
					"There is already a location client connected");
		}
	}


	/**
	 * Gets called when the geofences where added, sets inProgress to false
	 * (so other requests can be made) and disconnects the locationclient.
	 */
	@Override
	public void onAddGeofencesResult(int arg0, String[] arg1) {
		inProgress = false;
		locationClient.disconnect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	/**
	 * When the LocationClient is connected the geofences in the geofenceList
	 * get added and the geofences in the removeList are removed.
	 */
	@Override
	public void onConnected(Bundle arg0) {
		// Get the PendingIntent for the request
		pendingIntent = getPendingIntent();
		// Send a request to add the current geofences
		if (geofenceList != null && geofenceList.size() > 0) {
			locationClient.addGeofences(geofenceList, pendingIntent, this);
			Log.i(TAG, "Geofences added");
			geofenceList = new ArrayList<Geofence>();
		} else if (removeList != null && removeList.size() > 0) {
			locationClient.removeGeofences(removeList, this);
			Log.i(TAG, "Geofences removed");
			removeList = new ArrayList<String>();
		}

	}

	/**
	 * When the locationClient is disconnected inProgress is set to false and
	 * the locationClient get set to null.
	 */
	@Override
	public void onDisconnected() {
		// Turn off the request flag
		inProgress = false;
		// Destroy the current location client
		locationClient = null;
	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int arg0,
			PendingIntent arg1) {
	}

	/**
	 * Clears the geofences from the storage, when they are removed from the system.
	 */
	@Override
	public void onRemoveGeofencesByRequestIdsResult(int arg0, String[] _ids) {
		geofenceStorage.clearGeofenceList(_ids);
	}

}
