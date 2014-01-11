/*
 * Copyright 2012 Jay Weisskopf
 *
 * Licensed under the MIT License (see LICENSE.txt)
 */

package net.jayschwa.android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;

/**
 * @author Jay Weisskopf
 */
public class SliderPreference extends DialogPreference implements
		OnCheckedChangeListener {

	protected final static int SEEKBAR_RESOLUTION = 7;

	protected int mValue;
	protected CharSequence[] mSummaries;
	protected int maxValue;
	protected int minValue;
	protected Context context;
	protected boolean changed = true;
	protected SeekBar seekbar;
	protected CheckBox checkbox;

	/**
	 * @param context
	 * @param attrs
	 */
	public SliderPreference(Context _context, AttributeSet attrs) {
		super(_context, attrs);
		setup(_context, attrs);
		maxValue = attrs.getAttributeIntValue(null, "maximum_value", 20);
		minValue = attrs.getAttributeIntValue(null, "minimum_value", 0);
		context = _context;
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SliderPreference(Context _context, AttributeSet attrs, int defStyle) {
		super(_context, attrs, defStyle);
		setup(_context, attrs);
		maxValue = attrs.getAttributeIntValue(null, "maximum_value", 20);
		context = _context;
	}

	private void setup(Context context, AttributeSet attrs) {
		setDialogLayoutResource(R.layout.slider_preference_dialog);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SliderPreference);
		try {
			setSummary(a
					.getTextArray(R.styleable.SliderPreference_android_summary));
		} catch (Exception e) {
			// Do nothing
		}
		a.recycle();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if (getPersistedInt(mValue) < 0) {
			changed = false;
			setValue(-1);
		} else {
			changed = true;
			setValue(getPersistedInt(mValue));
		}
	}

	@Override
	public CharSequence getSummary() {
		if (mSummaries != null && mSummaries.length > 0) {
			int index = (int) (mValue);
			index = Math.min(index, mSummaries.length - 1);
			if (!changed)
				return mSummaries[0];
			return mSummaries[index - minValue + 1];
		} else {
			return super.getSummary();
		}
	}

	public void setSummary(CharSequence[] summaries) {
		mSummaries = summaries;
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(summary);
//		mSummaries = null;
	}

	@Override
	public void setSummary(int summaryResId) {
		try {
			setSummary(getContext().getResources().getStringArray(summaryResId));
		} catch (Exception e) {
			super.setSummary(summaryResId);
		}
	}

	public int getValue() {
		return mValue;
	}

	public void setValue(int value) {

		if (value < 0) {
			persistInt(value);
		} else {
			value = Math.max(minValue, Math.min(value, maxValue));
			if (shouldPersist()) {
				persistInt(value);
			}
			if (value != mValue) {
				mValue = value;
				notifyChanged();
			}
		}
	}

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();


		seekbar = (SeekBar) view.findViewById(R.id.slider_preference_seekbar);

		checkbox = (CheckBox) view.findViewById(R.id.checkbox_seekbar);
		checkbox.setOnCheckedChangeListener(this);

		if (!changed) {
			checkbox.setChecked(false);
			seekbar.setEnabled(false);
		} else {
			checkbox.setChecked(true);
			seekbar.setEnabled(true);
		}

		seekbar.setMax(maxValue);
		seekbar.setProgress(mValue);
		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		final int newValue = (int) seekbar.getProgress();
		
		if (checkbox.isChecked() && positiveResult && callChangeListener(newValue)) {
			changed = true;
			setValue(newValue);
			setSummary(getSummary());
		} else if (positiveResult) {
			changed = false;
			setValue(-1);
			setSummary(getSummary());
		}

		super.onDialogClosed(positiveResult);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			seekbar.setEnabled(true);
//			changed = true;
		} else {
			seekbar.setEnabled(false);
//			changed = false;
		}

	}
}