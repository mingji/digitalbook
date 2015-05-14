package com.belwadi.sciencefun.entity;


public class Concept {

	public String id;
	public String syllabusId;
	public String gradeId;
	public String chapterId;
	public int enable;
	public String title;
	public String text;
	public String image;
	public String image_credit;
	public String image_source;
	public String image2;
	public String image2_credit;
	public String image2_source;
	public String create_date;
	public String timestamp;
	
	public boolean noteFlag =  false; // true : public note, false : private note
	public boolean refFlag = true; // true : public ref, false : private ref
	
	public Concept() {
		super();
	}

}
