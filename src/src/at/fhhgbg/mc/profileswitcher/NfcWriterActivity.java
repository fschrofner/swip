package at.fhhgbg.mc.profileswitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

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
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Activity used to write a profile on a NFC tag.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class NfcWriterActivity extends Activity implements
		DialogInterface.OnClickListener {

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
	private boolean writeTag(Tag tag) {
		try {

			StringBuffer buffer = new StringBuffer(filePath);
			int index = buffer.lastIndexOf("/", buffer.length() - 1);	// gets the last slash
			buffer.delete(0, index + 1); 								// removes all characters before the slash
			fileName = buffer.toString();								// saves the remaining string as file name

			fileInput = new FileInputStream(filePath);
			byte[] payload = new byte[(int) new File(filePath).length()];
			fileInput.read(payload); 									// saves the file into a byte array
			fileInput.close();
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