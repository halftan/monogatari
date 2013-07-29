package com.azusasoft.monogatari.controller;

import java.util.ArrayList;

import org.springframework.http.HttpMethod;
import org.uniid.android.client.Client;
import org.uniid.android.client.callbacks.ApiResponseCallback;
import org.uniid.java.client.entities.Entity;
import org.uniid.java.client.entities.User;
import org.uniid.java.client.response.ApiResponse;
import org.uniid.java.client.utils.JsonUtils;

import android.util.Log;

import com.azusasoft.monogatari.controller.DanmakuController.NewDanmakuListener;


public class MessageController {

	private static String UNIID_API_URL = "http://54.248.89.253:8080";
	private static String UNIID_ORG = "azusasoft";
	private static String UNIID_APP = "sandbox";
	
	private Client mClient = null;
	private String email;
	private String username;
	
	private ArrayList<String> mDanmakuList;

	// mono refers to 物 in Japanese
	private String mMono;
	
	public interface danmakuEventListener {
		public void pushFirstDanmaku(String mono);
		public void loadDanmaku(ArrayList<String> danmakuList);
	}
	
	private danmakuEventListener mDanmakuEventListener;
	
	private MessageController() {}
	
	private static MessageController mInstance = null;
	
	public static MessageController init(danmakuEventListener listener) {
		mInstance = new MessageController();
		mInstance.mDanmakuEventListener = listener;
		mInstance.mDanmakuList = new ArrayList<String>();
		mInstance.mClient = new Client();
		mInstance.mClient.setApiUrl(UNIID_API_URL);
		mInstance.mClient.setOrganizationId(UNIID_ORG);
		mInstance.mClient.setApplicationId(UNIID_APP);
//		mInstance.mClient.setClientSecret("YXA6sAc9Unk_ByF4HYHU9U1wh1hk8LI");
//		mInstance.mClient.setClientId("YXA6vPQQEOhSEeKKVnuBn11m3g");
//		mInstance.mClient.setAccessToken("YWMtlHav0PW4EeKfupGdNBdSdQAAAUA9nE9NqDy-I6Jk2ezKZ5EHoZtrgBeYo10");
		
		return mInstance;
	}
	
	public static MessageController getInstance() throws RuntimeException {
		if (mInstance != null)
			return mInstance;
		else throw new RuntimeException("Message Controller haven't been initialized!");
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
						mDanmakuEventListener.loadDanmaku(danmakuList);
					}
				} else {
					Log.i("danmaku mono entity", "creating new mono: " + mMono);
					Entity mono = new Entity("monos");
					mono.setProperty("name", JsonUtils.toJsonNode(mMono));
					Log.i("danmaku mono entity", "created jsonnode");
					mClient.createEntity(mono);
					mDanmakuEventListener.pushFirstDanmaku(mMono);
				}
				
			}
		}, "search danmaku thread").start();
	}
	
	public void postDanmaku(String text) {
		Entity danmakus = new Entity("danmakus");
		danmakus.setProperty("text", JsonUtils.toJsonNode(text));
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
		mDanmakuList.add(text);
	}
	
	public NewDanmakuListener newDanmakuWillBePosted = new NewDanmakuListener() {
		
		@Override
		public void newDanmaku(String text) {
			postDanmaku(text);
		}
	};

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
