package at.fhhgbg.mc.profileswitcher.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import org.xmlpull.v1.XmlPullParserException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.R.drawable;
import at.fhhgbg.mc.profileswitcher.R.layout;
import at.fhhgbg.mc.profileswitcher.R.string;
import at.fhhgbg.mc.profileswitcher.profile.XmlParserPref;

/**
 * Activity used to write a profile on a NFC tag.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */

public class NfcWriterActivity extends Activity implements
		DialogInterface.OnClickListener {

	public static final byte[] NFC_REVISION = {(byte) 00000000}; //our revision of the nfc data format, used for compatibility
	public static final int NFC_SIZE = 6;		//the number of bytes our nfc data uses
	
	boolean inWriteMode;	// if true the activity is ready to write
	boolean writeNameOnly; 	// if true not the whole profile will be written, but the name instead
	String filePath; 		// the path to the profile
	String fileName; 		// the filename of the profile
	NfcAdapter adapter;
	FileInputStream fileInput;
	SharedPreferences pref;


	/**
	 * Sets up everything to write on a tag. Gets the file path and the NFC
	 * adapter.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_nfc_writer);
		setupActionBar();

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		filePath = new String(getFilesDir() + "/"
				+ getIntent().getStringExtra("fileName") + "_profile.xml");
		adapter = NfcAdapter.getDefaultAdapter(this);
		createByteArray(getIntent().getStringExtra("fileName"));
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Enables the write mode when the activity is resumed.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		enableWriteMode();
		super.onResume();
	}

	/**
	 * Sets the activity in writeMode, prepares a pending intent for the
	 * tag-discovered-event and sets the priority (for new tags) over other
	 * apps.
	 */
	private void enableWriteMode() {
		inWriteMode = true;

		Intent intent = new Intent(this, getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		IntentFilter[] filters = new IntentFilter[] { tagDetected };

		adapter.enableForegroundDispatch(this, pi, filters, null);
	}

	/**
	 * When the activity is paused it revokes the higher priority on new NFC
	 * tags.
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		adapter.disableForegroundDispatch(this);
	}

	/**
	 * When the activity gets a new intent and is in write mode, it writes the
	 * desired profile on the tag and sets write mode to false.
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (inWriteMode) {
			inWriteMode = false;
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (!writeNameOnly)
				writeTag(tag);
			if (writeNameOnly)
				writeName(tag);
		}
	}

	/**
	 * Writes the complete profile containing the name and the file on a NFC
	 * tag.
	 * 
	 * @param tag
	 *            the discovered NFC tag
	 * @return true if the operation was successful, false otherwise
	 */
//	private boolean writeTag(Tag tag) {
//		try {
//
//			StringBuffer buffer = new StringBuffer(filePath);
//			int index = buffer.lastIndexOf("/", buffer.length() - 1);	// gets the last slash
//			buffer.delete(0, index + 1); 								// removes all characters before the slash
//			fileName = buffer.toString();								// saves the remaining string as file name
//
//			fileInput = new FileInputStream(filePath);
//			byte[] payload = new byte[(int) new File(filePath).length()];
//			fileInput.read(payload); 									// saves the file into a byte array
//			fileInput.close();
//			String application = "application/at.fhhgbg.mc.profileswitcher";
//			byte[] mimeBytes = application
//					.getBytes(Charset.forName("US-ASCII"));
//			NdefRecord cardFile = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
//					mimeBytes, new byte[0], payload);
//			NdefRecord cardName = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
//					mimeBytes, new byte[0], fileName.getBytes());
//			NdefMessage message = new NdefMessage(new NdefRecord[] { cardFile,
//					cardName });
//			Ndef ndef = Ndef.get(tag);
//
//			if (ndef != null) {
//				ndef.connect();
//
//				// checks if the tag is writeable
//				if (!ndef.isWritable()) {
//					Toast.makeText(this,
//							getResources().getString(R.string.ReadOnly),
//							Toast.LENGTH_SHORT).show();
//					return false;
//				}
//
//				// work out how much space we need for the data
//				int size = message.toByteArray().length;
//
//				// if there is not enough space you can choose to write the name only
//				if (ndef.getMaxSize() < size) {
//					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//					dialog.setTitle(getResources().getString(
//							R.string.DialogNotEnoughSpaceTitle));
//					dialog.setMessage(getResources().getString(
//							R.string.DialogNotEnoughSpaceMessage1)
//							+ "\n" + getResources().getString(
//							R.string.DialogNotEnoughSpaceMessage2));
//					dialog.setIcon(R.drawable.alerts_and_states_warning);
//					dialog.setPositiveButton(
//							getResources().getString(R.string.DialogPositive),
//							this);
//					dialog.setNegativeButton(
//							getResources().getString(R.string.DialogNegative),
//							this);
//					dialog.show();
//					return false;
//				}
//
//				ndef.writeNdefMessage(message);
//				Toast.makeText(this,
//						getResources().getString(R.string.TagSuccessful),
//						Toast.LENGTH_SHORT).show();
//
//				this.finish();
//				return true;
//			} else { // if tag is completely empty, it will be formatted
//				NdefFormatable format = NdefFormatable.get(tag);
//				if (format != null) {
//
//					try {
//						format.connect();
//						format.format(message);
//						Toast.makeText(this, getResources()
//								.getString(R.string.TagSuccessful),
//								Toast.LENGTH_SHORT).show();
//						this.finish();
//						return true;
//					} catch (IOException e) {
//						Toast.makeText(this, getResources()
//								.getString(R.string.NotFormatable),
//								Toast.LENGTH_SHORT).show();
//						return false;
//					} catch (FormatException e) {
//						e.printStackTrace();
//					}
//
//				} else {
//					Toast.makeText(this,
//							getResources().getString(R.string.NotSupported),
//							Toast.LENGTH_SHORT).show();
//					return false;
//				}
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (FormatException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
	
	public boolean writeTag(Tag tag){
		try {
			StringBuffer buffer = new StringBuffer(filePath);
			int index = buffer.lastIndexOf("/", buffer.length() - 1);	// gets the last slash
			buffer.delete(0, index + 1); 								// removes all characters before the slash
			fileName = buffer.toString();								// saves the remaining string as file name
			
			byte[] payload =  createByteArray(fileName);
			
			String application = "application/at.fhhgbg.mc.profileswitcher";
			byte[] mimeBytes = application
					.getBytes(Charset.forName("US-ASCII"));
			NdefRecord cardFile = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
					mimeBytes, new byte[0], payload);
			NdefRecord cardName = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
					mimeBytes, new byte[0], fileName.getBytes());
			NdefMessage message = new NdefMessage(new NdefRecord[] { cardFile,
					cardName });
			Ndef ndef = Ndef.get(tag);

			if (ndef != null) {
				ndef.connect();

				// checks if the tag is writeable
				if (!ndef.isWritable()) {
					Toast.makeText(this,
							getResources().getString(R.string.ReadOnly),
							Toast.LENGTH_SHORT).show();
					return false;
				}

				// work out how much space we need for the data
				int size = message.toByteArray().length;

				// if there is not enough space you can choose to write the name only
				if (ndef.getMaxSize() < size) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setTitle(getResources().getString(
							R.string.DialogNotEnoughSpaceTitle));
					dialog.setMessage(getResources().getString(
							R.string.DialogNotEnoughSpaceMessage1)
							+ "\n" + getResources().getString(
							R.string.DialogNotEnoughSpaceMessage2));
					dialog.setIcon(R.drawable.alerts_and_states_warning);
					dialog.setPositiveButton(
							getResources().getString(R.string.DialogPositive),
							this);
					dialog.setNegativeButton(
							getResources().getString(R.string.DialogNegative),
							this);
					dialog.show();
					return false;
				}

				ndef.writeNdefMessage(message);
				Toast.makeText(this,
						getResources().getString(R.string.TagSuccessful),
						Toast.LENGTH_SHORT).show();

				this.finish();
				return true;
			} else { // if tag is completely empty, it will be formatted
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {

					try {
						format.connect();
						format.format(message);
						Toast.makeText(this, getResources()
								.getString(R.string.TagSuccessful),
								Toast.LENGTH_SHORT).show();
						this.finish();
						return true;
					} catch (IOException e) {
						Toast.makeText(this, getResources()
								.getString(R.string.NotFormatable),
								Toast.LENGTH_SHORT).show();
						return false;
					} catch (FormatException e) {
						e.printStackTrace();
					}

				} else {
					Toast.makeText(this,
							getResources().getString(R.string.NotSupported),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	public byte[] createByteArray(String _profileName){
		XmlParserPref parser = new XmlParserPref(this, "");
		try {
			parser.initializeXmlParser(openFileInput(_profileName + "_profile.xml"));
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		byte[] payload =  new byte[NFC_SIZE];
		
		createFirstByte();
		createSecondByte();
		createThirdByte();
		
		return payload;
	}
	
	/**
	 * Returns the first byte to be written on a nfc-tag.
	 * Contains the settings for ringtone volume and alarm volume.
	 * Ringtone Volume: 4 Bits
	 * Alarm Volume: 4 Bits
	 * @param _pref the shared preferences of which to read the settings
	 * @return a short(byte containing 8 bit), which represents the settings
	 */
	private short createFirstByte(){
		//using short here to make it not too complicated because of signed bytes
		short byteOne;
		short ringtone_volume;
		short alarm_volume;
		
		//checking if the value is set to unchanged or not
		if(pref.getInt("ringtone_volume", -1) != -1){
			ringtone_volume = (short) pref.getInt("ringtone_volume", -1);
			Log.i("NfcWriterActivity", "ringtone_volume: " + ringtone_volume);
		} else {
			//15 = 1111, which equals unchanged
			ringtone_volume = (short) 15;
			Log.i("NfcWriterActivity", "ringtone_volume unchanged!");
		}
		
		if(pref.getInt("alarm_volume", -1) != -1){
			alarm_volume = (short) pref.getInt("alarm_volume", -1);
			Log.i("NfcWriterActivity", "alarm_volume: " + alarm_volume);
		} else {
			alarm_volume = (short) 15;
			Log.i("NfcWriterActivity", "alarm_volume unchanged!");
		}
		
		//now moving ringtone volume 4 bits to the left, to make space for the alarm volume
		ringtone_volume <<= 4;
		
		//using bitwise or to concatenate the two shorts into one
		byteOne = (short)(ringtone_volume | alarm_volume);
		
		Log.i("NfcWriterActivity", "first byte defined as: " + byteOne + "(" + Integer.toBinaryString(byteOne) + ")");
		
		return byteOne;
	}
	
	/**
	 * Returns the second byte to be written on a nfc-tag.
	 * Contains the settings for media volume and display timeout.
	 * Media Volume: 5 Bits
	 * Display Timeout: 3 Bits
	 * @param _pref the shared preferences of which to read the settings
	 * @return a short(byte containing 8 bit), which represents the settings
	 */
	private short createSecondByte(){
		short byteTwo;
		short media_volume;
		short display_time_out;
		
		//checking if the value is set to unchanged or not
		if(pref.getInt("media_volume", -1) != -1){
			media_volume = (short) pref.getInt("media_volume", -1);
			Log.i("NfcWriterActivity", "media_volume: " + media_volume);
		} else {
			//31 = 11111, which equals unchanged
			media_volume = (short) 31;
			Log.i("NfcWriterActivity", "media_volume unchanged!");
		}
		
		if(!pref.getString("display_time_out", "-1").equals("-1")){
			display_time_out = Short.parseShort(pref.getString("display_time_out", "-1"));
			Log.i("NfcWriterActivity", "display_time_out: " + display_time_out);
		} else {
			//7 = 111
			display_time_out = (short) 7;
			Log.i("NfcWriterActivity", "display_time_out unchanged!");
		}
		
		//now moving media volume 3 bits to the left, to make space for the display time out
		media_volume <<= 3;
		
		//using bitwise or to concatenate the two shorts into one
		byteTwo = (short)(media_volume | display_time_out);
		
		Log.i("NfcWriterActivity", "second byte defined as: " + byteTwo + "(" + Integer.toBinaryString(byteTwo) + ")");
		
		return byteTwo;
	}
	
	
	/**
	 * Returns the third byte to be written on a nfc-tag.
	 * Contains the settings for ringer mode, mobile data, wifi & bluetooth.
	 * Ringer Mode: 2 Bits.
	 * Mobile Data: 2 Bits.
	 * Wifi: 2 Bits.
	 * Bluetooth: 2 Bits.
	 * @param _pref the shared preferences of which to read the settings
	 * @return a short(byte containing 8 bit), which represents the settings
	 */
	private short createThirdByte(){
		short byteThree;
		short ringer_mode = -1;
		short mobile_data = -1;
		short wifi = -1;
		short bluetooth = -1;
		
		//ringer mode
		if(!pref.getString("ringer_mode", "unchanged").equals("unchanged")){
			if(pref.getString("ringer_mode", "unchanged").equals("normal")){
				//00
				ringer_mode = (short) 0;
			}
			if(pref.getString("ringer_mode","unchanged").equals("vibrate")){
				//01
				ringer_mode = (short) 1;
			}
			if(pref.getString("ringer_mode","unchanged").equals("silent")){
				//10
				ringer_mode = (short) 2;
			}
			Log.i("NfcWriterActivity", "ringer_mode: " + pref.getString("ringer_mode", "unchanged"));
		} else {
			//3 = 11, which equals unchanged
			ringer_mode = (short) 3;
			Log.i("NfcWriterActivity", "ringer_mode unchanged!");
		}
		
		//mobile data
		if(!pref.getString("mobile_data", "unchanged").equals("unchanged")){
			if(pref.getString("mobile_data", "unchanged").equals("enabled")){
				//00
				mobile_data = (short) 0;
			}
			if(pref.getString("mobile_data", "unchanged").equals("disabled")){
				//01
				mobile_data = (short) 1;
			}
			Log.i("NfcWriterActivity", "mobile_data: " + mobile_data);
		} else {
			//11
			mobile_data = (short) 3;
			Log.i("NfcWriterActivity", "mobile_data unchanged!");
		}
		
		//wifi
		if(!pref.getString("wifi", "unchanged").equals("unchanged")){
			if(pref.getString("wifi", "unchanged").equals("enabled")){
				//00
				wifi = (short) 0;
			}
			if(pref.getString("wifi", "unchanged").equals("disabled")){
				//01
				wifi = (short) 1;
			}
			Log.i("NfcWriterActivity", "wifi: " + wifi);
		} else {
			//11
			wifi = (short) 3;
			Log.i("NfcWriterActivity", "wifi unchanged!");
		}
		
		//bluetooth
		if(!pref.getString("bluetooth", "unchanged").equals("unchanged")){
			if(pref.getString("bluetooth", "unchanged").equals("enabled")){
				//00
				bluetooth = (short) 0;
			}
			if(pref.getString("bluetooth", "unchanged").equals("disabled")){
				//01
				bluetooth = (short) 1;
			}
			Log.i("NfcWriterActivity", "bluetooth: " + bluetooth);
		} else {
			//11
			bluetooth = (short) 3;
			Log.i("NfcWriterActivity", "bluetooth unchanged!");
		}
		
		//moving the bits accordingly so they can be concatenated correctly
		ringer_mode <<= 6;
		mobile_data <<= 4;
		wifi <<= 2;
		
		//using bitwise or to concatenate the two shorts into one
		byteThree = (short)(ringer_mode | mobile_data | wifi | bluetooth);
		
		Log.i("NfcWriterActivity", "third byte defined as: " + byteThree + "(" + Integer.toBinaryString(byteThree) + ")");
		
		return byteThree;
	}
	/**
	 * Only writes the name of the profile on the tag, if the file is too big to
	 * be saved directly. Does basically the same as the method above, but with
	 * the name only.
	 * 
	 * @param tag
	 *            the discovered NFC tag
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean writeName(Tag tag) {
		try {
			String application = "application/at.fhhgbg.mc.profileswitcher";
			byte[] mimeBytes = application
					.getBytes(Charset.forName("US-ASCII"));
			byte[] payload = pref.getString("name", "default").getBytes();
			NdefRecord cardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
					mimeBytes, new byte[0], payload);
			NdefMessage message = new NdefMessage(
					new NdefRecord[] { cardRecord });

			Ndef ndef = Ndef.get(tag);

			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					Toast.makeText(this,
							getResources().getString(R.string.ReadOnly),
							Toast.LENGTH_SHORT).show();
					return false;
				}

				// work out how much space we need for the data
				int size = message.toByteArray().length;

				if (ndef.getMaxSize() < size) {
					Toast.makeText(this,
							getResources().getString(R.string.NotEnoughSpace),
							Toast.LENGTH_SHORT).show();
					return false;
				}

				ndef.writeNdefMessage(message);
				Toast.makeText(this,
						getResources().getString(R.string.TagSuccessful),
						Toast.LENGTH_SHORT).show();
				this.finish();
				return true;
			} else {
				// attempt to format tag
				NdefFormatable format = NdefFormatable.get(tag);

				if (format != null) {
					try {
						format.connect();
						format.format(message);
						Toast.makeText(this, getResources()
								.getString(R.string.TagSuccessful),
								Toast.LENGTH_SHORT).show();
						this.finish();
						return true;
					} catch (IOException e) {
						Toast.makeText(this, getResources()
								.getString(R.string.NotFormatable),
								Toast.LENGTH_SHORT).show();
						return false;
					} catch (FormatException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(this,
							getResources().getString(R.string.NotSupported),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * If the user agrees to write the profile name only, writeNameOnly will be
	 * set to true, so the writeName method will be called later.
	 * 
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
	 *      int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {

		if (which == DialogInterface.BUTTON_POSITIVE) {
			writeNameOnly = true;
		}

		dialog.dismiss();
	}

	/**
	 * If the home button on the action bar is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}