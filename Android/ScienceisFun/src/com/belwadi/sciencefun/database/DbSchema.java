package com.belwadi.sciencefun.database;


public class DbSchema {
	public static class UserTable {
        public static String TABLE_NAME = "user_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_MAIL = "mail";
        public static String COLUMN_NAME = "name";
        public static String COLUMN_SCHOOL = "school";
        public static String COLUMN_ADDRESS = "address";
        public static String COLUMN_CITY = "city";
        public static String COLUMN_POSTAL_CODE = "postal_code";
        public static String COLUMN_COUNTRY = "country";
        public static String COLUMN_SYLLABUS = "syllabus";
        public static String COLUMN_GRADE = "grade";
        public static String COLUMN_SECTION = "section";
        public static String COLUMN_PHOTO = "photo_url";
        public static String COLUMN_UPDATE_USER = "update_user";
        public static String COLUMN_FRIENDS = "friends";
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_USER_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_MAIL + " TEXT NOT NULL, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_SCHOOL + " TEXT NOT NULL, " +
                COLUMN_ADDRESS + " TEXT NOT NULL, " +
                COLUMN_CITY + " TEXT NOT NULL, " +
                COLUMN_POSTAL_CODE + " TEXT NOT NULL, " +
                COLUMN_COUNTRY + " TEXT NOT NULL, " +
                COLUMN_SYLLABUS + " TEXT, " +
                COLUMN_GRADE + " TEXT NOT NULL, " +
                COLUMN_SECTION + " TEXT NOT NULL, " +
                COLUMN_PHOTO + " TEXT NOT NULL, " +
                COLUMN_UPDATE_USER + " TEXT NOT NULL, " +
                COLUMN_FRIENDS + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class LinkTable {
        public static String TABLE_NAME = "link_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_KIND = "kind"; // type of link
        public static String COLUMN_SYLLABUS = "syllabus"; // syllabus id
        public static String COLUMN_GRADE = "grade"; // grade id
        public static String COLUMN_CHAPTER = "chapter"; // chapter id
        public static String COLUMN_CONCEPT = "concept"; // concept id
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_LINK_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_KIND + " TEXT NOT NULL, " +
                COLUMN_SYLLABUS + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT NOT NULL, " +
                COLUMN_CHAPTER + " , " +
                COLUMN_CONCEPT + " , " +
                COLUMN_CREATE_DATE + " , " +
                COLUMN_TIMESTAMP + " ) ";
    }
	
	public static class SyllabusTable {
        public static String TABLE_NAME = "syllabus_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_ENABLE = "enable"; // check enabled for guest. 1 : enable, 0 : disable
        public static String COLUMN_TITLE = "title"; // Syllabus title
        public static String COLUMN_DESCRIPTION = "description"; // Syllabus title
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_SYLLABUS_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_ENABLE + " INTEGER DEFAULT 0, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class GradeTable {
        public static String TABLE_NAME = "grade_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_GRADE = "grade"; // Grade number
        public static String COLUMN_ENABLE = "enable"; // check enabled for guest. 1 : enable, 0 : disable
        public static String COLUMN_IMAGE = "image_url"; // Grade image URL
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_GRADE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_GRADE + " TEXT NOT NULL, " +
                COLUMN_ENABLE + " INTEGER DEFAULT 0, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class ChapterTable {
        public static String TABLE_NAME = "chapter_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_TITLE = "title"; // Chapter title
        public static String COLUMN_SYLLABUS = "syllabus_id"; // Syllabus id of the chapter
        public static String COLUMN_GRADE = "grade_id"; // Grade id of the chapter
        public static String COLUMN_ENABLE = "enable"; // check enabled for guest. 1 : enable, 0 : disable
        public static String COLUMN_DSCP = "description"; // Chapter description
        public static String COLUMN_IMAGE = "image_url"; // Chapter image URL
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_CHAPTER_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_SYLLABUS + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT NOT NULL, " +
                COLUMN_ENABLE + " INTEGER DEFAULT 0, " +
                COLUMN_DSCP + " TEXT NOT NULL, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class ConceptTable {
        public static String TABLE_NAME = "concept_table";
        public static String COLUMN_ID = "_id"; // Index
        public static String COLUMN_TITLE = "title"; // Concept title
        public static String COLUMN_SYLLABUS = "syllabus_id"; // Syllabus id of the concept
        public static String COLUMN_GRADE = "grade_id"; // Grade id of the concept
        public static String COLUMN_CHAPTER = "chapter_id"; // Chapter id of the concept
        public static String COLUMN_ENABLE = "enable"; // check enabled for guest. 1 : enable, 0 : disable
        public static String COLUMN_TEXT = "text"; // Concept text
        public static String COLUMN_IMAGE = "image_url"; // Concept image URL
        public static String COLUMN_IMAGE_CREDIT = "image_credit";
        public static String COLUMN_IMAGE_SRC = "image_source";
        public static String COLUMN_IMAGE2 = "image2_url"; // Concept image URL
        public static String COLUMN_IMAGE2_CREDIT = "image2_credit";
        public static String COLUMN_IMAGE2_SRC = "image2_source";
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_CONCEPT_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_SYLLABUS + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT NOT NULL, " +
                COLUMN_CHAPTER + " TEXT NOT NULL, " +
                COLUMN_ENABLE + " INTEGER DEFAULT 0, " +
                COLUMN_TEXT + " TEXT NOT NULL, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_IMAGE_CREDIT + " TEXT NOT NULL, " +
                COLUMN_IMAGE_SRC + " TEXT NOT NULL, " +
                COLUMN_IMAGE2 + " TEXT NOT NULL, " +
                COLUMN_IMAGE2_CREDIT + " TEXT NOT NULL, " +
                COLUMN_IMAGE2_SRC + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class VideoTable {
        public static String TABLE_NAME = "video_table";
        public static String COLUMN_ID = "_id";
        public static String COLUMN_URL = "url";
        public static String COLUMN_OWNER = "owner";
        public static String COLUMN_GRADE = "grade_id";
        public static String COLUMN_CHAPTER = "chapter_id";
        public static String COLUMN_CONCEPT = "concept_id";
        public static String COLUMN_SHARED_USER = "shared_user";
        public static String COLUMN_DEFAULED_USER = "defaulted_user";
        public static String COLUMN_PRIVATED_USER = "privated_user";
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_VIDEO_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_URL + " TEXT NOT NULL, " +
                COLUMN_OWNER + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT, " +
                COLUMN_CHAPTER + " TEXT, " +
                COLUMN_CONCEPT + " TEXT NOT NULL, " +
                COLUMN_SHARED_USER + " TEXT NOT NULL, " +
                COLUMN_DEFAULED_USER + " TEXT NOT NULL, " +
                COLUMN_PRIVATED_USER + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class ReferenceTable {
        public static String TABLE_NAME = "reference_table";
        public static String COLUMN_ID = "_id";
        public static String COLUMN_URL = "url";
        public static String COLUMN_TITLE = "title";
        public static String COLUMN_DSCP = "description";
        public static String COLUMN_IMAGE = "image";
        public static String COLUMN_OWNER = "owner";
        public static String COLUMN_GRADE = "grade_id";
        public static String COLUMN_CHAPTER = "chapter_id";
        public static String COLUMN_CONCEPT = "concept_id";
        public static String COLUMN_SHARED_USER = "shared_user";
        public static String COLUMN_DEFAULED_USER = "defaulted_user";
        public static String COLUMN_PRIVATED_USER = "privated_user";
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_REFERENCE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_URL + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DSCP + " TEXT NOT NULL, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_OWNER + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT, " +
                COLUMN_CHAPTER + " TEXT, " +
                COLUMN_CONCEPT + " TEXT NOT NULL, " +
                COLUMN_SHARED_USER + " TEXT NOT NULL, " +
                COLUMN_DEFAULED_USER + " TEXT NOT NULL, " +
                COLUMN_PRIVATED_USER + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class NoteTable {
        public static String TABLE_NAME = "note_table";
        public static String COLUMN_ID = "_id";
        public static String COLUMN_NOTE = "note";
        public static String COLUMN_OWNER = "owner";
        public static String COLUMN_GRADE = "grade_id";
        public static String COLUMN_CHAPTER = "chapter_id";
        public static String COLUMN_CONCEPT = "concept_id";
        public static String COLUMN_SHARED_USER = "shared_user";
        public static String COLUMN_DEFAULED_USER = "defaulted_user";
        public static String COLUMN_PRIVATED_USER = "privated_user";
        public static String COLUMN_CREATE_DATE = "create_date";
        public static String COLUMN_TIMESTAMP = "timestamp";

        public static String TABLE_NOTE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_NOTE + " TEXT NOT NULL, " +
                COLUMN_OWNER + " TEXT NOT NULL, " +
                COLUMN_GRADE + " TEXT, " +
                COLUMN_CHAPTER + " TEXT, " +
                COLUMN_CONCEPT + " TEXT NOT NULL, " +
                COLUMN_SHARED_USER + " TEXT NOT NULL, " +
                COLUMN_DEFAULED_USER + " TEXT NOT NULL, " +
                COLUMN_PRIVATED_USER + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT NOT NULL) ";
    }
	
	public static class UpdateTable {
        public static String TABLE_NAME = "update_table";
        public static String COLUMN_ID = "_id";
        public static String COLUMN_TYPE = "type";
        public static String COLUMN_CONTENT = "content";
        public static String COLUMN_CONTENT_ID = "content_id";
        public static String COLUMN_TEXT = "text";
        public static String COLUMN_OWNER = "owner";
        public static String COLUMN_ALLOWED_USERS = "allowed_users";
        public static String COLUMN_UNREAD_USERS = "unread_users";
        public static String COLUMN_CREATE_DATE = "create_date";

        public static String TABLE_UPDATE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                COLUMN_CONTENT + " TEXT NOT NULL, " +
                COLUMN_CONTENT_ID + " TEXT NOT NULL, " +
                COLUMN_TEXT + " TEXT NOT NULL, " +
                COLUMN_OWNER + " TEXT NOT NULL, " +
                COLUMN_ALLOWED_USERS + " TEXT NOT NULL, " +
                COLUMN_UNREAD_USERS + " TEXT NOT NULL, " +
                COLUMN_CREATE_DATE + " TEXT NOT NULL) ";
    }
}