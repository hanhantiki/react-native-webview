package vn.tiki.tiniapp.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import vn.tiki.tiniapp.TNEngine;

public class SQLiteRepo extends SQLiteOpenHelper {

    private static final String TAG = "Tini.SQLiteRepo";
    private static final String DATABASE_NAME = "tini.db";
    private static final int DATABASE_VERSION = 1;
    private static SQLiteRepo instance;
    private static AtomicBoolean isDBUpgrading = new AtomicBoolean(false);

    private SQLiteRepo(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SQLiteRepo createInstance(Context context) {
        if (null == instance) {
            instance = new SQLiteRepo(context);
        }
        return instance;
    }

    public static synchronized SQLiteRepo getInstance() {
        if (null == instance) {
            throw new IllegalStateException("SQLiteRepo::createInstance() needs to be called before SQLiteRepo::getInstance()!");
        }
        return instance;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLiteResourceRepo.CREATE_TABLE_SQL);
        // upgrade database if need
        onUpgrade(db, -1, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (isDBUpgrading.compareAndSet(false, true)) {
            long startTime = System.currentTimeMillis();
            Log.i(TAG,  "onUpgrade start, from " + oldVersion + " to " + newVersion + ".");
            if (-1 == oldVersion) {
                TNEngine.getInstance().postTask(new Runnable() {
                    @Override
                    public void run() {
                        isDBUpgrading.set(false);
                    }
                });
            } else {
                doUpgrade(db, oldVersion, newVersion);
                isDBUpgrading.set(false);
            }
            Log.i(TAG, "onUpgrade finish, cost " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    private void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // in future, if we want to change sqlite
        // we will upgrade in here
    }
}
