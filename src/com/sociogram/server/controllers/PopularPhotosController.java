package com.sociogram.server.controllers;

import static com.sociogram.server.common.ISocioGramConstants.ACCESS_TOKEN;
import static com.sociogram.server.common.ISocioGramConstants.DESCRIPTION;
import static com.sociogram.server.common.ISocioGramConstants.ERROR_CODE_400;
import static com.sociogram.server.common.ISocioGramConstants.IMAGE_LIMIT_PER_REQUEST;
import static com.sociogram.server.common.ISocioGramConstants.IS_PROFILE_IMAGE;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_ID;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_OWNER_USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_PRIVACY_LEVEL;
import static com.sociogram.server.common.ISocioGramConstants.MEDIA_UPLOAD_SOURCE;
import static com.sociogram.server.common.ISocioGramConstants.POPULAR_PHOTO;
import static com.sociogram.server.common.ISocioGramConstants.POPULAR_PHOTO_KIND;
import static com.sociogram.server.common.ISocioGramConstants.TAG;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_COMMENTS;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_LIKES;
import static com.sociogram.server.common.ISocioGramConstants.TOTAL_VIEWS;
import static com.sociogram.server.common.ISocioGramConstants.UPLOADED_DATE;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME;
import static com.sociogram.server.common.ISocioGramConstants.USERNAME_ACCESS_TOKEN_INVALID;
import static com.sociogram.server.common.ISocioGramConstants.USER_PHOTO_KIND;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sociogram.server.common.Util;

public class PopularPhotosController extends HttpServlet {
	private final DatastoreService datastore = Util.getDatastoreServiceInstance();
	private static final Logger logger = Logger.getLogger(PopularPhotosController.class
			.getCanonicalName());

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		Iterable<Entity> oldPopularPhotosData = Util
				.getAllDataOfKind(POPULAR_PHOTO_KIND);
		
		Util.deleteEntity(oldPopularPhotosData);
		

		Query query = new Query(USER_PHOTO_KIND);
		query.addFilter(TOTAL_LIKES, FilterOperator.GREATER_THAN, 0);
//		query.addSort(IS_PROFILE_IMAGE, SortDirection.DESCENDING);
		query.addSort(TOTAL_LIKES, SortDirection.DESCENDING);
		// query.addSort(TOTAL_VIEWS, SortDirection.DESCENDING);
		// query.addSort(TOTAL_COMMENTS, SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(query);
		Iterable<Entity> popularPhotos = pq.asIterable(FetchOptions.Builder
				.withLimit(50));

		logger.log(Level.INFO, "Query for cron job: " + query.toString());
		
		Entity popularPhotoEntity = null;
		for (Entity photoEntity : popularPhotos) {
			popularPhotoEntity = new Entity(POPULAR_PHOTO_KIND);

			popularPhotoEntity.setProperty(MEDIA_ID, photoEntity.getKey()
					.getId());
			popularPhotoEntity.setProperty(MEDIA_UPLOAD_SOURCE,
					photoEntity.getProperty(MEDIA_UPLOAD_SOURCE));
			popularPhotoEntity.setProperty(IS_PROFILE_IMAGE,
					photoEntity.getProperty(IS_PROFILE_IMAGE));
			popularPhotoEntity.setProperty(MEDIA_OWNER_USERNAME,
					photoEntity.getProperty(MEDIA_OWNER_USERNAME));
			popularPhotoEntity.setProperty(UPLOADED_DATE, photoEntity.getProperty(UPLOADED_DATE));
			popularPhotoEntity.setProperty(TAG, photoEntity.getProperty(TAG));
			popularPhotoEntity.setProperty(DESCRIPTION,
					photoEntity.getProperty(DESCRIPTION));
			popularPhotoEntity.setProperty(TOTAL_VIEWS,
					photoEntity.getProperty(TOTAL_VIEWS));
			popularPhotoEntity.setProperty(MEDIA_PRIVACY_LEVEL,
					photoEntity.getProperty(MEDIA_PRIVACY_LEVEL));
			popularPhotoEntity.setProperty(TOTAL_LIKES,
					photoEntity.getProperty(TOTAL_LIKES));
			popularPhotoEntity.setProperty(TOTAL_COMMENTS,
					photoEntity.getProperty(TOTAL_COMMENTS));

			Util.persistEntity(popularPhotoEntity);
		}

	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		JSONObject jsonObject = Util.createJSONFromRequest(req);

		String username = null;
		String accessToken = null;

		try {
			if (!jsonObject.isNull(USERNAME)) {
				username = jsonObject.getString(USERNAME);
			}
			if (!jsonObject.isNull(ACCESS_TOKEN)) {
				accessToken = jsonObject.getString(ACCESS_TOKEN);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		logger.log(Level.INFO, "Popular Photos");

		if (!Util.verifyAccessToken(username, accessToken)) {
			res.sendError(ERROR_CODE_400, USERNAME_ACCESS_TOKEN_INVALID);
			res.getWriter().write(USERNAME_ACCESS_TOKEN_INVALID);
			return;
		}

		Query query = new Query(POPULAR_PHOTO_KIND);
		PreparedQuery pq = datastore.prepare(query);
		List<Entity> popularPhotos = pq.asList(FetchOptions.Builder.withDefaults());
		int upperLimit = popularPhotos.size();
		if (upperLimit > IMAGE_LIMIT_PER_REQUEST) {
			upperLimit = 20;
		}

		JSONArray popularPhotosJsonArray = Util
				.makeJSONArrayFor20RandomPopularPhotos(popularPhotos,
						upperLimit);
		JSONObject popularPhotosJsonObject = new JSONObject();
		try {
			popularPhotosJsonObject.put(POPULAR_PHOTO, popularPhotosJsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		res.getWriter().write(popularPhotosJsonObject.toString());

	}

}
