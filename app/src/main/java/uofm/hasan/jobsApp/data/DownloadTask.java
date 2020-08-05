package uofm.hasan.jobsApp.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

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

import uofm.hasan.jobsApp.data.DatabaseAccessor;

public class DownloadTask extends AsyncTask<String, Void, String> {// This method represents downloading of the content
    //USES HACKERNEWS JOBS POSTING TO DOWNLOAD IT AND SAVE IT IN OUT JOBS DATABASE
    DatabaseAccessor jobsDatabase;
    ArrayList<String> titles;
    ArrayList<String> content;
    ArrayAdapter arrayAdapter;

    public DownloadTask(DatabaseAccessor jobsDatabase, ArrayList<String> titles, ArrayList<String> content, ArrayAdapter arrayAdapter) {
        this.jobsDatabase = jobsDatabase;
        this.titles = titles;
        this.content = content;
        this.arrayAdapter = arrayAdapter;
    }

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
            jobsDatabase.delete();
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
                    SQLiteStatement statement = jobsDatabase.compile(sql);
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

    public void updateView() {
        Cursor c = jobsDatabase.selectAllJobs();
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