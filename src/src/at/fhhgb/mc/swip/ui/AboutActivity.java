package at.fhhgb.mc.swip.ui;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import at.fhhgb.mc.swip.R;

/**
 * Activity to show the about screen.
 * 
 * @author Florian Schrofner & Dominik Koeltringer
 * 
 */
public class AboutActivity extends Activity implements OnClickListener {

	/**
	 * Sets the onClickListener of the TextView with the MIT-License to open up
	 * a webpage.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		setupActionBar();
		
		TextView text = (TextView) findViewById(R.id.license);
		text.setOnClickListener(this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * To include the MIT-License we used a weblink.
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://opensource.org/licenses/mit-license.php"));
		startActivity(intent);
	}

}