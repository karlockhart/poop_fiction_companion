package co.zer0.poopfictioncompanion;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PoopCompanion extends AppCompatActivity {
    private String storyContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poop_companion);

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
            fetcher.loadApplication(this);
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
        private ProgressDialog waitSpinner;

        public void loadApplication(Context context) {
            this.context = context;
            waitSpinner = new ProgressDialog(this.context);
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
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            // Only purpose of this method is to show our wait spinner, we dont
            // (and can't) show detailed progress updates
            waitSpinner = ProgressDialog.show(context, "Please Wait ...", "Initializing the application ...", true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            waitSpinner.cancel();
        }

        private void sendStoryUpdatedMessageToActivity(String story){
            Intent intent = new Intent("storyUpdated");
            intent.putExtra("content", story);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        }
    }
}