package android.cohort;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterNameActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_name_activity);
        
        Button searchButton = (Button) findViewById(R.id.search);
        searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Retrieve the search string
				EditText searchKey = (EditText) findViewById(R.id.search_text);
				String searchValue = searchKey.getText().toString().trim();
				
				//Checks to see if the entry is valid
				if (searchValue.equals("")){
					String alert = getString(R.string.empty_string_alert);
					Toast.makeText(getApplicationContext(), alert, Toast.LENGTH_SHORT).show();
					return;
				}
				
				//Launches the next screen
				Intent settingsActivity = new Intent(getApplicationContext(), TwitterSearchResultsWithImagesActivity.class);
				
				//Passes the search string to the next activity
				String extraName = getString(R.string.search_extra_name);
				settingsActivity.putExtra(extraName, searchValue);
				
				startActivity(settingsActivity);
			}
		});
    }
}