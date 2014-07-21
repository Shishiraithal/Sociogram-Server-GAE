package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.GENERAL_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.IS_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_PASSWORD_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class LoginController extends HttpServlet {
//	private DatastoreService datastoreService = Util.getDatastoreServiceInstance();
	
	private static final Logger logger = Logger.getLogger(LoginController.class.getCanonicalName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		
		JSONObject jsonObject = Util.createJSONFromRequest(req);
		
		String username = null; 
		String password = null;
		try {
			if(!jsonObject.isNull(USERNAME)){
				username = jsonObject.getString(USERNAME);
			}
			
			if(!jsonObject.isNull(PASSWORD)){
				password = jsonObject.getString(PASSWORD);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO, "loging in user: " + username);
		
		if(Util.isNullOrEmpty(username) || Util.isNullOrEmpty(password)){
			res.sendError(ERROR_CODE_400,FIELDS_MISSING);
//			res.setStatus(ERROR_CODE_400);
//			res.getOutputStream().write(FIELDS_MISSING.getBytes());
			return;
		}
		
		Key key = KeyFactory.createKey(USER_KIND,username);
		
		Entity userProfileEntity = null;
		userProfileEntity = Util.findEntity(key);
	  	
		if(userProfileEntity != null){
			if(userProfileEntity.getProperty(PASSWORD).toString().equals(password)){
				Util.generateAccessToken(username);
				userProfileEntity = Util.findEntity(key);
				//prepare JSON with user data and max 20 user shared images blob key.
				//send the JSON in response to client.
				String kind = USER_PHOTO_KIND;
				Key ancestorKey = KeyFactory.createKey(USER_KIND, username);
				
				String[] searchBy = new String[1];
				searchBy[0] = IS_PROFILE_IMAGE; 
				Integer[] searchFor = new Integer[1];
				searchFor[0] = GENERAL_IMAGE;
				
				
				
				FetchOptions fetchOptions = FetchOptions.Builder.withLimit(IMAGE_LIMIT_PER_REQUEST);
				
				Iterable<Entity> entities = Util.listEntities(ancestorKey, kind, searchBy, searchFor, fetchOptions);
				
				String UserProfileInfoAndPhotosJSONFormat = LoginControllerUtil.writeJSONResponseForLogin(userProfileEntity, entities);
				res.getOutputStream().write(UserProfileInfoAndPhotosJSONFormat.getBytes());
				
			}
			else{
				res.sendError(ERROR_CODE_400,USERNAME_PASSWORD_INVALID);
//				res.setStatus(ERROR_CODE_400);
//				res.getOutputStream().write(USERNAME_PASSWORD_INVALID.getBytes());
			}
		}else{
			res.sendError(ERROR_CODE_400,USERNAME_PASSWORD_INVALID);
//			res.setStatus(ERROR_CODE_400);
//			res.getOutputStream().write(USERNAME_PASSWORD_INVALID.getBytes());
			
		}
	}
}
