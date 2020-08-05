package uofm.hasan.jobsApp.presentation;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import uofm.hasan.jobsApp.data.DownloadTask;
import uofm.hasan.jobsApp.R;
import uofm.hasan.jobsApp.data.DatabaseAccessor;


public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    DatabaseAccessor jobsDatabase; // creating a jobs Database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jobsDatabase = new DatabaseAccessor(this);
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

        DownloadTask task = new DownloadTask(jobsDatabase, titles, content, arrayAdapter);
        task.updateView();
        try {
            task.execute(" https://hacker-news.firebaseio.com/v0/jobstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
