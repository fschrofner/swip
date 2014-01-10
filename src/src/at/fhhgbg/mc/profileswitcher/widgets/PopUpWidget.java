package at.fhhgbg.mc.profileswitcher.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import at.fhhgbg.mc.profileswitcher.R;
import at.fhhgbg.mc.profileswitcher.ui.ListDialogActivity;

/**
 * WidgetProvider for the Pop-Up Widget.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class PopUpWidget extends AppWidgetProvider {

	/**
	 * Sets the onClickPendingIntents for all views of the widget to start the
	 * pop-up.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context,
	 *      android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(Context _context, AppWidgetManager _appWidgetManager,
			int[] _appWidgetIds) {
		Intent intent = new Intent(_context, ListDialogActivity.class);
		PendingIntent pi = PendingIntent.getActivity(_context, 0, intent, 0);
		RemoteViews views = new RemoteViews(_context.getPackageName(),
				R.layout.layout_pop_up_widget);
		views.setOnClickPendingIntent(R.id.buttonPopUp, pi);
		_appWidgetManager.updateAppWidget(_appWidgetIds, views);
		super.onUpdate(_context, _appWidgetManager, _appWidgetIds);
	}
}