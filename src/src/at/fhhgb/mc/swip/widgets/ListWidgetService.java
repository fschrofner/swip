package at.fhhgb.mc.swip.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;
import at.flosch.logwrap.Log;

/**
 * Returns the RemoteViewsFactory, which is needed for a collection widget.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class ListWidgetService extends RemoteViewsService {
	final static String TAG = "ListWidgetService";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent _intent) {
		Log.i(TAG, "service called!");
		return new ListWidgetFactory(this.getApplicationContext(), _intent,
				getFilesDir());
	}

}