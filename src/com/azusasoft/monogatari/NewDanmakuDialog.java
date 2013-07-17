package com.azusasoft.monogatari;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.TextView;

import com.azusasoft.monogatari.CameraTestActivity.DanmakuHandler;

public class NewDanmakuDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		// Inflate and set the view
		View view = inflater.inflate(R.layout.danmaku_dialog, null);
		TextView text = (TextView) view.findViewById(R.id.title);
		text.setText(getArguments().getString("Debug"));
		builder.setView(view)
			.setPositiveButton(R.string.confirm, new OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Message msg = new Message();
					Bundle data = new Bundle();
					msg.what = DanmakuHandler.NEW_AND_POST_DANMAKU;
					data.putString(DanmakuHandler.DANMAKU_TEXT_KEY,
							((EditText) NewDanmakuDialog.this.getDialog()
									.findViewById(R.id.input)).getText().toString());
					msg.setData(data);
					((CameraTestActivity) getActivity()).danmakuHandler
						.sendMessage(msg);
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
}
