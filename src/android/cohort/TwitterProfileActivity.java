package android.cohort;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterProfileActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.twitter_profile_activity);

		
		String extraName = getString(R.string.tweet_extra_name);
		String tweetValue = (String) getIntent().getSerializableExtra(extraName);

		try {
			JSONObject tweetObject = new JSONObject(tweetValue);
			String url = tweetObject.getString("profile_image_url");
			AsyncTask<String, String, Bitmap> task = getTask();
			task.execute(url);
			
			TextView name = (TextView) findViewById(R.id.name);
			String username = tweetObject.getString("from_user");
			name.setText(username);
			
		} catch (JSONException e) {
			String imageAlert = getString(R.string.image_alert);
			Toast.makeText(getApplicationContext(), imageAlert, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		super.onCreate(savedInstanceState);
	}

	private AsyncTask<String, String, Bitmap> getTask() {
		return new AsyncTask<String, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				String url = params[0];
				HttpParams httpParameters = new BasicHttpParams();
				HttpGet httpRequest = new HttpGet(url);

				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

				httpClient.getConnectionManager().closeExpiredConnections();
				try {
					HttpResponse response = httpClient.execute(httpRequest);
					HttpEntity entity = response.getEntity();
					InputStream is = null;
					try {
						BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
						is = bufHttpEntity.getContent();
						Bitmap bitmap = BitmapFactory.decodeStream(is);
						return bitmap;
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
							}
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					httpClient.getConnectionManager().closeExpiredConnections();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				ImageView image = (ImageView) findViewById(R.id.pic);
				image.setImageBitmap(result);
				super.onPostExecute(result);
			}
		};
	}
}
