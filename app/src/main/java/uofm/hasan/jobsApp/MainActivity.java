package uofm.hasan.jobsApp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    SQLiteDatabase jobsDb; // creating a jobs Database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { //changing color of the actionBar to provide an interactive view
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.mygradient));

        }
        ListView listView = (ListView) findViewById(R.id.listView); //Listview to display jobs in a list form

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // Whenever a particular job posting is clicked

                Intent intent = new Intent(getApplicationContext(), JobsActivity.class);
                intent.putExtra("content", content.get(i));

                startActivity(intent);
            }
        });

        jobsDb = this.openOrCreateDatabase("Jobs", MODE_PRIVATE, null);

        jobsDb.execSQL("CREATE TABLE IF NOT EXISTS jobs (id INTEGER PRIMARY KEY, jobId INTEGER, title VARCHAR, content VARCHAR)");

        updateView();

        DownloadTask task = new DownloadTask();

        try {
            task.execute(" https://hacker-news.firebaseio.com/v0/jobstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {// This method represents downloading of the content
        //USES HACKERNEWS JOBS POSTING TO DOWNLOAD IT AND SAVE IT IN OUT JOBS DATABASE

        @Override
        protected String doInBackground(String... strings) {

            String result = "";

            URL url;

            HttpURLConnection urlConnection = null;

            try {

                url = new URL(strings[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }


                JSONArray jsonJobs = new JSONArray(result);

                int maxItems = 30; //max of 30 items

                if (jsonJobs.length() < maxItems) {

                    maxItems = jsonJobs.length();

                }

                jobsDb.execSQL("DELETE FROM jobs");

                for (int i = 0; i < maxItems; i++) {

                    String jobId = jsonJobs.getString(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + jobId + ".json?print=pretty");

                    urlConnection = (HttpURLConnection) url.openConnection();

                    in = urlConnection.getInputStream();

                    reader = new InputStreamReader(in);

                    data = reader.read();

                    String jsonJobInfo = "";

                    while (data != -1) {
                        char current = (char) data;

                        jsonJobInfo += current;

                        data = reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(jsonJobInfo);

                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String jobUrl = jsonObject.getString("url");

                        String jobTitle = jsonObject.getString("title");


                        String sql = "INSERT INTO jobs (jobId, title, content) VALUES (?, ?, ?)"; //Inserting into JobsDatabase

                        SQLiteStatement statement = jobsDb.compileStatement(sql);

                        statement.bindString(1, jobId);
                        statement.bindString(2, jobTitle);
                        statement.bindString(3, jobUrl);

                        statement.execute();

                    }

                }

            } catch (MalformedURLException e) {  // Catching Exceptions
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateView();
        }
    }

    public void updateView() {
        Cursor c = jobsDb.rawQuery("SELECT * FROM jobs", null);

        int contentIndex = c.getColumnIndex("content"); // navigating to the content and title
        int titleIndex = c.getColumnIndex("title");

        if (c.moveToFirst()) {
            titles.clear();
            content.clear();

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));
            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged(); //notifying
        }
    }

}
