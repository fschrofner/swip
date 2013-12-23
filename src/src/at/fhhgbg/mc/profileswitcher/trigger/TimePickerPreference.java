package at.fhhgbg.mc.profileswitcher.trigger;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.fhhgbg.mc.profileswitcher.R;

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

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		
		picker = (TimePicker) view.findViewById(R.id.timepicker);
		picker.setIs24HourView(true);
		
		checkbox = (CheckBox) view.findViewById(R.id.checkbox_timepicker);
		checkbox.setOnCheckedChangeListener(this);
		
		if (getPersistedString("Ignored").contains(":")) {
			checkbox.setChecked(true);
			picker.setEnabled(true);
		} else {
			checkbox.setChecked(false);
			picker.setEnabled(false);
		}

		return view;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(lastHour);
		picker.setCurrentMinute(lastMinute);
	}

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
			String time = "Ignored";
			
			if (callChangeListener(time)) {
				persistString(time);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time = null;

		if (restoreValue) {
			if (defaultValue == null) {
				time = getPersistedString("Ignored");
			} else  if (getPersistedString("Ignored").contains(":")){
				Log.i("TimePreference", "contains :");
				time = getPersistedString(defaultValue.toString());
				checkbox.setChecked(true);
				picker.setEnabled(true);
				lastHour = getHour(time);
				lastMinute = getMinute(time);
			} else {
				Log.i("TimePreference", "else");
				time = getPersistedString(defaultValue.toString());
				checkbox.setChecked(false);
				picker.setEnabled(false);
			}
		} else {
			time = defaultValue.toString();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean _isChecked) {
		if (_isChecked) {
			picker.setEnabled(true);
		} else {
			picker.setEnabled(false);
		}
	}
}