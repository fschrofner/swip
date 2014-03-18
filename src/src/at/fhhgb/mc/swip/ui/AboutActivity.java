package at.fhhgb.mc.swip.ui;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		if (pref.getBoolean("dark_theme", false)) {
			setTheme(R.style.AppThemeDark);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		setupActionBar();
		
		TextView text = (TextView) findViewById(R.id.license_mit);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.license_gpl);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.license_apache);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.textViewFlorianSchrofner);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.textViewTranslatorChinese2);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.textViewTranslatorRussian2);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.textViewTranslatorItalian2);
		text.setOnClickListener(this);
		text = (TextView) findViewById(R.id.textViewTranslatorPortuguese2);
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
	 * To include the licenses we use weblinks.
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View _view) {
		if(_view.getId() == R.id.license_mit){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://opensource.org/licenses/mit-license.php"));
			startActivity(intent);
		} else if(_view.getId() == R.id.license_gpl){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.gnu.org/licenses/gpl-3.0.html"));
			startActivity(intent);
		} else if(_view.getId() == R.id.license_apache){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
			startActivity(intent);
		} else if(_view.getId() == R.id.textViewFlorianSchrofner){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://flosch.at"));
			startActivity(intent);
		} 
		
		//Here the links to translator pages are added
		else if(_view.getId() == R.id.textViewTranslatorChinese2){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://plus.google.com/110308759094073852821/"));
			startActivity(intent);
		} else if(_view.getId() == R.id.textViewTranslatorRussian2){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://plus.google.com/+DmitryGaich"));
			startActivity(intent);
		} else if(_view.getId() == R.id.textViewTranslatorItalian2){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://plus.google.com/+ClaudioArseni"));
			startActivity(intent);
		} else if(_view.getId() == R.id.textViewTranslatorPortuguese2){
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://crowdin.net/profile/diogosena"));
			startActivity(intent);
		}

	}

}