package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.ACTION;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_OWNER_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.IMG;
import static com.sociogram.server.common.ISocioGramConstants.INVALID_USER;
import static com.sociogram.server.common.ISocioGramConstants.LIKED_BY_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.LIKE_ACTION;
import static com.sociogram.server.common.ISocioGramConstants.LIKE_KIND;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_DOESNOT_EXIST;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.START_INDEX;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_COMMENTS;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_LIKES;
import static com.sociogram.server.common.ISocioGramConstants.UNLIKE_ACTION;
import static com.sociogram.server.common.ISocioGramConstants.UPPER_LIMIT;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VID;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class LikesController extends HttpServlet {

	// private final DatastoreService datastore =
	// Util.getDatastoreServiceInstance();
	// private BlobstoreService blobstore = Util.getBlobstoreServiceInstance();

	// private BlobInfoFactory blobInfoFactory = new BlobInfoFactory(datastore);

	private static final Logger logger = Logger.getLogger(LikesController.class
			.getCanonicalName());

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String mediaId = req.getParameter(MEDIA_ID);
		String upperLimit = req.getParameter(UPPER_LIMIT);
		String startIndex = req.getParameter(START_INDEX);
		String accessToken = req.getParameter(ACCESS_TOKEN);
		String username = req.getParameter(USERNAME);
		String mediaOwnerUsername = req.getParameter(MEDIA_OWNER_USERNAME);
		String media = req.getParameter(MEDIA);
		
		if (Util.isNullOrEmpty(mediaOwnerUsername)
				|| Util.isNullOrEmpty(mediaId)
				|| Util.isNullOrEmpty(startIndex)
				|| Util.isNullOrEmpty(accessToken)
				|| Util.isNullOrEmpty(username)
				|| Util.isNullOrEmpty(media)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().print(FIELDS_MISSING);
			return;
		}

		if (!Util.verifyUser(mediaOwnerUsername)) {
			res.sendError(ERROR_CODE_400, IMAGE_OWNER_INVALID);
			res.getWriter().write(IMAGE_OWNER_INVALID);
			return;
		}
		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().print(USERNAME_ACCESS_TOKEN_INVALID);
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
		
		Key ancestorKey = KeyFactory.createKey(kind,
				Long.valueOf(mediaId));

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

		Iterable<Entity> likeEntities = Util.listEntities(ancestorKey,
				LIKE_KIND, searchBy, searchFor, fetchOptions);

		String likeJsonData = Util.makeJSONArrayForLikes(likeEntities);
		res.getWriter().write(likeJsonData);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		logger.log(Level.INFO, "like user pics");

		String likedByUser = null;
		String mediaOwnerUsername = null;
		String action = null;
		String mediaId = null;
		String accessToken = EMPTY_STRING;
		String media = EMPTY_STRING;

		JSONObject jsonObject = Util.createJSONFromRequest(req);
		try {
			if (!jsonObject.isNull(LIKED_BY_USERNAME)) {
				likedByUser = jsonObject.getString(LIKED_BY_USERNAME);
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
			if (!jsonObject.isNull(ACCESS_TOKEN)) {
				accessToken = jsonObject.getString(ACCESS_TOKEN);
			}
			if (!jsonObject.isNull(MEDIA)) {
				media = jsonObject.getString(MEDIA);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (Util.isNullOrEmpty(likedByUser)
				|| Util.isNullOrEmpty(mediaOwnerUsername)
				|| Util.isNullOrEmpty(action) || Util.isNullOrEmpty(mediaId)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		if (!Util.verifyUser(mediaOwnerUsername)) {
			res.sendError(ERROR_CODE_400, INVALID_USER);
			res.getWriter().write(INVALID_USER);
			return;
		}

		if (!Util.verifyAccessToken(likedByUser, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().write(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}
		
		long id = Long.valueOf(mediaId);
		Key key = KeyFactory.createKey(USER_KIND, mediaOwnerUsername);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		
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

		if (LIKE_ACTION.equalsIgnoreCase(action)) {
			likeMedia(res, userMediaEntity, media, id, likedByUser,
					mediaOwnerUsername, kind);

		} else if (UNLIKE_ACTION.equalsIgnoreCase(action)) {
			unlikeMedia(res, userMediaEntity, media, id, likedByUser,
					mediaOwnerUsername, kind);
		}
	}

	private void unlikeMedia(HttpServletResponse res, Entity userMediaEntity,
			String media, long id, String likedByUser,
			String mediaOwnerUsername, String kind) {
		// delete entry in the LIKE kind
		// Decrement the like counter in USER PHOTO KIND
		
		Key ancestorKey = KeyFactory.createKey(kind, id);

		String[] searchBy = new String[2];
		searchBy[0] = LIKED_BY_USERNAME;
		searchBy[1] = MEDIA_ID;
		Object[] searchFor = new Object[2];
		searchFor[0] = likedByUser;
		searchFor[1] = id;

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		Iterable<Entity> entities = Util.listEntities(ancestorKey, LIKE_KIND, searchBy, searchFor,
				fetchOptions);

		if (entities.iterator().hasNext()) {
			Util.deleteEntity(entities.iterator().next().getKey());
		}

		int totalLikes = Integer.valueOf(userMediaEntity.getProperty(
				TOTAL_LIKES).toString());
		if (totalLikes > 0) {
			totalLikes = totalLikes - 1;
			userMediaEntity.setProperty(TOTAL_LIKES, totalLikes);
			Util.persistEntity(userMediaEntity);
		}
		if(IMAGE.equalsIgnoreCase(media)){
			Util.updatePopularPhotoData(id, TOTAL_LIKES, totalLikes);
		}

	}

	private void likeMedia(HttpServletResponse res, Entity userMediaEntity,
			String media, long id, String likedByUser,
			String mediaOwnerUsername, String kind) {
		Key key = KeyFactory.createKey(kind, id);
		// make entry in the LIKE kind
		// Increment the like counter in USER PHOTO KIND
		Entity likeEntity = new Entity(LIKE_KIND, key);
		likeEntity.setProperty(MEDIA_ID, id);
		if(IMAGE.equalsIgnoreCase(media)){
			likeEntity.setProperty(MEDIA, IMG);
		}else if(VIDEO.equalsIgnoreCase(media)){
			likeEntity.setProperty(MEDIA, VID);
		}
		likeEntity.setProperty(LIKED_BY_USERNAME, likedByUser);
		likeEntity.setProperty(MEDIA_OWNER_USERNAME, mediaOwnerUsername);
		Util.persistEntity(likeEntity);

		int totalLikes = Integer.valueOf(userMediaEntity.getProperty(
				TOTAL_LIKES).toString());
		totalLikes = totalLikes + 1;
		userMediaEntity.setProperty(TOTAL_LIKES, totalLikes);
		Util.persistEntity(userMediaEntity);
		
		if(IMAGE.equalsIgnoreCase(media)){
			Util.updatePopularPhotoData(id, TOTAL_LIKES, totalLikes);
		}


		String likeJsonString = Util.makeJSONObjectForLike(likeEntity);

		try {
			res.getWriter().write(likeJsonString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
