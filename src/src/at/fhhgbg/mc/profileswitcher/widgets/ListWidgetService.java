package at.fhhgbg.mc.profileswitcher.widgets;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Returns the RemoteViewsFactory, which is needed for a collection widget.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */

public class ListWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent _intent) {
		Log.i("service", "service called!");
		return new ListWidgetFactory(this.getApplicationContext(), _intent,
				getFilesDir());
	}

}