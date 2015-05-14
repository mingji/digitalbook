package com.belwadi.sciencefun.entity;


public class YoutubeItemInfo {
	
	public YoutubeItemInfo() {
		super();
	}
	
	public class YbId {
		public String kind;
		public String videoId;
		
		public YbId() {
			super();
		}
	}
	
	public class YbSnippet {
		public String publishedAt;
		public String title;
		public String description;
		public String channelTitle;
		public String categoryId;		
		
		public YbSnippet() {
			super();
		}
	}
	
	public class YbContentDetails {
		public String duration;
		public String dimension;
		public String definition;
		
		public YbContentDetails() {
			super();
		}
	}
	
	public class YbStatistics {
		public String viewCount;
		public String likeCount;
		public String dislikeCount;
		public String favoriteCount;
		public String commentCount;		
		
		public YbStatistics() {
			super();
		}
	}
}

