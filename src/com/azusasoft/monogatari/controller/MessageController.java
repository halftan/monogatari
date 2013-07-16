package com.azusasoft.monogatari.controller;

import org.uniid.android.client.Client;
import org.uniid.java.client.entities.User;
import org.uniid.java.client.response.ApiResponse;

public class MessageController {

	private String UNIID_API_URL = "http://54.248.89.253:8080";
	private String UNIID_ORG = "azusasoft";
	private String UNIID_APP = "monogatari";
	
	private Client mClient = null;
	private String email;
	private String username;
	
	public MessageController() {
		mClient = new Client();
		mClient.setApiUrl(UNIID_API_URL);
		mClient.setOrganizationId(UNIID_ORG);
		mClient.setApplicationId(UNIID_APP);
	}

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
