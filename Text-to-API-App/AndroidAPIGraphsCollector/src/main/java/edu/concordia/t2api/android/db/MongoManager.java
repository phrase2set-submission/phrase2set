package edu.concordia.t2api.android.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public final class MongoManager {
	// public static final String DBUSER = "t2api";
	// public static final String DBPASSWORD = "t2apiandroid";
	// public static final String URI = "mongodb://" + DBUSER + ":" + DBPASSWORD
	// + "@ds127399.mlab.com:27399/heroku_8jb2rtm8";

	public static final String DATABASE = "t2api";
	public static final String COLLECTION_PROJECTS = "android_projects";

	private static MongoClient mc = new MongoClient("localhost");
	private static MongoDatabase md = mc.getDatabase(DATABASE);

	public static MongoDatabase getDatabase() {
		return md;
	}
}
