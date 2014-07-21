package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.EMAIL;
import static com.sociogram.server.common.ISocioGramConstants.HAS_PROFILE_PIC;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.MOBILE_NO;
import static com.sociogram.server.common.ISocioGramConstants.NAME;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_IMAGES_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_VIDEOS_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE_ID;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USER_MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.USER_PRIVACY_LEVEL;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class LoginControllerUtil {

	 private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
	
	
	/**
 	 * Writes the entity in JSON format
 	 * 
 	 * @param userEntity  entity to return as JSON string
     * @param userPhotos  Iterable entities to be inserted in JSON object of User.
     * @return  String JSON object in string format
 	 */
public static String writeJSONResponseForLogin(Entity userEntity, Iterable<Entity> userPhotos) {
	logger.log(Level.INFO, "creating JSON format object");
	
	JSONObject jsonObject = new JSONObject();
	try {
		jsonObject.put(ACCESS_TOKEN, userEntity.getProperty(ACCESS_TOKEN));
		jsonObject.put(NAME, userEntity.getProperty(NAME));
		jsonObject.put(USERNAME, userEntity.getProperty(USERNAME));
		jsonObject.put(EMAIL, userEntity.getProperty(EMAIL));
		String profileImageId = userEntity.getProperty(PROFILE_IMAGE_ID).toString();
		if(Util.isNullOrEmpty(profileImageId)){
			jsonObject.put(HAS_PROFILE_PIC, false);	
		}else{
			jsonObject.put(HAS_PROFILE_PIC, true);
		}
		
		jsonObject.put(MOBILE_NO, userEntity.getProperty(MOBILE_NO));
		jsonObject.put(NO_OF_IMAGES_UPLOADED, userEntity.getProperty(NO_OF_IMAGES_UPLOADED));
		jsonObject.put(NO_OF_VIDEOS_UPLOADED, userEntity.getProperty(NO_OF_VIDEOS_UPLOADED));
		jsonObject.put(USER_PRIVACY_LEVEL, userEntity.getProperty(USER_PRIVACY_LEVEL));
		
		JSONArray photoArray = Util.makeJSONArrayForUserMedia(userPhotos,IMAGE_LIMIT_PER_REQUEST);
		
		jsonObject.put(USER_MEDIA,photoArray);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return jsonObject.toString();
}

}
