package com.sociogram.server.common;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.BLOB_KEY;
import static com.sociogram.server.common.ISocioGramConstants.COMMENTED_BY_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_ID;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_TEXT;
import static com.sociogram.server.common.ISocioGramConstants.COMMENT_TIME_STAMP;
import static com.sociogram.server.common.ISocioGramConstants.DESCRIPTION;
import static com.sociogram.server.common.ISocioGramConstants.FACEBOOK;
import static com.sociogram.server.common.ISocioGramConstants.GENERAL_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IMG;
import static com.sociogram.server.common.ISocioGramConstants.IS_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.IS_THUMBNAIL_FOR_VIDEO;
import static com.sociogram.server.common.ISocioGramConstants.LIKED_BY_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.LIKE_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_PRIVACY_LEVEL;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_SOURCE_FACEBOOK;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_SOURCE_SOCIOGRAM;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_SOURCE_TWITTER;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_UPLOAD_SOURCE;
import static com.sociogram.server.common.ISocioGramConstants.POPULAR_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE;
import static com.sociogram.server.common.ISocioGramConstants.PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.TAG;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_COMMENTS;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_LIKES;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_VIEWS;
import static com.sociogram.server.common.ISocioGramConstants.TWITTER;
import static com.sociogram.server.common.ISocioGramConstants.UPLOADED_DATE;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USER_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.USER_VIDEO_THUMBNAIL_KIND;
import static com.sociogram.server.common.ISocioGramConstants.VID;
import static com.sociogram.server.common.ISocioGramConstants.VIDEO;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class Util {

	private static final Logger logger = Logger.getLogger(Util.class
			.getCanonicalName());
	private static final DatastoreService datastore = DatastoreServiceFactory
			.getDatastoreService();
	private static final BlobstoreService blobstore = BlobstoreServiceFactory
			.getBlobstoreService();

	private static final BlobInfoFactory blobInfoFactory = new BlobInfoFactory(
			datastore);

	/**
	 * 
	 * @param entity
	 *            : entity to be persisted
	 */
	public static void persistEntity(Entity entity) {
		logger.log(Level.INFO, "Saving entity");
		datastore.put(entity);
	}

	/**
	 * Delete the entity from persistent store represented by the key
	 * 
	 * @param key
	 *            : key to delete the entity from the persistent store
	 */
	public static void deleteEntity(Key key) {
		logger.log(Level.INFO, "Deleting entity");
		datastore.delete(key);
	}

	/**
	 * Delete the entities from persistent store represented by their respective
	 * keys
	 * 
	 * @param Iterable
	 *            <Entity> : key to delete the entity from the persistent store
	 */
	public static void deleteEntity(Iterable<Entity> entities) {
		logger.log(Level.INFO, "Deleting entities");
		for (Entity entity : entities) {
			datastore.delete(entity.getKey());
		}

	}

	/**
	 * Delete list of entities given their keys
	 * 
	 * @param keys
	 */
	public static void deleteEntity(final List<Key> keys) {
		datastore.delete(new Iterable<Key>() {
			public Iterator<Key> iterator() {
				return keys.iterator();
			}
		});
	}

	/**
	 * Search and return the entity from datastore.
	 * 
	 * @param key
	 *            : key to find the entity
	 * @return entity
	 */

	public static Entity findEntity(Key key) {
		logger.log(Level.INFO, "Search the entity with key: " + key);
		try {
			return datastore.get(key);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	/***
	 * Search entities based on search criteria
	 * 
	 * @param kind
	 * @param searchBy
	 *            : Searching Criteria (Property)
	 * @param searchFor
	 *            : Searching Value (Property Value)
	 * @return List all entities of a kind from the cache or datastore (if not
	 *         in cache) with the specified properties
	 */
	private static Iterable<Entity> listEntities(String kind, String searchBy,
			String searchFor) {
		logger.log(Level.INFO, "Search entities based on search criteria");
		Query q = new Query(kind);
		if (searchFor != null && !"".equals(searchFor)) {
			q.addFilter(searchBy, FilterOperator.EQUAL, searchFor);
		}
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable();
	}

	/***
	 * Search entities based on search criteria
	 * 
	 * @param ancestorKey
	 * @param kind
	 * @param searchBy
	 *            : Searching Criteria (Property)
	 * @param searchFor
	 *            : Searching Value (Property Value)
	 * @param fetchOptions
	 * @return List all entities of a kind from the cache or datastore (if not
	 *         in cache) with the specified properties
	 */
	public static Iterable<Entity> listEntities(Key ancestorKey, String kind,
			String[] searchBy, Object[] searchFor, FetchOptions fetchOptions) {
		logger.log(Level.INFO, "Search entities based on search criteria");
		Query q = new Query(kind);
		if (ancestorKey != null) {
			q.setAncestor(ancestorKey);
		}
		for (int i = 0; i < searchBy.length; i++) {
			if (searchFor[i] != null && !"".equals(searchFor[i])) {
				q.addFilter(searchBy[i], FilterOperator.EQUAL, searchFor[i]);
			}
		}
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable(fetchOptions);
	}

	/**
	 * Search entities based on ancestor
	 * 
	 * @param kind
	 * @param ancestor
	 * @return
	 */
	public static Iterable<Entity> listChildren(String kind, Key ancestor) {
		logger.log(Level.INFO, "Search entities based on parent");
		Query q = new Query(kind);
		q.setAncestor(ancestor);
		q.addFilter(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN,
				ancestor);
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable();
	}

	/**
	 * 
	 * @param kind
	 * @param ancestor
	 * @return
	 */
	public static Iterable<Entity> listChildKeys(String kind, Key ancestor) {
		logger.log(Level.INFO, "Search entities based on parent");
		Query q = new Query(kind);
		q.setAncestor(ancestor).setKeysOnly();
		q.addFilter(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN,
				ancestor);
		PreparedQuery pq = datastore.prepare(q);
		return pq.asIterable();
	}

	/**
	 * List the entities in JSON format
	 * 
	 * @param entities
	 *            entities to return as JSON strings
	 * @return
	 */
	public static String writeJSON(Iterable<Entity> entities) {
		logger.log(Level.INFO, "creating JSON format object");
		StringBuilder sb = new StringBuilder();

		int i = 0;
		sb.append("{\"data\": [");
		for (Entity result : entities) {
			Map<String, Object> properties = result.getProperties();
			sb.append("{");
			if (result.getKey().getName() == null)
				sb.append("\"name\" : \"" + result.getKey().getId() + "\",");
			else
				sb.append("\"name\" : \"" + result.getKey().getName() + "\",");

			for (String key : properties.keySet()) {
				sb.append("\"" + key + "\" : \"" + properties.get(key) + "\",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append("},");
			i++;
		}
		if (i > 0) {
			sb.deleteCharAt(sb.lastIndexOf(","));
		}
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * Retrieves Parent and Child entities into JSON String
	 * 
	 * @param entities
	 *            : List of parent entities
	 * @param childKind
	 *            : Entity type for Child
	 * @param fkName
	 *            : foreign-key to the parent in the child entity
	 * @return JSON string
	 */
	public static String writeJSON(Iterable<Entity> entities, String childKind,
			String fkName) {
		logger.log(Level.INFO,
				"creating JSON format object for parent child relation");
		StringBuilder sb = new StringBuilder();
		int i = 0;
		sb.append("{\"data\": [");
		for (Entity result : entities) {
			Map<String, Object> properties = result.getProperties();
			sb.append("{");
			if (result.getKey().getName() == null)
				sb.append("\"name\" : \"" + result.getKey().getId() + "\",");
			else
				sb.append("\"name\" : \"" + result.getKey().getName() + "\",");
			for (String key : properties.keySet()) {
				sb.append("\"" + key + "\" : \"" + properties.get(key) + "\",");
			}
			Iterable<Entity> child = listEntities(childKind, fkName,
					String.valueOf(result.getKey().getId()));
			for (Entity en : child) {
				for (String key : en.getProperties().keySet()) {
					sb.append("\"" + key + "\" : \""
							+ en.getProperties().get(key) + "\",");
				}
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append("},");
			i++;
		}
		if (i > 0) {
			sb.deleteCharAt(sb.lastIndexOf(","));
		}
		sb.append("]}");
		return sb.toString();
	}

	/**
	 * Takes the photos uploaded by user and returns its JSON array.
	 * 
	 * @param userMedia
	 * @param upperLimit
	 * @return JSON Array object
	 */
	public static JSONArray makeJSONArrayForUserMedia(
			Iterable<Entity> userMedia, int upperLimit) {
		JSONArray MediaArray = new JSONArray();
		JSONObject mediaObject = null;
		int count = 0;

		// this iterable should start from the startIndex provided:
		// IMPLEMENTATION PENDING

		for (Entity media : userMedia) {
			if (count == upperLimit) {
				break;
			}
			mediaObject = new JSONObject();

			try {

				mediaObject.put(MEDIA_ID, media.getKey().getId());
				mediaObject.put(MEDIA_UPLOAD_SOURCE,
						media.getProperty(MEDIA_UPLOAD_SOURCE));
				mediaObject.put(MEDIA_OWNER_USERNAME,
						media.getProperty(MEDIA_OWNER_USERNAME));
				if (media.hasProperty(IS_PROFILE_IMAGE)) {
					mediaObject.put(IS_PROFILE_IMAGE,
							media.getProperty(IS_PROFILE_IMAGE));
				}
				Date date = (Date) media.getProperty(UPLOADED_DATE);
				mediaObject.put(UPLOADED_DATE, date.getTime());
				mediaObject.put(TAG, media.getProperty(TAG));
				mediaObject.put(DESCRIPTION, media.getProperty(DESCRIPTION));
				mediaObject.put(TOTAL_VIEWS, media.getProperty(TOTAL_VIEWS));
				mediaObject.put(MEDIA_PRIVACY_LEVEL,
						media.getProperty(MEDIA_PRIVACY_LEVEL));
				mediaObject.put(TOTAL_LIKES, media.getProperty(TOTAL_LIKES));
				mediaObject.put(TOTAL_COMMENTS,
						media.getProperty(TOTAL_COMMENTS));

			} catch (JSONException e) {
				e.printStackTrace();
			}

			MediaArray.put(mediaObject);
			count++;
		}
		return MediaArray;
	}

	/**
	 * Takes the photos uploaded by user and returns its JSON array.
	 * 
	 * @return JSON Array object
	 * @param popularPhotosList
	 * @param upperLimit
	 */
	public static JSONArray makeJSONArrayFor20RandomPopularPhotos(
			List<Entity> popularPhotosList, int upperLimit) {
		JSONArray photoArray = new JSONArray();
		JSONObject photoObject = null;
		Entity popularPhoto = null;
		int count = 0;

		Random random = new Random();
		int i = 0;
		HashMap<Integer, Entity> popularPhotoMap = new HashMap<Integer, Entity>();
		Integer[] indexArray = new Integer[upperLimit];
		while (count < upperLimit) {
			i = random.nextInt(popularPhotosList.size());
			if (popularPhotoMap.get(i) == null) {
				popularPhotoMap.put(i, popularPhotosList.get(i));
				indexArray[count] = i;
				count++;
			}
		}

		for (i = 0; i < indexArray.length; i++) {
			popularPhoto = popularPhotoMap.get(indexArray[i]);

			photoObject = new JSONObject();

			try {

				photoObject.put(MEDIA_ID, popularPhoto.getProperty(MEDIA_ID));
				photoObject.put(IS_PROFILE_IMAGE,
						popularPhoto.getProperty(IS_PROFILE_IMAGE));
				photoObject.put(MEDIA_UPLOAD_SOURCE,
						popularPhoto.getProperty(MEDIA_UPLOAD_SOURCE));
				photoObject.put(MEDIA_OWNER_USERNAME,
						popularPhoto.getProperty(MEDIA_OWNER_USERNAME));
				Date date = (Date) popularPhoto.getProperty(UPLOADED_DATE);
				photoObject.put(UPLOADED_DATE, date.getTime());
				photoObject.put(TAG, popularPhoto.getProperty(TAG));
				photoObject.put(DESCRIPTION,
						popularPhoto.getProperty(DESCRIPTION));
				photoObject.put(TOTAL_VIEWS,
						popularPhoto.getProperty(TOTAL_VIEWS));
				photoObject.put(MEDIA_PRIVACY_LEVEL,
						popularPhoto.getProperty(MEDIA_PRIVACY_LEVEL));
				photoObject.put(TOTAL_LIKES,
						popularPhoto.getProperty(TOTAL_LIKES));
				photoObject.put(TOTAL_COMMENTS,
						popularPhoto.getProperty(TOTAL_COMMENTS));

			} catch (JSONException e) {
				e.printStackTrace();
			}

			photoArray.put(photoObject);

		}
		return photoArray;

	}

	/**
	 * Utility method to send the error back to UI
	 * 
	 * @throws IOException
	 * @return
	 * @param ex
	 */
	public static String getErrorMessage(Exception ex) throws IOException {
		return "Error:" + ex.toString();
	}

	/**
	 * get DatastoreService instance
	 * 
	 * @return DatastoreService instance
	 */
	public static DatastoreService getDatastoreServiceInstance() {
		return datastore;
	}

	/**
	 * get BlobstoreService instance
	 * 
	 * @return BlobstoreService instance
	 */
	public static BlobstoreService getBlobstoreServiceInstance() {
		return blobstore;
	}

	public static Iterable<Entity> getParentSpecificData(String kind,
			Key ancestorKey, FetchOptions fetchOptions) {
		// Key ancestorKey = KeyFactory.createKey(USER, username);
		Query q = new Query(kind);
		q.setAncestor(ancestorKey);
		// q.addFilter(Entity.KEY_RESERVED_PROPERTY,
		// FilterOperator.GREATER_THAN, ancestorKey);
		PreparedQuery pq = datastore.prepare(q);
		// Iterable<Entity> entities = pq.asIterable(fetchOptions);
		return pq.asIterable(fetchOptions);
	}

	public static Iterable<Entity> getAllDataOfKind(String kind) {
		Query q = new Query(kind);
		PreparedQuery pq = datastore.prepare(q);
		// Iterable<Entity> entities = pq.asIterable();
		return pq.asIterable();

	}

	/**
	 * Utility method to check if the passed string is empty or null Returns
	 * true if string is empty or null, false otherwise.
	 * 
	 * @return
	 * @param field
	 */
	public static boolean isNullOrEmpty(String field) {
		if (null == field || "".equals(field)) {
			return true;
		}
		return false;
	}

	public static JSONObject createJSONFromRequest(HttpServletRequest request) {

		BufferedReader bufferedReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		JSONObject jsonObject = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			stringBuilder = null;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		try {
			if (stringBuilder != null) {
				jsonObject = new JSONObject(stringBuilder.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			jsonObject = null;
		}
		return jsonObject;
	}

	public static byte[] makeBufferFromChunks(byte[] buffer, int bytesRead,
			byte[] oldData) {

		int newSize = bytesRead;
		int length = 0;
		if (oldData != null) {
			newSize = bytesRead + oldData.length;
			length = oldData.length;
		}
		byte[] data = new byte[newSize];
		int i = 0;

		if (oldData != null) {
			System.arraycopy(oldData, 0, data, 0, oldData.length);
			System.arraycopy(buffer, 0, data, oldData.length, bytesRead);
		} else {
			System.arraycopy(buffer, 0, data, 0, bytesRead);
		}

		// for (i = 0; i < length; i++) {
		// data[i] = oldData[i];
		// }
		// for (int j = 0; j < bytesRead; i++, j++) {
		// data[i] = buffer[j];
		// }

		return data;

	}

	public static BlobKey putInBlobStore(String contentType, byte[] filebytes,
			int length) {
		if (filebytes == null || filebytes.length == 0) {
			return new BlobKey("");
		}

		// Get a file service
		FileService fileService = FileServiceFactory.getFileService();
		AppEngineFile file = null;
		try {
			file = fileService.createNewBlobFile(contentType);

			// Open a channel to write to it
			boolean lock = true;
			FileWriteChannel writeChannel = fileService.openWriteChannel(file,
					lock);

			// This time we write to the channel using standard Java
			BufferedInputStream in = new BufferedInputStream(
					new ByteArrayInputStream(filebytes));
			byte[] buffer;
			// int defaultBufferSize = 524288;
			buffer = new byte[length];
			int read;
			while ((read = in.read(buffer)) > 0) { // -1 means EndOfStream
				System.out.println(read);
				ByteBuffer bb = ByteBuffer.wrap(buffer);
				writeChannel.write(bb);
			}
			writeChannel.closeFinally();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileService.getBlobKey(file);
	}

	public static String generateAccessToken(String username) {
		String accessToken = username + new Date().getTime();
		Key key = KeyFactory.createKey(USER_KIND, username);
		Entity entity = Util.findEntity(key);
		if (entity == null) {
			accessToken = "";
		}
		entity.setProperty(ACCESS_TOKEN, accessToken);
		Util.persistEntity(entity);
		return accessToken;
	}

	public static boolean verifyAccessToken(String username, String accessToken) {

		if (Util.isNullOrEmpty(accessToken) || Util.isNullOrEmpty(username)) {
			return false;
		}

		Key key = KeyFactory.createKey(USER_KIND, username);
		Entity entity = Util.findEntity(key);
		if (entity == null) {
			return false;
		}
		String userAccessToken = entity.getProperty(ACCESS_TOKEN).toString();
		if (accessToken.equalsIgnoreCase(userAccessToken)) {
			return true;
		}

		return false;
	}

	public static boolean verifyUser(String username) {

		Key key = KeyFactory.createKey(USER_KIND, username);
		Entity entity = Util.findEntity(key);
		if (entity == null) {
			return false;
		}

		return true;
	}

	public static void fetchAllDataFromBlobStore(HttpServletResponse res,
			Iterable<Entity> entities) {

		byte[] responseBytes = null;
		BlobKey blobKey = null;
		BlobInfo blobInfo = null;
		if (entities.iterator().hasNext()) {
			Entity result = entities.iterator().next();
			blobKey = new BlobKey(result.getProperty(BLOB_KEY).toString());
			blobInfo = blobInfoFactory.loadBlobInfo(blobKey);

			long length = blobInfo.getSize();
			if (length > 1015808) {
				fetchDataFromBlobStoreInChunks(res, blobKey, blobInfo);
			} else {
				responseBytes = blobstore.fetchData(blobKey, 0, length);
			}
		}

		if (blobInfo != null && responseBytes != null) {
			sendImageDataInResponse(res, blobInfo, responseBytes);
		}
	}

	private static void fetchDataFromBlobStoreInChunks(HttpServletResponse res,
			BlobKey blobKey, BlobInfo blobInfo) {

		long totalBytesToRead = blobInfo.getSize();
		int bufferSize = 8192;
		if (bufferSize > totalBytesToRead) {
			bufferSize = (int) totalBytesToRead;
		}

		byte[] oldData = null;
		byte[] blobBytes = null;
		long startIndex = 0;
		int endIndex = bufferSize;

		while (startIndex < totalBytesToRead) {
			blobBytes = blobstore.fetchData(blobKey, startIndex, endIndex);
			oldData = Util.makeBufferFromChunks(blobBytes, blobBytes.length,
					oldData);
			startIndex = endIndex + 1;

			if (startIndex > totalBytesToRead) {
				startIndex = totalBytesToRead;
			}

			blobBytes = null;
			long diff = Math.abs(totalBytesToRead - startIndex);
			if (diff < bufferSize) {
				endIndex += (int) diff;
			} else {
				endIndex += bufferSize;
			}

			if (endIndex > totalBytesToRead) {
				endIndex = (int) totalBytesToRead;
			}
		}
		if (blobInfo != null && oldData != null) {
			sendImageDataInResponse(res, blobInfo, oldData);
		}

	}

	public static String makeJSONArrayForLikes(Iterable<Entity> likeEntities) {
		JSONObject likeObject = null;
		JSONArray likeArray = new JSONArray();
		for (Entity like : likeEntities) {

			likeObject = new JSONObject();
			try {
				likeObject.put(LIKE_ID, like.getKey().getId());
				likeObject.put(MEDIA_ID, like.getProperty(MEDIA_ID));
				likeObject.put(LIKED_BY_USERNAME,
						like.getProperty(LIKED_BY_USERNAME));
				if(IMG == Integer.parseInt(like.getProperty(MEDIA).toString())){
					likeObject.put(MEDIA, IMAGE);
				}else if(VID == Integer.parseInt(like.getProperty(MEDIA).toString())){
					likeObject.put(MEDIA, VIDEO);
				}
				likeObject.put(MEDIA_OWNER_USERNAME,
						like.getProperty(MEDIA_OWNER_USERNAME));
				likeArray.put(likeObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		likeObject = new JSONObject();
		try {
			likeObject.put("likes", likeArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return likeObject.toString();
	}

	public static String makeJSONObjectForLike(Entity likeEntity) {
		JSONObject likeObject = null;
		likeObject = new JSONObject();
		try {
			likeObject.put(LIKE_ID, likeEntity.getKey().getId());
			likeObject.put(MEDIA_ID, likeEntity.getProperty(MEDIA_ID));
			if(IMG == Integer.parseInt(likeEntity.getProperty(MEDIA).toString())){
				likeObject.put(MEDIA, IMAGE);
			}else if(VID == Integer.parseInt(likeEntity.getProperty(MEDIA).toString())){
				likeObject.put(MEDIA, VIDEO);
			}
			likeObject.put(LIKED_BY_USERNAME,
					likeEntity.getProperty(LIKED_BY_USERNAME));
			likeObject.put(MEDIA_OWNER_USERNAME,
					likeEntity.getProperty(MEDIA_OWNER_USERNAME));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return likeObject.toString();
	}

	private static void sendImageDataInResponse(HttpServletResponse res,
			BlobInfo blobInfo, byte[] responseBytes) {
		String contentDisposition = "attachment; filename="
				+ blobInfo.getFilename();
		res.addHeader("Content-Length", String.valueOf(blobInfo.getSize()));
		res.addHeader("Content-Disposition", contentDisposition);
		res.setContentType(blobInfo.getContentType());

		try {
			res.getOutputStream().write(responseBytes);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String makeJSONArrayForComments(
			Iterable<Entity> commentEntities) {
		JSONObject commentObject = null;
		JSONArray commentArray = new JSONArray();
		Date date = null;

		for (Entity comment : commentEntities) {

			commentObject = new JSONObject();

			try {
				date = (Date) comment.getProperty(COMMENT_TIME_STAMP);

				commentObject.put(COMMENT_TIME_STAMP, date.getTime());
				commentObject.put(MEDIA_ID, comment.getProperty(MEDIA_ID));
				
				if(IMG == Integer.parseInt(comment.getProperty(MEDIA).toString())){
					commentObject.put(MEDIA, IMAGE);
				}else if(VID == Integer.parseInt(comment.getProperty(MEDIA).toString())){
					commentObject.put(MEDIA, VIDEO);
				}
				
				commentObject.put(COMMENT_TEXT,
						comment.getProperty(COMMENT_TEXT));
				commentObject.put(COMMENTED_BY_USERNAME,
						comment.getProperty(COMMENTED_BY_USERNAME));
				commentObject.put(MEDIA_OWNER_USERNAME,
						comment.getProperty(MEDIA_OWNER_USERNAME));
				commentObject.put(COMMENT_ID, comment.getKey().getId());

				commentArray.put(commentObject);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		commentObject = new JSONObject();
		try {
			commentObject.put("comments", commentArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return commentObject.toString();
	}

	public static String makeJSONObjectForComment(Entity commentEntity) {
		JSONObject commentObject = null;
		JSONArray commentArray = new JSONArray();
		Date date = null;
		commentObject = new JSONObject();

		try {
			date = (Date) commentEntity.getProperty(COMMENT_TIME_STAMP);

			commentObject.put(COMMENT_TIME_STAMP, date.getTime());
			commentObject.put(MEDIA_ID, commentEntity.getProperty(MEDIA_ID));
			if(IMG == Integer.parseInt(commentEntity.getProperty(MEDIA).toString())){
				commentObject.put(MEDIA, IMAGE);
			}else if(VID == Integer.parseInt(commentEntity.getProperty(MEDIA).toString())){
				commentObject.put(MEDIA, VIDEO);
			}
			commentObject.put(COMMENT_TEXT,
					commentEntity.getProperty(COMMENT_TEXT));
			commentObject.put(COMMENTED_BY_USERNAME,
					commentEntity.getProperty(COMMENTED_BY_USERNAME));
			commentObject.put(MEDIA_OWNER_USERNAME,
					commentEntity.getProperty(MEDIA_OWNER_USERNAME));
			commentObject.put(COMMENT_ID, commentEntity.getKey().getId());

			commentArray.put(commentObject);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return commentObject.toString();
	}

	public static Entity createUserPhotoObjectInDatastore(Key key,
			BlobKey blobKey, String tag, String username, int totalViews,
			int totalLikes, int totalComments, String photoPrivacyLevel,
			String description, String mediaUploadSource, String action) {

		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
		Entity userPhoto = new Entity(USER_PHOTO_KIND, key);
		userPhoto.setProperty(BLOB_KEY, blobKey.getKeyString());
		userPhoto.setProperty(TAG, tag);
		userPhoto.setProperty(MEDIA_OWNER_USERNAME, username);
		userPhoto.setProperty(UPLOADED_DATE, blobInfo.getCreation());
		userPhoto.setProperty(TOTAL_VIEWS, totalViews);
		userPhoto.setProperty(TOTAL_LIKES, totalLikes);
		userPhoto.setProperty(TOTAL_COMMENTS, totalComments);
		userPhoto.setProperty(MEDIA_PRIVACY_LEVEL, photoPrivacyLevel);
		userPhoto.setProperty(DESCRIPTION, description);
		int imageUploadSourceId = MEDIA_SOURCE_SOCIOGRAM;
		if (mediaUploadSource.equalsIgnoreCase(FACEBOOK)) {
			imageUploadSourceId = MEDIA_SOURCE_FACEBOOK;
		} else if (mediaUploadSource.equalsIgnoreCase(TWITTER)) {
			imageUploadSourceId = MEDIA_SOURCE_TWITTER;
		}

		userPhoto.setProperty(MEDIA_UPLOAD_SOURCE, imageUploadSourceId);

		if (IMAGE.equalsIgnoreCase(action)) {
			userPhoto.setProperty(IS_PROFILE_IMAGE, GENERAL_IMAGE);
		} else if (PROFILE.equalsIgnoreCase(action)) {
			userPhoto.setProperty(IS_PROFILE_IMAGE, PROFILE_IMAGE);
		}

		Util.persistEntity(userPhoto);

		return userPhoto;

	}

	public static Entity createUserVideoObjectInDatastore(Key key,
			BlobKey blobKey, String tag, String username, int totalViews,
			int totalLikes, int totalComments, String photoPrivacyLevel,
			String description, String mediaUploadSource, String action) {

		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
		Entity userVideo = new Entity(USER_VIDEO_KIND, key);
		userVideo.setProperty(BLOB_KEY, blobKey.getKeyString());
		userVideo.setProperty(TAG, tag);
		userVideo.setProperty(MEDIA_OWNER_USERNAME, username);
		userVideo.setProperty(UPLOADED_DATE, blobInfo.getCreation());
		userVideo.setProperty(TOTAL_VIEWS, totalViews);
		userVideo.setProperty(TOTAL_LIKES, totalLikes);
		userVideo.setProperty(TOTAL_COMMENTS, totalComments);
		userVideo.setProperty(MEDIA_PRIVACY_LEVEL, photoPrivacyLevel);
		userVideo.setProperty(DESCRIPTION, description);
		int videoUploadSourceId = MEDIA_SOURCE_SOCIOGRAM;
		if (mediaUploadSource.equalsIgnoreCase(FACEBOOK)) {
			videoUploadSourceId = MEDIA_SOURCE_FACEBOOK;
		} else if (mediaUploadSource.equalsIgnoreCase(TWITTER)) {
			videoUploadSourceId = MEDIA_SOURCE_TWITTER;
		}

		userVideo.setProperty(MEDIA_UPLOAD_SOURCE, videoUploadSourceId);

		Util.persistEntity(userVideo);

		return userVideo;

	}

	
	public static Entity createUserVideoThumbnailObjectInDatastore(BlobKey blobKey,String username, String mediaId) {

		long id = Long.valueOf(mediaId);
		Key key = KeyFactory.createKey(USER_VIDEO_KIND, id);
		
		Entity userVideoThumbnail = new Entity(USER_VIDEO_THUMBNAIL_KIND, key);
		
		userVideoThumbnail.setProperty(USERNAME, username);
		userVideoThumbnail.setProperty(BLOB_KEY, blobKey.getKeyString());
		userVideoThumbnail.setProperty(IS_THUMBNAIL_FOR_VIDEO, id);
		
		Util.persistEntity(userVideoThumbnail);

		return userVideoThumbnail;

	}

	
	public static JSONObject makeUserMediaJsonObject(Entity userMedia) {
		JSONObject mediaObject = new JSONObject();
		try {
			mediaObject.put(MEDIA_ID, userMedia.getKey().getId());

			mediaObject.put(MEDIA_UPLOAD_SOURCE,
					userMedia.getProperty(MEDIA_UPLOAD_SOURCE));
			mediaObject.put(MEDIA_OWNER_USERNAME,
					userMedia.getProperty(MEDIA_OWNER_USERNAME));
			Date date = (Date) userMedia.getProperty(UPLOADED_DATE);
			mediaObject.put(UPLOADED_DATE, date.getTime());
			mediaObject.put(TAG, userMedia.getProperty(TAG));
			mediaObject.put(DESCRIPTION, userMedia.getProperty(DESCRIPTION));
			mediaObject.put(TOTAL_VIEWS, userMedia.getProperty(TOTAL_VIEWS));
			mediaObject.put(MEDIA_PRIVACY_LEVEL,
					userMedia.getProperty(MEDIA_PRIVACY_LEVEL));
			mediaObject.put(TOTAL_LIKES, userMedia.getProperty(TOTAL_LIKES));
			mediaObject.put(TOTAL_COMMENTS,
					userMedia.getProperty(TOTAL_COMMENTS));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return mediaObject;
	}

	public static void updatePopularPhotoData(long id,String propertyName, int propertyValue) {
		String[] searchBy = new String[1];
		searchBy[0] = MEDIA_ID;
		Object[] searchFor = new Object[1];
		searchFor[0] = id;

		Iterable<Entity> popularPhotoEntities = Util.listEntities(null,
				POPULAR_PHOTO_KIND, searchBy, searchFor,
				FetchOptions.Builder.withDefaults());
		if(popularPhotoEntities.iterator().hasNext()){
			Entity popularPhotoEntity = popularPhotoEntities.iterator().next();
			popularPhotoEntity.setProperty(propertyName, propertyValue);
			Util.persistEntity(popularPhotoEntity);
			if(TOTAL_LIKES.equalsIgnoreCase(propertyName) && propertyValue == 0){
				Util.deleteEntity(popularPhotoEntity.getKey());
			}
		}
		
	}
}
