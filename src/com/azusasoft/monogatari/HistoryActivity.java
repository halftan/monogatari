package com.azusasoft.monogatari;

import java.util.Date;

import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.azusasoft.monogatari.data.HistoryAdapter;
import com.azusasoft.monogatari.data.HistoryDbHelper;

public class HistoryActivity extends ListActivity {
	
	private FrameLayout mFrame;
	private ImageView mImageView;
	private ListView mListView;
	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	private HistoryDbHelper mDbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_activity);
		mFrame = (FrameLayout) findViewById(R.id.main_frame);
		mImageView = (ImageView) findViewById(R.id.image_preview);
		mListView = (ListView) findViewById(android.R.id.list);
		mDbHelper = new HistoryDbHelper(this);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String db_id = ((TextView) view.findViewById(R.id.db_id)).getText().toString();
				Bitmap bm = mDbHelper.getPhotoById(db_id);
				if (bm != null) {
					mImageView.setImageBitmap(bm);
					mFrame.bringChildToFront(mImageView);
					mImageView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	/**
	 * fired when mImageView is clicked
	 */
	public void hideImage(View v) {
		v.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onPause() {
		super.onPause();
		mCursor.close();
		mDbHelper.close();
		mDbHelper = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDbHelper == null)
			mDbHelper = new HistoryDbHelper(this);
		mCursor = mDbHelper.getAllHistory();
		mAdapter = new SimpleCursorAdapter(this, R.layout.history_row, mCursor,
				HistoryAdapter.FROM, HistoryAdapter.TO, 0);
		mAdapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (view.getId() != R.id.date_text)
					return false;
				
				Date time = new Date(cursor.getLong(columnIndex));
				((TextView) view).setText(
						DateFormat.getDateFormat(HistoryActivity.this).format(time));
				return true;
			}
		});
		setListAdapter(mAdapter);
	}
	
}
