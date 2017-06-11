package saleem.elec3607_pong;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by saleem on 20/05/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "matchHistoryDatabase"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    private String databaseCreateSql;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        /*
        * create table matches (
            id integer primary key autoincrement,
            p1_score integer,
            p2_score integer,
            date text
        );
        * */
        databaseCreateSql = context.getResources().getString(R.string.database_create);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // first time the app has run, so create the database
        db.execSQL(databaseCreateSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
