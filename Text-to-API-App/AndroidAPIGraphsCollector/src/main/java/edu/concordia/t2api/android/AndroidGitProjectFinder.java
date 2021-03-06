package edu.concordia.t2api.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.bson.Document;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.mongodb.client.MongoCollection;

import edu.concordia.t2api.android.db.MongoManager;

public class AndroidGitProjectFinder {

	public static final int SLEEP_TIME = 3000;
	public static final int NUM_PROJECTS = 1000;
	public static final int PAGE_SIZE = 100;

	/**
	 * 
	 * @param args[0] - github username
	 * @param args[1] - password
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(AndroidPackageNames.androidPackageNames);
		int numIterations = NUM_PROJECTS / PAGE_SIZE;

		Github github = new RtGithub(args[0], args[1]);

		for (int i = 0; i < numIterations; i++) {

			Request request = github.entry().uri().path("/search/repositories").queryParam("q", "android language:Java")
					.queryParam("sort", "stars").queryParam("order", "desc").queryParam("per_page", "" + PAGE_SIZE)
					.queryParam("page", "" + (i + 1)).back().method(Request.GET);

			JsonArray items = request.fetch().as(JsonResponse.class).json().readObject().getJsonArray("items");

			MongoCollection<Document> projectCollection = MongoManager.getDatabase()
					.getCollection(MongoManager.COLLECTION_PROJECTS);

			List<Document> allProjects = new ArrayList<Document>();

			for (JsonValue item : items) {
				JsonObject repoData = (JsonObject) item;
				System.out.print("name " + repoData.getString("name"));
				System.out.println("\tclone_url " + repoData.getString("clone_url"));

				Document doc = Document.parse(repoData.toString());
				allProjects.add(doc.append("analyzed", false));
			}

			projectCollection.insertMany(allProjects);
			Thread.sleep(3000);
		}
	}

}
