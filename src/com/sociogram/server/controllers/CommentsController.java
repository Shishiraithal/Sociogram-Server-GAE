package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.*;
import static com.sociogram.server.common.ISocioGramConstants.ACTION;
import static com.sociogram.server.common.ISocioGramConstants.ADD;
import static com.sociogram.server.common.ISocioGramConstants.BLOB_KEY;
import static com.sociogram.server.common.ISocioGramConstants.COMMENTED_BY_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_ID;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_KIND;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_TEXT;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_TIME_STAMP;
import static com.sociogram.server.common.ISocioGramConstants.DELETE;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_OWNER_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.IMG;
import static com.sociogram.server.common.ISocioGramConstants.INVALID_USER;
import static com.sociogram.server.common.ISocioGramConstants.IS_THUMBNAIL_FOR_VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_DOESNOT_EXIST;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.START_INDEX;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_COMMENTS;
import static com.sociogram.server.common.ISocioGramConstants.UPPER_LIMIT;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_THUMBNAIL_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VID;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class CommentsController extends HttpServlet {

	// private final DatastoreService datastore =
	// Util.getDatastoreServiceInstance();
	// private BlobstoreService blobstore = Util.getBlobstoreServiceInstance();
	//
	// private BlobInfoFactory blobInfoFactory = new BlobInfoFactory(datastore);

	private static final Logger logger = Logger
			.getLogger(CommentsController.class.getCanonicalName());

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String imageId = req.getParameter(MEDIA_ID);
		String upperLimit = req.getParameter(UPPER_LIMIT);
		String startIndex = req.getParameter(START_INDEX);
		String accessToken = req.getParameter(ACCESS_TOKEN);
		String username = req.getParameter(USERNAME);
		String mediaOwnerUsername = req.getParameter(MEDIA_OWNER_USERNAME);
		String media = req.getParameter(MEDIA);

		if (Util.isNullOrEmpty(mediaOwnerUsername)
				|| Util.isNullOrEmpty(imageId)
				|| Util.isNullOrEmpty(startIndex)
				|| Util.isNullOrEmpty(accessToken)
				|| Util.isNullOrEmpty(username)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		if (!Util.verifyUser(mediaOwnerUsername)) {
			res.sendError(ERROR_CODE_400, IMAGE_OWNER_INVALID);
			res.getWriter().write(IMAGE_OWNER_INVALID);
			return;
		}
		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().write(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		int offset = Integer.parseInt(startIndex);
		int limit = COMMENT_LIMIT_PER_REQUEST;

		String kind = EMPTY_STRING;
		if (IMAGE.equalsIgnoreCase(media)) {
			kind = USER_PHOTO_KIND;
		} else if (VIDEO.equalsIgnoreCase(media)) {
			kind = USER_VIDEO_KIND;
		}

		Key ancestorKey = KeyFactory.createKey(kind, Long.valueOf(imageId));

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		fetchOptions.offset(offset);
		if (!Util.isNullOrEmpty(upperLimit)) {
			limit = Integer.valueOf(upperLimit);
			fetchOptions.limit(limit);
		}

		String[] searchBy = new String[1];
		searchBy[0] = MEDIA_OWNER_USERNAME;
		String[] searchFor = new String[1];
		searchFor[0] = mediaOwnerUsername;

		Iterable<Entity> commentEntities = Util.listEntities(ancestorKey,
				COMMENT_KIND, searchBy, searchFor, fetchOptions);

		// Iterable<Entity> commentEntities =
		// Util.getParentSpecificData(COMMENT,
		// ancestorKey, fetchOptions);

		String commentObject = Util.makeJSONArrayForComments(commentEntities);

		res.getWriter().write(commentObject);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		logger.log(Level.INFO, "serving user pics");

		String commentByUser = null;
		String mediaOwnerUsername = null;
		String action = null;
		String mediaId = null;

		String commentText = null;
		String commentId = null;
		String accessToken = EMPTY_STRING;
		String media = EMPTY_STRING;

		JSONObject jsonObject = Util.createJSONFromRequest(req);
		try {
			if (!jsonObject.isNull(COMMENTED_BY_USERNAME)) {
				commentByUser = jsonObject.getString(COMMENTED_BY_USERNAME);
			}
			if (!jsonObject.isNull(MEDIA_OWNER_USERNAME)) {
				mediaOwnerUsername = jsonObject.getString(MEDIA_OWNER_USERNAME);
			}
			if (!jsonObject.isNull(ACTION)) {
				action = jsonObject.getString(ACTION);
			}
			if (!jsonObject.isNull(MEDIA_ID)) {
				mediaId = jsonObject.getString(MEDIA_ID);
			}

			if (!jsonObject.isNull(COMMENT_TEXT)) {
				commentText = jsonObject.getString(COMMENT_TEXT);
			}
			if (!jsonObject.isNull(COMMENT_ID)) {
				commentId = jsonObject.getString(COMMENT_ID);
			}
			if (!jsonObject.isNull(ACCESS_TOKEN)) {
				accessToken = jsonObject.getString(ACCESS_TOKEN);
			}
			if (!jsonObject.isNull(MEDIA)) {
				media = jsonObject.getString(MEDIA);
			}
		} catch (JSONException e) {
			e.printStackTrace();

		}

		if (Util.isNullOrEmpty(ACCESS_TOKEN)) {
			res.sendError(400, "Access Token not valid, Login Again");
			res.getWriter().write("Access Token not valid, Login again");
			return;
		}

		if (ADD.equalsIgnoreCase(action)) {
			if (Util.isNullOrEmpty(commentByUser)
					|| Util.isNullOrEmpty(mediaOwnerUsername)
					|| Util.isNullOrEmpty(mediaId)
					|| Util.isNullOrEmpty(commentText)
					|| Util.isNullOrEmpty(media)) {
				res.sendError(ERROR_CODE_400, FIELDS_MISSING);
				res.getWriter().write(FIELDS_MISSING);
				return;
			}
		} else if (DELETE.equalsIgnoreCase(action)) {
			if (Util.isNullOrEmpty(commentByUser)
					|| Util.isNullOrEmpty(mediaOwnerUsername)
					|| Util.isNullOrEmpty(mediaId)
					|| Util.isNullOrEmpty(commentId)) {
				res.sendError(ERROR_CODE_400, FIELDS_MISSING);
				res.getWriter().print(FIELDS_MISSING);
				return;
			}
		}

		if (!Util.verifyUser(commentByUser)
				|| !Util.verifyUser(mediaOwnerUsername)) {
			res.sendError(ERROR_CODE_400, INVALID_USER);
			res.getWriter().write(INVALID_USER);
			return;
		}

		if (!Util.verifyAccessToken(commentByUser, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().print(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		long id = Long.valueOf(mediaId);
		Key key = KeyFactory.createKey(USER_KIND, mediaOwnerUsername);

		String kind = EMPTY_STRING;
		if (IMAGE.equalsIgnoreCase(media)) {
			kind = USER_PHOTO_KIND;
		} else if (VIDEO.equalsIgnoreCase(media)) {
			kind = USER_VIDEO_KIND;
		}else{
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		Key ancestorKey = KeyFactory.createKey(key, kind, id);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		Iterable<Entity> entities = Util.getParentSpecificData(kind,
				ancestorKey, fetchOptions);
		Entity userMediaEntity = null;
		if (entities.iterator().hasNext()) {
			userMediaEntity = entities.iterator().next();
		}

		if (userMediaEntity == null) {
			res.sendError(ERROR_CODE_400, MEDIA_DOESNOT_EXIST);
			res.getWriter().write(MEDIA_DOESNOT_EXIST);
			return;
		}

		if (ADD.equalsIgnoreCase(action)) {
			addCommentOnMedia(res, userMediaEntity, media, kind, id,
					commentByUser, mediaOwnerUsername, commentText);

		} else if (DELETE.equalsIgnoreCase(action)) {

			deleteCommentOnMedia(res, userMediaEntity, media, kind, id,
					commentByUser, mediaOwnerUsername, commentText, commentId);

		}
	}

	private void deleteCommentOnMedia(HttpServletResponse res,
			Entity userMediaEntity, String media, String kind, long id,
			String commentByUser, String mediaOwnerUsername, String commentText, String commentId) {
		// delete entry in the COMMENT kind
		// Decrement the like counter in USER_PHOTO KIND

		long cmtId = Long.valueOf(commentId);

		Key key = KeyFactory.createKey(kind, id);
		Key ancestorKey = KeyFactory.createKey(key, COMMENT_KIND, cmtId);

		String[] searchBy = new String[2];
		searchBy[0] = COMMENTED_BY_USERNAME;
		searchBy[1] = MEDIA_ID;
		Object[] searchFor = new Object[2];
		searchFor[0] = commentByUser;
		searchFor[1] = id;

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		
		Iterable<Entity> entities = Util.listEntities(ancestorKey, COMMENT_KIND, searchBy, searchFor,
				fetchOptions);

		if (entities.iterator().hasNext()) {
			Util.deleteEntity(entities.iterator().next().getKey());
		}

		int totalComments = Integer.valueOf(userMediaEntity.getProperty(
				TOTAL_COMMENTS).toString());
		if (totalComments > 0) {
			totalComments = totalComments - 1;
			userMediaEntity.setProperty(TOTAL_COMMENTS, totalComments);
			Util.persistEntity(userMediaEntity);
		}
		
		if(IMAGE.equalsIgnoreCase(media)){
			Util.updatePopularPhotoData(id, TOTAL_COMMENTS, totalComments);
		}


	}

	private void addCommentOnMedia(HttpServletResponse res,
			Entity userMediaEntity, String media, String kind, long id,
			String commentByUser, String mediaOwnerUsername, String commentText) {
		// make entry in the COMMENT kind
		// Increment the like counter in USER_PHOTO KIND

		Key key = KeyFactory.createKey(kind, id);
		Entity commentEntity = new Entity(COMMENT_KIND, key);
		commentEntity.setProperty(MEDIA_ID, id);
		if(IMAGE.equalsIgnoreCase(media)){
			commentEntity.setProperty(MEDIA, IMG);
		}else if(VIDEO.equalsIgnoreCase(media)){
			commentEntity.setProperty(MEDIA, VID);
		}
		commentEntity.setProperty(COMMENTED_BY_USERNAME, commentByUser);
		commentEntity.setProperty(MEDIA_OWNER_USERNAME, mediaOwnerUsername);
		commentEntity.setProperty(COMMENT_TIME_STAMP, new Date());
		commentEntity.setProperty(COMMENT_TEXT, commentText);

		Util.persistEntity(commentEntity);
		// int i = 0;
		int totalComments = Integer.valueOf(userMediaEntity.getProperty(
				TOTAL_COMMENTS).toString());
		totalComments = totalComments + 1;
		userMediaEntity.setProperty(TOTAL_COMMENTS, totalComments);
		
		if(IMAGE.equalsIgnoreCase(media)){
			Util.updatePopularPhotoData(id, TOTAL_COMMENTS, totalComments);
		}
		
		Util.persistEntity(userMediaEntity);

		String commentObject = Util.makeJSONObjectForComment(commentEntity);
		try {
			res.getWriter().write(commentObject);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
