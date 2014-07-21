package com.sociogram.server.controllers;


import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.ADMIN_EMAIL;
import static com.sociogram.server.common.ISocioGramConstants.ADMIN_NAME;
import static com.sociogram.server.common.ISocioGramConstants.EMAIL;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.MOBILE_NO;
import static com.sociogram.server.common.ISocioGramConstants.NAME;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_IMAGES_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_VIDEOS_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.PASSWORD;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE_ID;
import static com.sociogram.server.common.ISocioGramConstants.PUBLIC;
import static com.sociogram.server.common.ISocioGramConstants.SOCIOGRAM_ACCOUNT_ACTIVATED;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USER_ALREADY_EXIST;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PRIVACY_LEVEL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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


public class SignupController extends HttpServlet {
//	private DatastoreService datastoreService = Util.getDatastoreServiceInstance();
	private static final Logger logger = Logger.getLogger(SignupController.class.getCanonicalName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException{
		doPost(req,res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		JSONObject jsonObject = Util.createJSONFromRequest(req);
		
		String name = null;
		String username = null; 
		String password = null;
		String recepientEmail = EMPTY_STRING;
		String profileImageId = EMPTY_STRING;
		String mobileNo = EMPTY_STRING;
		String userPrivacyLevel = PUBLIC; //public or private. Default: public
		int noOfImagesUploaded = 0;
		int noOfVideosUploaded = 0;
		
		try {
			if(!jsonObject.isNull(NAME)){
				name = jsonObject.getString(NAME);
			}
			
			if(!jsonObject.isNull(USERNAME)){
				username = jsonObject.getString(USERNAME);
			}
			
			if(!jsonObject.isNull(PASSWORD)){
				password = jsonObject.getString(PASSWORD);
			}
			if(!jsonObject.isNull(EMAIL)){
				recepientEmail = jsonObject.getString(EMAIL);
			}
			if(!jsonObject.isNull(MOBILE_NO)){
				mobileNo = jsonObject.getString(MOBILE_NO);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO, "signup user: " + username);
		
		
		if(Util.isNullOrEmpty(username) || Util.isNullOrEmpty(password)){
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}
		
		if(Util.isNullOrEmpty(name)){
			name = username;
		}
		
		Key key = KeyFactory.createKey(USER_KIND,username);
		
		Entity signupEntity = Util.findEntity(key);
		
		if(signupEntity != null){
			
			res.sendError(ERROR_CODE_400, USER_ALREADY_EXIST);
			res.getWriter().write(USER_ALREADY_EXIST);
			return;
		}
		
		
		
				
		//Everything has been validated now put the new user in datastore.
		signupEntity = new Entity(key);
		signupEntity.setProperty(NAME,name);
		signupEntity.setProperty(USERNAME,username);
		signupEntity.setProperty(PASSWORD,password);
		signupEntity.setProperty(PROFILE_IMAGE_ID,profileImageId);
		signupEntity.setProperty(USER_PRIVACY_LEVEL,userPrivacyLevel);
		signupEntity.setProperty(EMAIL,recepientEmail);
		signupEntity.setProperty(MOBILE_NO,mobileNo);
		signupEntity.setProperty(NO_OF_IMAGES_UPLOADED,noOfImagesUploaded);
		signupEntity.setProperty(NO_OF_VIDEOS_UPLOADED,noOfVideosUploaded);
		Util.persistEntity(signupEntity);
		
		String accessToken = Util.generateAccessToken(username);
		
		if(!Util.isNullOrEmpty(recepientEmail)){
			//generate welcome email
			generateWelcomeEmail(name, username, recepientEmail);
		}
		
		
		JSONObject signupSuccessJson = new JSONObject();
		try {
			signupSuccessJson.put(ACCESS_TOKEN, accessToken);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		res.getWriter().write(signupSuccessJson.toString());
		
	}
	
	private void generateWelcomeEmail(String name, String username, String recepientEmail){
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        if(Util.isNullOrEmpty(name)){
        	name = username;
        }

        StringBuilder msgBody  = new StringBuilder();
        msgBody.append("welcome");
        msgBody.append(name);
        msgBody.append("\n");
        msgBody.append("Congratulations! Your account on SocioGram has been registered.");
        msgBody.append("\n");
        msgBody.append("Go social with SocioGram. You can upload your photos, share with family and friends.");
        msgBody.append("\n");
        msgBody.append("We hope you enjoy your time with SocioGram.");
        msgBody.append("\n");
        msgBody.append("See you around.");
        msgBody.append("Best Regards,");
        msgBody.append("SocioGram Team.");

        try {
            Message msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(ADMIN_EMAIL, ADMIN_NAME));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recepientEmail, name));

            
            msg.setSubject(SOCIOGRAM_ACCOUNT_ACTIVATED);
            msg.setText(msgBody.toString());
            Transport.send(msg);

        } catch (AddressException e) {
        	e.printStackTrace();
        } catch (MessagingException e) {
        	e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
