/**
 * 
 */
package miner;

import java.util.ArrayList;
import java.util.TreeMap;

import config.DatabaseConfig;
import config.GlobalConfig;
import storage.GraphDatabase;
import utils.FileUtils;
import utils.GraphComparisonData;
import utils.GraphComparisonUtils;
import utils.Logger;
import utils.StringGraphComparisonData;
import data.SampleAnswerData;
import data.TestData;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;
import datastructure.PostInfoData;
import evaluation.GraphQueryEvaluation;

/**
 * @author anhnt
 *
 */
public class TestDataExecutor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		doPruneDatabase(DatabaseConfig.graphDatabasePath, DatabaseConfig.graphDatabasePathPrune);
//		doPruneDatabase( DatabaseConfig.graphDatabasePath500, DatabaseConfig.graphDatabasePath500Prune );
//		doBuildParentChild(DatabaseConfig.graphDatabasePath500Prune, DatabaseConfig.graphDatabasePath500ParentChild);
//		doMain(DatabaseConfig.graphDatabasePath500ParentChild, true);

//		doBuildParentChild(DatabaseConfig.graphDatabasePathPrune, DatabaseConfig.graphDatabasePathParentChild);
		
//		doMain(DatabaseConfig.graphDatabasePathNew, true);
		doMain(DatabaseConfig.graphDatabasePathParentChild, true);

	}
	
//	public static void doMergeDatabaseSimple(String graphDatabasePath1, String graphDatabasePath2, String graphDatabaseMergePath){
//		Logger.log("Loading database 1");
//		GraphDatabase graphDB1 = GraphDatabase.readGraphDatabase(graphDatabasePath1);
//		Logger.log("Loading database 2");
//		GraphDatabase graphDB2 = GraphDatabase.readGraphDatabase(graphDatabasePath2);
//		
//
//		Logger.log("Storing database");
//		graphDB1.storeThisDatabase(graphDatabaseMergePath);
//
//	}
	
	
	public static void doPruneDatabase(String databasePath, String databasePrunedPath){
		Logger.log("Loading database");
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(databasePath);
		Logger.log("Pruning");
		graphDB.prune();
		Logger.log("Storing database");
		graphDB.storeThisDatabase(databasePrunedPath);
		
	}
	
	public static void doBuildParentChild(String databasePath, String databasePathParentChild){
		Logger.log("Loading database");
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(databasePath);		
		graphDB.doStatistics();
		graphDB.buildAllChildSimple();
		graphDB.storeThisDatabase(databasePathParentChild);
	}
	
	public static void doMain(String databasePath, boolean isBuildElementList){
		Logger.log("Loading test data");
		TestData testData = (TestData) FileUtils.readObjectFile(GlobalConfig.postTestDataPath);
		Logger.log("Loading database");
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(databasePath);
//		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(DatabaseConfig.graphDatabasePathOld);
		
		
		Node.globalData = graphDB.globalData;
		ArrayList<PostInfoData> posts = new ArrayList<PostInfoData>();
		for (SampleAnswerData sample:testData.sampleData){
			long id = sample.answerId;
//			if (id==1045909)
			{
				ArrayList<String> snippets = new ArrayList<String>();
				snippets.add(sample.code);
//				Logger.log("code: " + snippets.get(0));
				PostInfoData post = new PostInfoData(id, snippets);
				posts.add(post);
			}
		}
//		for (PostInfoData post:posts){
//			Logger.log(post);
//		}
		
//		CLTDataProcessing.proceedAll(posts, graphDB.globalData);
//		GlobalData globalData = new GlobalData(); 
		TreeMap<Long, ArrayList<Graph>> postGraphsMap = CLTDataProcessing.proceedAll(posts, graphDB, graphDB.globalData, isBuildElementList);
		
//		Logger.log("postGraphsMap: " + postGraphsMap);
		
		GraphQueryEvaluation gqe = new GraphQueryEvaluation();
		TreeMap<Long, Graph> idxGraphsMap = gqe.createGraphs(GlobalConfig.bigQueryListPath,graphDB);
		 
		 //TODO: should check label
		 GraphComparisonData graphCompData = new GraphComparisonData();
		 StringGraphComparisonData strGraphCompData = new StringGraphComparisonData();
//		 Logger.log("postGraphsMap: " + postGraphsMap);

		 for (Long idx:postGraphsMap.keySet()){
			
			 
			 Graph actualGraph = postGraphsMap.get(idx).get(0);
			 Graph synGraph = idxGraphsMap.get(idx);
			 if (synGraph==null||actualGraph==null)
				 continue;
			 
//			 Logger.log("actualGraph: "+ actualGraph);
//			 Logger.log("synGraph: "+ synGraph);

			 GraphComparisonUtils.compareGraphWithshape(actualGraph, synGraph, graphCompData, idx);
			 GraphComparisonUtils.compareGraphWithStrSeq(actualGraph, synGraph, strGraphCompData, graphDB.globalData);

			
		 }
		 
		 Logger.log(System.lineSeparator() + System.lineSeparator() + "Statistics over graph comparison");
		 graphCompData.doStatistics();
		 
		 Logger.log(System.lineSeparator() + System.lineSeparator() + "Statistics over graph sequence comparison");
		 strGraphCompData.doStatistics();
	}
	
	

}
