package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.ACTION;
import static com.sociogram.server.common.ISocioGramConstants.BLOB_KEY;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IS_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IS_THUMBNAIL_FOR_VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.NO_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE_ID;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_THUMBNAIL_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO_THUMBNAIL;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.sociogram.server.common.Util;

public class MediaDownloadController extends HttpServlet {

	private final DatastoreService datastore = Util
			.getDatastoreServiceInstance();
	private final BlobstoreService blobstore = Util
			.getBlobstoreServiceInstance();

	private final BlobInfoFactory blobInfoFactory = new BlobInfoFactory(
			datastore);

	
	private static final Logger logger = Logger
			.getLogger(MediaDownloadController.class.getName());

	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		logger.log(Level.INFO, "do post calling doGet()");
		logger.info("MediaDownloadController: : do post calling doGet()");
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
//		logger.log(Level.INFO, "in doGet()");
		logger.info("MediaDownloadController: In doget()");
		
//		logger.log(Level.INFO, "serving user pics");

		String username = req.getParameter(USERNAME);
		String mediaOwnerUsername = req.getParameter(MEDIA_OWNER_USERNAME);
		String action = req.getParameter(ACTION);
		String mediaId = req.getParameter(MEDIA_ID);
		String accessToken = req.getParameter(ACCESS_TOKEN);

		if ((!Util.isNullOrEmpty(mediaId) && action.equalsIgnoreCase(PROFILE))
				|| Util.isNullOrEmpty(action)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().write(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		// BlobKey blobKey = null;

		Key key = KeyFactory.createKey(USER_KIND, username);

		if (PROFILE.equalsIgnoreCase(action)) {
			downloadProfileImage(res, key, mediaOwnerUsername);
		} else if (IMAGE.equalsIgnoreCase(action)) {
			downloadImage(res, key, mediaId, mediaOwnerUsername);
		} else if (VIDEO.equalsIgnoreCase(action)) {
			streamVideo(res, key, mediaId, mediaOwnerUsername);
		} else if (VIDEO_THUMBNAIL.equalsIgnoreCase(action)) {
			downloadVideoThumbnail(res, mediaId, mediaOwnerUsername);
		}
	}

	private void downloadVideoThumbnail(HttpServletResponse res,
			String mediaId, String mediaOwnerUsername) {
		
		String kind = USER_VIDEO_THUMBNAIL_KIND;
		long id = Long.valueOf(mediaId);
//		Key ancestorKey = KeyFactory.createKey(USER_VIDEO_KIND, id);

		String[] searchBy = new String[2];
		searchBy[0] = USERNAME;
		searchBy[1] = IS_THUMBNAIL_FOR_VIDEO;
		Object[] searchFor = new Object[2];
		searchFor[0] = mediaOwnerUsername;
		searchFor[1] = id;

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		
		Iterable<Entity> entities = Util.listEntities(null,
				kind, searchBy, searchFor, fetchOptions);
		
		Util.fetchAllDataFromBlobStore(res, entities);
	}

	private void downloadProfileImage(HttpServletResponse res, Key key,
			String mediaOwnerUsername) {
		String mediaId = EMPTY_STRING;
		if (!Util.isNullOrEmpty(mediaOwnerUsername)
				&& Util.verifyUser(mediaOwnerUsername)) {
			key = KeyFactory.createKey(USER_KIND, mediaOwnerUsername);
		}
		Entity userProfileEntity = Util.findEntity(key);
		mediaId = userProfileEntity.getProperty(PROFILE_IMAGE_ID).toString();
		if (Util.isNullOrEmpty(mediaId)) {
			try {
				res.sendError(ERROR_CODE_400, NO_PROFILE_IMAGE);
				res.getWriter().write(NO_PROFILE_IMAGE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		long profileImageId = Long.valueOf(mediaId);

		String kind = USER_PHOTO_KIND;

		Key ancestorKey = KeyFactory.createKey(key, kind, profileImageId);
		String[] searchBy = new String[1];
		searchBy[0] = IS_PROFILE_IMAGE;
		Integer[] searchFor = new Integer[1];
		searchFor[0] = PROFILE_IMAGE;

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		Iterable<Entity> entities = Util.listEntities(ancestorKey, kind,
				searchBy, searchFor, fetchOptions);
		Util.fetchAllDataFromBlobStore(res, entities);
	}

	private void streamVideo(HttpServletResponse res, Key key, String mediaId,
			String mediaOwnerUsername) {

		String kind = USER_VIDEO_KIND;

		long id = Long.valueOf(mediaId);
		if (!Util.isNullOrEmpty(mediaOwnerUsername)
				&& Util.verifyUser(mediaOwnerUsername)) {
			key = KeyFactory.createKey(USER_KIND, mediaOwnerUsername);
		}

		Key ancestorKey = KeyFactory.createKey(key, kind, id);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		Iterable<Entity> entities = Util.getParentSpecificData(kind,
				ancestorKey, fetchOptions);

		BlobKey blobKey = null;
		if (entities.iterator().hasNext()) {
			Entity result = entities.iterator().next();
			blobKey = new BlobKey(result.getProperty(BLOB_KEY).toString());
			BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
			res.setContentLength(new Long(blobInfo.getSize()).intValue());
			res.setContentType(blobInfo.getContentType());

			try {
				blobstore.serve(blobKey, res);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void downloadImage(HttpServletResponse res, Key key,
			String mediaId, String mediaOwnerUsername) {
		String kind = USER_PHOTO_KIND;
		long id = Long.valueOf(mediaId);
		if (!Util.isNullOrEmpty(mediaOwnerUsername)
				&& Util.verifyUser(mediaOwnerUsername)) {
			key = KeyFactory.createKey(USER_KIND, mediaOwnerUsername);
		}

		Key ancestorKey = KeyFactory.createKey(key, kind, id);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		Iterable<Entity> entities = Util.getParentSpecificData(kind,
				ancestorKey, fetchOptions);
		Util.fetchAllDataFromBlobStore(res, entities);

	}

}
