package com.belwadi.sciencefun.entity;


public class Video {

	public String id;
	public String gradeId;
	public String chapterId;
	public String conceptId;
	public String url;
	public String ownerId;
	public String create_date;
	public String timestamp;
	
	public String shared_user;
	public String defaulted_user;
	public String privated_user;
	
	public boolean isLoading = false;
	
	public Video() {
		super();		
	}

}
