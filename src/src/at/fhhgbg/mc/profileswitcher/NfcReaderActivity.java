package at.fhhgbg.mc.profileswitcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/**
 * 
 * Transparent(defined in the manifest) activity that applies the profile saved
 * on the NFC tag. Identifies if the profile is saved directly on the tag or if
 * only the profile name is saved.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class NfcReaderActivity extends Activity {

	/**
	 * Reads the tag object that is handed over from the intent and applies the
	 * profile which is saved on it. Differentiates between profiles that are
	 * saved completely on the tag and profiles which are only saved with their
	 * name.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String application = "application/at.fhhgbg.mc.profileswitcher";

		if (intent.getType() != null && intent.getType().equals(application)) {
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msg = (NdefMessage) rawMsgs[0];

			// if the message only contains one element, only the name was saved
			// on the profile
			if (msg.getRecords().length == 1) {
				XmlParser parser = new XmlParser(this);

				try {
					String profileName = new String(
							msg.getRecords()[0].getPayload());
					parser.initializeXmlParser(openFileInput(profileName
							+ ".xml"));
					Toast toast = Toast.makeText(this, profileName
							+ " was applied!", Toast.LENGTH_SHORT);
					toast.show();
				} catch (FileNotFoundException e) {
					Toast toast = Toast.makeText(this, "Profile "
							+ new String(msg.getRecords()[0].getPayload())
							+ " not found!", Toast.LENGTH_SHORT);
					toast.show();
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (msg.getRecords().length == 2) {
				// if the message contains two records also the file itself was
				// saved

				String profileName = new String(
						msg.getRecords()[1].getPayload());
				byte[] file = msg.getRecords()[0].getPayload();

				try {
					File outputFile = new File(getFilesDir(), new String(
							profileName));
					FileOutputStream fileOutput = new FileOutputStream(
							outputFile);
					fileOutput.write(file);
					fileOutput.close();
					XmlParser parser = new XmlParser(this);
					parser.initializeXmlParser(openFileInput(profileName
							+ ".xml"));
					Toast toast = Toast.makeText(this, profileName
							+ " was applied!", Toast.LENGTH_SHORT);
					toast.show();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}
			}
			this.finish();
		}
	}
}