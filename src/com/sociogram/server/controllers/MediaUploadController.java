package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.*;
import static com.sociogram.server.common.ISocioGramConstants.ACTION;
import static com.sociogram.server.common.ISocioGramConstants.DESCRIPTION;
import static com.sociogram.server.common.ISocioGramConstants.EMPTY_STRING;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.FIELDS_MISSING;
import static com.sociogram.server.common.ISocioGramConstants.GENERAL_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_UPLOAD_FAILED;
import static com.sociogram.server.common.ISocioGramConstants.INVALID_USER;
import static com.sociogram.server.common.ISocioGramConstants.IS_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.LENGTH;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_PRIVACY_LEVEL;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_UPLOAD_SOURCE;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_IMAGES_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.NO_OF_VIDEOS_UPLOADED;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE_ID;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE_REMOVED;
import static com.sociogram.server.common.ISocioGramConstants.PUBLIC;
import static com.sociogram.server.common.ISocioGramConstants.REMOVE_PROFILE_PIC;
import static com.sociogram.server.common.ISocioGramConstants.START_INDEX;
import static com.sociogram.server.common.ISocioGramConstants.TAG;
import static com.sociogram.server.common.ISocioGramConstants.UPPER_LIMIT;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO_THUMBNAIL;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class MediaUploadController extends HttpServlet {

	private final DatastoreService datastoreService = DatastoreServiceFactory
			.getDatastoreService();

	private static final Logger logger = Logger
			.getLogger(MediaUploadController.class.getCanonicalName());
	private final BlobInfoFactory blobInfoFactory = new BlobInfoFactory(
			datastoreService);

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String action = req.getParameter("action");
		if(action!=null && !"".equals(action)){
			if(REMOVE_PROFILE_PIC.equalsIgnoreCase(action)){
				removeProfileImage(req,  res);
			}
		} else{
			// this is for sending data only ... in form of jsons
			sendData(req,res);
		}
	}
	
	private void removeProfileImage(HttpServletRequest req,
			HttpServletResponse res) throws ServletException, IOException  {
		
		logger.log(Level.INFO, "In removeProfileImage()");
		
		String username = req.getParameter(USERNAME);
		String accessToken = req.getParameter(ACCESS_TOKEN);
		String action = req.getParameter(ACTION);
		
		logger.log(Level.INFO, "username="+username + "accessToken="+accessToken+"action="+action);
		
		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}
		if (Util.isNullOrEmpty(action)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			return;
		}

		Key key = KeyFactory.createKey(USER_KIND, username);

		Entity userProfileEntity = Util.findEntity(key);

		if (userProfileEntity == null) {
			logger.log(Level.INFO, "userProfileEntityu" + userProfileEntity);
			res.sendError(ERROR_CODE_400, INVALID_USER);
			return;
		}

		if (REMOVE_PROFILE_PIC.equalsIgnoreCase(action)) {
			removeProfilePic(res, userProfileEntity);
			return;
		}
		
	}

	public void sendData(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		String upperLimit = req.getParameter(UPPER_LIMIT);
		String startIndex = req.getParameter(START_INDEX);
		String accessToken = req.getParameter(ACCESS_TOKEN);
		String username = req.getParameter(USERNAME);
		String media = req.getParameter(MEDIA);

		if (Util.isNullOrEmpty(media)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			return;
		}

		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		int limit = IMAGE_LIMIT_PER_REQUEST;
		Key ancestorKey = KeyFactory.createKey(USER_KIND, username);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		if (!Util.isNullOrEmpty(startIndex)) {
			int offset = Integer.parseInt(startIndex);
			fetchOptions.offset(offset);
		}

		if (!Util.isNullOrEmpty(upperLimit)) {
			limit = Integer.valueOf(upperLimit);
			fetchOptions.limit(limit);
		}

		String[] searchBy = new String[1];
		Integer[] searchFor = new Integer[1];

		String mediaKind = EMPTY_STRING;
		if (IMAGE.equalsIgnoreCase(media)) {
			mediaKind = USER_PHOTO_KIND;
			searchBy[0] = IS_PROFILE_IMAGE;
			searchFor[0] = GENERAL_IMAGE;
		} else if (VIDEO.equalsIgnoreCase(media)) {
			mediaKind = USER_VIDEO_KIND;
		} else {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		Iterable<Entity> userMediaEntites = Util.listEntities(ancestorKey,
				mediaKind, searchBy, searchFor, fetchOptions);

		JSONObject userMediaObject = new JSONObject();
		JSONArray userMediaArray = Util.makeJSONArrayForUserMedia(
				userMediaEntites, limit);

		try {
			userMediaObject.put(USER_MEDIA, userMediaArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		res.getWriter().write(userMediaObject.toString());

	}

	@SuppressWarnings("deprecation")
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String username = null;
		String action = null;
		String mediaId = null;
		String description = "";
		String photoPrivacyLevel = PUBLIC; // default: public. Could be
											// private (In Future Version)
		String tag = ""; // location (In future version)
		int totalViews = 0; // default value = 0
		int totalLikes = 0; // default value = 0;
		int totalComments = 0; // default value = 0;
		String accessToken = "";
		String mediaUploadSource = "";

		int length = 0;
		logger.log(Level.INFO,
				"uploading by user: " + req.getParameter(USERNAME));

		ServletFileUpload upload = new ServletFileUpload();

		byte[] bytes = null;
		byte[] oldData = null;
		FileItemIterator iter;
		String contentType = null;
		try {
			iter = upload.getItemIterator(req);

			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();

				if (item.isFormField()) {

					if (USERNAME.equals(item.getFieldName())) {
						username = Streams.asString(stream);
					} else if (ACTION.equals(item.getFieldName())) {
						action = Streams.asString(stream);
					} else if (MEDIA_ID.equals(item.getFieldName())) {
						mediaId = Streams.asString(stream);
					} else if (DESCRIPTION.equals(item.getFieldName())) {
						description = Streams.asString(stream);
					} else if (MEDIA_PRIVACY_LEVEL.equals(item.getFieldName())) {
						photoPrivacyLevel = Streams.asString(stream);
					} else if (TAG.equals(item.getFieldName())) {
						tag = Streams.asString(stream);
					} else if (MEDIA_UPLOAD_SOURCE.equals(item.getFieldName())) {
						mediaUploadSource = Streams.asString(stream);
					} else if (ACCESS_TOKEN.equals(item.getFieldName())) {
						accessToken = Streams.asString(stream);
					} else if (LENGTH.equals(item.getFieldName())) {
						length = Integer.parseInt(Streams.asString(stream));
						if (length == 0) {
							length = oldData.length;
						}
					}

				} else {
					int len;
					oldData = null;
					bytes = new byte[stream.available()];
					while ((len = stream.read(bytes, 0, bytes.length)) != -1) {
						oldData = makeBufferFromChunks(bytes, len, oldData);
						bytes = new byte[8192];
					}
					String fieldName = item.getFieldName();
					String fileName = item.getName();
					contentType = item.getContentType();

					int l = stream.available();
					byte[] b = new byte[l];
					stream.read(b, 0, l);
					System.out.println("File field " + fieldName
							+ " with file name " + fileName + " detected.");
				}
			}
		} catch (FileUploadException e1) {
			e1.printStackTrace();
		}

		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().write(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}
		if (Util.isNullOrEmpty(action)) {
			res.sendError(ERROR_CODE_400, FIELDS_MISSING);
			res.getWriter().write(FIELDS_MISSING);
			return;
		}

		BlobKey blobKey = Util.putInBlobStore(contentType, oldData, length);
		logger.log(Level.INFO,
				"BlobKey for the file is: " + blobKey);
		if (Util.isNullOrEmpty(blobKey.toString())) {
			res.sendError(ERROR_CODE_400, IMAGE_UPLOAD_FAILED);
			res.getWriter().write(IMAGE_UPLOAD_FAILED);
			return;
		}

		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);

		Key key = KeyFactory.createKey(USER_KIND, username);

		Entity userProfileEntity = Util.findEntity(key);

		if (userProfileEntity == null) {
			res.sendError(ERROR_CODE_400, INVALID_USER);
			return;
		}

		if (REMOVE_PROFILE_PIC.equalsIgnoreCase(action)) {
			removeProfilePic(res, userProfileEntity);
			return;
		} else if (IMAGE.equalsIgnoreCase(action)
				|| PROFILE.equalsIgnoreCase(action)) {
			uploadImage(res, userProfileEntity, key, blobKey, tag, username,
					totalViews, totalLikes, totalComments, photoPrivacyLevel,
					description, mediaUploadSource, action);
		} else if (VIDEO.equalsIgnoreCase(action)) {
			uploadVideo(res, userProfileEntity, key, blobKey, tag, username,
					totalViews, totalLikes, totalComments, photoPrivacyLevel,
					description, mediaUploadSource, action);
		} else if (VIDEO_THUMBNAIL.equalsIgnoreCase(action)) {
			if(Util.isNullOrEmpty(mediaId)){
				res.sendError(ERROR_CODE_400, FIELDS_MISSING);
				res.getWriter().write(FIELDS_MISSING);
				return;
			}
			uploadVideoThumbnail(res, blobKey, username, action, mediaId);
		}

	}

	private void uploadVideoThumbnail(HttpServletResponse res, BlobKey blobKey,
			String username, String action, String mediaId) {

		Entity userVideoThumbnail = Util
				.createUserVideoThumbnailObjectInDatastore(blobKey, username,
						mediaId);

		try {
			res.getWriter().write("Thumbnail saved successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void uploadVideo(HttpServletResponse res, Entity userProfileEntity,
			Key key, BlobKey blobKey, String tag, String username,
			int totalViews, int totalLikes, int totalComments,
			String photoPrivacyLevel, String description,
			String mediaUploadSource, String action) {

		Entity userVideo = Util.createUserVideoObjectInDatastore(key, blobKey,
				tag, username, totalViews, totalLikes, totalComments,
				photoPrivacyLevel, description, mediaUploadSource, action);

		long videoId = userVideo.getKey().getId();

		JSONObject videoObject = null;
		videoObject = Util.makeUserMediaJsonObject(userVideo);

		int videosUploaded = Integer.valueOf(userProfileEntity.getProperty(
				NO_OF_VIDEOS_UPLOADED).toString());
		videosUploaded = videosUploaded + 1;
		userProfileEntity.setProperty(NO_OF_VIDEOS_UPLOADED, videosUploaded);
		Util.persistEntity(userProfileEntity);

		if (videoObject != null) {
			try {
				res.getWriter().write(videoObject.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void uploadImage(HttpServletResponse res, Entity userProfileEntity,
			Key key, BlobKey blobKey, String tag, String username,
			int totalViews, int totalLikes, int totalComments,
			String photoPrivacyLevel, String description,
			String mediaUploadSource, String action) {

		Entity userPhoto = Util.createUserPhotoObjectInDatastore(key, blobKey,
				tag, username, totalViews, totalLikes, totalComments,
				photoPrivacyLevel, description, mediaUploadSource, action);

		long imageId = userPhoto.getKey().getId();

		JSONObject photoObject = null;
		if (PROFILE.equalsIgnoreCase(action)) {
			userProfileEntity.setProperty(PROFILE_IMAGE_ID, imageId);
			Util.persistEntity(userProfileEntity);
			return;
		} else if (IMAGE.equalsIgnoreCase(action)) {
			photoObject = Util.makeUserMediaJsonObject(userPhoto);

			int imagesUploaded = Integer.valueOf(userProfileEntity.getProperty(
					NO_OF_IMAGES_UPLOADED).toString());
			imagesUploaded = imagesUploaded + 1;
			userProfileEntity
					.setProperty(NO_OF_IMAGES_UPLOADED, imagesUploaded);
			Util.persistEntity(userProfileEntity);
		}

		if (photoObject != null) {
			try {
				res.getWriter().write(photoObject.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private byte[] makeBufferFromChunks(byte[] buffer, int bytesRead,
			byte[] oldData) {

		int newSize = bytesRead;
		int length = 0;
		if (oldData != null) {
			newSize = bytesRead + oldData.length;
			length = oldData.length;
		}
		byte[] data = new byte[newSize];
		if (oldData != null) {
			System.arraycopy(oldData, 0, data, 0, oldData.length);
			System.arraycopy(buffer, 0, data, oldData.length, bytesRead);
		} else {
			System.arraycopy(buffer, 0, data, 0, bytesRead);
		}

		// int i = 0;
		// for (i = 0; i < length; i++) {
		// data[i] = oldData[i];
		// }
		// for (int j = 0; j < bytesRead; i++, j++) {
		// data[i] = buffer[j];
		// }

		return data;

	}

	private void removeProfilePic(HttpServletResponse res,
			Entity userProfileEntity) {
		
		logger.log(Level.INFO, "In removeProfilePic() start");
		
		userProfileEntity.setProperty(PROFILE_IMAGE_ID, EMPTY_STRING);
		Util.persistEntity(userProfileEntity);
		try {
			res.getWriter().print(PROFILE_IMAGE_REMOVED);
			logger.log(Level.INFO, "In removeProfileImage() end " + userProfileEntity.getProperty(PROFILE_IMAGE_ID));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
