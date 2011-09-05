package android.cohort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterSearchResultsWithImagesActivity extends Activity {

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
		ListView x = (ListView) findViewById(R.id.listView);
		x.setAdapter(getArrayAdapter(resultList));
	}

	private ListAdapter getArrayAdapter(ArrayList<JSONObject> tweets) {
		return new ArrayAdapter<JSONObject>(getApplicationContext(), R.layout.tweet_preview_with_image, tweets) {
			public View getView(int position, View v, ViewGroup parent) {
				if (v == null) {
					LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.tweet_preview_with_image, null);
				}

				JSONObject tweet = getItem(position);

				try {
					final String imageUrl = tweet.getString("profile_image_url");
					final ImageView image = (ImageView) v.findViewById(R.id.tweet_user);
					
					if (image.getTag() != null && image.getTag().equals(imageUrl))
						return v;
					
					image.setImageBitmap(null);
					image.setTag(imageUrl);
					(new AsyncTask<String, String, Bitmap>() {
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
							
							
							if (!image.getTag().equals(imageUrl))
								return;
							
							image.setImageBitmap(result);
							super.onPostExecute(result);
						}
					}).execute(imageUrl);
				} catch (JSONException e) {
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
				progressDialog = ProgressDialog.show(TwitterSearchResultsWithImagesActivity.this, searchingForTweetsTitle, searchingForTweets);
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
