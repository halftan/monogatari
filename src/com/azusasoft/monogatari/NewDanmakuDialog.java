package com.azusasoft.monogatari;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.azusasoft.monogatari.controller.DanmakuController;

public class NewDanmakuDialog extends DialogFragment {
	public static final String SCANNED_BARCODE_KEY = "barcode key";

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
					try {
						DanmakuController.getInstance().newDanmaku(
							((EditText) NewDanmakuDialog.this.getDialog()
									.findViewById(R.id.input)).getText().toString());
					} catch (RuntimeException e) {
						Log.e("new danmaku dialog", e.getMessage());
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
}
