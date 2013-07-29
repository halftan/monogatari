package com.azusasoft.monogatari.data;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera.Size;
import android.provider.BaseColumns;
import android.util.Log;

public class HistoryDbHelper {
	private static final String TAG = "scan history";
	
	static final String DB_NAME = "scan_history.db";
	static final int DB_VERSION = 1;
	
	public static final String TABLE = "event";
	
	public static final String C_ID = BaseColumns._ID; 
	public static final String C_CREATED_AT = "created_at";
	public static final String C_BARCODE = "barcode";
	public static final String C_PHOTO = "photo";
	
	public static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";
	
	public static final String[] DB_HISTORY_COLUMNS = {
		C_CREATED_AT, C_BARCODE, C_PHOTO
	};

	public static final String[] DB_NO_PHOTO_COLUMNS = {
		C_ID, C_CREATED_AT, C_BARCODE 
	};
	
	public static final String[] DB_PHOTO_COLUMN = {
		C_PHOTO 
	};
	
	public static class DbHelper extends SQLiteOpenHelper {
		static final String TAG = "DbHelper";
		Context mContext;

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String.format("create table %s (" +
					"%s INTEGER PRIMARY KEY AUTOINCREMENT, %s int, " +
					"%s varchar(255), %s BLOB)", TABLE, C_ID, C_CREATED_AT,
					C_BARCODE, C_PHOTO);
			
			db.execSQL(sql);
			Log.d(TAG, "onCreated sql: " + sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists" + TABLE);
			Log.d(TAG, "onUpgraded");
			onCreate(db);
		}
	}
	
	private final DbHelper mDbHelper;

	/**
	 * Constructor of ScanHistory
	 */
	public HistoryDbHelper(Context context) {
		mDbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized Data.");
	}
	
	public void close() {
		mDbHelper.close();
	}

	public Cursor getAllHistory() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return db.query(TABLE, DB_NO_PHOTO_COLUMNS, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public Bitmap getPhotoById(String id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, DB_PHOTO_COLUMN, C_ID + " = ?",
				new String[] { id }, null, null, null);
		cursor.moveToFirst();
		byte[] data = cursor.getBlob(cursor.getColumnIndex(C_PHOTO));
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		if (data != null)
			return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		else
			return null;
	}

	public void insertBarcodeAndJpeg(String barcode, byte[] data) {
		ContentValues values = new ContentValues();
		values.put(C_BARCODE, barcode);
		values.put(C_PHOTO, data);
		Date date = new Date();
		values.put(C_CREATED_AT, String.valueOf(date.getTime()));
		insertOrIgnore(values);
		Log.d(TAG, "saved jpeg");
	}
	
	public void insertBarcodeAndPhoto(final String barcode, final byte[] photo, final Size size) {
		new Runnable() {
			
			@Override
			public void run() {
				ContentValues values = new ContentValues();
				byte[] data = new byte[photo.length * 4];
				for (int i = 0; i < photo.length; ++i) {
					data[i*4] = data[i*4 + 1] = data[i*4 + 2] = (byte) ~(photo[i]);
					data[i*4 + 3] = -1;
				}
				Bitmap bm = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
				bm.copyPixelsFromBuffer(ByteBuffer.wrap(data));
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				bm.compress(CompressFormat.JPEG, 80, os);
				
				values.put(C_BARCODE, barcode);
				values.put(C_PHOTO, os.toByteArray());
				Date date = new Date();
				values.put(C_CREATED_AT, String.valueOf(date.getTime()));
				insertOrIgnore(values);
				
			}
		}.run();
	}
	
	public void insertOrIgnore(ContentValues values) {
		Log.d(TAG, "insertOrIgnore on " + values.toString());
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
}
