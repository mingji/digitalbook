package com.belwadi.sciencefun.entity;

import android.graphics.Bitmap;


public class UserInfo {

	public boolean isValid;
	public String _id;
	public String name, mail, passwd, confirmPasswd, schoolName, address, city, country, postcode, syllabus, grade, section;
	public String photo_url, syllabus_id, grade_id, create_date, timestamp;
	public String update_on_user, friends;
	public Bitmap photo;
	
	public boolean isFriendGroup = false; // this is used in ClassFragment.
	public boolean isScientist = false;

	public UserInfo() {
		super();
		this.isValid = false;
	}

	public UserInfo(String id, String name, String photo_url) {
		super();
		this.isValid = false;
		
		this._id = id;
		this.name = name;
		this.photo_url = photo_url;
	}
}
