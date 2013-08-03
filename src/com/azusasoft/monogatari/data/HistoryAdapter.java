package com.azusasoft.monogatari.data;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.azusasoft.monogatari.R;

public class HistoryAdapter {
	
	private static final String TAG = "history adapter";
	
	public static final String[] FROM = {
		HistoryDbHelper.C_ID, HistoryDbHelper.C_BARCODE, HistoryDbHelper.C_CREATED_AT
	};
	public static final int[] TO = {
		 R.id.db_id, R.id.barcode_text, R.id.date_text
	};
	
	public static final ViewBinder getViewBinder() {
		return new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (view.getId() != R.id.date_text)
					return false;
				
				((TextView) view).setText(
						DateFormat.format("yyyy-MM-dd hh:mm:ss", cursor.getLong(columnIndex)));
				Log.i(TAG, DateFormat.format("yyyy-MM-dd hh:mm:ss", cursor.getLong(columnIndex)).toString());
				return true;
			}
		};
	}

}
