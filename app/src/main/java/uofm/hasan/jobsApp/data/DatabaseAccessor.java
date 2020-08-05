package uofm.hasan.jobsApp.data;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

public class DatabaseAccessor {
    private SQLiteDatabase jobsDatabase; // creating a jobs Database

    public DatabaseAccessor(Activity activity) {
        jobsDatabase = activity.openOrCreateDatabase("Jobs", android.content.Context.MODE_PRIVATE, null);
        jobsDatabase.execSQL("CREATE TABLE IF NOT EXISTS jobs (id INTEGER PRIMARY KEY, jobId INTEGER, title VARCHAR, content VARCHAR)");
    }

    public Cursor selectAllJobs() {
        return jobsDatabase.rawQuery("SELECT * FROM jobs", null);
    }

    public SQLiteStatement compile(String s) {
        return jobsDatabase.compileStatement(s);
    }

    public void delete() {
        jobsDatabase.execSQL("DELETE FROM jobs");
    }


}
