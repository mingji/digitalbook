package com.belwadi.sciencefun.entity;

import com.belwadi.sciencefun.utils.Utility;

import android.content.Context;

public class GlobalValue {

	private UserInfo mUserInfo;
	private Syllabus mSyllabus;
	private Grade mGrade;
	private Scientist mScientist;

	public UserInfo getCurrentUser()
	{
		if (mUserInfo == null)
			this.mUserInfo = new UserInfo();
		
		return mUserInfo;
	}

	public void setCurrentUser(UserInfo info)
	{
		if (info == null)
			return;
		
		this.mUserInfo = info;
	}
	
	public Scientist getScientist(Context ctx)
	{
		if (mScientist == null) {
			this.mScientist = new Scientist();
			
			Scientist tmp = Utility.getRandomScientist(ctx);
			if (tmp != null) {
				this.mScientist.name = tmp.name;
				this.mScientist.imageUrl = tmp.imageUrl;
			}
		}
		
		return mScientist;
	}

	public void setScientist(Scientist scientist)
	{
		if (scientist == null)
			return;
		
		this.mScientist = scientist;
	}
	
	public Syllabus getCurrentSyllabus()
	{
		if (mSyllabus == null)
			this.mSyllabus = new Syllabus();
		
		return mSyllabus;
	}

	public void setCurrentSyllabus(Syllabus syllabus)
	{
		if (syllabus == null)
			return;
		
		this.mSyllabus = syllabus;
	}
	
	public Grade getCurrentGrade()
	{
		if (mGrade == null)
			this.mGrade = new Grade();
		
		return mGrade;
	}

	public void setCurrentGrade(Grade grade)
	{
		if (grade == null)
			return;
		
		this.mGrade = grade;
	}

	private static GlobalValue instance = null;

	public static synchronized GlobalValue getInstance(){

		if(null == instance){
			instance = new GlobalValue();
		}
		return instance;
	}

}
