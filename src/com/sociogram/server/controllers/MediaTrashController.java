package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.*;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_KIND;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IS_THUMBNAIL_FOR_VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.LIKE_KIND;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_DOESNOT_EXIST;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_IMAGES_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_VIDEOS_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_THUMBNAIL_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;

import java.io.IOException;
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

public class MediaTrashController extends HttpServlet {

	private static final Logger logger = Logger
			.getLogger(MediaTrashController.class.getCanonicalName());

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		logger.log(Level.INFO, "serving user pics");

		String media = null;
		String mediaId = null;
		String videoId = null;
		String username = EMPTY_STRING;
		String accessToken = EMPTY_STRING;

		JSONObject jsonObject = Util.createJSONFromRequest(req);
		try {

			if (!jsonObject.isNull(MEDIA)) {
				media = jsonObject.getString(MEDIA);
			}
			if (!jsonObject.isNull(MEDIA_ID)) {
				mediaId = jsonObject.getString(MEDIA_ID);
			}
			if (!jsonObject.isNull(USERNAME)) {
				username = jsonObject.getString(USERNAME);
			}
			if (!jsonObject.isNull(ACCESS_TOKEN)) {
				accessToken = jsonObject.getString(ACCESS_TOKEN);
			}
		} catch (JSONException e) {
			e.printStackTrace();

		}
		if (Util.isNullOrEmpty(mediaId) || Util.isNullOrEmpty(media)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().print(FIELDS_MISSING);
			return;
		}

		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().print(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		long id = Long.valueOf(mediaId);
		Key key = KeyFactory.createKey(USER_KIND, username);

		String mediaKind = EMPTY_STRING;
		if (IMAGE.equalsIgnoreCase(media)) {
			mediaKind = USER_PHOTO_KIND;
		} else if (VIDEO.equalsIgnoreCase(media)) {
			mediaKind = USER_VIDEO_KIND;
		} else {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		Key ancestorKey = KeyFactory.createKey(key, mediaKind, id);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		Iterable<Entity> entities = Util.getParentSpecificData(mediaKind,
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
		// Media exists, Delete the media from data store and delete all the
		// comments and likes on it as well.
		removeMediaFromDatastore(username, id, userMediaEntity, mediaKind);

	}

	private void removeMediaFromDatastore(String username, long id,
			Entity userMediaEntity, String mediaKind) {

		BlobKey blobKeyToDelete = new BlobKey(userMediaEntity.getProperty(BLOB_KEY).toString());
		Util.getBlobstoreServiceInstance().delete(blobKeyToDelete);
		
		Util.deleteEntity(userMediaEntity.getKey());

		String[] searchBy = new String[2];
		searchBy[0] = MEDIA_OWNER_USERNAME;
		searchBy[1] = MEDIA_ID;
		Object[] searchFor = new Object[2];
		searchFor[0] = username;
		searchFor[1] = id;

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		Key ancestorKey = KeyFactory.createKey(mediaKind, id);
		Iterable<Entity> commentEntities = Util.listEntities(ancestorKey,
				COMMENT_KIND, searchBy, searchFor, fetchOptions);
		Util.deleteEntity(commentEntities);

		Iterable<Entity> likeEntities = Util.listEntities(ancestorKey,
				LIKE_KIND, searchBy, searchFor, fetchOptions);
		Util.deleteEntity(likeEntities);

		Key key = KeyFactory.createKey(USER_KIND, username);
		Entity userProfileEntity = Util.findEntity(key);

		if (USER_PHOTO_KIND.equalsIgnoreCase(mediaKind)) {
			int noOfImagesUplaoded = Integer.valueOf(userProfileEntity
					.getProperty(NO_OF_IMAGES_UPLOADED).toString());
			if (noOfImagesUplaoded > 0) {
				noOfImagesUplaoded -= 1;
				userProfileEntity.setProperty(NO_OF_IMAGES_UPLOADED,
						noOfImagesUplaoded);
			}
		} else if (USER_VIDEO_KIND.equalsIgnoreCase(mediaKind)) {
			int noOfVideosUplaoded = Integer.valueOf(userProfileEntity
					.getProperty(NO_OF_VIDEOS_UPLOADED).toString());
			if (noOfVideosUplaoded > 0) {
				noOfVideosUplaoded -= 1;
				userProfileEntity.setProperty(NO_OF_VIDEOS_UPLOADED,
						noOfVideosUplaoded);
			}

			searchBy = new String[1];
			searchBy[0] = IS_THUMBNAIL_FOR_VIDEO;
			searchFor = new Object[1];
			searchFor[0] = id;

			fetchOptions = FetchOptions.Builder.withDefaults();

			Iterable<Entity> videoThumbnailEntities = Util.listEntities(null,
					USER_VIDEO_THUMBNAIL_KIND, searchBy, searchFor,
					fetchOptions);
			if(videoThumbnailEntities.iterator().hasNext()){
				Entity videoThumbnailEntity = videoThumbnailEntities.iterator().next();
				BlobKey videoThumbnailBlobKeyToDelete = new BlobKey(videoThumbnailEntity.getProperty(BLOB_KEY).toString());
				Util.getBlobstoreServiceInstance().delete(videoThumbnailBlobKeyToDelete);
				Util.deleteEntity(videoThumbnailEntity.getKey());
			}

		}

		Util.persistEntity(userProfileEntity);
	}

}
