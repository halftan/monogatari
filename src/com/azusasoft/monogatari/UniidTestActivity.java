package com.azusasoft.monogatari;
/*package com.azusasoft.tsukomiyi;

import org.uniid.android.client.Client;
import org.uniid.android.client.callbacks.ApiResponseCallback;
import org.uniid.android.client.response.ApiResponse;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UniidTestActivity extends Activity
	implements ApiResponseCallback {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Client client = new Client("47a728fa-cd19-11e2-9b2c-755d3438c100",
				"965c3d00-cd19-11e2-80a2-21ce29bddf7b");
		client.authorizeAppClientAsync("YXA6llw9AM0ZEeKAoiHOKb3few",
				"YXA6vMRTQWVGSzcN434hf82lK-5ZxOE", this);

		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		TextView result = (TextView) findViewById(R.id.scanText);
		Button testButton = (Button) findViewById(R.id.scanButton);
		
		testButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
	}

	@Override
	public void onException(Exception e) {
		Log.e("UniId", e.getMessage());
	}

	@Override
	public void onResponse(org.usergrid.java.client.response.ApiResponse arg0) {
		// TODO Auto-generated method stub
		
	}

}
*/