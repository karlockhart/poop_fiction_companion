package co.zer0.poopfictioncompanion;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PoopCompanion extends AppCompatActivity {
    private String storyContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poop_companion);

        // Broadcast receiver to get story content.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                storyReceiver, new IntentFilter("storyUpdated"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poop_companion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getTinyStory(View v) {
        this.fetchStory(getString(R.string.tiny_story_url));
    }

    public void getShortStory(View v) {
        this.fetchStory(getString(R.string.short_story_url));
    }

    public void getMediumStory(View v) {
        this.fetchStory(getString(R.string.meduium_story_url));
    }

    public void getLongStory(View v) {
        this.fetchStory(getString(R.string.long_story_url));
    }

    public void fetchStory(String source) {
        try {
            StoryFetcher fetcher = new StoryFetcher();
            fetcher.setContext(this);
            fetcher.execute(new URL(source));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver storyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView story = (TextView) findViewById(R.id.textView);
            story.setText(Html.fromHtml(intent.getStringExtra("content")));
        }
    };

    private class StoryFetcher extends AsyncTask<URL, Long, String> {

        private Context context;
        private ProgressDialog progressDialog;

        public void setContext(Context context) {
            // Set the calling activity context, not the cleanest way but reasonable.
            this.context = context;
        }

        @Override
        protected String doInBackground(URL... params) {
            for (URL param : params) {
                try {
                    Document doc = Jsoup.connect(param.toString()).get();
                    Elements content = doc.getElementsByClass("content");
                    sendStoryUpdatedMessageToActivity(content.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;

                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Show the progress dialog while the download is in progress.
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setMessage("Finding you a story ...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Done fetching, get rid of the dialog.
            progressDialog.dismiss();
        }

        private void sendStoryUpdatedMessageToActivity(String story){
            // Remove links.
            story = Jsoup.clean(story, Whitelist.relaxed().removeTags("a"));

            // Add attribution.
            story = story + "<p>Stories courtesy of gutenberg.org via readpoopfiction.com</p>";

            // Create an intent to transfer the story contents.
            Intent intent = new Intent("storyUpdated");
            intent.putExtra("content", story);

            // Send the message.
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        }
    }
}