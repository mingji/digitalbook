package com.belwadi.sciencefun.webservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.belwadi.sciencefun.entity.Beans.*;

import android.content.Context;
import android.os.Handler;


public class WebApiInstance {

	private ExecutorService executorService;
	private Handler handler = new Handler();// handler to display images in UI thread
	
	public enum Type {
		
		/* User Actions */		
		SIGN_UP, SIGN_IN, SIGN_OUT,
		
		GET_PROFILE, SET_PROFILE,
		
		CHANGE_PWD, RESET_PWD,
		
		CHECK_MAIL_ADDR,
		
		REPORT_USER,
		
		/* Content Action */
		DELETE_CONTENT,
		
		ADD_VIDEO, ADD_NOTE, ADD_REF,
		
		SET_DEFAULT, SET_PRIVATE,
		
		GET_LINKS, GET_SYLLABUSES, GET_GRADES, GET_CHAPTERS, GET_CONCEPTS,
		
		GET_VIDEOS, GET_NOTES, GET_REFS, // By Grade
		GET_VIDEOS_CHAPTER, GET_NOTES_CHAPTER, GET_REFS_CHAPTER, // By Chapter
		GET_VIDEOS_CONCEPT, GET_NOTES_CONCEPT, GET_REFS_CONCEPT, // By Concept
		
		/* Classmates Action */
		GET_USERS, INVITE_USER, ADD_USER, DEL_USER,
		
		SHARE_CONTENT,
		
		SET_RECEIVE,
		
		GET_UPDATES,
		
		GET_YOUTUBE_INFO, GET_YOUTUBE_SEARCH
	};

	private WebApiInstance() { }
	 
    private static class SingletonHolder { 
            public static final WebApiInstance instance = new WebApiInstance();
    }

    public static WebApiInstance getInstance() {
            return SingletonHolder.instance;
    }

    public void init(Context context) {
//		executorService = Executors.newFixedThreadPool(5);
		executorService = Executors.newCachedThreadPool();
	}
    
    public void init(Context context, int numberOfThreads) {
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
    
	public void init(Context context, String directory) {
//		executorService = Executors.newFixedThreadPool(5);
		executorService = Executors.newCachedThreadPool();
	}
	
	
	public void init(Context context, String directory, int numberOfThreads) {
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
	
	public void executeAPI(Type type, Object param, WebApiCallback callback)
	{
		callback.onPreProcessing(type, param);
		executorService.submit(new ApiRunner(type, param, callback));
	}    
	
	class ApiRunner implements Runnable {
		WebApiCallback webApiCallback;
		Object parameter;
		Type type;

		ApiRunner(Type type, Object param, WebApiCallback callback) {
			webApiCallback = callback;
			parameter = param;
			this.type = type; 
		}

		@Override
		public void run() {
			try {
				
				Object result = callApi(type, parameter);
				
				ApiResult apiResult = new ApiResult(type, parameter, result, webApiCallback);
				handler.post(apiResult);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	class ApiResult implements Runnable {
		WebApiCallback webApiCallback;
		Object parameter, result;
		Type type;

		ApiResult(Type type, Object param, Object obj, WebApiCallback callback) {
			webApiCallback = callback;
			parameter = param;
			result = obj;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				if (webApiCallback != null)
					webApiCallback.onResultProcessing(type, parameter, result);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}
	
	Object callApi(Type type, Object param)
	{
		Object result = null;
		
		switch (type)
		{
		case GET_PROFILE:
			result = getProfile(param); 
			break;
		
		case GET_LINKS:
			result = getLinkList(param); 
			break;
		case GET_SYLLABUSES:
			result = getSyllabusList(param); 
			break;
		case GET_GRADES:
			result = getGradeList(param); 
			break;			
		case GET_CHAPTERS:
			result = getChapterList(param); 
			break;
		case GET_CONCEPTS:
			result = getConceptList(param); 
			break;
		case GET_VIDEOS:
			result = getVideoList(param); 
			break;
		case GET_VIDEOS_CHAPTER:
			result = getVideoListByChapter(param); 
			break;
		case GET_VIDEOS_CONCEPT:
			result = getVideoListByConcept(param); 
			break;
		case GET_REFS:
			result = getRefList(param); 
			break;
		case GET_REFS_CHAPTER:
			result = getRefListByChapter(param); 
			break;
		case GET_REFS_CONCEPT:
			result = getRefListByConcept(param); 
			break;
		case GET_NOTES:
			result = getNoteList(param); 
			break;
		case GET_NOTES_CHAPTER:
			result = getNoteListByChapter(param); 
			break;
		case GET_NOTES_CONCEPT:
			result = getNoteListByConcept(param); 
			break;
			
		case DELETE_CONTENT:
			result = delContent(param);
			break;
			
		case SET_DEFAULT:
			result = setDefaultContent(param);
			break;
			
		case ADD_NOTE:
			result = addNote(param);
			break;
			
		case ADD_VIDEO:
			result = addVideo(param);
			break;
			
		case GET_USERS:
			result = getUserList(param); 
			break;
		case GET_UPDATES:
			result = getUpdateList(param); 
			break;
			
		case GET_YOUTUBE_INFO:
			result = getYoutubeInfo(param); 
			break;
		case GET_YOUTUBE_SEARCH:
			result = getYoutubeSearch(param); 
			break;
		default:
			break;
		}
		
		return result;
	}
	
	/* **************************************************************************
	 *                           Users Actions
	 * *************************************************************************/
	Object getProfile(Object obj)
	{
		Object result = null;
		GetProfileParam param = (GetProfileParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetProfile(param.token);
		return result;
	}
	
	/* **************************************************************************
	 *                           Contents Actions
	 * *************************************************************************/
	Object getLinkList(Object obj)
	{
		Object result = null;
		GetLinkLstParam param = (GetLinkLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetLinkList(param);
		return result;
	}
	
	Object getSyllabusList(Object obj)
	{
		Object result = null;
		GetSyllabusLstParam param = (GetSyllabusLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllSyllabusList(param.checksum);
		return result;
	}
	
	Object getGradeList(Object obj)
	{
		Object result = null;
		GetGradeLstParam param = (GetGradeLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllGradeList(param.checksum);
		return result;
	}
	
	Object getChapterList(Object obj)
	{
		Object result = null;
		GetChapterLstParam param = (GetChapterLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetChapterList(param);
		return result;
	}
	
	Object getConceptList(Object obj)
	{
		Object result = null;
		GetConceptLstParam param = (GetConceptLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetConceptList(param.checksum, param.syllabus, param.grade, param.start, param.count);
		return result;
	}
	
	Object getVideoList(Object obj)
	{
		Object result = null;
		GetVideoLstParam param = (GetVideoLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllVideoList(param.checksum, param.grade, param.start, param.count);
		return result;
	}
	
	Object getVideoListByChapter(Object obj)
	{
		Object result = null;
		GetVideoLstParam param = (GetVideoLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetVideoListByChapter(param.checksum, param.chapterId, param.start, param.count);
		return result;
	}
	
	Object getVideoListByConcept(Object obj)
	{
		Object result = null;
		GetVideoLstParam param = (GetVideoLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetVideoListByConcept(param.checksum, param.conceptId, param.start, param.count);
		return result;
	}
	
	Object getRefList(Object obj)
	{
		Object result = null;
		GetRefLstParam param = (GetRefLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllRefList(param.checksum, param.grade, param.start, param.count);
		return result;
	}
	
	Object getRefListByChapter(Object obj)
	{
		Object result = null;
		GetRefLstParam param = (GetRefLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetRefListByChapter(param.checksum, param.chapterId, param.start, param.count);
		return result;
	}
	
	Object getRefListByConcept(Object obj)
	{
		Object result = null;
		GetRefLstParam param = (GetRefLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetRefListByConcept(param.checksum, param.conceptId, param.start, param.count);
		return result;
	}	
	
	Object getNoteList(Object obj)
	{
		Object result = null;
		GetNoteLstParam param = (GetNoteLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllNoteList(param.checksum, param.grade, param.start, param.count);
		return result;
	}
	
	Object getNoteListByChapter(Object obj)
	{
		Object result = null;
		GetNoteLstParam param = (GetNoteLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetNoteListByChapter(param.checksum, param.chapterId, param.start, param.count);
		return result;
	}
	
	Object getNoteListByConcept(Object obj)
	{
		Object result = null;
		GetNoteLstParam param = (GetNoteLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetNoteListByConcept(param.checksum, param.conceptId, param.start, param.count);
		return result;
	}
	
	Object addVideo(Object obj)
	{
		Object result = null;
		AddVideoParam param = (AddVideoParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.AddVideo(param.token, param.concept_id, param.video, param.isPrivate);
		
		return result;
	}

	Object addNote(Object obj)
	{
		Object result = null;
		AddNoteParam param = (AddNoteParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.AddNote(param.token, param.concept_id, param.note, param.isPrivate);
		
		return result;
	}
	
	Object delContent(Object obj)
	{
		Object result = null;
		DelContentParam param = (DelContentParam)obj;
		
		if (param == null)
			return null;		
		result = Server.DeleteContent(param.token, param.type, param.content_id);
		
		return result;
	}
	
	Object setDefaultContent(Object obj)
	{
		Object result = null;
		SetDefaultContentParam param = (SetDefaultContentParam)obj;
		
		if (param == null)
			return null;		
		result = Server.SetDefaultContent(param.token, param.concept_id, param.type, param.content_id);
		
		return result;
	}
	
	/* **************************************************************************
	 *                           Classmates Actions
	 * *************************************************************************/
	Object getUserList(Object obj)
	{
		Object result = null;
		GetUserLstParam param = (GetUserLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetAllUserList(param.checksum, param.start, param.count);
		return result;
	}
	
	Object getUpdateList(Object obj)
	{
		Object result = null;
		GetUpdateLstParam param = (GetUpdateLstParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.GetUpdateList(param.token, param.start, param.count);
		return result;
	}
	
	/* **************************************************************************
	 *                           Other Actions
	 * *************************************************************************/
	Object getYoutubeInfo(Object obj)
	{
		Object result = null;
		GetYoutubeInfoParam param = (GetYoutubeInfoParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.getYoutubeInfo(param.youtubeId);
		return result;
	}
	
	Object getYoutubeSearch(Object obj)
	{
		Object result = null;
		GetYoutubeSearchParam param = (GetYoutubeSearchParam)obj;
		
		if (param == null)
			return null;		
		
		result = Server.getYoutubeSearch(param.search_key);
		return result;
	}
}
