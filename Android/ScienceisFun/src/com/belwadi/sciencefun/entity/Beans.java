package com.belwadi.sciencefun.entity;

import java.util.ArrayList;

import com.belwadi.sciencefun.entity.YoutubeItemInfo.*;

public class Beans {

	public static class SignInResult {
		public int status_code;
		public String message;
		public String token;
		public String timestamp;
	}

	public static class SignOutResult {
		public int status_code;
		public String message;
	}

	public static class CheckMailResult {
		public int status_code;
		public String message;
		public boolean duplicated;
	}

	public static class ResetPwdResult {
		public int status_code;
		public String message;
	}

	public static class SignUpResult {
		public int status_code;
		public String message;
	}

	public static class GetProfileParam {
		public String token;
	}

	public static class Profile {
		public String id;
		public String name;
		public String email;
		public String school_name;
		public String school_addr;
		public String school_city;
		public String school_postalcode;
		public String school_country;
		public String syllabus;
		public String grade;
		public String section;
		public String photo;
		public String timestamp;
	}

	public static class GetProfileResult {
		public int status_code;
		public String message;
		public Profile profile;
	}

	public static class SaveProfilepResult {
		public int status_code;
		public String message;
		public String timestamp;
	}

	public static class ChangePwdResult {
		public int status_code;
		public String message;
	}

	public static class GetUserLstParam {
		public long checksum;
		public int start;
		public int count;
	}

	public static class UserResult {
		public String _id;
		public String name;
		public String email;
		public String school_name;
		public String school_addr;
		public String school_city;
		public String school_postalcode;
		public String school_country;
		public String syllabus;
		public String grade;
		public String section;
		public String photo;
		public ArrayList<String> update_on_users;
		public ArrayList<String> friends;
		public String date;
		public String updated_date;
	}

	public static class GetAllUserLstResult {
		public int status_code;
		public String message;
		public ArrayList<UserResult> users;
		public boolean eof;
	}

	public static class GetFriendProfileResult {
		public int status_code;
		public String message;
		public Profile profile;
	}

	public static class GetLinkLstParam {		
		public String kind;
		public String syllabus;
		public String grade;
		public String chapter;
		public int start;
		public int count;
		
		public long checksum;
	}
	
	public static class LinkResult {
		public String _id;
		public String kind;
		public String syllabus;
		public String grade;
		public String chapter;
		public String concept;
		public String date;
		public String updated_date;
	}
	
	public static class GetLinkLstResult {
		public int status_code;
		public String message;
		public ArrayList<LinkResult> links;
		public boolean eof;
	}
	
	public static class GetSyllabusLstParam {
		public long checksum;
	}
	
	public static class SyllabusResult {
		public String _id;
		public boolean enabled;
		public String title;
		public String description;
		public String date;
		public String updated_date;
	}
	
	public static class GetAllSyllabusLstResult {
		public int status_code;
		public String message;
		public ArrayList<SyllabusResult> syllabuses;
	}
	
	public static class GetGradeLstParam {
		public long checksum;
	}
	
	public static class GradeResult {
		public String _id;
		public String grade;
		public String image;
		public boolean enabled;
		public String date;
		public String updated_date;
	}

	public static class GetAllGradeLstResult {
		public int status_code;
		public String message;
		public ArrayList<GradeResult> grades;
	}

	public static class GetChapterLstParam {
		public String syllabus;
		public String grade;		
		public long checksum;
		public int start;
		public int count;
	}

	public static class ChapterResult {
		public String _id;		
		public String syllabus;
		public String grade;
		public boolean enabled;
		public String title;
		public String description;
		public String image;
		public String date;
		public String updated_date;
	}

	public static class GetChapterLstResult {
		public int status_code;
		public String message;		
		public String syllabus;
		public String grade;
		public ArrayList<ChapterResult> chapters;
		public boolean eof;
	}

	public static class GetConceptLstParam {
		public long checksum;
		public String syllabus;
		public String grade;
		public int start;
		public int count;
	}

	public static class ConceptResult {
		public String _id;
		public String syllabus;
		public String grade;
		public String chapter;
		public boolean enabled;
		public String title;
		public String text;
		public String image;
		public String image_credit;
		public String image_source;
		public String image2;
		public String image2_credit;
		public String image2_source;
		public String date;
		public String updated_date;
	}

	public static class GetConceptLstResult {
		public int status_code;
		public String message;
		public ArrayList<ConceptResult> concepts;
		public boolean eof;
	}

	public static class GetVideoLstParam {
		public long checksum;
		public String grade;
		public int start;
		public int count;

		public String chapterId;
		public String conceptId;
	}

	public static class VideoResult {
		public String _id;
		public String url;
		public String owner;
		public String grade;
		public String chapter;
		public String concept;
		public ArrayList<String> shared_user;
		public ArrayList<String> defaulted_user;
		public ArrayList<String> privated_user;
		public String date;
		public String updated_date;
	}

	public static class GetVideoLstResult {
		public int status_code;
		public String message;
		public ArrayList<VideoResult> videos;
		public boolean eof;
	}

	public static class GetRefLstParam {
		public long checksum;
		public String grade;
		public int start;
		public int count;

		public String chapterId;
		public String conceptId;
	}

	public static class ReferenceResult {
		public String _id;
		public String url;
		public String title;
		public String description;
		public String image;
		public String owner;
		public String grade;
		public String chapter;
		public String concept;
		public ArrayList<String> shared_user;
		public ArrayList<String> defaulted_user;
		public ArrayList<String> privated_user;
		public String date;
		public String updated_date;
	}

	public static class GetRefLstResult {
		public int status_code;
		public String message;
		public ArrayList<ReferenceResult> references;
		public boolean eof;
	}

	public static class GetNoteLstParam {
		public long checksum;
		public String grade;
		public int start;
		public int count;

		public String chapterId;
		public String conceptId;
	}

	public static class NoteResult {
		public String _id;
		public String note;
		public String owner;
		public String grade;
		public String chapter;
		public String concept;
		public ArrayList<String> shared_user;
		public ArrayList<String> defaulted_user;
		public ArrayList<String> privated_user;
		public String date;
		public String updated_date;
	}

	public static class GetNoteLstResult {
		public int status_code;
		public String message;
		public ArrayList<NoteResult> notes;
		public boolean eof;
	}

	public static class GetUpdateLstParam {
		public String token;
		public int start;
		public int count;
	}

	public static class UpdateResult {
		public String _id;
		public String type;
		public String content;
		public String content_id;
		public String text;
		public String owner;
		public ArrayList<String> allowed_users;
		public ArrayList<String> unread_users;
		public String date;
	}

	public static class GetUpdateLstResult {
		public int status_code;
		public String message;
		public ArrayList<UpdateResult> updates;
		public boolean eof;
	}
	
	public static class AddVideoParam {
		public String token;
		public String concept_id;
		public String video;
		public boolean isPrivate;
	}

	public static class AddVideoResult {
		public int status_code;
		public String message;
		public String video_id;
	}

	public static class AddRefResult {
		public int status_code;
		public String message;
		public String reference_id;
		public String reference_title;
		public String reference_description;
		public String reference_image;
	}
	
	public static class AddNoteParam {
		public String token;
		public String concept_id;
		public String note;
		public boolean isPrivate;
	}
	
	public static class AddNoteResult {
		public int status_code;
		public String message;
		public String note_id;
	}	
	
	public static class DelContentParam {
		public String token;
		public String type;
		public String content_id;
	}

	public static class DelContentResult {
		public int status_code;
		public String message;
	}
	
	public static class SetDefaultContentParam {
		public String token;
		public String concept_id;
		public String type;
		public String content_id;
	}

	public static class SetDefaultContentResult {
		public int status_code;
		public String message;
	}	

	public static class SetPrivateContentResult {
		public int status_code;
		public String message;
	}

	public static class SetReceiveResult {
		public int status_code;
		public String message;
	}

	public static class SetShareContentResult {
		public int status_code;
		public String message;
	}

	public static class InviteUserResult {
		public int status_code;
		public String message;
	}

	public static class AddFriendResult {
		public int status_code;
		public String message;
	}

	public static class DelFriendResult {
		public int status_code;
		public String message;
	}

	public static class SendReportUserResult {
		public int status_code;
		public String message;
	}
	
	public static class GetYoutubeInfoParam {
		public boolean isDefault;
		public Object exParam;
		
		public String youtubeId;
	}
	
	public static class YoutubeInfo {
		public String id;
		public YbSnippet snippet;
		public YbContentDetails contentDetails;
		public YbStatistics statistics;
	}
	
	public static class YoutubeError {
		public int code;
		public String message;
	}
	
	public static class GetYoutubeInfoResult {
		public ArrayList<YoutubeInfo> items;
		public YoutubeError error;
	}
	
	public static class GetYoutubeSearchParam {
		public String search_key;
	}
	
	public static class YoutubeSearchItem {
		public YbId id;
		public YbSnippet snippet;

		// extend variables
		public boolean isAdded = false;
		public boolean isLoading = false;
		public YoutubeInfo info = null;
	}
	
	public static class YoutubeSearch {
		public ArrayList<YoutubeSearchItem> items;
	}
	
	public static class GetYoutubeSearchResult {
		public ArrayList<YoutubeSearchItem> items;
		public YoutubeError error;
	}
}
