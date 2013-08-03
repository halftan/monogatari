package com.azusasoft.monogatari;

import java.util.Date;

import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.azusasoft.monogatari.data.HistoryAdapter;
import com.azusasoft.monogatari.data.HistoryDbHelper;

public class HistoryActivity extends ListActivity {
	private static final String TAG = "history activity";
	
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
		mCursor = mDbHelper.getAllHistory();
		mAdapter = new SimpleCursorAdapter(this, R.layout.history_row, mCursor,
				HistoryAdapter.FROM, HistoryAdapter.TO, 0);
		mAdapter.setViewBinder(HistoryAdapter.getViewBinder());
		setListAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String db_id = ((TextView) view.findViewById(R.id.db_id)).getText().toString();
				Log.d(TAG, "Start to find photo by id " + db_id);
				Bitmap bm = mDbHelper.getPhotoById(db_id);
				if (bm != null) {
					mImageView.setImageBitmap(bm);
					mFrame.bringChildToFront(mImageView);
					mImageView.setVisibility(View.VISIBLE);
				} else {
					Toast.makeText(HistoryActivity.this, R.string.no_result, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	/**
	 * fired when mImageView is clicked
	 */
	public void hideImage(View v) {
		v.setVisibility(View.INVISIBLE);
		mFrame.bringChildToFront(mListView);
	}

	@Override
	public void onStop() {
		super.onStop();
		mCursor.close();
		mCursor = null;
		mDbHelper.close();
		mDbHelper = null;
		mAdapter = null;
		setListAdapter(null);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (mDbHelper == null) {
			mDbHelper = new HistoryDbHelper(this);
			mCursor = mDbHelper.getAllHistory();
		}
		if (mCursor == null || mCursor.isClosed()) {
			mCursor = mDbHelper.getAllHistory();
		}
		mAdapter = new SimpleCursorAdapter(this, R.layout.history_row, mCursor,
				HistoryAdapter.FROM, HistoryAdapter.TO, 0);
		mAdapter.setViewBinder(HistoryAdapter.getViewBinder());
		setListAdapter(mAdapter);
	}
	
}
