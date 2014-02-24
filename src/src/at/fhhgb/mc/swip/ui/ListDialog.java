package at.fhhgb.mc.swip.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.services.Handler;

/**
 * Implements the pop-up dialog with a list of profiles (handed over as argument)
 * and a settings button.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ListDialog extends DialogFragment implements OnClickListener {

	// contains the profiles handed over as argument
	private String[] list;

	/**
	 * Sets up the profile pop-up.
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// gets the profiles from the argument
		list = (String[]) getArguments().getCharSequenceArray("ProfileList");

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.textListDialogTitle);
		builder.setIcon(R.drawable.profile_switcher_dialog_icon);
		builder.setItems(list, this);
		builder.setNeutralButton(getResources().getString(R.string.settings),
				new OnClickListener() { // the settings button

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getActivity()
								.getApplicationContext(), MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}

				});
		return builder.create();
	}

	/**
	 * Implements the onClickListener for the pop-up list.
	 * 
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
	 *      int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {

		// applies the selected profile(which indicates the position inside the
		// list)
		
		Handler handler = new Handler(getActivity());
		handler.applyProfile(list[which]);

		this.getActivity().finish();
	}

	/**
	 * When the dialog is dismissed the transparent activity will be closed.
	 * 
	 * @see android.app.DialogFragment#onDismiss(android.content.DialogInterface)
	 */
	@Override
	public void onDismiss(DialogInterface dialog) {
		this.getActivity().finish();
		super.onDismiss(dialog);
	}

}