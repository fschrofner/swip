package at.fhhgb.mc.swip.trigger;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.fhhgb.mc.swip.R;

/**
 * A preference object, which contains a time picker.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class TimePickerPreference extends DialogPreference implements
OnCheckedChangeListener {
	
	private int lastHour = 0;
	private int lastMinute = 0;
	private TimePicker picker = null;
	private CheckBox checkbox;

	public static int getHour(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[0]));
	}

	public static int getMinute(String time) {
		String[] pieces = time.split(":");
		
		Integer.parseInt(time.split(":")[1]);

		return (Integer.parseInt(pieces[1]));
	}

	public TimePickerPreference(Context _context, AttributeSet attrs) {
		super(_context, attrs);
		setDialogLayoutResource(R.layout.layout_timepicker);

		setPositiveButtonText(R.string.set);
		setNegativeButtonText(R.string.cancel);
	}

	/**
	 * @see android.preference.DialogPreference#onCreateDialogView()
	 */
	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		
		picker = (TimePicker) view.findViewById(R.id.timepicker);
		picker.setIs24HourView(true);
		
		checkbox = (CheckBox) view.findViewById(R.id.checkbox_timepicker);
		checkbox.setOnCheckedChangeListener(this);
		
		if (getPersistedString(getContext().getString(R.string.ignored)).contains(":")) {
			checkbox.setChecked(true);
			picker.setEnabled(true);
		} else {
			checkbox.setChecked(false);
			picker.setEnabled(false);
		}

		return view;
	}

	/**
	 * @see android.preference.DialogPreference#onBindDialogView(android.view.View)
	 */
	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(lastHour);
		picker.setCurrentMinute(lastMinute);
	}

	/**
	 * @see android.preference.DialogPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (checkbox.isChecked() && positiveResult) {
			lastHour = picker.getCurrentHour();
			lastMinute = picker.getCurrentMinute();

			String time = String.format("%02d", lastHour) + ":"
					+ String.format("%02d", lastMinute);

			if (callChangeListener(time)) {
				persistString(time);
			}
		} else if (positiveResult) {
			String time = getContext().getString(R.string.ignored);
			
			if (callChangeListener(time)) {
				persistString(time);
			}
		}
	}

	/**
	 * @see android.preference.Preference#onGetDefaultValue(android.content.res.TypedArray, int)
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	/**
	 * @see android.preference.Preference#onSetInitialValue(boolean, java.lang.Object)
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time = null;

		if (restoreValue) {
			if (defaultValue == null) {
				time = getPersistedString(getContext().getString(R.string.ignored));
			} else  if (getPersistedString(getContext().getString(R.string.ignored)).contains(":")){
				time = getPersistedString(defaultValue.toString());
				checkbox.setChecked(true);
				picker.setEnabled(true);
				lastHour = getHour(time);
				lastMinute = getMinute(time);
			} else {
				time = getPersistedString(defaultValue.toString());
				checkbox.setChecked(false);
				picker.setEnabled(false);
			}
		} else {
			time = defaultValue.toString();
		}
	}

	/**
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean _isChecked) {
		if (_isChecked) {
			picker.setEnabled(true);
		} else {
			picker.setEnabled(false);
		}
	}
}