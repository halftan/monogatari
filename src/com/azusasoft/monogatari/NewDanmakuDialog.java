package com.azusasoft.monogatari;

import org.springframework.util.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.azusasoft.monogatari.controller.DanmakuController;

public class NewDanmakuDialog extends DialogFragment {
	public static final String TAG = "danmaku dialog";
	public static final String SCANNED_BARCODE_KEY = "barcode.dialog";
	public static final String MESSAGE_KEY = "message.dialog";
	
	private static final int INPUT_INDEX = 0;
	private static final int BUTTON_INDEX = 1;
	
	private static boolean mIsPreview = false;
	private static String mJoinedDanmakuText = null;

	private ViewGroup mDanmakuGumi;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		// Inflate and set the view
		View view = inflater.inflate(R.layout.danmaku_dialog, null);
		TextView text = (TextView) view.findViewById(R.id.title);
		String title = getArguments().getString(MESSAGE_KEY);
		if (title != null) {
			text.setText(title);
		}
		
		View previewCheck = view.findViewById(R.id.is_preview);
		((Checkable) previewCheck).setChecked(mIsPreview);
		previewCheck.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean checked = ((CheckedTextView) v).isChecked();
				Log.d(TAG, "Checked state: " + (checked ? "False" : "True"));
				((CheckedTextView) v).toggle();
				mIsPreview = !checked;
			}
		});
		
		mDanmakuGumi = (ViewGroup) view.findViewById(R.id.danmaku_gumi);
		
		View newGumiBtn = view.findViewById(R.id.new_danmaku_gumi);
		newGumiBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				newDanmakuGumiInput();
			}
		});
		
		builder.setView(view)
			.setPositiveButton(R.string.confirm, new OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						DanmakuController.getInstance().newDanmaku(
								getJoinedDanmakuGumi(), !mIsPreview);
					} catch (RuntimeException e) {
						Log.e("new danmaku dialog", "Unable to send new danmaku", e);
					}
				}
			})
			.setNegativeButton(R.string.cancel, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					NewDanmakuDialog.this.getDialog().cancel();
				}
			});
		
		return builder.create();
	}
	
	private String getJoinedDanmakuGumi() {
		StringBuilder compound = new StringBuilder();
		for (int i = 0; i < GetDanmakuGumi().getChildCount(); ++i) {
			ViewGroup container = (ViewGroup) GetDanmakuGumi().getChildAt(i);
			EditText child = (EditText) container.getChildAt(INPUT_INDEX);
			compound.append(child.getText());
			compound.append('\n');
		}
		mJoinedDanmakuText = compound.toString();
		mJoinedDanmakuText = StringUtils.trimTrailingCharacter(mJoinedDanmakuText, '\n');
		return mJoinedDanmakuText;
	}
	
	private void newDanmakuGumiInput() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.danmaku_gumi_input, GetDanmakuGumi(), false);
		Log.d("danmaku", "Child count is " + view.getChildCount());
		view.getChildAt(BUTTON_INDEX).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("delete danmaku", "Detached");
				NewDanmakuDialog.this.GetDanmakuGumi().removeView((View) v.getParent());
			}
		});
		GetDanmakuGumi().addView(view);
	}
	
	private ViewGroup GetDanmakuGumi() {
		if (mDanmakuGumi == null)
			mDanmakuGumi = (ViewGroup) getView().findViewById(R.id.danmaku_gumi);
		return mDanmakuGumi;
	}
	
	private class DeleteDanmakuGumiListener implements View.OnClickListener{
		private final int mId;
		
		DeleteDanmakuGumiListener(int id) {
			mId = id;
		}

		@Override
		public void onClick(View v) {
			Log.d("delete danmaku", "Fired " + mId);
			NewDanmakuDialog.this.GetDanmakuGumi().removeView((View) v.getParent());
		}
	}
	
}
