package at.fhhgb.mc.swip.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import at.fhhgb.mc.swip.R;
import at.fhhgb.mc.swip.ui.MainActivity;

/**
 * AppWidgetProvider for the ListWidget.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ListWidget extends AppWidgetProvider {

	/**
	 * Sets all RemoteAdapters for the Widget.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context,
	 *      android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(Context _context, AppWidgetManager _appWidgetManager,
			int[] _appWidgetIds) {

		Intent intentButton = new Intent(_context, MainActivity.class);
		intentButton.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent piButton = PendingIntent.getActivity(_context, 0,
				intentButton, 0);
		RemoteViews viewsButton = new RemoteViews(_context.getPackageName(),
				R.layout.layout_list_widget);
		viewsButton.setOnClickPendingIntent(R.id.buttonEditWidget, piButton);

		_appWidgetManager.updateAppWidget(_appWidgetIds, viewsButton);

		for (int i = 0; i < _appWidgetIds.length; i++) {
			Intent intent = new Intent(_context, ListWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					_appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			
			RemoteViews views = new RemoteViews(_context.getPackageName(),
					R.layout.layout_list_widget);
			views.setRemoteAdapter(_appWidgetIds[0], R.id.list_widget, intent);

			Intent clickIntent = new Intent(_context, WidgetActivity.class);
			PendingIntent clickPI = PendingIntent.getActivity(_context, 0,
					clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			views.setPendingIntentTemplate(R.id.list_widget, clickPI);

			_appWidgetManager.updateAppWidget(_appWidgetIds[i], views);
		}
		super.onUpdate(_context, _appWidgetManager, _appWidgetIds);
	}

	/**
	 * Updates the data (profiles) when a new intent is received.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context,
	 *      android.content.Intent)
	 */
	@Override
	public void onReceive(Context _context, Intent _intent) {
		AppWidgetManager manager = AppWidgetManager.getInstance(_context);
		ComponentName thisAppWidget = new ComponentName(
				_context.getPackageName(), ListWidget.class.getName());
		int[] appWidgetIds = manager.getAppWidgetIds(thisAppWidget);
		manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget);
		
		onUpdate(_context, manager, appWidgetIds);
	}
}