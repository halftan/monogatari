package com.azusasoft.monogatari.data;

import java.text.ParseException;
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
		HistoryDbHelper.C_BARCODE, HistoryDbHelper.C_CREATED_AT, HistoryDbHelper.C_ID
	};
	public static final int[] TO = {
		R.id.barcode_text, R.id.date_text, R.id.db_id
	};
	
	public static final ViewBinder getViewBinder(final Context context) {
		return new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (view.getId() != R.id.date_text)
					return false;
				
				Date time = new Date(cursor.getLong(columnIndex));
				((TextView) view).setText(
						DateFormat.getDateFormat(context).format(time));
				Log.i(TAG, DateFormat.getDateFormat(context).format(time));
				return true;
			}
		};
	}

}
