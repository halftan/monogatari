//package com.azusasoft.monogatari;
//
//import org.uniid.android.client.Client;
//import org.uniid.android.client.callbacks.ApiResponseCallback;
//
//import android.app.Activity;
//import android.content.pm.ActivityInfo;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class UniidTestActivity extends Activity {
//
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		Client client = new Client();
//		client.authorizeAppClientAsync(clientId, clientSecret, callback)
//
//		setContentView(R.layout.main);
//
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		
//		TextView result = (TextView) findViewById(R.id.scanText);
//		Button testButton = (Button) findViewById(R.id.scanButton);
//		
//		testButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//	}
//
//}