package com.azusasoft.monogatari.controller;

import java.util.ArrayList;

import org.springframework.http.HttpMethod;
import org.uniid.android.client.Client;
import org.uniid.android.client.callbacks.ApiResponseCallback;
import org.uniid.java.client.entities.Entity;
import org.uniid.java.client.entities.User;
import org.uniid.java.client.response.ApiResponse;
import org.uniid.java.client.utils.JsonUtils;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.azusasoft.monogatari.CameraTestActivity.DanmakuHandler;

public class MessageController {

	private String UNIID_API_URL = "http://54.248.89.253:8080";
	private String UNIID_ORG = "azusasoft";
	private String UNIID_APP = "monogatari";
	
	private Client mClient = null;
	private String email;
	private String username;
	
	private DanmakuHandler mHandler;
	private ArrayList<String> mDanmakuList;

	// mono refers to 物 in Japanese
	private String mMono;
	
	public interface onNoDanmakuListener {
		public void pushFirstDanmaku(String mono);
	}
	
	private onNoDanmakuListener mNoDanmakuListener;
	
	public MessageController(DanmakuHandler handler, onNoDanmakuListener listener) {
		mHandler = handler;
		mNoDanmakuListener = listener;
		mDanmakuList = new ArrayList<String>();
		mClient = new Client();
		mClient.setApiUrl(UNIID_API_URL);
		mClient.setOrganizationId(UNIID_ORG);
		mClient.setApplicationId(UNIID_APP);
		mClient.setClientSecret("YXA6sAc9Unk_ByF4HYHU9U1wh1hk8LI");
		mClient.setClientId("YXA6vPQQEOhSEeKKVnuBn11m3g");
		mClient.setAccessToken("YWMtp8ZpgPEdEeKzG4loRmbDpQAAAUAfbhwYU7sPQTmqQwDRtA7hk3i4IAJes6Q");
	}
	
	public String getTargetBarcode() {
		return mMono;
	}

	public void searchDanmakuFor(String code) {
		mMono = code;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ApiResponse response = null;
				try {
					response = mClient.apiRequest(HttpMethod.GET, null, null, UNIID_ORG+"/"+UNIID_APP
							, "monos", mMono, "owns");
				} catch (Exception e) {
					Log.e("Danmaku uniid request", e.toString());
				}
				ArrayList<String> danmakuList = new ArrayList<String>();
				if (response != null && response.getFirstEntity() != null &&
						response.getEntityCount() != 0) {
					for (Entity entity : response.getEntities()) {
						danmakuList.add(entity.getProperties().get("text").textValue());
					}
					if (danmakuList.size() != 0) {
						Log.i("danmaku displaying", "Danmaku size is " + danmakuList.size());
						Message msg = new Message();
						Bundle data = new Bundle();
						msg.what = DanmakuHandler.DISPLAY_DANMAKU;
						data.putStringArrayList(DanmakuHandler.DANMAKU_TEXT_LIST_KEY, danmakuList);
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
				} else {
					Log.i("danmaku mono entity", "creating new mono: " + mMono);
					Entity mono = new Entity("monos");
					mono.setProperty("name", JsonUtils.toJsonNode(mMono));
					Log.i("danmaku mono entity", "created jsonnode");
					mClient.createEntity(mono);
					mNoDanmakuListener.pushFirstDanmaku(mMono);
				}
				
			}
		}, "search danmaku thread").start();
	}
	
	public void pushDanmaku(String danmaku) {
		Entity danmakus = new Entity("danmakus");
		danmakus.setProperty("text", JsonUtils.toJsonNode(danmaku));
		mClient.createEntityAsync(danmakus, new ApiResponseCallback() {
	        @Override
	        public void onException(Exception ex) {
	                Log.i("danmaku entity", ex.toString());
	        }
	        @Override
	        public void onResponse(ApiResponse response) {
	                Log.d("danmaku entity", response.getFirstEntity().getUuid().toString());
	        		Log.i("danmaku entity", "creating connection");
	                mClient.connectEntitiesAsync("monos", mMono, "owns",
	                		response.getFirstEntity().getUuid().toString(),
	                		new ApiResponseCallback() {
								
								@Override
								public void onException(Exception arg0) {
									Log.i("danmaku entity", "creating connection failed");
								}
								
								@Override
								public void onResponse(ApiResponse arg0) {
									Log.i("danmaku entity", "creating connection succeeded");
								}
							});
	        }
		});
		mDanmakuList.add(danmaku);
	}

/*	public void onDataChange(DataSnapshot snapshot) {
		if (snapshot.getChildrenCount() == 0)
			mNoDanmakuListener.postFirstDanmaku(mMono);
		else
			for (DataSnapshot child : snapshot.getChildren()) {
				String text = (String) child.getValue();
				Log.d("Danmaku fetch", text);
				mDanmakuList.add(text);
				mHandler.insertDanmaku(text);
			}
	}*/

	public ApiResponse login(String usernameArg, String passwordArg) {

		ApiResponse response = null;

		try {
			response = mClient.authorizeAppUser(usernameArg, passwordArg);
		} catch (Exception e) {
			response = null;
		}

		if ((response != null) && !"invalid_grant".equals(response.getError())) {
			User user = response.getUser();
			email = user.getEmail();
			username = user.getUsername();
		}

		return response;
	}
}
