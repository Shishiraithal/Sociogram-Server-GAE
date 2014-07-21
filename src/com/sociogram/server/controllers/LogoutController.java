package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.INVALID_USER;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;

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

public class LogoutController extends HttpServlet {
	
//	private DatastoreService datastoreService = Util.getDatastoreServiceInstance();
	
	private static final Logger logger = Logger.getLogger(LogoutController.class.getCanonicalName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		JSONObject jsonObject = Util.createJSONFromRequest(req);
		
		String username = null; 
		try {
			if(!jsonObject.isNull(USERNAME)){
				username = jsonObject.getString(USERNAME);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO, "Logout user: " + username);
		
		if(Util.isNullOrEmpty(username)){
			res.sendError(ERROR_CODE_400,FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}
		
		Key key = KeyFactory.createKey(USER_KIND,username);
		
		Entity userProfileEntity = null;
		userProfileEntity = Util.findEntity(key);
	  	
		if(userProfileEntity != null){
			userProfileEntity.setProperty(ACCESS_TOKEN, EMPTY_STRING);
			Util.persistEntity(userProfileEntity);
		}else{
			res.sendError(ERROR_CODE_400,INVALID_USER);
			res.getWriter().write(INVALID_USER);
		}
	}
}
