package at.fhhgb.mc.swip.ui;


import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import at.fhhgb.mc.swip.profile.Profile;
import at.fhhgb.mc.swip.services.Handler;

/**
 * 
 * Transparent(defined in the manifest) activity that applies the profile saved
 * on the NFC tag. Converts the bits saved on the tag into a profile object, which is then
 * given to the handler, who applies the profile.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class NfcReaderActivity extends Activity {

	/**
	 * Reads the tag object that is handed over from the intent and applies the
	 * profile which is saved on it.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String application = "application/at.fhhgb.mc.swip";

		if (intent.getType() != null && intent.getType().equals(application)) {
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msg = (NdefMessage) rawMsgs[0];
				
				
			byte[] nfcProfile = msg.getRecords()[0].getPayload();
			

			
			if(nfcProfile[0] == NfcWriterActivity.NFC_REVISION){
				Profile profile = new Profile("NFC-Profile");
				readFirstByte(profile, nfcProfile[1]);
				readSecondByte(profile,nfcProfile[2]);
				readThirdByte(profile, nfcProfile[3]);
				boolean setBrightness = readFourthByte(profile, nfcProfile[4]);
				Log.i("NfcReaderActivity", "setBrightness defined as: " + setBrightness);
				readFifthByte(profile, nfcProfile[5], setBrightness);
				readSixthByte(profile, nfcProfile[6]);
				
				Handler handler = new Handler(this);
				handler.applyProfile(profile);
			}

		}
		this.finish();
	}


	private void readFirstByte(Profile _profile, short byteOne){
		short ringtone_volume;
		short alarm_volume;
		
		//the bits will be moved 4 bits to the right
		// & 255 is used to get the two's complement of the byte into the beginning of the short
		ringtone_volume = (short)(byteOne & 255);
		ringtone_volume >>= 4;
		
		alarm_volume = byteOne;
		//15 = 0000 1111 which will remove the first 4 bits
		alarm_volume = (short) (alarm_volume & 15);
		
		//15 == 1111, which equals unchanged for this setting
		if(ringtone_volume != 15){
			_profile.setRingtoneVolume(ringtone_volume);
		} else {
			_profile.setRingtoneVolume(-1);
		}
		
		if(alarm_volume != 15){
			_profile.setAlarmVolume(alarm_volume);
		} else {
			_profile.setAlarmVolume(alarm_volume);
		}
		
		return;
	}
	
	private void readSecondByte(Profile _profile, short byteTwo){
		short media_volume;
		short display_timeout;
		
		//the bits should be moved 3 bits to the right(size of display timeout)
		media_volume = (short)(byteTwo & 255);
		media_volume >>= 3;
		
		display_timeout = byteTwo;
		display_timeout = (short)(byteTwo & 7);
		
		//31 = 11111 = unchanged
		if(media_volume != 31){
			_profile.setMediaVolume(media_volume);
		} else {
			_profile.setMediaVolume(-1);
		}
		
		//7 = 111 = unchanged
		if(display_timeout != 7){
			_profile.setScreenTimeOut(display_timeout);
		} else {
			_profile.setScreenTimeOut(-1);
		}
		
		return;	
	}
	
	private void readThirdByte(Profile _profile, short byteThree){
		short ringermode;
		short mobile_data;
		short wifi;
		short bluetooth;
		
		ringermode = (short)(byteThree & 255);
		ringermode >>= 6;
		
		//3 = 11, to filter the first two bits
		mobile_data = byteThree;
		mobile_data >>= 4;
		mobile_data = (short)(mobile_data & 3);
		
		wifi = byteThree;
		wifi >>= 2;
		wifi = (short)(wifi & 3);
		
		bluetooth = byteThree;
		bluetooth = (short)(bluetooth & 3);
		
		//3 = 11 = unchanged
		if(ringermode != 3){
			if(ringermode == 0){
				_profile.setRingerMode(Profile.mode.normal);
			}
			if(ringermode == 1){
				_profile.setRingerMode(Profile.mode.vibrate);
			}
			if(ringermode == 2){
				_profile.setRingerMode(Profile.mode.silent);
			}
		} else {
			_profile.setRingerMode(Profile.mode.unchanged);
		}
		
		
		if(mobile_data != 3){
			//00 = enabled
			if(mobile_data == 0){
				_profile.setMobileData(Profile.state.enabled);
			}
			if(mobile_data == 1){
				_profile.setMobileData(Profile.state.disabled);
			}
		} else {
			_profile.setMobileData(Profile.state.unchanged);
		}
		
		
		if(wifi != 3){
			if(wifi == 0){
				_profile.setWifi(Profile.state.enabled);
			}
			if(wifi == 1){
				_profile.setWifi(Profile.state.disabled);
			}
		} else {
			_profile.setWifi(Profile.state.unchanged);
		}
		
		if(bluetooth != 3){
			if(bluetooth == 0){
				_profile.setBluetooth(Profile.state.enabled);
			}
			if(bluetooth == 1){
				_profile.setBluetooth(Profile.state.disabled);
			}
		} else {
			_profile.setBluetooth(Profile.state.unchanged);
		}
		
		return;
	}
	
	/**
	 * Method used to read the fourth byte of a nfc tag and to set the according
	 * settings in the handed over profile object.
	 * Contains the settings for auto brightness and a bit for the brightness (unchanged bit).
	 * @param _profile the profile object where you want the settings to be set
	 * @param byteFour the byte of which to read
	 * @return true = brightness should be applied, false = brightness should not be applied.
	 */
	private boolean readFourthByte(Profile _profile, short byteFour){
		short brightnessOne;
		short autoBrightness;
		
		brightnessOne = (short)(byteFour & 255);
		brightnessOne >>= 7;
		Log.i("NfcReaderActivity", "BrightnessOne defined as: " + brightnessOne);
		
		autoBrightness = byteFour;
		autoBrightness >>= 5;
		autoBrightness = (short)(autoBrightness & 3);
	
		if(autoBrightness != 3){
			if(autoBrightness == 0){
				_profile.setScreenBrightnessAutoMode(Profile.state.enabled);
			}
			if(autoBrightness == 1){
				_profile.setScreenBrightnessAutoMode(Profile.state.disabled);
			}
		} else {
			_profile.setScreenBrightnessAutoMode(Profile.state.unchanged);
		}
		
		//this bit is used to differtiate between changed/unchanged
		if(brightnessOne == 1){
			_profile.setScreenBrightness(-1);
			return false;
		}
		return true;
	}
	
	private void readFifthByte(Profile _profile, short byteFive, boolean setBrightness){
		short brightnessTwo = (short)(byteFive & 255);
		Log.i("NfcReaderActivity","brightnessTwo: " +  brightnessTwo);
		if(setBrightness && _profile.getScreenBrightnessAutoMode() != Profile.state.enabled){
			_profile.setScreenBrightness(brightnessTwo);
		}
		return;
	}
	
	private void readSixthByte(Profile _profile, short byteSix){
		short gps;
		short nfc;
		short lockscreen;
		short airplane;
		
		Log.i("NfcReaderActivity", "byteSix: " + byteSix);
		gps = (short) (byteSix & 255);
		gps >>= 6;
		
		nfc = byteSix;
		nfc >>= 4;
		nfc = (short)(nfc & 3);
		
		lockscreen = byteSix;
		lockscreen >>= 2;
		lockscreen = (short)(lockscreen & 3);
		
		airplane = byteSix;
		airplane = (short)(airplane & 3);
		
		if(gps != 3){
			if(gps == 0){
				_profile.setGps(Profile.state.enabled);
			}
			if(gps == 1){
				_profile.setGps(Profile.state.disabled);
			}
		} else {
			_profile.setGps(Profile.state.unchanged);
		}
		
		if(nfc != 3){
			if(nfc == 0){
				_profile.setNfc(Profile.state.enabled);
			}
			if(nfc == 1){
				_profile.setNfc(Profile.state.disabled);
			}
		} else {
			_profile.setNfc(Profile.state.unchanged);
		}
		
		if(lockscreen != 3){
			if(lockscreen == 0){
				_profile.setLockscreen(Profile.state.enabled);
			}
			if(lockscreen == 1){
				_profile.setLockscreen(Profile.state.disabled);
			}
		} else {
			_profile.setLockscreen(Profile.state.unchanged);
		}
		
		if(airplane != 3){
			if(airplane == 0){
				_profile.setAirplane_mode(Profile.state.enabled);
			}
			if(airplane == 1){
				_profile.setAirplane_mode(Profile.state.disabled);
			}
		} else {
			_profile.setAirplane_mode(Profile.state.unchanged);
		}
	}
}