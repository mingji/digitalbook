package com.belwadi.sciencefun.webservice;

public class Constants {

	/* Published server */
//	public static final String HTTP_HOME = "http://60.21.159.162:3000/";
//	public static final String HTTP_BASIC_PARAM = "http://60.21.159.162:3000/api";

	public static final String HTTP_HOME = "http://www.scienceisfun.in/";
	public static final String HTTP_BASIC_PARAM = "http://www.scienceisfun.in/api";

	/* Local server */
//	public static final String HTTP_HOME = "http://10.70.1.11:3000/";
//	public static final String HTTP_BASIC_PARAM = "http://10.70.1.11:3000/api";
	
	// Key for Android application	
//	public static final String DEVELOPER_KEY = "AIzaSyB8EPP8UXrfqPaZ3nlJneqa46F9RXTCJFc"; // for debugging
	public static final String DEVELOPER_KEY = "AIzaSyDOx2bcy23XSQeNWpwD72XatrN0e355GtE"; // distribution
	
	// Key for Server application
	public static final String SERVER_APP_KEY = "AIzaSyAzPYLRYina5ozWsy1673GD0JiOVl9BDSQ";
	
	public static final String HTTP_YOUTUBE_IMG = "http://img.youtube.com/vi/";
	
	/* Youtube API version 2 */
//	public static final String YOUTUBE_INFO_URL_PART1 = "https://gdata.youtube.com/feeds/api/videos/";
//	public static final String YOUTUBE_INFO_URL_PART2 = "?v=2&alt=jsonc&format=5";
	
	/* Youtube API version 3 */
	public static final String YOUTUBE_INFO_URL_PART1 = "https://www.googleapis.com/youtube/v3/videos?part=snippet%2C+contentDetails%2C+statistics&id=";
	public static final String YOUTUBE_INFO_URL_PART2 = "&key=" + SERVER_APP_KEY;
	
	/* Youtube API version 2 */
//	public static final String YOUTUBE_SEARCH_URL_PART1 = "http://gdata.youtube.com/feeds/api/videos/?q=";
//	public static final String YOUTUBE_SEARCH_URL_PART2 = "&max-results=15&v=2&alt=jsonc&format=5";
	
	/* Youtube API version 3 */
	public static final String YOUTUBE_SEARCH_URL_PART1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=";
	public static final String YOUTUBE_SEARCH_URL_PART2 = "&type=video&maxResults=15&key=" + SERVER_APP_KEY;	
	
	public static final String TOKEN = "token";
	public static final String CHECKSUM = "checksum";
	public static final String START = "start";
	public static final String COUNT = "count";
	
	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String USER_NAME = "username";
	public static final String MAIL = "mail";
	
	public static final String PASSWORD = "password";
	public static final String OLD_PASSWORD = "old_password";
	public static final String NEW_PASSWORD = "new_password";
	public static final String CONFIRM_PASSWORD = "confirm_password";
	
	public static final String REG_ID = "registration_id";
	
	public static final String SCHOOL_NAME = "school_name";
	public static final String SCHOOL_ADDRESS = "school_addr";
	public static final String SCHOOL_CITY = "school_city";
	public static final String SCHOOL_POSTALCODE = "school_postalcode";
	public static final String SCHOOL_COUNTRY = "school_country";
	
	public static final String KIND = "kind";
	public static final String SYLLABUS = "syllabus";
	public static final String GRADE = "grade";
	public static final String CHAPTER = "chapter";
	public static final String CONCEPT = "concept";
	public static final String SECTION = "section";
	
	public static final String PHOTO = "photo";
	public static final String PHOTO_REMOVED = "photo_removed";
	
	public static final String CONTENT_TYPE = "content_type";
	public static final String CONTENT_ID = "content_id";
	public static final String VIDEO = "video";
	public static final String REFERENCE = "reference";
	public static final String NOTE = "note";
	public static final String IS_PRIVATE = "isPrivate";
	
	public static final String GRADE_ID = "grade_id";
	public static final String CHAPTER_ID = "chapter_id";
	public static final String CONCEPT_ID = "concept_id";
	public static final String USER_ID = "user_id";
	public static final String UPDATE = "update";
	public static final String REASON = "reason";
	
	public static final String NETWORK_ERR = "Network Connection Error. Please try again later.";
	
	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_NOUPDATE = 210;
	public static final int STATUS_CODE_FAIL = 400;
	
	public static final int COUNT_UNIT = 35;

	/* Sign In */
	public static final String SERVER_SIGNIN_URL = HTTP_BASIC_PARAM + "/signin";
	
	/* Sign Out */
	public static final String SERVER_SIGNOUT_URL = HTTP_BASIC_PARAM + "/signout";
	
	/* Sign Out */
	public static final String SERVER_CHECK_MAIL_URL = HTTP_BASIC_PARAM + "/email/verification";
	
	/* Reset Password */
	public static final String SERVER_RESET_PWD_URL = HTTP_BASIC_PARAM + "/password/reset";
	
	/* Sign Up */
	public static final String SERVER_SIGNUP_URL = HTTP_BASIC_PARAM + "/signup";
	
	/* Get Profile */
	public static final String SERVER_GET_PROFILE_URL = HTTP_BASIC_PARAM + "/profile/get";
	
	/* Set Profile */
	public static final String SERVER_SET_PROFILE_URL = HTTP_BASIC_PARAM + "/profile/set";
	
	/* Change Password */
	public static final String SERVER_CHANGE_PWD_URL = HTTP_BASIC_PARAM + "/password/change";
	
	/* Get Link */
	public static final String SERVER_LINK_URL = HTTP_BASIC_PARAM + "/get/link";
	
	/* Get Grades */
	public static final String SERVER_ALL_SYLLABUS_URL = HTTP_BASIC_PARAM + "/get/syllabus/total";
	
	/* Get Grades */
	public static final String SERVER_ALL_GRADE_URL = HTTP_BASIC_PARAM + "/get/grade/total";
	
	/* Get Chapters */
	public static final String SERVER_ALL_CHAPTER_URL = HTTP_BASIC_PARAM + "/get/chapter/total";
	public static final String SERVER_CHAPTERS_URL = HTTP_BASIC_PARAM + "/get/chapters";
	
	/* Get Concepts */
//	public static final String SERVER_ALL_CONCEPT_URL = HTTP_BASIC_PARAM + "/get/concept/total";
	public static final String SERVER_CONCEPTS_URL = HTTP_BASIC_PARAM + "/get/concepts";
	
	/* Get Default Content */
	public static final String SERVER_GET_DEFAULT_URL = HTTP_BASIC_PARAM + "/grade/chapter/concept/contents/default";
	
	/* Get Videos */
	public static final String SERVER_GET_VIDEO_URL = HTTP_BASIC_PARAM + "/grade/chapter/concept/videos";
	public static final String SERVER_ALL_VIDEO_URL = HTTP_BASIC_PARAM + "/get/video/total";
	public static final String SERVER_CHAPTER_VIDEO_URL = HTTP_BASIC_PARAM + "/get/video/chapter";
	public static final String SERVER_CONCEPT_VIDEO_URL = HTTP_BASIC_PARAM + "/get/video/concept";
	
	/* Add Video */
	public static final String SERVER_ADD_VIDEO_URL = HTTP_BASIC_PARAM + "/content/add/video";
	
	/* Get References */
	public static final String SERVER_GET_REF_URL = HTTP_BASIC_PARAM + "/grade/chapter/concept/references";
	public static final String SERVER_ALL_REF_URL = HTTP_BASIC_PARAM + "/get/reference/total";
	public static final String SERVER_CHAPTER_REF_URL = HTTP_BASIC_PARAM + "/get/reference/chapter";
	public static final String SERVER_CONCEPT_REF_URL = HTTP_BASIC_PARAM + "/get/reference/concept";
	
	/* Add Reference */
	public static final String SERVER_ADD_REF_URL = HTTP_BASIC_PARAM + "/content/add/reference";
	
	/* Get Notes */
	public static final String SERVER_GET_NOTE_URL = HTTP_BASIC_PARAM + "/grade/chapter/concept/notes";
	public static final String SERVER_ALL_NOTE_URL = HTTP_BASIC_PARAM + "/get/note/total";
	public static final String SERVER_CHAPTER_NOTE_URL = HTTP_BASIC_PARAM + "/get/note/chapter";
	public static final String SERVER_CONCEPT_NOTE_URL = HTTP_BASIC_PARAM + "/get/note/concept";
	
	/* Get Updates */
	public static final String SERVER_GET_UPDATE_URL = HTTP_BASIC_PARAM + "/get/updates/total";
	
	/* Add Note */
	public static final String SERVER_ADD_NOTE_URL = HTTP_BASIC_PARAM + "/content/add/note";
	
	/* Delete Content */
	public static final String SERVER_DEL_CONTENT_URL = HTTP_BASIC_PARAM + "/deletecontent";
	
	/* Set Default Content */
	public static final String SERVER_SET_DEFAULT_URL = HTTP_BASIC_PARAM + "/content/set/default";
	
	/* Set Private Content */
	public static final String SERVER_SET_PRIVATE_URL = HTTP_BASIC_PARAM + "/content/set/private";
	
	/* Set Receive */
	public static final String SERVER_SET_RECEIVE_URL = HTTP_BASIC_PARAM + "/set/update";
	
	/* Share Content */
	public static final String SERVER_SET_SHARE_URL = HTTP_BASIC_PARAM + "/share/content";
	
	/* Invite User */
	public static final String SERVER_INVITE_USER_URL = HTTP_BASIC_PARAM + "/invite";
	
	/* Add/Delete Friend */
	public static final String SERVER_ADD_FRIEND_URL = HTTP_BASIC_PARAM + "/add/friend";
	public static final String SERVER_DEL_FRIEND_URL = HTTP_BASIC_PARAM + "/remove/friend";
	
	/* Get Friend's Profile */
	public static final String SERVER_ALL_USER_URL = HTTP_BASIC_PARAM + "/get/user/total";
	
	/* Report User */
	public static final String SERVER_REPORT_USER_URL = HTTP_BASIC_PARAM + "/report";		
}
