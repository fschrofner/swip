package at.fhhgbg.mc.profileswitcher.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.services.Handler;

/**
 * Sets the views for every collection widget and adds the data.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

	List<String> profileList = new ArrayList<String>();
	Context context;
	int appWidgetId;
	File directory;

	ListWidgetFactory(Context _context, Intent _intent, File _file) {
		Log.i("List View", "constructor");
		context = _context;
		appWidgetId = _intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		directory = _file;
	}

	/**
	 * Refreshes the list of profiles.
	 */
	private void refreshListView() {
		Log.i("Widget List", "refreshListView()");
		
		Boolean firstRun = false;
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		firstRun = pref.getBoolean("FIRST_RUN", false);
		
		if (!firstRun) {
			Handler handler = new Handler(context);
			handler.createStandardProfiles();
			
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("FIRST_RUN", true);
			editor.commit();
		}
		
		profileList.clear();
		String[] fileList = directory.list();
		StringBuffer sb = new StringBuffer();

		for (String file : fileList) {
			if (file.contains("_profile")) {
				sb.append(file);
				sb.delete(sb.length() - 12, sb.length());
				profileList.add(sb.toString());
				sb.delete(0, sb.length());
			}
		}

		Collections.sort(profileList, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
					return 1;
				if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
					return -1;
				return 0;
			}

		});

		Log.i("Widget List", "List refreshed!");
	}

	@Override
	public int getCount() {
		return profileList.size();
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	/**
	 * Returns the view for the item at the given position.
	 * 
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getViewAt(int)
	 */
	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews item = new RemoteViews(context.getPackageName(),
				R.layout.layout_list_item_widget);
		item.setTextViewText(R.id.list_item_text_widget,
				profileList.get(position));

		Intent i = new Intent();

		i.putExtra("fileName", profileList.get(position));
		item.setOnClickFillInIntent(R.id.list_item_text_widget, i);

		return (item);
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public void onCreate() {
		Log.i("Widget List", "onCreate()");
		refreshListView();
	}

	/**
	 * Refreshes the profileList if the data has changed (MainActivity stopped)
	 * 
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDataSetChanged()
	 */
	@Override
	public void onDataSetChanged() {
		Log.i("Factory", "onDataSetChanged");
		refreshListView();
	}

	@Override
	public void onDestroy() {
	}

}