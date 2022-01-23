package vn.tiki.tiniapp.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SQLiteResourceRepo {
    private static final String TAG = "Tini.SQLiteResourceRepo";
    private static final String TABLE_NAME = "Resource";
    private static final String COLUMN_RESOURCE_ID = "resourceID";
    private static final String COLUMN_RESOURCE_SHA1 = "resourceSha1";
    private static final String COLUMN_RESOURCE_SIZE = "resourceSize";
    private static final String COLUMN_LAST_UPDATE_TIME = "resourceUpdateTime";
    private static final String COLUMN_CACHE_EXPIRED_TIME = "cacheExpiredTime";

    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
            "id  integer PRIMARY KEY autoincrement" +
            " , " + COLUMN_RESOURCE_ID + " text not null" +
            " , " + COLUMN_RESOURCE_SHA1 + " text not null" +
            " , " + COLUMN_RESOURCE_SIZE + " integer default 0" +
            " , " + COLUMN_LAST_UPDATE_TIME + " integer default 0" +
            " , " + COLUMN_CACHE_EXPIRED_TIME + " integer default 0" +
            " ); ";

    /**
     * Get sonic ResourceData by unique resource id
     *
     * @param resourceId a unique resource id
     * @return ResourceData
     */
    @NonNull
    public static ResourceData getResourceData(String resourceId) {
        SQLiteDatabase db = SQLiteRepo.getInstance().getWritableDatabase();
        ResourceData resourceData = getResourceData(db, resourceId);
        if (null == resourceData) {
            resourceData = new ResourceData();
        }
        return resourceData;
    }

    /**
     * Get sonic resourceData by unique resource id
     *
     * @param db The database.
     * @param resourceId a unique resource id
     * @return ResourceData
     */
    private static ResourceData getResourceData(SQLiteDatabase db, String resourceId) {
        Cursor cursor = db.query(TABLE_NAME,
                getAllResourceDataColumn(),
                COLUMN_RESOURCE_ID + "=?",
                new String[] {resourceId},
                null, null, null);

        ResourceData resourceData = null;
        if (cursor != null && cursor.moveToFirst()) {
            resourceData = queryResourceData(cursor);
        }
        if(cursor != null){
            cursor.close();
        }
        return resourceData;
    }

    private static String[] getAllResourceDataColumn() {
        return new String[]{
                COLUMN_RESOURCE_ID,
                COLUMN_RESOURCE_SHA1,
                COLUMN_RESOURCE_SIZE,
                COLUMN_LAST_UPDATE_TIME,
                COLUMN_CACHE_EXPIRED_TIME
        };
    }

    /**
     * translate cursor to resource data.
     * @param cursor db cursor
     */
    private static ResourceData queryResourceData(Cursor cursor) {
        ResourceData resourceData = new ResourceData();
        resourceData.resourceId = cursor.getString(cursor.getColumnIndex(COLUMN_RESOURCE_ID));
        resourceData.resourceSha1 = cursor.getString(cursor.getColumnIndex(COLUMN_RESOURCE_SHA1));
        resourceData.resourceSize = cursor.getLong(cursor.getColumnIndex(COLUMN_RESOURCE_SIZE));
        resourceData.lastUpdateTime = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_UPDATE_TIME));
        resourceData.expiredTime = cursor.getLong(cursor.getColumnIndex(COLUMN_CACHE_EXPIRED_TIME));
        return resourceData;
    }

    /**
     * Save or update sonic resourceData with a unique resource id
     *
     * @param resourceId   a unique resource id
     * @param resourceData ResourceData
     */
    static void saveResourceData(String resourceId, ResourceData resourceData) {
        SQLiteDatabase db = SQLiteRepo.getInstance().getWritableDatabase();
        saveResourceData(db, resourceId, resourceData);
    }

    /**
     * Save or update sonic resourceData with a unique resource id
     *
     * @param db The database.
     * @param resourceId   a unique resource id
     * @param resourceData ResourceData
     */
    private static void saveResourceData(SQLiteDatabase db, String resourceId, ResourceData resourceData) {
        resourceData.resourceId = resourceId;
        ResourceData storedResourceData = getResourceData(db, resourceId);
        if (storedResourceData != null) {
            updateResourceData(db, resourceId, resourceData);
        } else {
            insertResourceData(db, resourceId, resourceData);
        }
    }

    private static void insertResourceData(SQLiteDatabase db, String resourceId, ResourceData resourceData) {
        ContentValues contentValues = getContentValues(resourceId, resourceData);
        db.insert(TABLE_NAME, null, contentValues);
    }

    private static void updateResourceData(SQLiteDatabase db, String resourceId, ResourceData resourceData) {
        ContentValues contentValues = getContentValues(resourceId, resourceData);
        db.update(TABLE_NAME, contentValues, COLUMN_RESOURCE_ID + "=?",
                new String[] {resourceId});
    }

    static List<ResourceData> getAllResourceData() {
        List<ResourceData> resourceDataList = new ArrayList<ResourceData>();
        SQLiteDatabase db = SQLiteRepo.getInstance().getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, getAllResourceDataColumn(),
                null,null,null, null, "");
        while(cursor != null && cursor.moveToNext()) {
            resourceDataList.add(queryResourceData(cursor));
        }
        return resourceDataList;
    }

    @NonNull
    private static ContentValues getContentValues(String resourceId, ResourceData resourceData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RESOURCE_ID, resourceId);
        contentValues.put(COLUMN_RESOURCE_SHA1, resourceData.resourceSha1);
        contentValues.put(COLUMN_RESOURCE_SIZE, resourceData.resourceSize);
        contentValues.put(COLUMN_LAST_UPDATE_TIME, resourceData.lastUpdateTime);
        contentValues.put(COLUMN_CACHE_EXPIRED_TIME, resourceData.expiredTime);
        return contentValues;
    }


    /**
     * Remove a unique resource data
     *
     * @param resourceId A unique resource id
     */
    static void removeResourceData(String resourceId) {
        SQLiteDatabase db = SQLiteRepo.getInstance().getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_RESOURCE_ID + "=?",
                new String[] {resourceId});
    }

    /**
     * Remove all sonic data
     */
    static synchronized void clear() {
        SQLiteDatabase db = SQLiteRepo.getInstance().getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
}
