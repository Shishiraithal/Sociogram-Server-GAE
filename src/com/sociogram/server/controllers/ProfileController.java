package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.ACTION;
import static com.sociogram.server.common.ISocioGramConstants.CHANGE_INFO;
import static com.sociogram.server.common.ISocioGramConstants.CHANGE_PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.EMAIL;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.INFORMATION_UPDATED;
import static com.sociogram.server.common.ISocioGramConstants.MOBILE_NO;
import static com.sociogram.server.common.ISocioGramConstants.NAME;
import static com.sociogram.server.common.ISocioGramConstants.NEW_PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.OLD_PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.OLD_PASSWORD_INCORRECT;
import static com.sociogram.server.common.ISocioGramConstants.PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.PUBLIC;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PRIVACY_LEVEL;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class ProfileController extends HttpServlet {
//	private DatastoreService datastoreService = Util.getDatastoreServiceInstance();
	
	private static final Logger logger = Logger.getLogger(ProfileController.class.getCanonicalName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		
		JSONObject jsonObject = Util.createJSONFromRequest(req);
		
		String username = null; 
		String accessToken = null;
		String oldPassword = null;
		String newPassword = null;

		String name = EMPTY_STRING;
		String email = EMPTY_STRING;
		String mobileNo = EMPTY_STRING;
		String userPrivacyLevel = PUBLIC; 
		String action = null;
		
		try {
			if(!jsonObject.isNull(USERNAME)){
				username = jsonObject.getString(USERNAME);
			}
			if(!jsonObject.isNull(ACCESS_TOKEN)){
				accessToken = jsonObject.getString(ACCESS_TOKEN);
			}
			
			if(!jsonObject.isNull(OLD_PASSWORD)){
				oldPassword = jsonObject.getString(OLD_PASSWORD);
			}
			if(!jsonObject.isNull(NEW_PASSWORD)){
				newPassword = jsonObject.getString(NEW_PASSWORD);
			}
			if(!jsonObject.isNull(ACTION)){
				action = jsonObject.getString(ACTION);
			}
			if(!jsonObject.isNull(NAME)){
				name = jsonObject.getString(NAME);
			}
			if(!jsonObject.isNull(EMAIL)){
				email = jsonObject.getString(EMAIL);
			}
			if(!jsonObject.isNull(USER_PRIVACY_LEVEL)){
				userPrivacyLevel = jsonObject.getString(USER_PRIVACY_LEVEL);
				if(Util.isNullOrEmpty(userPrivacyLevel)){
					userPrivacyLevel = PUBLIC;
				}
			}
			if(!jsonObject.isNull(MOBILE_NO)){
				mobileNo = jsonObject.getString(MOBILE_NO);
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO, action + " user: " + username);
		
		if(Util.isNullOrEmpty(action)){
			res.sendError(ERROR_CODE_400,FIELDS_MISSING);
//			res.setStatus(ERROR_CODE_400);
			res.getOutputStream().write(FIELDS_MISSING.getBytes());
			return;
		}
		if(!Util.verifyAccessToken(username, accessToken)){
			res.sendError(ERROR_CODE_400,USERNAME_ACCESS_TOKEN_INVALID);
//			res.setStatus(ERROR_CODE_400);
			res.getOutputStream().write(USERNAME_ACCESS_TOKEN_INVALID.getBytes());
			return;
		}
		
		Key key = KeyFactory.createKey(USER_KIND,username);
		
		Entity userProfileEntity = Util.findEntity(key);
		
		if(CHANGE_PASSWORD.equalsIgnoreCase(action)){
			changePassword(res, userProfileEntity, oldPassword, newPassword);
			res.getOutputStream().write(INFORMATION_UPDATED.getBytes());
		}
		else if(CHANGE_INFO.equalsIgnoreCase(action)){
			changeProfileInfo(userProfileEntity, name, email,userPrivacyLevel,mobileNo);
			res.getOutputStream().write(INFORMATION_UPDATED.getBytes());
		}
		
	}

	private void changeProfileInfo(Entity userProfileEntity, String name,
			String email, String userPrivacyLevel, String mobileNo) {
		if(!Util.isNullOrEmpty(email)){
			userProfileEntity.setProperty(EMAIL, email);
		}
		if(!Util.isNullOrEmpty(name)){
			userProfileEntity.setProperty(NAME, name);
		}
		if(!Util.isNullOrEmpty(mobileNo)){
			userProfileEntity.setProperty(MOBILE_NO, mobileNo);
		}
		if(!Util.isNullOrEmpty(userPrivacyLevel)){
			userProfileEntity.setProperty(USER_PRIVACY_LEVEL, userPrivacyLevel);
		}
		Util.persistEntity(userProfileEntity);
		
	}

	private void changePassword(HttpServletResponse res, Entity userProfileEntity, String oldPassword, String newPassword) {
		if(!Util.isNullOrEmpty(oldPassword) || !Util.isNullOrEmpty(newPassword)){
			if(userProfileEntity.getProperty(PASSWORD).toString().equals(oldPassword)){
				userProfileEntity.setProperty(PASSWORD, newPassword);
				Util.persistEntity(userProfileEntity);
				try {
					res.getOutputStream().write(INFORMATION_UPDATED.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					res.sendError(ERROR_CODE_400,OLD_PASSWORD_INCORRECT);
//					res.setStatus(ERROR_CODE_400);
					res.getOutputStream().write(OLD_PASSWORD_INCORRECT.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
