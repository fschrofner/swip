package at.fhhgbg.mc.profileswitcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Class used to fill the main activity with profiles.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ArrayListAdapter extends ArrayAdapter<String> implements
		OnClickListener {

	List<String> list;
	Context context;
	String element;

	public ArrayListAdapter(Context _context, int textViewResourceId,
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

		element = list.get(position);

		if (element != null) {
			// adds the profiles
			TextView v = null;
			v = (TextView) convertView.findViewById(R.id.textViewProfileName);
			v.setText(element);

			// adds the edit buttons
			ImageButton b = null;
			b = (ImageButton) convertView.findViewById(R.id.buttonEdit);
			b.setFocusable(false);
			b.setOnClickListener(this);
			b.setTag(this.element);
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
		XmlParserPref xmlParserPref = new XmlParserPref(context, v.getTag()
				.toString());

		try {
			xmlParserPref.initializeXmlParser(context.openFileInput(v.getTag()
					+ "_profile.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Intent i = new Intent(context, ProfileEditActivity.class);
		context.startActivity(i);
	}

}