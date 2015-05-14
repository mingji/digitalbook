package com.belwadi.sciencefun.database;

import java.util.ArrayList;

import com.belwadi.sciencefun.entity.Chapter;
import com.belwadi.sciencefun.entity.Concept;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Link;
import com.belwadi.sciencefun.entity.Note;
import com.belwadi.sciencefun.entity.Reference;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.Update;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Video;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.utils.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager {
	private static final String mDbName = "ScienceFun.db";
	public static final int mDbVersion = 1;

	private OpenHelper mOpener;
	private SQLiteDatabase mDb;

	private Context mContext;

	public DatabaseManager(Context context) {
		this.mContext = context;
		this.mOpener = new OpenHelper(context, mDbName, null, mDbVersion);
		mDb = mOpener.getWritableDatabase();
	}

	/* User Table */
	public void addUserItem(UserResult user)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.UserTable.COLUMN_ID, user._id);
		values.put(DbSchema.UserTable.COLUMN_MAIL, user.email);
		values.put(DbSchema.UserTable.COLUMN_NAME, user.name);
		values.put(DbSchema.UserTable.COLUMN_SCHOOL, user.school_name);
		values.put(DbSchema.UserTable.COLUMN_ADDRESS, user.school_addr);
		values.put(DbSchema.UserTable.COLUMN_CITY, user.school_city);
		values.put(DbSchema.UserTable.COLUMN_POSTAL_CODE, user.school_postalcode);
		values.put(DbSchema.UserTable.COLUMN_COUNTRY, user.school_country);
		values.put(DbSchema.UserTable.COLUMN_SYLLABUS, user.syllabus);
		values.put(DbSchema.UserTable.COLUMN_GRADE, user.grade);
		values.put(DbSchema.UserTable.COLUMN_SECTION, user.section);
		values.put(DbSchema.UserTable.COLUMN_PHOTO, user.photo);
		values.put(DbSchema.UserTable.COLUMN_UPDATE_USER, user.update_on_users.toString());
		values.put(DbSchema.UserTable.COLUMN_FRIENDS, user.friends.toString());
		values.put(DbSchema.UserTable.COLUMN_CREATE_DATE, user.date);
		values.put(DbSchema.UserTable.COLUMN_TIMESTAMP, user.updated_date);
		
		if (getUser(user._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.UserTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.UserTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + user._id};
	        mDb.update(DbSchema.UserTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void editUserItem(UserInfo user)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.UserTable.COLUMN_ID, user._id);
		values.put(DbSchema.UserTable.COLUMN_MAIL, user.mail);
		values.put(DbSchema.UserTable.COLUMN_NAME, user.name);
		values.put(DbSchema.UserTable.COLUMN_SCHOOL, user.schoolName);
		values.put(DbSchema.UserTable.COLUMN_ADDRESS, user.address);
		values.put(DbSchema.UserTable.COLUMN_CITY, user.city);
		values.put(DbSchema.UserTable.COLUMN_POSTAL_CODE, user.postcode);
		values.put(DbSchema.UserTable.COLUMN_COUNTRY, user.country);
		values.put(DbSchema.UserTable.COLUMN_SYLLABUS, user.syllabus_id);
		values.put(DbSchema.UserTable.COLUMN_GRADE, user.grade_id);
		values.put(DbSchema.UserTable.COLUMN_SECTION, user.section);
		values.put(DbSchema.UserTable.COLUMN_PHOTO, user.photo_url);
		values.put(DbSchema.UserTable.COLUMN_UPDATE_USER, user.update_on_user);
		values.put(DbSchema.UserTable.COLUMN_FRIENDS, user.friends);
		values.put(DbSchema.UserTable.COLUMN_CREATE_DATE, user.create_date);
		values.put(DbSchema.UserTable.COLUMN_TIMESTAMP, user.timestamp);
		
		if (getUser(user._id) != null) {
			String whereClause = DbSchema.UserTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + user._id};
	        mDb.update(DbSchema.UserTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delUserItem(String userId)
	{
		String whereClause = DbSchema.UserTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + userId};
        mDb.delete(DbSchema.UserTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void resetUserTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.UserTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.UserTable.TABLE_USER_CREATE);
	}

	public ArrayList<UserInfo> getUser()
	{
		ArrayList<UserInfo> userList = new ArrayList<UserInfo>();

		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				UserInfo user = new UserInfo();

				user._id = cursor.getString(0);
				user.mail = cursor.getString(1);
				user.name = cursor.getString(2);
				user.schoolName = cursor.getString(3);
				user.address = cursor.getString(4);
				user.city = cursor.getString(5);
				user.postcode = cursor.getString(6);
				user.country = cursor.getString(7);
				user.syllabus_id = cursor.getString(8);
				user.grade_id = cursor.getString(9);
				user.section = cursor.getString(10);
				user.photo_url = cursor.getString(11);
				user.update_on_user = cursor.getString(12);
				user.friends = cursor.getString(13);
				user.create_date = cursor.getString(14);
				user.timestamp = cursor.getString(15);

				userList.add(user);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return userList;
	}
	
	public ArrayList<UserInfo> getFriends(UserInfo userInfo)
	{
		ArrayList<UserInfo> userList = new ArrayList<UserInfo>();
		
		if (userInfo._id == null || userInfo._id.isEmpty())
			return null;

		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME + " where "
				+ DbSchema.UserTable.COLUMN_FRIENDS + " like '%" + userInfo._id + "%'" + " or "
				+ "( " + DbSchema.UserTable.COLUMN_SCHOOL + " = '" + userInfo.schoolName + "'" + " and "
				+ DbSchema.UserTable.COLUMN_GRADE + " = '" + userInfo.grade_id + "'" +" and "
				+ DbSchema.UserTable.COLUMN_SECTION + " = '" + userInfo.section + "'" +")";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				UserInfo user = new UserInfo();

				user._id = cursor.getString(0);
				user.mail = cursor.getString(1);
				user.name = cursor.getString(2);
				user.schoolName = cursor.getString(3);
				user.address = cursor.getString(4);
				user.city = cursor.getString(5);
				user.postcode = cursor.getString(6);
				user.country = cursor.getString(7);
				user.syllabus_id = cursor.getString(8);
				user.grade_id = cursor.getString(9);
				user.section = cursor.getString(10);
				user.photo_url = cursor.getString(11);
				user.update_on_user = cursor.getString(12);
				user.friends = cursor.getString(13);
				user.create_date = cursor.getString(14);
				user.timestamp = cursor.getString(15);

				userList.add(user);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return userList;
	}
	
	public ArrayList<UserInfo> searchUserByNameMail(String searchTxt)
	{
		ArrayList<UserInfo> userList = new ArrayList<UserInfo>();
		
		if (searchTxt == null || searchTxt.isEmpty())
			return null;

		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME + " where "
				+ DbSchema.UserTable.COLUMN_NAME + " like '%" + searchTxt + "%' or "
				+ DbSchema.UserTable.COLUMN_MAIL + " like '%" + searchTxt + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				UserInfo user = new UserInfo();

				user._id = cursor.getString(0);
				user.mail = cursor.getString(1);
				user.name = cursor.getString(2);
				user.schoolName = cursor.getString(3);
				user.address = cursor.getString(4);
				user.city = cursor.getString(5);
				user.postcode = cursor.getString(6);
				user.country = cursor.getString(7);
				user.syllabus_id = cursor.getString(8);
				user.grade_id = cursor.getString(9);
				user.section = cursor.getString(10);
				user.photo_url = cursor.getString(11);
				user.update_on_user = cursor.getString(12);
				user.friends = cursor.getString(13);
				user.create_date = cursor.getString(14);
				user.timestamp = cursor.getString(15);

				userList.add(user);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return userList;
	}
	
	public UserInfo getUser(String id)
	{
		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME + " where "
				+ DbSchema.UserTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		UserInfo user = new UserInfo();
		
		if (cursor.moveToFirst()) {
			user._id = cursor.getString(0);
			user.mail = cursor.getString(1);
			user.name = cursor.getString(2);
			user.schoolName = cursor.getString(3);
			user.address = cursor.getString(4);
			user.city = cursor.getString(5);
			user.postcode = cursor.getString(6);
			user.country = cursor.getString(7);
			user.syllabus_id = cursor.getString(8);
			user.grade_id = cursor.getString(9);
			user.section = cursor.getString(10);
			user.photo_url = cursor.getString(11);
			user.update_on_user = cursor.getString(12);
			user.friends = cursor.getString(13);
			user.create_date = cursor.getString(14);
			user.timestamp = cursor.getString(15);
		} else {
			user = null;
		}

		cursor.close();

		return user;
	}
	
	public UserInfo getUserByEmail(String email)
	{
		if (email == null || email.isEmpty())
			return null;
		
		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME + " where "
				+ DbSchema.UserTable.COLUMN_MAIL + " = '" + email + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		UserInfo user = new UserInfo();
		
		if (cursor.moveToFirst()) {
			user._id = cursor.getString(0);
			user.mail = cursor.getString(1);
			user.name = cursor.getString(2);
			user.schoolName = cursor.getString(3);
			user.address = cursor.getString(4);
			user.city = cursor.getString(5);
			user.postcode = cursor.getString(6);
			user.country = cursor.getString(7);
			user.syllabus_id = cursor.getString(8);
			user.grade_id = cursor.getString(9);
			user.section = cursor.getString(10);
			user.photo_url = cursor.getString(11);
			user.update_on_user = cursor.getString(12);
			user.friends = cursor.getString(13);
			user.create_date = cursor.getString(14);
			user.timestamp = cursor.getString(15);
		} else {
			user = null;
		}

		cursor.close();

		return user;
	}
	
	public long getUserCheckSum()
	{
		long ret = 0;

		String selectQuery = "select * from " + DbSchema.UserTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(15);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	/*******************************************************************************
	 * 
	 * 								Link Table
	 * 
	 *******************************************************************************/

	public void addLinkItem(LinkResult link)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.LinkTable.COLUMN_ID, link._id);
		values.put(DbSchema.LinkTable.COLUMN_KIND, link.kind);
		values.put(DbSchema.LinkTable.COLUMN_SYLLABUS, link.syllabus);
		values.put(DbSchema.LinkTable.COLUMN_GRADE, link.grade);
		values.put(DbSchema.LinkTable.COLUMN_CHAPTER, link.chapter);
		values.put(DbSchema.LinkTable.COLUMN_CONCEPT, link.concept);
		values.put(DbSchema.LinkTable.COLUMN_CREATE_DATE, link.date);
		values.put(DbSchema.LinkTable.COLUMN_TIMESTAMP, link.updated_date);
		
		if (getSyllabus(link._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.LinkTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.LinkTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + link._id};
	        mDb.update(DbSchema.LinkTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateLinkItem(Link link)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.LinkTable.COLUMN_ID, link.id);
		values.put(DbSchema.LinkTable.COLUMN_KIND, link.kind);
		values.put(DbSchema.LinkTable.COLUMN_SYLLABUS, link.syllabus);
		values.put(DbSchema.LinkTable.COLUMN_GRADE, link.grade);
		values.put(DbSchema.LinkTable.COLUMN_CHAPTER, link.chapter);
		values.put(DbSchema.LinkTable.COLUMN_CONCEPT, link.concept);
		values.put(DbSchema.LinkTable.COLUMN_CREATE_DATE, link.create_date);
		values.put(DbSchema.LinkTable.COLUMN_TIMESTAMP, link.timestamp);
		
		if (getSyllabus(link.id) != null) {
			String whereClause = DbSchema.LinkTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + link.id};
	        mDb.update(DbSchema.LinkTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delLinkItem(String linktId)
	{
		String whereClause = DbSchema.LinkTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + linktId};
        mDb.delete(DbSchema.LinkTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void resetLinkTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.LinkTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.LinkTable.TABLE_LINK_CREATE);
	}
	
	public void delGradeLinks()
	{
		ArrayList<String> linkList = new ArrayList<String>();

		String selectQuery;
		
		selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
				+ DbSchema.LinkTable.COLUMN_KIND + " = 'grade'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String linkId;

				linkId = cursor.getString(0);

				linkList.add(linkId);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : linkList)
		{
			delLinkItem (id);
		}
	}
	
	public void delConceptLinksByParam(String syllabusId, String gradeId)
	{
		ArrayList<String> linkList = new ArrayList<String>();

		String selectQuery;
		
		if (syllabusId == null || gradeId == null)
			selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
					+ DbSchema.LinkTable.COLUMN_KIND + " = 'concept' and "
					+ DbSchema.LinkTable.COLUMN_SYLLABUS + " = '" + syllabusId + "' and "
					+ DbSchema.LinkTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String linkId;

				linkId = cursor.getString(0);

				linkList.add(linkId);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : linkList)
		{
			delLinkItem (id);
		}
	}

	public ArrayList<Link> getLink()
	{
		ArrayList<Link> linkList = new ArrayList<Link>();

		String selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Link link = new Link();

				link.id = cursor.getString(0);
				link.kind = cursor.getString(1);
				link.syllabus = cursor.getString(2);
				link.grade = cursor.getString(3);
				link.chapter = cursor.getString(4);
				link.concept = cursor.getString(5);
				link.create_date = cursor.getString(6);
				link.timestamp = cursor.getString(7);

				linkList.add(link);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return linkList;
	}

	public Link getLink(String id)
	{
		String selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
				+ DbSchema.LinkTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Link link = new Link();
		
		if (cursor.moveToFirst()) {
			link.id = cursor.getString(0);
			link.kind = cursor.getString(1);
			link.syllabus = cursor.getString(2);
			link.grade = cursor.getString(3);
			link.chapter = cursor.getString(4);
			link.concept = cursor.getString(5);
			link.create_date = cursor.getString(6);
			link.timestamp = cursor.getString(7);
		} else {
			link = null;
		}

		cursor.close();

		return link;
	}
	
	public ArrayList<Link> getGradeLinks(String syllabus)
	{
		ArrayList<Link> linkList = new ArrayList<Link>();

		String selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
				+ DbSchema.LinkTable.COLUMN_KIND + " = 'grade' and " 
				+ DbSchema.LinkTable.COLUMN_SYLLABUS + " = '" + syllabus + "'"; 


		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Link link = new Link();

				link.id = cursor.getString(0);
				link.kind = cursor.getString(1);
				link.syllabus = cursor.getString(2);
				link.grade = cursor.getString(3);
				link.chapter = cursor.getString(4);
				link.concept = cursor.getString(5);
				link.create_date = cursor.getString(6);
				link.timestamp = cursor.getString(7);

				linkList.add(link);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return linkList;
	}
	
	public ArrayList<Link> getConceptLinksByParam(String syllabusId, String gradeId, String chapterId)
	{
		ArrayList<Link> linkList = new ArrayList<Link>();

		String selectQuery;
		
		if (syllabusId == null || gradeId == null || chapterId == null)
			return linkList;
		else
			selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
					+ DbSchema.LinkTable.COLUMN_KIND + " = 'concept' and " 
					+ DbSchema.LinkTable.COLUMN_SYLLABUS + " = '" + syllabusId + "'" + " and "
					+ DbSchema.LinkTable.COLUMN_GRADE + " = '" + gradeId + "'" + " and "
					+ DbSchema.LinkTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Link link = new Link();

				link.id = cursor.getString(0);
				link.kind = cursor.getString(1);
				link.syllabus = cursor.getString(2);
				link.grade = cursor.getString(3);
				link.chapter = cursor.getString(4);
				link.concept = cursor.getString(5);
				link.create_date = cursor.getString(6);
				link.timestamp = cursor.getString(7);

				linkList.add(link);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return linkList;
	}
	
	public long getGradeLinkCheckSum()
	{
		long ret = 0;

		String selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
				+ DbSchema.LinkTable.COLUMN_KIND + " = 'grade'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(7);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getConceptLinkCheckSum(String syllabusId, String gradeId)
	{
		long ret = 0;

		String selectQuery;
		
		if (syllabusId == null || gradeId == null)
			selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.LinkTable.TABLE_NAME + " where "
					+ DbSchema.LinkTable.COLUMN_KIND + " = 'concept' and "
					+ DbSchema.LinkTable.COLUMN_SYLLABUS + " = '" + syllabusId + "' and "
					+ DbSchema.LinkTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(7);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	/*******************************************************************************
	 * 
	 * 								Syllabus Table
	 * 
	 *******************************************************************************/

	public void addSyllabusItem(SyllabusResult syllabus)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.SyllabusTable.COLUMN_ID, syllabus._id);
		values.put(DbSchema.SyllabusTable.COLUMN_ENABLE, syllabus.enabled);
		values.put(DbSchema.SyllabusTable.COLUMN_TITLE, syllabus.title);
		values.put(DbSchema.SyllabusTable.COLUMN_DESCRIPTION, syllabus.description);
		values.put(DbSchema.SyllabusTable.COLUMN_CREATE_DATE, syllabus.date);
		values.put(DbSchema.SyllabusTable.COLUMN_TIMESTAMP, syllabus.updated_date);
		
		if (getSyllabus(syllabus._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.SyllabusTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.SyllabusTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + syllabus._id};
	        mDb.update(DbSchema.SyllabusTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateSyllabusItem(Syllabus syllabus)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.SyllabusTable.COLUMN_ID, syllabus.id);
		values.put(DbSchema.SyllabusTable.COLUMN_ENABLE, syllabus.enable);
		values.put(DbSchema.SyllabusTable.COLUMN_TITLE, syllabus.title);
		values.put(DbSchema.SyllabusTable.COLUMN_DESCRIPTION, syllabus.description);
		values.put(DbSchema.SyllabusTable.COLUMN_CREATE_DATE, syllabus.create_date);
		values.put(DbSchema.SyllabusTable.COLUMN_TIMESTAMP, syllabus.timestamp);
		
		if (getSyllabus(syllabus.id) != null) {
			String whereClause = DbSchema.SyllabusTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + syllabus.id};
	        mDb.update(DbSchema.SyllabusTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delSyllabusItem()
	{}
	
	public void resetSyllabusTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.SyllabusTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.SyllabusTable.TABLE_SYLLABUS_CREATE);
	}

	public ArrayList<Syllabus> getSyllabus()
	{
		ArrayList<Syllabus> syllabusList = new ArrayList<Syllabus>();

		String selectQuery = "select * from " + DbSchema.SyllabusTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Syllabus syllabus = new Syllabus();

				syllabus.id = cursor.getString(0);
				syllabus.enable = cursor.getInt(1);
				syllabus.title = cursor.getString(2);
				syllabus.description = cursor.getString(3);
				syllabus.create_date = cursor.getString(4);
				syllabus.timestamp = cursor.getString(5);

				if (syllabus.enable == 1)
					syllabusList.add(syllabus);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return syllabusList;
	}

	public Syllabus getSyllabus(String id)
	{
		String selectQuery = "select * from " + DbSchema.SyllabusTable.TABLE_NAME + " where "
				+ DbSchema.SyllabusTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Syllabus syllabus = new Syllabus();
		
		if (cursor.moveToFirst()) {
			syllabus.id = cursor.getString(0);
			syllabus.enable = cursor.getInt(1);
			syllabus.title = cursor.getString(2);
			syllabus.description = cursor.getString(3);
			syllabus.create_date = cursor.getString(4);
			syllabus.timestamp = cursor.getString(5);
		} else {
			syllabus = null;
		}

		cursor.close();

		return syllabus;
	}
	
	public Syllabus getSyllabusByTitle(String title)
	{
		String selectQuery = "select * from " + DbSchema.SyllabusTable.TABLE_NAME + " where "
				+ DbSchema.SyllabusTable.COLUMN_TITLE + " = '" + title + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Syllabus syllabus = new Syllabus();
		
		if (cursor.moveToFirst()) {
			syllabus.id = cursor.getString(0);
			syllabus.enable = cursor.getInt(1);
			syllabus.title = cursor.getString(2);
			syllabus.description = cursor.getString(3);
			syllabus.create_date = cursor.getString(4);
			syllabus.timestamp = cursor.getString(5);
		} else {
			syllabus = null;
		}

		cursor.close();

		return syllabus;
	}
	
	public long getSyllabusCheckSum()
	{
		long ret = 0;

		String selectQuery = "select * from " + DbSchema.SyllabusTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(5);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}

	/*******************************************************************************
	 * 
	 * 								Grade Table
	 * 
	 *******************************************************************************/

	public void addGradeItem(GradeResult grade)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.GradeTable.COLUMN_ID, grade._id);
		values.put(DbSchema.GradeTable.COLUMN_GRADE, grade.grade);
		values.put(DbSchema.GradeTable.COLUMN_ENABLE, grade.enabled);
		values.put(DbSchema.GradeTable.COLUMN_IMAGE, grade.image);
		values.put(DbSchema.GradeTable.COLUMN_CREATE_DATE, grade.date);
		values.put(DbSchema.GradeTable.COLUMN_TIMESTAMP, grade.updated_date);
		
		if (getGrade(grade._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.GradeTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.GradeTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + grade._id};
	        mDb.update(DbSchema.GradeTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateGradeItem(Grade grade)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.GradeTable.COLUMN_ID, grade.id);
		values.put(DbSchema.GradeTable.COLUMN_GRADE, grade.grade_name);
		values.put(DbSchema.GradeTable.COLUMN_ENABLE, grade.enable);
		values.put(DbSchema.GradeTable.COLUMN_IMAGE, grade.image);
		values.put(DbSchema.GradeTable.COLUMN_CREATE_DATE, grade.create_date);
		values.put(DbSchema.GradeTable.COLUMN_TIMESTAMP, grade.timestamp);
		
		if (getGrade(grade.id) != null) {
			String whereClause = DbSchema.GradeTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + grade.id};
	        mDb.update(DbSchema.GradeTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delGradeItem()
	{}
	
	public void resetGradeTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.GradeTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.GradeTable.TABLE_GRADE_CREATE);
	}

	public ArrayList<Grade> getGrade()
	{
		ArrayList<Grade> gradeList = new ArrayList<Grade>();

		String selectQuery = "select * from " + DbSchema.GradeTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Grade grade = new Grade();

				grade.id = cursor.getString(0);
				grade.grade_name = cursor.getString(1);
				grade.enable = cursor.getInt(2);
				grade.image = cursor.getString(3);
				grade.create_date = cursor.getString(4);
				grade.timestamp = cursor.getString(5);

				gradeList.add(grade);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return gradeList;
	}

	public Grade getGrade(String id)
	{
		String selectQuery = "select * from " + DbSchema.GradeTable.TABLE_NAME + " where "
				+ DbSchema.GradeTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Grade grade = new Grade();
		
		if (cursor.moveToFirst()) {
			grade.id = cursor.getString(0);
			grade.grade_name = cursor.getString(1);
			grade.enable = cursor.getInt(2);
			grade.image = cursor.getString(3);
			grade.create_date = cursor.getString(4);
			grade.timestamp = cursor.getString(5);
		} else {
			grade = null;
		}

		cursor.close();

		return grade;
	}
	
	public Grade getGradeByName(String gradeName)
	{
		String selectQuery = "select * from " + DbSchema.GradeTable.TABLE_NAME + " where "
				+ DbSchema.GradeTable.COLUMN_GRADE + " = '" + gradeName + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Grade grade = new Grade();
		
		if (cursor.moveToFirst()) {
			grade.id = cursor.getString(0);
			grade.grade_name = cursor.getString(1);
			grade.enable = cursor.getInt(2);
			grade.image = cursor.getString(3);
			grade.create_date = cursor.getString(4);
			grade.timestamp = cursor.getString(5);
		} else {
			grade = null;
		}

		cursor.close();

		return grade;
	}
	
	public long getGradeCheckSum()
	{
		long ret = 0;

		String selectQuery = "select * from " + DbSchema.GradeTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(5);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}

	/*******************************************************************************
	 * 
	 * 								Chapter Table
	 * 
	 *******************************************************************************/
	public void addChapterItem(ChapterResult chapter)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ChapterTable.COLUMN_ID, chapter._id);
		values.put(DbSchema.ChapterTable.COLUMN_TITLE, chapter.title);
		values.put(DbSchema.ChapterTable.COLUMN_SYLLABUS, chapter.syllabus + "");
		values.put(DbSchema.ChapterTable.COLUMN_GRADE, chapter.grade + "");
		values.put(DbSchema.ChapterTable.COLUMN_ENABLE, chapter.enabled);
		values.put(DbSchema.ChapterTable.COLUMN_DSCP, chapter.description);
		values.put(DbSchema.ChapterTable.COLUMN_IMAGE, chapter.image);
		values.put(DbSchema.ChapterTable.COLUMN_CREATE_DATE, chapter.date);
		values.put(DbSchema.ChapterTable.COLUMN_TIMESTAMP, chapter.updated_date);
		
		if (getChapter(chapter._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.ChapterTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.ChapterTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + chapter._id};
	        mDb.update(DbSchema.ChapterTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateChapterItem(Chapter chapter)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ChapterTable.COLUMN_ID, chapter.id);
		values.put(DbSchema.ChapterTable.COLUMN_TITLE, chapter.title);
		values.put(DbSchema.ChapterTable.COLUMN_SYLLABUS, chapter.syllabus_id);
		values.put(DbSchema.ChapterTable.COLUMN_GRADE, chapter.grade_id);
		values.put(DbSchema.ChapterTable.COLUMN_ENABLE, chapter.enable);
		values.put(DbSchema.ChapterTable.COLUMN_DSCP, chapter.description);
		values.put(DbSchema.ChapterTable.COLUMN_IMAGE, chapter.image);
		values.put(DbSchema.ChapterTable.COLUMN_CREATE_DATE, chapter.create_date);
		values.put(DbSchema.ChapterTable.COLUMN_TIMESTAMP, chapter.timestamp);
		
		if (getChapter(chapter.id) != null) {
			String whereClause = DbSchema.ChapterTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + chapter.id};
	        mDb.update(DbSchema.ChapterTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delChapterItem(String chapterId)
	{
		String whereClause = DbSchema.ChapterTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + chapterId};
        mDb.delete(DbSchema.ChapterTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void delChapterByParam(String syllabusId, String gradeId)
	{
		ArrayList<String> chapterList = new ArrayList<String>();

		String selectQuery;
		
		if (syllabusId == null || gradeId == null)
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
					+ DbSchema.ChapterTable.COLUMN_SYLLABUS + " = '" + syllabusId + "' and "
					+ DbSchema.ChapterTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String chapterId;

				chapterId = cursor.getString(0);

				chapterList.add(chapterId);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : chapterList)
		{
			delChapterItem (id);
		}
	}
	
	public void resetChapterTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.ChapterTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.ChapterTable.TABLE_CHAPTER_CREATE);
	}

	public ArrayList<Chapter> getAllChapter()
	{
		ArrayList<Chapter> chapterList = new ArrayList<Chapter>();

		String selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Chapter chapter = new Chapter();

				chapter.id = cursor.getString(0);
				chapter.title = cursor.getString(1);
				chapter.syllabus_id = cursor.getString(2);
				chapter.grade_id = cursor.getString(3);
				chapter.enable = cursor.getInt(4);
				chapter.description = cursor.getString(5);
				chapter.image = cursor.getString(6);
				chapter.create_date = cursor.getString(7);
				chapter.timestamp = cursor.getString(8);

				chapterList.add(chapter);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return chapterList;
	}
	
	public ArrayList<Chapter> getChapterByGrade(String gradeId)
	{
		ArrayList<Chapter> chapterList = new ArrayList<Chapter>();
		String selectQuery;

		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
					+ DbSchema.ChapterTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Chapter chapter = new Chapter();

				chapter.id = cursor.getString(0);
				chapter.title = cursor.getString(1);
				chapter.syllabus_id = cursor.getString(2);
				chapter.grade_id = cursor.getString(3);
				chapter.enable = cursor.getInt(4);
				chapter.description = cursor.getString(5);
				chapter.image = cursor.getString(6);
				chapter.create_date = cursor.getString(7);
				chapter.timestamp = cursor.getString(8);

				if (chapter.enable == 1)
					chapterList.add(chapter);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return chapterList;
	}
	
	public Chapter getChapter(String id)
	{
		String selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
				+ DbSchema.ChapterTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Chapter chapter = new Chapter();
		
		if (cursor.moveToFirst()) {
			chapter.id = cursor.getString(0);
			chapter.title = cursor.getString(1);
			chapter.syllabus_id = cursor.getString(2);
			chapter.grade_id = cursor.getString(3);
			chapter.enable = cursor.getInt(4);
			chapter.description = cursor.getString(5);
			chapter.image = cursor.getString(6);
			chapter.create_date = cursor.getString(7);
			chapter.timestamp = cursor.getString(8);
		} else {
			chapter = null;
		}

		cursor.close();

		return chapter;
	}
	
	public long getChapterCheckSum(String syllabusId, String gradeId)
	{
		long ret = 0;

		String selectQuery;
		
		if (syllabusId == null && gradeId == null)
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME;
		else if (syllabusId == null && gradeId != null)
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
					+ DbSchema.ChapterTable.COLUMN_GRADE + " = '" + gradeId + "'";
		else if (syllabusId != null && gradeId == null)
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
					+ DbSchema.ChapterTable.COLUMN_SYLLABUS + " = '" + syllabusId + "'";
		else
			selectQuery = "select * from " + DbSchema.ChapterTable.TABLE_NAME + " where "
					+ DbSchema.ChapterTable.COLUMN_SYLLABUS + " = '" + syllabusId + "'" + " and "
					+ DbSchema.ChapterTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(8);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	/*******************************************************************************
	 * 
	 * 								Concept Table
	 * 
	 *******************************************************************************/

	public void addConceptItem(ConceptResult concept)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ConceptTable.COLUMN_ID, concept._id);
		values.put(DbSchema.ConceptTable.COLUMN_TITLE, concept.title);
		values.put(DbSchema.ConceptTable.COLUMN_SYLLABUS, concept.syllabus);
		values.put(DbSchema.ConceptTable.COLUMN_GRADE, concept.grade);
		values.put(DbSchema.ConceptTable.COLUMN_CHAPTER, concept.chapter);
		values.put(DbSchema.ConceptTable.COLUMN_ENABLE, concept.enabled);
		values.put(DbSchema.ConceptTable.COLUMN_TEXT, concept.text);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE, concept.image);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE_CREDIT, concept.image_credit);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE_SRC, concept.image_source);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2, concept.image2);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2_CREDIT, concept.image2_credit);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2_SRC, concept.image2_source);
		values.put(DbSchema.ConceptTable.COLUMN_CREATE_DATE, concept.date);
		values.put(DbSchema.ConceptTable.COLUMN_TIMESTAMP, concept.updated_date);
		
		if (getConcept(concept._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.ConceptTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.ConceptTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + concept._id};
	        mDb.update(DbSchema.ConceptTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateConceptItem(Concept concept)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ConceptTable.COLUMN_ID, concept.id);
		values.put(DbSchema.ConceptTable.COLUMN_TITLE, concept.title);
		values.put(DbSchema.ConceptTable.COLUMN_SYLLABUS, concept.syllabusId);
		values.put(DbSchema.ConceptTable.COLUMN_GRADE, concept.gradeId);
		values.put(DbSchema.ConceptTable.COLUMN_CHAPTER, concept.chapterId);
		values.put(DbSchema.ConceptTable.COLUMN_ENABLE, concept.enable);
		values.put(DbSchema.ConceptTable.COLUMN_TEXT, concept.text);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE, concept.image);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE_CREDIT, concept.image_credit);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE_SRC, concept.image_source);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2, concept.image2);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2_CREDIT, concept.image2_credit);
		values.put(DbSchema.ConceptTable.COLUMN_IMAGE2_SRC, concept.image2_source);
		values.put(DbSchema.ConceptTable.COLUMN_CREATE_DATE, concept.create_date);
		values.put(DbSchema.ConceptTable.COLUMN_TIMESTAMP, concept.timestamp);
		
		if (getConcept(concept.id) != null) {
			String whereClause = DbSchema.ConceptTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + concept.id};
	        mDb.update(DbSchema.ConceptTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delConceptItem(String conceptId)
	{
		String whereClause = DbSchema.ConceptTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + conceptId};
        mDb.delete(DbSchema.ConceptTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void delConceptByParam(String syllabusId, String gradeId)
	{
		ArrayList<String> conceptList = new ArrayList<String>();

		String selectQuery;
		
		if (syllabusId == null || gradeId == null)
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
					+ DbSchema.ConceptTable.COLUMN_SYLLABUS + " = '" + syllabusId + "' and "
					+ DbSchema.ConceptTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String conceptId;

				conceptId = cursor.getString(0);

				conceptList.add(conceptId);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : conceptList)
		{
			delConceptItem (id);
		}
	}
	
	public void resetConceptTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.ConceptTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.ConceptTable.TABLE_CONCEPT_CREATE);
	}

	public ArrayList<Concept> getAllConcept()
	{
		ArrayList<Concept> conceptList = new ArrayList<Concept>();

		String selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Concept concept = new Concept();

				concept.id = cursor.getString(0);
				concept.title = cursor.getString(1);
				concept.syllabusId = cursor.getString(2);
				concept.gradeId = cursor.getString(3);
				concept.chapterId = cursor.getString(4);
				concept.enable = cursor.getInt(5);
				concept.text = cursor.getString(6);
				concept.image = cursor.getString(7);
				concept.image_credit = cursor.getString(8);
				concept.image_source = cursor.getString(9);
				concept.image2 = cursor.getString(10);
				concept.image2_credit = cursor.getString(11);
				concept.image2_source = cursor.getString(12);
				concept.create_date = cursor.getString(13);
				concept.timestamp = cursor.getString(14);

				conceptList.add(concept);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return conceptList;
	}
	
	public ArrayList<Concept> getConceptByParam(String syllabusId, String gradeId, String chapterId)
	{
		ArrayList<Concept> conceptList = new ArrayList<Concept>();
//		
//		String selectQuery;
//
//		if (gradeId == null || chapterId == null)
//			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME;
//		else
//			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
//					+ DbSchema.ConceptTable.COLUMN_SYLLABUS + " = '" + syllabusId + "'" + " and "
//					+ DbSchema.ConceptTable.COLUMN_GRADE + " = '" + gradeId + "'" + " and "
//					+ DbSchema.ConceptTable.COLUMN_CHAPTER + " = '" + chapterId + "'";
//
//		Cursor cursor = mDb.rawQuery(selectQuery, null);		
//		if (cursor.moveToFirst()) {
//			do {
//				Concept concept = new Concept();
//
//				concept.id = cursor.getString(0);
//				concept.title = cursor.getString(1);
//				concept.syllabusId = cursor.getString(2);
//				concept.gradeId = cursor.getString(3);
//				concept.chapterId = cursor.getString(4);
//				concept.text = cursor.getString(5);
//				concept.image = cursor.getString(6);
//				concept.image_credit = cursor.getString(7);
//				concept.image_source = cursor.getString(8);
//				concept.create_date = cursor.getString(9);
//				concept.timestamp = cursor.getString(10);
//
//				conceptList.add(concept);
//			} while (cursor.moveToNext());
//		}
//
//		cursor.close();
		
		ArrayList<Link> links = getConceptLinksByParam(syllabusId, gradeId, chapterId);
		for (Link link : links)
		{
			Concept concept = getConcept(link.concept);
			if (concept.enable == 1)
				conceptList.add(concept);
		}
		
		return conceptList;
	}
	
	public Concept getConcept(String id)
	{
		String selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
				+ DbSchema.ConceptTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Concept concept = new Concept();
		
		if (cursor.moveToFirst()) {
			concept.id = cursor.getString(0);
			concept.title = cursor.getString(1);
			concept.syllabusId = cursor.getString(2);
			concept.gradeId = cursor.getString(3);
			concept.chapterId = cursor.getString(4);
			concept.enable = cursor.getInt(5);
			concept.text = cursor.getString(6);
			concept.image = cursor.getString(7);
			concept.image_credit = cursor.getString(8);
			concept.image_source = cursor.getString(9);
			concept.image2 = cursor.getString(10);
			concept.image2_credit = cursor.getString(11);
			concept.image2_source = cursor.getString(12);
			concept.create_date = cursor.getString(13);
			concept.timestamp = cursor.getString(14);
		} else {
			concept = null;
		}

		cursor.close();

		return concept;
	}
	
	public long getConceptCheckSum(String gradeId, String chapterId)
	{
		long ret = 0;

		String selectQuery;
		
		if (gradeId == null && chapterId == null)
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME;
		else if (gradeId == null && chapterId != null)
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
					+ DbSchema.ConceptTable.COLUMN_CHAPTER + " = '" + chapterId + "'";
		else if (gradeId != null && chapterId == null)
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
					+ DbSchema.ConceptTable.COLUMN_GRADE + " = '" + gradeId + "'";
		else
			selectQuery = "select * from " + DbSchema.ConceptTable.TABLE_NAME + " where "
					+ DbSchema.ConceptTable.COLUMN_GRADE + " = '" + gradeId + "'" + " and "
					+ DbSchema.ConceptTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(14);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}

	/*******************************************************************************
	 * 
	 * 								Video Table
	 * 
	 *******************************************************************************/

	public void addVideoItem(VideoResult video)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.VideoTable.COLUMN_ID, video._id);
		values.put(DbSchema.VideoTable.COLUMN_URL, video.url);
		values.put(DbSchema.VideoTable.COLUMN_OWNER, video.owner);
		values.put(DbSchema.VideoTable.COLUMN_GRADE, video.grade);
		values.put(DbSchema.VideoTable.COLUMN_CHAPTER, video.chapter);
		values.put(DbSchema.VideoTable.COLUMN_CONCEPT, video.concept);
		values.put(DbSchema.VideoTable.COLUMN_SHARED_USER, video.shared_user.toString());
		values.put(DbSchema.VideoTable.COLUMN_DEFAULED_USER, video.defaulted_user.toString());
		values.put(DbSchema.VideoTable.COLUMN_PRIVATED_USER, video.privated_user.toString());
		values.put(DbSchema.VideoTable.COLUMN_CREATE_DATE, video.date);
		values.put(DbSchema.VideoTable.COLUMN_TIMESTAMP, video.updated_date);
		
		if (getVideo(video._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.VideoTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.VideoTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + video._id};
	        mDb.update(DbSchema.VideoTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateVideoItem(Video video)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.VideoTable.COLUMN_ID, video.id);
		values.put(DbSchema.VideoTable.COLUMN_URL, video.url);
		values.put(DbSchema.VideoTable.COLUMN_OWNER, video.ownerId);		
		values.put(DbSchema.VideoTable.COLUMN_GRADE, video.gradeId);
		values.put(DbSchema.VideoTable.COLUMN_CHAPTER, video.chapterId);
		values.put(DbSchema.VideoTable.COLUMN_CONCEPT, video.conceptId);
		values.put(DbSchema.VideoTable.COLUMN_SHARED_USER, video.shared_user);
		values.put(DbSchema.VideoTable.COLUMN_DEFAULED_USER, video.defaulted_user);
		values.put(DbSchema.VideoTable.COLUMN_PRIVATED_USER, video.privated_user);
		values.put(DbSchema.VideoTable.COLUMN_CREATE_DATE, video.create_date);
		values.put(DbSchema.VideoTable.COLUMN_TIMESTAMP, video.timestamp);
		
		if (getVideo(video.id) != null) {
			String whereClause = DbSchema.VideoTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + video.id};
	        mDb.update(DbSchema.VideoTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delVideoItem(String id)
	{
		String whereClause = DbSchema.VideoTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + id};
        mDb.delete(DbSchema.VideoTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void delVideoByGrade(String gradeId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delVideoItem (id);
		}
	}
		
	public void delVideoByChapter(String chapterId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delVideoItem (id);
		}
	}
	
	public void delVideoByConcept(String conceptId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delVideoItem (id);
		}
	}
	
	public void resetVideoTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.VideoTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.VideoTable.TABLE_VIDEO_CREATE);
	}

	public ArrayList<Video> getVideo()
	{
		ArrayList<Video> videoList = new ArrayList<Video>();

		String selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Video video = new Video();

				video.id = cursor.getString(0);
				video.url = cursor.getString(1);
				video.ownerId = cursor.getString(2);
				video.gradeId = cursor.getString(3);
				video.chapterId = cursor.getString(4);
				video.conceptId = cursor.getString(5);
				video.shared_user = cursor.getString(6);
				video.defaulted_user = cursor.getString(7);
				video.privated_user = cursor.getString(8);
				video.create_date = cursor.getString(9);
				video.timestamp = cursor.getString(10);

				videoList.add(video);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return videoList;
	}
	
	public Video getVideo(String id)
	{
		String selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
				+ DbSchema.VideoTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Video video = new Video();
		
		if (cursor.moveToFirst()) {
			video.id = cursor.getString(0);
			video.url = cursor.getString(1);
			video.ownerId = cursor.getString(2);
			video.gradeId = cursor.getString(3);
			video.chapterId = cursor.getString(4);
			video.conceptId = cursor.getString(5);
			video.shared_user = cursor.getString(6);
			video.defaulted_user = cursor.getString(7);
			video.privated_user = cursor.getString(8);
			video.create_date = cursor.getString(9);
			video.timestamp = cursor.getString(10);
		} else {
			video = null;
		}

		cursor.close();

		return video;
	}
	
	public ArrayList<Video> getVideoByConceptUser(String conceptId, String userId, ArrayList<UserInfo> classmates)
	{
		ArrayList<Video> videoList = new ArrayList<Video>();
		
		String selectQuery;
		String subQuery = "";
		
		if (classmates != null) {
			for (UserInfo classmate: classmates) {
				subQuery += " or " + DbSchema.VideoTable.COLUMN_SHARED_USER + " like '%" + classmate._id + "%'";
			}
		}

		if (conceptId == null || userId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ "( " + DbSchema.VideoTable.COLUMN_OWNER + " = 'admin'" + " or "
					+ DbSchema.VideoTable.COLUMN_SHARED_USER + " like '%" +userId + "%'"
					+ subQuery + ")"
					+ " ORDER BY " + DbSchema.VideoTable.COLUMN_CREATE_DATE;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Video video = new Video();

				video.id = cursor.getString(0);
				video.url = cursor.getString(1);
				video.ownerId = cursor.getString(2);
				video.gradeId = cursor.getString(3);
				video.chapterId = cursor.getString(4);
				video.conceptId = cursor.getString(5);
				video.shared_user = cursor.getString(6);
				video.defaulted_user = cursor.getString(7);
				video.privated_user = cursor.getString(8);
				video.create_date = cursor.getString(9);
				video.timestamp = cursor.getString(10);

				videoList.add(video);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return videoList;
	}
	
	public ArrayList<Video> getVideoByConceptFriend(String conceptId, String friendId, boolean isPublic)
	{
		ArrayList<Video> videoList = new ArrayList<Video>();
		
		String selectQuery;

		if (conceptId == null || friendId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.VideoTable.COLUMN_SHARED_USER + " like '%" +friendId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Video video = new Video();

				video.id = cursor.getString(0);
				video.url = cursor.getString(1);
				video.ownerId = cursor.getString(2);
				video.gradeId = cursor.getString(3);
				video.chapterId = cursor.getString(4);
				video.conceptId = cursor.getString(5);
				video.shared_user = cursor.getString(6);
				video.defaulted_user = cursor.getString(7);
				video.privated_user = cursor.getString(8);
				video.create_date = cursor.getString(9);
				video.timestamp = cursor.getString(10);
				
				if (isPublic == true) {
					if (!video.privated_user.contains(friendId))
						videoList.add(video);
				} else {
					videoList.add(video);
				}				
			} while (cursor.moveToNext());
		}

		cursor.close();

		return videoList;
	}
	
	public long getVideoCheckSum(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getVideoCheckSumByGrade(String gradeId)
	{
		long ret = 0;

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getVideoCheckSumByChapter(String chapterId)
	{
		long ret = 0;

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getVideoCheckSumByConcept(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public Video getDefaultVideo(String conceptId, String userId)
	{
		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME;
		else if (userId == null)
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'";
		else if (userId.equals("admin"))
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.VideoTable.COLUMN_OWNER + " = '" +userId + "'";
		else
			selectQuery = "select * from " + DbSchema.VideoTable.TABLE_NAME + " where "
					+ DbSchema.VideoTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.VideoTable.COLUMN_DEFAULED_USER + " like '%" +userId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Video video = new Video();
		
		if (cursor.moveToFirst()) {
			video.id = cursor.getString(0);
			video.url = cursor.getString(1);
			video.ownerId = cursor.getString(2);
			video.gradeId = cursor.getString(3);
			video.chapterId = cursor.getString(4);
			video.conceptId = cursor.getString(5);
			video.shared_user = cursor.getString(6);
			video.defaulted_user = cursor.getString(7);
			video.privated_user = cursor.getString(8);
			video.create_date = cursor.getString(9);
			video.timestamp = cursor.getString(10);
		} else {
			video = null;
		}
		
		cursor.close();

		return video;
	}

	/*******************************************************************************
	 * 
	 * 								Reference Table
	 * 
	 *******************************************************************************/
	
	public void addReferenceItem(ReferenceResult ref)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ReferenceTable.COLUMN_ID, ref._id);
		values.put(DbSchema.ReferenceTable.COLUMN_URL, ref.url);
		values.put(DbSchema.ReferenceTable.COLUMN_TITLE, ref.title);
		values.put(DbSchema.ReferenceTable.COLUMN_DSCP, ref.description);
		values.put(DbSchema.ReferenceTable.COLUMN_IMAGE, ref.image);
		values.put(DbSchema.ReferenceTable.COLUMN_OWNER, ref.owner);
		values.put(DbSchema.ReferenceTable.COLUMN_GRADE, ref.grade);
		values.put(DbSchema.ReferenceTable.COLUMN_CHAPTER, ref.chapter);
		values.put(DbSchema.ReferenceTable.COLUMN_CONCEPT, ref.concept);
		values.put(DbSchema.ReferenceTable.COLUMN_SHARED_USER, ref.shared_user.toString());
		values.put(DbSchema.ReferenceTable.COLUMN_DEFAULED_USER, ref.defaulted_user.toString());
		values.put(DbSchema.ReferenceTable.COLUMN_PRIVATED_USER, ref.privated_user.toString());
		values.put(DbSchema.ReferenceTable.COLUMN_CREATE_DATE, ref.date);
		values.put(DbSchema.ReferenceTable.COLUMN_TIMESTAMP, ref.updated_date);
		
		if (getReference(ref._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.ReferenceTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.ReferenceTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + ref._id};
	        mDb.update(DbSchema.ReferenceTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateReferenceItem(Reference ref)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.ReferenceTable.COLUMN_ID, ref.id);
		values.put(DbSchema.ReferenceTable.COLUMN_URL, ref.url);
		values.put(DbSchema.ReferenceTable.COLUMN_TITLE, ref.title);
		values.put(DbSchema.ReferenceTable.COLUMN_DSCP, ref.description);
		values.put(DbSchema.ReferenceTable.COLUMN_IMAGE, ref.image);
		values.put(DbSchema.ReferenceTable.COLUMN_OWNER, ref.ownerId);
		values.put(DbSchema.ReferenceTable.COLUMN_GRADE, ref.gradeId);
		values.put(DbSchema.ReferenceTable.COLUMN_CHAPTER, ref.chapterId);
		values.put(DbSchema.ReferenceTable.COLUMN_CONCEPT, ref.conceptId);
		values.put(DbSchema.ReferenceTable.COLUMN_SHARED_USER, ref.shared_user);
		values.put(DbSchema.ReferenceTable.COLUMN_DEFAULED_USER, ref.defaulted_user);
		values.put(DbSchema.ReferenceTable.COLUMN_PRIVATED_USER, ref.privated_user);
		values.put(DbSchema.ReferenceTable.COLUMN_CREATE_DATE, ref.create_date);
		values.put(DbSchema.ReferenceTable.COLUMN_TIMESTAMP, ref.timestamp);
		
		if (getReference(ref.id) != null) {
			String whereClause = DbSchema.ReferenceTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + ref.id};
	        mDb.update(DbSchema.ReferenceTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}
	
	public void delReferenceItem(String id)
	{
		String whereClause = DbSchema.ReferenceTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + id};
        mDb.delete(DbSchema.ReferenceTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void delReferenceByGrade(String gradeId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delReferenceItem (id);
		}
	}
	
	public void delReferenceByChapter(String chapterId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delReferenceItem (id);
		}
	}
	
	public void delReferenceByConcept(String conceptId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delReferenceItem (id);
		}
	}
	
	public void resetReferenceTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.ReferenceTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.ReferenceTable.TABLE_REFERENCE_CREATE);
	}

	public ArrayList<Reference> getReference()
	{
		ArrayList<Reference> refList = new ArrayList<Reference>();

		String selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Reference ref = new Reference();

				ref.id = cursor.getString(0);
				ref.url = cursor.getString(1);
				ref.title = cursor.getString(2);
				ref.description = cursor.getString(3);
				ref.image = cursor.getString(4);
				ref.ownerId = cursor.getString(5);
				ref.gradeId = cursor.getString(6);
				ref.chapterId = cursor.getString(7);
				ref.conceptId = cursor.getString(8);
				ref.shared_user = cursor.getString(9);
				ref.defaulted_user = cursor.getString(10);
				ref.privated_user = cursor.getString(11);
				ref.create_date = cursor.getString(12);
				ref.timestamp = cursor.getString(13);

				refList.add(ref);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return refList;
	}

	public Reference getReference(String id)
	{
		String selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
				+ DbSchema.ReferenceTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Reference ref = new Reference();
		
		if (cursor.moveToFirst()) {
			ref.id = cursor.getString(0);
			ref.url = cursor.getString(1);
			ref.title = cursor.getString(2);
			ref.description = cursor.getString(3);
			ref.image = cursor.getString(4);
			ref.ownerId = cursor.getString(5);
			ref.gradeId = cursor.getString(6);
			ref.chapterId = cursor.getString(7);
			ref.conceptId = cursor.getString(8);
			ref.shared_user = cursor.getString(9);
			ref.defaulted_user = cursor.getString(10);
			ref.privated_user = cursor.getString(11);
			ref.create_date = cursor.getString(12);
			ref.timestamp = cursor.getString(13);
		} else {
			ref = null;
		}

		cursor.close();

		return ref;
	}
	
	public ArrayList<Reference> getReferenceByConceptUser(String conceptId, String userId)
	{
		ArrayList<Reference> refList = new ArrayList<Reference>();
		
		String selectQuery;

		if (conceptId == null || userId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ "( " + DbSchema.ReferenceTable.COLUMN_OWNER + " = 'admin'" + " or "
					+ DbSchema.ReferenceTable.COLUMN_SHARED_USER + " like '%" +userId + "%'" + ")";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Reference ref = new Reference();

				ref.id = cursor.getString(0);
				ref.url = cursor.getString(1);
				ref.title = cursor.getString(2);
				ref.description = cursor.getString(3);
				ref.image = cursor.getString(4);
				ref.ownerId = cursor.getString(5);
				ref.gradeId = cursor.getString(6);
				ref.chapterId = cursor.getString(7);
				ref.conceptId = cursor.getString(8);
				ref.shared_user = cursor.getString(9);
				ref.defaulted_user = cursor.getString(10);
				ref.privated_user = cursor.getString(11);
				ref.create_date = cursor.getString(12);
				ref.timestamp = cursor.getString(13);

				refList.add(ref);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return refList;
	}
	
	public ArrayList<Reference> getReferenceByConceptFriend(String conceptId, String friendId, boolean isPublic)
	{
		ArrayList<Reference> refList = new ArrayList<Reference>();
		
		String selectQuery;

		if (conceptId == null || friendId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.ReferenceTable.COLUMN_SHARED_USER + " like '%" +friendId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Reference ref = new Reference();

				ref.id = cursor.getString(0);
				ref.url = cursor.getString(1);
				ref.title = cursor.getString(2);
				ref.description = cursor.getString(3);
				ref.image = cursor.getString(4);
				ref.ownerId = cursor.getString(5);
				ref.gradeId = cursor.getString(6);
				ref.chapterId = cursor.getString(7);
				ref.conceptId = cursor.getString(8);
				ref.shared_user = cursor.getString(9);
				ref.defaulted_user = cursor.getString(10);
				ref.privated_user = cursor.getString(11);
				ref.create_date = cursor.getString(12);
				ref.timestamp = cursor.getString(13);
				
				if (isPublic ==  true) {
					if (!ref.privated_user.contains(friendId))
						refList.add(ref);
				} else {
					refList.add(ref);
				}
			} while (cursor.moveToNext());
		}

		cursor.close();

		return refList;
	}
	
	public long getRefCheckSum(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(13);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getRefCheckSumByGrade(String gradeId)
	{
		long ret = 0;

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_GRADE+ " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(13);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getRefCheckSumByChapter(String chapterId)
	{
		long ret = 0;

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CHAPTER+ " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(13);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getRefCheckSumByConcept(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT+ " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(13);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public Reference getDefaultReference(String conceptId, String userId)
	{
		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME;
		else if (userId == null)
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'";
		else if (userId.equals("admin"))
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.ReferenceTable.COLUMN_OWNER + " = '" +userId + "'";
		else
			selectQuery = "select * from " + DbSchema.ReferenceTable.TABLE_NAME + " where "
					+ DbSchema.ReferenceTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.ReferenceTable.COLUMN_DEFAULED_USER + " like '%" +userId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Reference ref = new Reference();
		
		if (cursor.moveToFirst()) {
			ref.id = cursor.getString(0);
			ref.url = cursor.getString(1);
			ref.title = cursor.getString(2);
			ref.description = cursor.getString(3);
			ref.image = cursor.getString(4);
			ref.ownerId = cursor.getString(5);
			ref.gradeId = cursor.getString(6);
			ref.chapterId = cursor.getString(7);
			ref.conceptId = cursor.getString(8);
			ref.shared_user = cursor.getString(9);
			ref.defaulted_user = cursor.getString(10);
			ref.privated_user = cursor.getString(11);
			ref.create_date = cursor.getString(12);
			ref.timestamp = cursor.getString(13);
		} else {
			ref = null;
		}
		
		cursor.close();

		return ref;
	}

	/*******************************************************************************
	 * 
	 * 								Note Table
	 * 
	 *******************************************************************************/

	public void addNoteItem(NoteResult note)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.NoteTable.COLUMN_ID, note._id);
		values.put(DbSchema.NoteTable.COLUMN_NOTE, note.note);
		values.put(DbSchema.NoteTable.COLUMN_OWNER, note.owner);
		values.put(DbSchema.NoteTable.COLUMN_GRADE, note.grade);
		values.put(DbSchema.NoteTable.COLUMN_CHAPTER, note.chapter);
		values.put(DbSchema.NoteTable.COLUMN_CONCEPT, note.concept);
		values.put(DbSchema.NoteTable.COLUMN_SHARED_USER, note.shared_user.toString());
		values.put(DbSchema.NoteTable.COLUMN_DEFAULED_USER, note.defaulted_user.toString());
		values.put(DbSchema.NoteTable.COLUMN_PRIVATED_USER, note.privated_user.toString());
		values.put(DbSchema.NoteTable.COLUMN_CREATE_DATE, note.date);
		values.put(DbSchema.NoteTable.COLUMN_TIMESTAMP, note.updated_date);
		
		if (getNote(note._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.NoteTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.NoteTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + note._id};
	        mDb.update(DbSchema.NoteTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateNoteItem(Note note)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.NoteTable.COLUMN_ID, note.id);
		values.put(DbSchema.NoteTable.COLUMN_NOTE, note.text);
		values.put(DbSchema.NoteTable.COLUMN_OWNER, note.ownerId);
		values.put(DbSchema.NoteTable.COLUMN_GRADE, note.gradeId);
		values.put(DbSchema.NoteTable.COLUMN_CHAPTER, note.chapterId);
		values.put(DbSchema.NoteTable.COLUMN_CONCEPT, note.conceptId);
		values.put(DbSchema.NoteTable.COLUMN_SHARED_USER, note.shared_user);
		values.put(DbSchema.NoteTable.COLUMN_DEFAULED_USER, note.defaulted_user);
		values.put(DbSchema.NoteTable.COLUMN_PRIVATED_USER, note.privated_user);
		values.put(DbSchema.NoteTable.COLUMN_CREATE_DATE, note.create_date);
		values.put(DbSchema.NoteTable.COLUMN_TIMESTAMP, note.timestamp);
		
		if (getNote(note.id) != null) {
			String whereClause = DbSchema.NoteTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + note.id};
	        mDb.update(DbSchema.NoteTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delNoteItem(String id)
	{
		String whereClause = DbSchema.NoteTable.COLUMN_ID + " = ?";
        String[] whereArgs = {"" + id};
        mDb.delete(DbSchema.NoteTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	public void delNoteByGrade(String gradeId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delNoteItem (id);
		}
	}
	
	public void delNoteByChapter(String chapterId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delNoteItem (id);
		}
	}
	
	public void delNoteByConcept(String conceptId)
	{
		ArrayList<String> itemList = new ArrayList<String>();

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String id;

				id = cursor.getString(0);

				itemList.add(id);
			} while (cursor.moveToNext());
		}

		cursor.close();

		for (String id : itemList)
		{
			delNoteItem (id);
		}
	}
	
	public void resetNoteTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.NoteTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.NoteTable.TABLE_NOTE_CREATE);
	}

	public ArrayList<Note> getNote()
	{
		ArrayList<Note> noteList = new ArrayList<Note>();

		String selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note();

				note.id = cursor.getString(0);
				note.text = cursor.getString(1);
				note.ownerId = cursor.getString(2);
				note.gradeId = cursor.getString(3);
				note.chapterId = cursor.getString(4);
				note.conceptId = cursor.getString(5);
				note.shared_user = cursor.getString(6);
				note.defaulted_user = cursor.getString(7);
				note.privated_user = cursor.getString(8);
				note.create_date = cursor.getString(9);
				note.timestamp = cursor.getString(10);

				noteList.add(note);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return noteList;
	}
	
	public Note getNote(String id)
	{
		String selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
				+ DbSchema.NoteTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Note note = new Note();
		
		if (cursor.moveToFirst()) {
			note.id = cursor.getString(0);
			note.text = cursor.getString(1);
			note.ownerId = cursor.getString(2);
			note.gradeId = cursor.getString(3);
			note.chapterId = cursor.getString(4);
			note.conceptId = cursor.getString(5);
			note.shared_user = cursor.getString(6);
			note.defaulted_user = cursor.getString(7);
			note.privated_user = cursor.getString(8);
			note.create_date = cursor.getString(9);
			note.timestamp = cursor.getString(10);
		} else {
			note = null;
		}

		cursor.close();

		return note;
	}
	
	public ArrayList<Note> getNoteByConceptUser(String conceptId, String userId, ArrayList<UserInfo> classmates)
	{
		ArrayList<Note> noteList = new ArrayList<Note>();
		
		String selectQuery;
		String subQuery = "";
		
		if (classmates != null) {
			for (UserInfo classmate: classmates) {
				subQuery += " or " + DbSchema.NoteTable.COLUMN_SHARED_USER + " like '%" + classmate._id + "%'";
			}
		}

		if (conceptId == null || userId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ "( " + DbSchema.NoteTable.COLUMN_OWNER + " = 'admin' or "
					+ DbSchema.NoteTable.COLUMN_SHARED_USER + " like '%" + userId + "%'"
					+ subQuery + ")"
					+ " ORDER BY " + DbSchema.NoteTable.COLUMN_CREATE_DATE + " DESC";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note();

				note.id = cursor.getString(0);
				note.text = cursor.getString(1);
				note.ownerId = cursor.getString(2);
				note.gradeId = cursor.getString(3);
				note.chapterId = cursor.getString(4);
				note.conceptId = cursor.getString(5);
				note.shared_user = cursor.getString(6);
				note.defaulted_user = cursor.getString(7);
				note.privated_user = cursor.getString(8);
				note.create_date = cursor.getString(9);
				note.timestamp = cursor.getString(10);

				noteList.add(note);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return noteList;
	}
	
	public ArrayList<Note> getNoteByConceptFriend(String conceptId, String friendId, boolean isPublic)
	{
		ArrayList<Note> noteList = new ArrayList<Note>();
		
		String selectQuery;

		if (conceptId == null || friendId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.NoteTable.COLUMN_SHARED_USER + " like '%" +friendId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note();

				note.id = cursor.getString(0);
				note.text = cursor.getString(1);
				note.ownerId = cursor.getString(2);
				note.gradeId = cursor.getString(3);
				note.chapterId = cursor.getString(4);
				note.conceptId = cursor.getString(5);
				note.shared_user = cursor.getString(6);
				note.defaulted_user = cursor.getString(7);
				note.privated_user = cursor.getString(8);
				note.create_date = cursor.getString(9);
				note.timestamp = cursor.getString(10);
				
				if (isPublic == true) {
					if (!note.privated_user.contains(friendId))
						noteList.add(note);
				} else {
					noteList.add(note);
				}
			} while (cursor.moveToNext());
		}

		cursor.close();

		return noteList;
	}
	
	public long getNoteCheckSum(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getNoteCheckSumByGrade(String gradeId)
	{
		long ret = 0;

		String selectQuery;
		
		if (gradeId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_GRADE + " = '" + gradeId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getNoteCheckSumByChapter(String chapterId)
	{
		long ret = 0;

		String selectQuery;
		
		if (chapterId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CHAPTER + " = '" + chapterId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public long getNoteCheckSumByConcept(String conceptId)
	{
		long ret = 0;

		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				String timestamp = cursor.getString(10);

				ret += Utility.getValueFromTimestamp(timestamp);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return ret;
	}
	
	public Note getDefaultNote(String conceptId, String userId)
	{
		String selectQuery;
		
		if (conceptId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME;
		else if (userId == null)
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'";
		else if (userId.equals("admin"))
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.NoteTable.COLUMN_OWNER + " = '" +userId + "'";
		else
			selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
					+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
					+ DbSchema.NoteTable.COLUMN_DEFAULED_USER + " like '%" +userId + "%'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			
			if (conceptId != null && userId != null) {
				selectQuery = "select * from " + DbSchema.NoteTable.TABLE_NAME + " where "
						+ DbSchema.NoteTable.COLUMN_CONCEPT + " = '" + conceptId + "'" + " and "
						+ DbSchema.NoteTable.COLUMN_OWNER + " = '" +userId + "'";
				
				cursor = mDb.rawQuery(selectQuery, null);
			}
			
			if (cursor.getCount() < 1) {
				cursor.close();
				return null;
			}
		}
		
		Note note = new Note();
		
		if (cursor.moveToLast()) {
			note.id = cursor.getString(0);
			note.text = cursor.getString(1);
			note.ownerId = cursor.getString(2);
			note.gradeId = cursor.getString(3);
			note.chapterId = cursor.getString(4);
			note.conceptId = cursor.getString(5);
			note.shared_user = cursor.getString(6);
			note.defaulted_user = cursor.getString(7);
			note.privated_user = cursor.getString(8);
			note.create_date = cursor.getString(9);
			note.timestamp = cursor.getString(10);
		} else {
			note = null;
		}
		
		cursor.close();

		return note;
	}
	
	/*******************************************************************************
	 * 
	 * 								Update Table
	 * 
	 *******************************************************************************/

	public void addUpdateItem(UpdateResult update)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.UpdateTable.COLUMN_ID, update._id);
		values.put(DbSchema.UpdateTable.COLUMN_TYPE, update.type);
		values.put(DbSchema.UpdateTable.COLUMN_CONTENT, update.content);
		values.put(DbSchema.UpdateTable.COLUMN_CONTENT_ID, update.content_id);
		values.put(DbSchema.UpdateTable.COLUMN_TEXT, update.text);
		values.put(DbSchema.UpdateTable.COLUMN_OWNER, update.owner);
		values.put(DbSchema.UpdateTable.COLUMN_ALLOWED_USERS, update.allowed_users.toString());
		values.put(DbSchema.UpdateTable.COLUMN_UNREAD_USERS, update.unread_users.toString());
		values.put(DbSchema.UpdateTable.COLUMN_CREATE_DATE, update.date);
		
		if (getNote(update._id) == null) {
			// Inserting Row
			long rowId = mDb.insert(DbSchema.UpdateTable.TABLE_NAME, null, values);
		} else {
			String whereClause = DbSchema.UpdateTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + update._id};
	        mDb.update(DbSchema.UpdateTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void updateNoteItem(Update update)
	{
		ContentValues values = new ContentValues();

		values.put(DbSchema.UpdateTable.COLUMN_ID, update._id);
		values.put(DbSchema.UpdateTable.COLUMN_TYPE, update.type);
		values.put(DbSchema.UpdateTable.COLUMN_CONTENT, update.content);
		values.put(DbSchema.UpdateTable.COLUMN_CONTENT_ID, update.content_id);
		values.put(DbSchema.UpdateTable.COLUMN_TEXT, update.text);
		values.put(DbSchema.UpdateTable.COLUMN_OWNER, update.owner);
		values.put(DbSchema.UpdateTable.COLUMN_ALLOWED_USERS, update.allowed_users);
		values.put(DbSchema.UpdateTable.COLUMN_UNREAD_USERS, update.unread_users);
		values.put(DbSchema.UpdateTable.COLUMN_CREATE_DATE, update.create_date);
		
		if (getNote(update._id) != null) {
			String whereClause = DbSchema.UpdateTable.COLUMN_ID + " = ?";
	        String[] whereArgs = {"" + update._id};
	        mDb.update(DbSchema.UpdateTable.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void delUpdateItem()
	{}
	
	public void resetUpdateTable()
	{
		mDb.execSQL("drop table if exists " + DbSchema.UpdateTable.TABLE_NAME);
		
		mDb.execSQL(DbSchema.UpdateTable.TABLE_UPDATE_CREATE);
	}

	public ArrayList<Update> getUpdate()
	{
		ArrayList<Update> updateList = new ArrayList<Update>();

		String selectQuery = "select * from " + DbSchema.UpdateTable.TABLE_NAME;

		Cursor cursor = mDb.rawQuery(selectQuery, null);		
		if (cursor.moveToFirst()) {
			do {
				Update update = new Update();

				update._id = cursor.getString(0);
				update.type = cursor.getString(1);
				update.content = cursor.getString(2);
				update.content_id = cursor.getString(3);
				update.text = cursor.getString(4);
				update.owner = cursor.getString(5);
				update.allowed_users = cursor.getString(6);
				update.unread_users = cursor.getString(7);
				update.create_date = cursor.getString(8);

				updateList.add(update);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return updateList;
	}
	
	public Update getUpdate(String id)
	{
		String selectQuery = "select * from " + DbSchema.UpdateTable.TABLE_NAME + " where "
				+ DbSchema.UpdateTable.COLUMN_ID + " = '" + id + "'";

		Cursor cursor = mDb.rawQuery(selectQuery, null);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		}
		
		Update update = new Update();
		
		if (cursor.moveToFirst()) {
			update._id = cursor.getString(0);
			update.type = cursor.getString(1);
			update.content = cursor.getString(2);
			update.content_id = cursor.getString(3);
			update.text = cursor.getString(4);
			update.owner = cursor.getString(5);
			update.allowed_users = cursor.getString(6);
			update.unread_users = cursor.getString(7);
			update.create_date = cursor.getString(8);
		} else {
			update = null;
		}

		cursor.close();

		return update;
	}
	
	private class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context, String dbName, CursorFactory factory,
				int version) {
			super(context, dbName, null, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

			db.execSQL(DbSchema.UserTable.TABLE_USER_CREATE);

			db.execSQL(DbSchema.LinkTable.TABLE_LINK_CREATE);
			db.execSQL(DbSchema.SyllabusTable.TABLE_SYLLABUS_CREATE);
			db.execSQL(DbSchema.GradeTable.TABLE_GRADE_CREATE);
			db.execSQL(DbSchema.ChapterTable.TABLE_CHAPTER_CREATE);
			db.execSQL(DbSchema.ConceptTable.TABLE_CONCEPT_CREATE);

			db.execSQL(DbSchema.VideoTable.TABLE_VIDEO_CREATE);
			db.execSQL(DbSchema.ReferenceTable.TABLE_REFERENCE_CREATE);
			db.execSQL(DbSchema.NoteTable.TABLE_NOTE_CREATE);
			db.execSQL(DbSchema.UpdateTable.TABLE_UPDATE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

			db.execSQL("drop table if exists " + DbSchema.UserTable.TABLE_NAME);

			db.execSQL("drop table if exists " + DbSchema.LinkTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.SyllabusTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.GradeTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.ChapterTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.ConceptTable.TABLE_NAME);

			db.execSQL("drop table if exists " + DbSchema.VideoTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.ReferenceTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.NoteTable.TABLE_NAME);
			db.execSQL("drop table if exists " + DbSchema.UpdateTable.TABLE_NAME);

			onCreate(db);
		}

	}
}