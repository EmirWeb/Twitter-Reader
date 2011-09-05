package android.cohort;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterSearchResultsActivity extends Activity {

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String extraName = getString(R.string.search_extra_name);
		String searchValue = (String) getIntent().getSerializableExtra(extraName);

		setContentView(R.layout.twitter_search_results_activity);

		AsyncTask<String, String, String> task = getAsyncTask();
		task.execute(searchValue);

		super.onCreate(savedInstanceState);
	}

	protected void parseResults(String jsonString) {
		try {
			JSONObject results = new JSONObject(jsonString);
			JSONArray resultsArray = results.getJSONArray("results");

			ArrayList<JSONObject> resultList = new ArrayList<JSONObject>();

			for (int i = 0; i < resultsArray.length(); i++) {
				JSONObject jsonTweet = resultsArray.getJSONObject(i);
				resultList.add(jsonTweet);
			}
			setListAdapter(resultList);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void setListAdapter(ArrayList<JSONObject> resultList) {
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(getArrayAdapter(resultList));
	}

	private ListAdapter getArrayAdapter(ArrayList<JSONObject> tweets) {
		return new ArrayAdapter<JSONObject>(getApplicationContext(), R.layout.tweet_preview, tweets) {
			public View getView(int position, View v, ViewGroup parent) {
				if (v == null) {
					LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.tweet_preview, null);
				}

				JSONObject tweet = getItem(position);

				TextView userView = (TextView) v.findViewById(R.id.tweet_user);

				try {
					String userString = tweet.getString("from_user");
					userView.setText(userString);
				} catch (JSONException e) {
					userView.setText("NO_USERNAME");
				}

				TextView tweetView = (TextView) v.findViewById(R.id.tweet);

				try {
					String tweetString = tweet.getString("text");
					tweetView.setText(tweetString);
				} catch (JSONException e) {
					tweetView.setText("NO_TWEET");
				}

				v.setOnClickListener(getOnClickListener(tweet));

				return v;
			}
		};
	}

	protected OnClickListener getOnClickListener(JSONObject tweet) {
		final String tweetString = tweet.toString();
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent twitterProfileActivity = new Intent(getApplicationContext(), TwitterProfileActivity.class);
				String extraName = getString(R.string.tweet_extra_name);
				twitterProfileActivity.putExtra(extraName, tweetString);
				startActivity(twitterProfileActivity);
			}
		};
	}

	public AsyncTask<String, String, String> getAsyncTask() {
		return new AsyncTask<String, String, String>() {
			@Override
			protected void onPreExecute() {
				String searchingForTweetsTitle = getString(R.string.searching_for_tweets_title);
				String searchingForTweets = getString(R.string.searching_for_tweets);
				progressDialog = ProgressDialog.show(TwitterSearchResultsActivity.this, searchingForTweetsTitle, searchingForTweets);
			}

			@Override
			protected String doInBackground(String... params) {
				String tweets = getTweets(params[0]);
				return tweets;
			}

			private String getTweets(String string) {
				String url = "http://search.twitter.com/search.json?q=" + string.trim();
				HttpParams httpParameters = new BasicHttpParams();
				HttpGet httpRequest = new HttpGet(url);

				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

				httpClient.getConnectionManager().closeExpiredConnections();
				try {
					HttpResponse response = httpClient.execute(httpRequest);
					String result = EntityUtils.toString(response.getEntity());
					return result;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					httpClient.getConnectionManager().closeExpiredConnections();
				}
				return null;
			}

			protected void onPostExecute(String result) {
				parseResults(result);
				progressDialog.dismiss();
				progressDialog = null;
			};
		};
	}
}
