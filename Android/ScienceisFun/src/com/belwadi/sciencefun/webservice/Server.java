package com.belwadi.sciencefun.webservice;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.entity.UserInfo;

public class Server {	

	public static SignInResult SignIn(String username, String password, String gcmRegId) {

		HttpParams param = new HttpParams();
		param.addParam(Constants.EMAIL, username);
		param.addParam(Constants.PASSWORD, password);
//		param.addParam(Constants.REG_ID, gcmRegId);
		param.addParam(Constants.REG_ID, "non-used value");

		String response = HttpApi.sendRequest(Constants.SERVER_SIGNIN_URL, param, null);

		SignInResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SignInResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SignIn", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SignOutResult SignOut(String token) {

		HttpParams param = new HttpParams();
		param.addParam(Constants.TOKEN, token);

		String response = HttpApi.sendRequest(Constants.SERVER_SIGNOUT_URL, param, null);

		SignOutResult result = null;
		if (response != null) {
			try {				Gson gson = new Gson();
				result = gson.fromJson(response, SignOutResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SignOut", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static CheckMailResult CheckMailAddress(String mail) {

		HttpParams param = new HttpParams();
		param.addParam(Constants.EMAIL, mail);

		String response = HttpApi.sendRequest(Constants.SERVER_CHECK_MAIL_URL, param, null);

		CheckMailResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, CheckMailResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("CheckMailAddress", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static ResetPwdResult ResetPassword(String mail) {

		HttpParams param = new HttpParams();
		param.addParam(Constants.EMAIL, mail);

		String response = HttpApi.sendRequest(Constants.SERVER_RESET_PWD_URL, param, null);

		ResetPwdResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, ResetPwdResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("ResetPassword", e.toString());
			}
			return result;
		} else
			return null;
	}

	public static SignUpResult SignUp(UserInfo userInfo) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.NAME, userInfo.name);
		param.addParam(Constants.EMAIL, userInfo.mail);
		param.addParam(Constants.PASSWORD, userInfo.passwd);
		param.addParam(Constants.CONFIRM_PASSWORD, userInfo.confirmPasswd);
		param.addParam(Constants.SCHOOL_NAME, userInfo.schoolName);
		param.addParam(Constants.SCHOOL_ADDRESS, userInfo.address);
		param.addParam(Constants.SCHOOL_CITY, userInfo.city);
		param.addParam(Constants.SCHOOL_POSTALCODE, userInfo.postcode);
		param.addParam(Constants.SCHOOL_COUNTRY, userInfo.country);
		param.addParam(Constants.SYLLABUS, userInfo.syllabus_id);
		param.addParam(Constants.GRADE, userInfo.grade_id);
		param.addParam(Constants.SECTION, userInfo.section);		
		
		String response = HttpApi.sendRequestWithImage(Constants.SERVER_SIGNUP_URL, param, userInfo.photo, null);

		SignUpResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SignUpResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SignUp", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetProfileResult GetProfile(String token) {

		HttpParams param = new HttpParams();
		param.addParam(Constants.TOKEN, token);

		String response = HttpApi.sendRequest(Constants.SERVER_GET_PROFILE_URL, param, null);

		GetProfileResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetProfileResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetProfile", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SaveProfilepResult SaveProfile(String token, UserInfo userInfo) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.NAME, userInfo.name);
		param.addParam(Constants.SCHOOL_NAME, userInfo.schoolName);
		param.addParam(Constants.SCHOOL_ADDRESS, userInfo.address);
		param.addParam(Constants.SCHOOL_CITY, userInfo.city);
		param.addParam(Constants.SCHOOL_POSTALCODE, userInfo.postcode);
		param.addParam(Constants.SCHOOL_COUNTRY, userInfo.country);
		param.addParam(Constants.SYLLABUS, userInfo.syllabus_id);
		param.addParam(Constants.GRADE, userInfo.grade_id);
		param.addParam(Constants.SECTION, userInfo.section);
		param.addParam(Constants.PHOTO_REMOVED, "false");
		
		String response = HttpApi.sendRequestWithImage(Constants.SERVER_SET_PROFILE_URL, param, userInfo.photo, null);

		SaveProfilepResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SaveProfilepResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SaveProfile", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static ChangePwdResult ChangePassword(String token, String oldPwd, String newPwd, String confirm) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.OLD_PASSWORD, oldPwd);
		param.addParam(Constants.NEW_PASSWORD, newPwd);
		param.addParam(Constants.CONFIRM_PASSWORD, confirm);
		
		String response = HttpApi.sendRequest(Constants.SERVER_CHANGE_PWD_URL, param, null);

		ChangePwdResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, ChangePwdResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("ChangePassword", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetLinkLstResult GetLinkList(GetLinkLstParam params) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.KIND, params.kind);
		param.addParam(Constants.SYLLABUS, params.syllabus);
		param.addParam(Constants.GRADE, params.grade);
		param.addParam(Constants.CHAPTER, params.chapter);
		param.addParam(Constants.START, Integer.toString(params.start));
		param.addParam(Constants.COUNT, Integer.toString(params.count));
		param.addParam(Constants.CHECKSUM, Long.toString(params.checksum));
		
		String response = HttpApi.sendRequest(Constants.SERVER_LINK_URL, param, null);

		GetLinkLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetLinkLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetLinkList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetAllSyllabusLstResult GetAllSyllabusList(long checksum) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_SYLLABUS_URL, param, null);

		GetAllSyllabusLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetAllSyllabusLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllSyllabusList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetAllGradeLstResult GetAllGradeList(long checksum) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_GRADE_URL, param, null);

		GetAllGradeLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetAllGradeLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllGradeList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetChapterLstResult GetChapterList(GetChapterLstParam params) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.SYLLABUS, params.syllabus);
		param.addParam(Constants.GRADE, params.grade);		
		param.addParam(Constants.CHECKSUM, Long.toString(params.checksum));
		param.addParam(Constants.START, Integer.toString(params.start));
		param.addParam(Constants.COUNT, Integer.toString(params.count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CHAPTERS_URL, param, null);

		GetChapterLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetChapterLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetChapterList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetConceptLstResult GetConceptList(long checksum, String syllabusId, String gradeId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.SYLLABUS, syllabusId);
		param.addParam(Constants.GRADE, gradeId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CONCEPTS_URL, param, null);

		GetConceptLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetConceptLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetConceptList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetVideoLstResult GetAllVideoList(long checksum, String gradeId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.GRADE, gradeId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_VIDEO_URL, param, null);

		GetVideoLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetVideoLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllVideoList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetVideoLstResult GetVideoListByChapter(long checksum, String chapterId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CHAPTER, chapterId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CHAPTER_VIDEO_URL, param, null);

		GetVideoLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetVideoLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllVideoList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetVideoLstResult GetVideoListByConcept(long checksum, String conceptId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CONCEPT, conceptId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CONCEPT_VIDEO_URL, param, null);

		GetVideoLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetVideoLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllVideoList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetRefLstResult GetAllRefList(long checksum, String gradeId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.GRADE, gradeId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_REF_URL, param, null);

		GetRefLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetRefLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllRefList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetRefLstResult GetRefListByChapter(long checksum, String chapterId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CHAPTER, chapterId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CHAPTER_REF_URL, param, null);

		GetRefLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetRefLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllRefList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetRefLstResult GetRefListByConcept(long checksum, String conceptId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CONCEPT, conceptId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CONCEPT_REF_URL, param, null);

		GetRefLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetRefLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllRefList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetNoteLstResult GetAllNoteList(long checksum, String gradeId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.GRADE, gradeId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_NOTE_URL, param, null);

		GetNoteLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetNoteLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllNoteList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetNoteLstResult GetNoteListByChapter(long checksum, String chapterId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CHAPTER, chapterId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CHAPTER_NOTE_URL, param, null);

		GetNoteLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetNoteLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllNoteList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetNoteLstResult GetNoteListByConcept(long checksum, String conceptId, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.CONCEPT, conceptId);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_CONCEPT_NOTE_URL, param, null);

		GetNoteLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetNoteLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllNoteList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetUpdateLstResult GetUpdateList(String token, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_GET_UPDATE_URL, param, null);

		GetUpdateLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetUpdateLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetUpdateList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static AddVideoResult AddVideo(String token, String conceptId, String videoUrl, boolean isPrivate) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONCEPT_ID, conceptId);
		param.addParam(Constants.VIDEO, videoUrl);
		param.addParam(Constants.IS_PRIVATE, Boolean.toString(isPrivate));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ADD_VIDEO_URL, param, null);

		AddVideoResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, AddVideoResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("AddVideo", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static AddRefResult AddReference(String token, String conceptId, String refUrl, boolean isPrivate) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONCEPT_ID, conceptId);
		param.addParam(Constants.REFERENCE, refUrl);
		param.addParam(Constants.IS_PRIVATE, Boolean.toString(isPrivate));
				
		String response = HttpApi.sendRequest(Constants.SERVER_ADD_REF_URL, param, null);

		AddRefResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, AddRefResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("AddReference", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static AddNoteResult AddNote(String token, String conceptId, String note, boolean isPrivate) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONCEPT_ID, conceptId);
		param.addParam(Constants.NOTE, note);
		param.addParam(Constants.IS_PRIVATE, Boolean.toString(isPrivate));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ADD_NOTE_URL, param, null);

		AddNoteResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, AddNoteResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("AddNote", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static DelContentResult DeleteContent(String token, String contentType, String cotentId) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONTENT_TYPE, contentType);
		param.addParam(Constants.CONTENT_ID, cotentId);
		
		String response = HttpApi.sendRequest(Constants.SERVER_DEL_CONTENT_URL, param, null);

		DelContentResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, DelContentResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("DeleteContent", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SetDefaultContentResult SetDefaultContent(String token, String conceptId, String contentType, String cotentId) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONCEPT_ID, conceptId);
		param.addParam(Constants.CONTENT_TYPE, contentType);
		param.addParam(Constants.CONTENT_ID, cotentId);
		
		String response = HttpApi.sendRequest(Constants.SERVER_SET_DEFAULT_URL, param, null);

		SetDefaultContentResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SetDefaultContentResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SetDefaultContent", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SetPrivateContentResult SetPrivateContent(String token,
			String conceptId, String contentType, String cotentId, boolean isPrivate) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.CONCEPT_ID, conceptId);
		param.addParam(Constants.CONTENT_TYPE, contentType);
		param.addParam(Constants.CONTENT_ID, cotentId);
		param.addParam(Constants.IS_PRIVATE, Boolean.toString(isPrivate));
		
		String response = HttpApi.sendRequest(Constants.SERVER_SET_PRIVATE_URL, param, null);

		SetPrivateContentResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SetPrivateContentResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SetPrivateContent", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SetReceiveResult SetReceive(String token, String friendId, boolean isReveive) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.USER_ID, friendId);
		param.addParam(Constants.UPDATE, Boolean.toString(isReveive));
		
		String response = HttpApi.sendRequest(Constants.SERVER_SET_RECEIVE_URL, param, null);

		SetReceiveResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SetReceiveResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SetReceive", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SetShareContentResult SetShareContent(String token, String friendId, String contentType, String cotentId) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.USER_ID, friendId);
		param.addParam(Constants.CONTENT_TYPE, contentType);
		param.addParam(Constants.CONTENT_ID, cotentId);
		
		String response = HttpApi.sendRequest(Constants.SERVER_SET_SHARE_URL, param, null);

		SetShareContentResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SetShareContentResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SetShareContent", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static InviteUserResult InviteUser(String token, String mail) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.MAIL, mail);
		
		String response = HttpApi.sendRequest(Constants.SERVER_INVITE_USER_URL, param, null);

		InviteUserResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, InviteUserResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("InviteUser", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static AddFriendResult AddFriend(String token, String userId) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.USER_ID, userId);
		
		String response = HttpApi.sendRequest(Constants.SERVER_ADD_FRIEND_URL, param, null);

		AddFriendResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, AddFriendResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("AddFriend", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static DelFriendResult DelFriend(String token, String userId) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.USER_ID, userId);
		
		String response = HttpApi.sendRequest(Constants.SERVER_DEL_FRIEND_URL, param, null);

		DelFriendResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, DelFriendResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("DelFriend", e.toString());
			}
			return result;
		} else
			return null;
	}

	public static GetAllUserLstResult GetAllUserList(long checksum, int start, int count) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.CHECKSUM, Long.toString(checksum));
		param.addParam(Constants.START, Integer.toString(start));
		param.addParam(Constants.COUNT, Integer.toString(count));
		
		String response = HttpApi.sendRequest(Constants.SERVER_ALL_USER_URL, param, null);

		GetAllUserLstResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetAllUserLstResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("GetAllUserList", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static SendReportUserResult SendReportUser(String token, String userId, String reason) {

		HttpParams param = new HttpParams();
		
		param.addParam(Constants.TOKEN, token);
		param.addParam(Constants.USER_ID, userId);
		param.addParam(Constants.REASON, reason);
		
		String response = HttpApi.sendRequest(Constants.SERVER_REPORT_USER_URL, param, null);

		SendReportUserResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, SendReportUserResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("SendReportUser", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetYoutubeInfoResult getYoutubeInfo(String videoId) {

		String url = Constants.YOUTUBE_INFO_URL_PART1 + videoId + Constants.YOUTUBE_INFO_URL_PART2;
		
		String response = HttpApi.sendHttpsGetRequest(url, null);

		GetYoutubeInfoResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetYoutubeInfoResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("getYoutubeInfo", e.toString());
			}
			return result;
		} else
			return null;
	}
	
	public static GetYoutubeSearchResult getYoutubeSearch(String search_key) {

		String url = Constants.YOUTUBE_SEARCH_URL_PART1 + search_key + Constants.YOUTUBE_SEARCH_URL_PART2;
		
		String response = HttpApi.sendGetRequest(url, null, null);

		GetYoutubeSearchResult result = null;
		if (response != null) {
			try {
				Gson gson = new Gson();
				result = gson.fromJson(response, GetYoutubeSearchResult.class);
			} catch (JsonSyntaxException e) {
				Log.e("getYoutubeSearch", e.toString());
			}
			return result;
		} else
			return null;
	}
}
