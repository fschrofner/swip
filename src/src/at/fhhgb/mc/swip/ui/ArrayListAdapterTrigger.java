package at.fhhgb.mc.swip.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.constants.SharedPrefConstants;
import at.fhhgb.mc.swip.trigger.XmlParserPrefTrigger;

/**
 * Class used to fill the list in the trigger fragment with triggers.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ArrayListAdapterTrigger extends ArrayAdapter<String> implements
		OnClickListener {

	List<String> list;
	Context context;
	String element;

	public ArrayListAdapterTrigger(Context _context, int textViewResourceId,
			List<String> objects) {
		super(_context, textViewResourceId, objects);
		list = objects;
		context = _context;
	}

	/**
	 * Returns the view of the list filled with content.
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			Context c = getContext();
			LayoutInflater inflater = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.layout_list_profiles_item,
					null);
		}
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		
		ImageButton buttonEdit = (ImageButton) convertView.findViewById(R.id.buttonEdit);
		if (pref.getBoolean(SharedPrefConstants.DARK_THEME, false)) {
			buttonEdit.setImageDrawable(getContext().getResources().getDrawable(R.drawable.content_edit_dark));
		}

		element = list.get(position);

		if (element != null) {
			// adds the profiles
			TextView v = null;
			StringBuilder sb = new StringBuilder();
			sb.append(element);
			sb.delete(sb.length() - 12, sb.length());
			v = (TextView) convertView.findViewById(R.id.textViewProfileName);
			v.setText(sb.toString());

			// adds the edit buttons
			buttonEdit.setFocusable(false);
			buttonEdit.setOnClickListener(this);
			buttonEdit.setTag(sb.toString());
			
			if (element.contains("_tri_dis")) {
				v.setTextColor(Color.GRAY);
				buttonEdit.setEnabled(false);
				buttonEdit.setImageDrawable(null);
			}
		}
		return convertView;
	}

	/**
	 * The onClickListener-method for the edit button.
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		XmlParserPrefTrigger xmlParserPrefTrigger = new XmlParserPrefTrigger(
				context, v.getTag().toString());

		try {
			xmlParserPrefTrigger.initializeXmlParser(context.openFileInput(v
					.getTag() + "_trigger.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Intent i = new Intent(context, TriggerEditActivity.class);
		context.startActivity(i);
	}

}