package com.sociogram.server.common;

public interface ISocioGramConstants {
	
	
	int MEDIA_SOURCE_SOCIOGRAM = 0;
	int MEDIA_SOURCE_FACEBOOK = 1;
	int MEDIA_SOURCE_TWITTER = 2;
	
	int PROFILE_IMAGE = 1;
	int GENERAL_IMAGE = 0;
	
	int IMG = 1;
	int VID = 2;
	
	String SOCIOGRAM = "sociogram";
	String FACEBOOK = "facebook";
	String TWITTER = "twitter";
	
	
	int IMAGE_LIMIT_PER_REQUEST = 20;
	int COMMENT_LIMIT_PER_REQUEST = 20;
	int LIKE_LIMIT_PER_REQUEST = 20;
	
	String ADMIN_EMAIL = "waqas.butt@northbaysolutions.net";
	String ADMIN_NAME = "SocioGram";
	
	String EMPTY_STRING = "";
	String PUBLIC = "public";
	String PRIVATE = "private";
	
	String ACTION = "action";
	String LENGTH = "length";
	
	String ID = "id";
	
	//SocioGram Datastore Kinds(table names)
	String USER_KIND  = "User";
	String USER_PHOTO_KIND = "UserPhoto";
	String USER_VIDEO_KIND = "UserVideo";
	String USER_VIDEO_THUMBNAIL_KIND = "UserVideoThumbnail";
	String POPULAR_PHOTO_KIND = "PopularPhoto";
	String LIKE_KIND = "Like";
	String COMMENT_KIND = "Comment";
	
	//Attributes for KIND User
	String NAME = "name";
	String USERNAME = "username";
	String PASSWORD = "password";
	String OLD_PASSWORD = "oldPassword";
	String NEW_PASSWORD = "newPassword";
	String EMAIL = "email";
	String MOBILE_NO = "mobileNo";
	String PROFILE_IMAGE_ID = "profileImageId";
	String NO_OF_IMAGES_UPLOADED = "noOfImagesUploaded";
	String NO_OF_VIDEOS_UPLOADED = "noOfVideosUploaded";
	String USER_PRIVACY_LEVEL  = "userPrivacyLevel";
	String HAS_PROFILE_PIC = "hasProfilePic";
	String ACCESS_TOKEN = "accessToken";
	
	
	//Attributes for KIND UserPhoto
	String BLOB_KEY = "blobKey";
	String UPLOADED_DATE = "uploadedDate";
	String TOTAL_LIKES = "totalLikes";
	String TOTAL_COMMENTS = "totalComments";
	String TOTAL_VIEWS = "totalViews";
	String MEDIA_PRIVACY_LEVEL = "mediaPrivacyLevel";
	String DESCRIPTION = "description";
	String TAG = "tag";
	
	String MEDIA_ID = "mediaId";
	
	String USER_MEDIA = "userMedia";
	
	String MEDIA_UPLOAD_SOURCE = "mediaUploadSource";
	String IS_PROFILE_IMAGE = "isProfileImage";
	
	String POPULAR_PHOTO = "popularPhoto";
	
	String IS_THUMBNAIL_FOR_VIDEO = "isThumbnailForVideo";
	
	
	//Attributes for KIND Like
	String LIKED_BY_USERNAME = "likedByUsername";
	String MEDIA_OWNER_USERNAME = "mediaOwnerUsername";
	String OWNER_USERNAME = "ownerUsername";
	String LIKE_ID = "likeId";
	
	
	//Attributes for KIND Comment
	String COMMENTED_BY_USERNAME = "commentedByUsername";
	String COMMENT_TIME_STAMP = "commentTimeStamp";
	String COMMENT_TEXT = "commentText";
	String COMMENT_ID = "commentId";
	String UPPER_LIMIT = "upperLimit";
	String START_INDEX = "startIndex";
	
	//Action Constants
	String MEDIA = "media";
	String VIDEO = "video"; // For video uploading/downloading/streaming
	String IMAGE = "image"; // For Image uplaoding/ downloading
	String VIDEO_THUMBNAIL = "video_thumbnail"; // For video thumbnail
	String PROFILE = "profile"; // For profile pic uploading.
	String LIKE_ACTION = "like"; // For pic liking.
	String UNLIKE_ACTION = "unlike"; // For pic disliking.
	String ADD = "add"; 
	String UPDATE = "update"; 
	String DELETE = "delete";
	String REMOVE_PROFILE_PIC = "remove_profile_pic";
	String CHANGE_PASSWORD = "change_password";
	
	String CHANGE_INFO = "change_info";
	
	//Response Message Constants
	int ERROR_CODE_400 = 400;
	String FIELDS_MISSING = "Requried fields are missing";
	String INVALID_USER = "Invalid user";
	String NO_PROFILE_IMAGE = "User doesn't have a profile image";
	String MEDIA_DOESNOT_EXIST = "Media does not Exist";
	String IMAGE_OWNER_INVALID = "Image owner invalid";
	String USERNAME_ACCESS_TOKEN_INVALID = "Username or access Token is invalid";
	String USERNAME_PASSWORD_INVALID = "Username and/or password is invalid";
	String IMAGE_UPLOAD_FAILED= "Image uploading operation failed, no data received!";
	String PROFILE_IMAGE_REMOVED = "Profile image removed";
	String INFORMATION_UPDATED = "Information Updated";
	String OLD_PASSWORD_INCORRECT = "Old password doesnot match";
	String USER_ALREADY_EXIST = "User with this name already exists, choose another please";
	String SOCIOGRAM_ACCOUNT_ACTIVATED = "Your SocioGram account has been activated!";
	
	
	
}
