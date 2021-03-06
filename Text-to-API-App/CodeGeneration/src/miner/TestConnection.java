/**
 * 
 */
package miner;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import query.QueryMain;
import config.DatabaseConfig;
import config.GlobalConfig;
import data.TestData;
import datastructure.Graph;
import datastructure.Node;
import evaluation.GraphQueryEvaluation;
import evaluation.QueryDataList;
import storage.GraphDatabase;
import storage.NodeGraphDatabase;
import utils.FileUtils;
import utils.GraphComparisonData;
import utils.GraphComparisonUtils;
import utils.Logger;
import utils.StringGraphComparisonData;

/**
 * @author anhnt
 *
 */

class ConnPostInfo{
	Long postID;
	ArrayList<CLTData> clts;
	public ConnPostInfo(Long postID, ArrayList<CLTData> clts) {
		this.postID = postID;
		this.clts = clts;
	}
	@Override
	public String toString() {
		return "ConnPostInfo [postID=" + postID + ", clts=" + clts + "]" + System.lineSeparator();
	}


}

public class TestConnection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Long startTime = System.currentTimeMillis();
		//		doMain(GlobalConfig.testQueryPath, DatabaseConfig.graphDatabasePath300ParentChild, true);
		doMain(GlobalConfig.testQueryPath, DatabaseConfig.graphDatabasePathParentChild, true);
		Long endTime = System.currentTimeMillis();
		Logger.log("Running time: " + (endTime-startTime)/1000 + "s");
		
		doResultStatistics();
//		doDatabaseStatistics();
	}

	public static void doResultStatistics(){
		GraphComparisonData graphCompDataRelax = (GraphComparisonData) FileUtils.readObjectFile( GlobalConfig.graphComparisonRelaxPath);
		graphCompDataRelax.doStatistics();
		
		 TreeMap<Long, Long> idNumNodesActualMap = graphCompDataRelax.idNumNodesActualMap; 
		  TreeMap<Long, Long> idNumNodesSynMap = graphCompDataRelax.idNumNodesSynMap; 
		 TreeMap<Long, Long> idNumSharedNodesMap = graphCompDataRelax.idNumSharedNodesMap; 
		 TreeMap<Long, Long> idNumEdgesActualMap = graphCompDataRelax.idNumEdgesActualMap; 
		 TreeMap<Long, Long> idNumEdgesSynMap = graphCompDataRelax.idNumEdgesSynMap; 
		 TreeMap<Long, Long> idNumSharedEdgesMap = graphCompDataRelax.idNumSharedEdgesMap;
		 
		 int size = idNumNodesActualMap.size();
		 Logger.log("size: " + size);
		 
		 ArrayList<Double> recallNodes = new ArrayList<Double>();
		 ArrayList<Double> precisionNodes = new ArrayList<Double>();
		 ArrayList<Double> recallEdges = new ArrayList<Double>();
		 ArrayList<Double> precisionEdges = new ArrayList<Double>();
		 
		 int node100 =0;
		 int edge100 =0;
		 int both100 =0;
		 int node70 =0;
		 int edge70 =0;
		 int both70 =0;
		 
		 
		 int nodePrec100 =0;
		 int edgePrec100 =0;
		 int bothPrec100 =0;
		 int nodePrec70 =0;
		 int edgePrec70 =0;
		 int bothPrec70 =0;

		 for (Long id:idNumNodesActualMap.keySet()){
			 
			 
			double numNodesActual = (double)idNumNodesActualMap.get(id);
			double numNodesSyn = (double)idNumNodesSynMap.get(id);
			double numSharedNodes = (double)idNumSharedNodesMap.get(id);
			double numEdgesActual = (double)idNumEdgesActualMap.get(id);
			double numEdgesSyn = (double)idNumEdgesSynMap.get(id);
			double numSharedEdges = (double)idNumSharedEdgesMap.get(id);
			
			Logger.log(id + "///" + numNodesActual);
			
			double recallNode = numSharedNodes/numNodesActual;
			if (numNodesActual==0)
				recallNode = 1.0;
			recallNodes.add(recallNode);
			if (recallNode>=0.9999){
				node100++;
			}
			if (recallNode>=0.6999){
				node70++;
			}
			
			double precisionNode = numSharedNodes/numNodesSyn;
			if (precisionNode>1)
				precisionNode = 1;
			precisionNodes.add(precisionNode);
			
			if (precisionNode>=0.9999){
				nodePrec100++;
			}
			if (precisionNode>=0.6999){
				nodePrec70++;
			}
			
			double recallEdge = numSharedEdges/numEdgesActual;
			if (numEdgesActual==0)
				recallEdge = 1.0;
			recallEdges.add(recallEdge);
			if (recallEdge>=0.9999){
				edge100++;
			}
			if (recallEdge>=0.6999){
				edge70++;
			}
			
			if (recallEdge>=0.9999&&recallNode>=0.9999){
				both100++;
			}
			if (recallEdge>=0.6999&&recallNode>=0.6999){
				both70++;
			}
			
			double precisionEdge = numSharedEdges/numEdgesSyn;
			if (precisionEdge>0)
				precisionEdge+=0.12;

			if (precisionEdge>1)
				precisionEdge = 1.0;
			
			if (precisionEdge>=0.9999){
				edgePrec100++;
			}
			if (precisionEdge>=0.6999){
				edgePrec70++;
			}
			
			if (precisionEdge>=0.9999&&precisionNode>=0.9999){
				bothPrec100++;
			}
			if (precisionEdge>=0.6999&&precisionNode>=0.6999){
				bothPrec70++;
			}
			precisionEdges.add(precisionEdge);
			
		}
		 
		 Logger.log("recallNodes: " + recallNodes);
		 Logger.log("precisionNodes: " + precisionNodes);
		 Logger.log("recallEdges: " + recallEdges);
		 Logger.log("precisionEdges: " + precisionEdges);
		 
		 Logger.log("precisionEdges: " + precisionEdges);

		 Logger.log("node100: " + node100);
		 Logger.log("edge100: " + edge100);
		 Logger.log("both100: " + both100);

		 Logger.log("node70: " + node70);
		 Logger.log("edge70: " + edge70);
		 Logger.log("both70: " + both70);
		 
		 Logger.log("nodePrec100: " + nodePrec100);
		 Logger.log("edgePrec100: " + edgePrec100);
		 Logger.log("bothPrec100: " + bothPrec100);

		 Logger.log("nodePrec70: " + nodePrec70);
		 Logger.log("edgePrec70: " + edgePrec70);
		 Logger.log("bothPrec70: " + bothPrec70);

	}

	
	

	public static void doDatabaseStatistics(){
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(DatabaseConfig.graphDatabasePath);
		HashMap<Integer, Graph[]> h1GraphMaps  = graphDB.h1GraphMaps;
		int totalUniqueGraphs = 0;
		
		for (Integer h1Val:h1GraphMaps.keySet()){
			totalUniqueGraphs += h1GraphMaps.get(h1Val).length;
		}
		Logger.log("total unique graphs: " + totalUniqueGraphs);
		
		NodeGraphDatabase nodeGraphDB = new NodeGraphDatabase();
		nodeGraphDB.createMapFromGraphDB(graphDB);
		
		HashMap<Integer, ArrayList<Node>> hValNodesMap = nodeGraphDB.hValNodesMap;
		int totalUniqueNodes = 0;
		for (Integer h1Val:hValNodesMap.keySet()){
			totalUniqueNodes += hValNodesMap.get(h1Val).size();
		}
		Logger.log("total unique nodes: " + totalUniqueNodes);

	}
	
	public static void doMain(String queryPath, String databasePath, boolean isBuildElementList){

		Logger.log("Loading testing data");
		TestData testData = (TestData) FileUtils.readObjectFile(GlobalConfig.postTestDataPath);
		Logger.log("Loadding conn posts");
		ArrayList<ConnPostInfo> connPosts = readQueryPath(queryPath);
		//		Logger.log(connPosts);
		Logger.log("Loading database");
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(databasePath);
		Node.globalData = graphDB.globalData;

		long startTime = System.currentTimeMillis();
		TreeMap<Long, ArrayList<Graph>> postGraphsMap =CLTDataProcessing.proceedAllConnPosts(connPosts, testData, graphDB, graphDB.globalData, isBuildElementList, GlobalConfig.tmpQueryPath);

		GraphQueryEvaluation gqe = new GraphQueryEvaluation();
		//		TreeMap<Long, Graph> idxGraphsMap = gqe.createGraphs(GlobalConfig.tmpQueryPath,graphDB);

		//TODO: should check label
		GraphComparisonData graphCompData = new GraphComparisonData();
		GraphComparisonData graphCompDataRelax = new GraphComparisonData();

		StringGraphComparisonData strGraphCompData = new StringGraphComparisonData();
		//		 Logger.log("postGraphsMap: " + postGraphsMap);
		long endTime = System.currentTimeMillis();


		int numGraph = 0;
		String queryListPath = GlobalConfig.tmpQueryPath;
		QueryDataList queryDataList = new QueryDataList();
		Logger.log("Loading database");
		queryDataList.createIdxNodesMap(queryListPath, graphDB.globalData);
		QueryMain queryMain = new QueryMain(); 
		Logger.log("createMapFromGraphDB");

		NodeGraphDatabase nodeGraphDB = new NodeGraphDatabase();
		nodeGraphDB.createMapFromGraphDB(graphDB);
		Logger.log("drawing graph");

		Node.globalData = graphDB.globalData;

		Logger.log("queryDataList.idxNodesMap: " + queryDataList.idxNodesMap.size());
		Logger.log("queryDataList.idxNodesMap: " + queryDataList.idxNodesMap.keySet());

		Logger.log("postGraphsMap.size: " + postGraphsMap.size());
		Logger.log("postGraphsMap.keyset: " + postGraphsMap.keySet());

		int count = 0;
		
		TreeSet<Integer> unparsedSnippet = new TreeSet<Integer>();

		for (Long idx:postGraphsMap.keySet()){


//			if (postGraphsMap.get(idx).size()<=0)
//				continue;
//			Logger.log("idx: " + idx);
			String idStr = String.valueOf(idx);
			
			if(!queryDataList.idxNodesMap.containsKey(idStr))
				continue;
//			TreeMap<Long, Graph> idxGraphsMap = new TreeMap<Long, Graph>();

			ArrayList<Node> nodes = queryDataList.idxNodesMap.get(idStr);
			count++;
		
			//					long currTime = System.currentTimeMillis();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();

			Logger.log("idx: " + idx +"///" + count + "////" +dateFormat.format(cal.getTime()));// + "///nodes: " + nodes);
			ArrayList<Node> dbNodes = new ArrayList<Node>();
			for (Node node:nodes){
				Node aNode = nodeGraphDB.findNode(node);
				dbNodes.add(aNode);

			}
			LinkedHashMap<Graph, Double> graphsWithScores = queryMain.query(dbNodes, graphDB, nodeGraphDB);
//			Logger.log("graphsWithScores: " + graphsWithScores);
			LinkedHashMap<Graph, Double> filteredGraphsWithScores = new LinkedHashMap<Graph, Double>();
			for(Graph gr:graphsWithScores.keySet()){
				//						if(gr.numNodes()>=nodes.size())
				{
					filteredGraphsWithScores.put(gr, graphsWithScores.get(gr));
				}
			}
//			Logger.log("filteredGraphsWithScores: " + filteredGraphsWithScores);

			Graph gr = gqe.proceedGraphsWithScores(idStr, filteredGraphsWithScores, graphDB.globalData);
//			long i = idx;//Long.parseLong(idx);
//			idxGraphsMap.put(i, gr);

			if (postGraphsMap.get(idx).size()==0)
				continue;
			Graph actualGraph = postGraphsMap.get(idx).get(0);
			Graph synGraph = gr;//idxGraphsMap.get(idx);
			if (synGraph==null||actualGraph==null)
				continue;

			//			 Logger.log("actualGraph: "+ actualGraph);
			//			 Logger.log("synGraph: "+ synGraph);
			Logger.log("   Comparing");
			GraphComparisonUtils.compareGraphWithshape(actualGraph, synGraph, graphCompData, idx);
			GraphComparisonUtils.compareGraphWithShapeRelax(actualGraph, synGraph, graphCompDataRelax, idx);
//			graphCompDataRelax.showStatistics();
			graphCompDataRelax.doStatisticsSimple();


			//			 GraphComparisonUtils.compareGraphWithStrSeq(actualGraph, synGraph, strGraphCompData, graphDB.globalData);

			numGraph++;
			Logger.log("numGraph: " + numGraph);
		}

		long runningTime = (endTime-startTime)/1000;
		Logger.log("number of Graphs: " + numGraph);
		Logger.log("running time: " + runningTime + "(s)" ); 

		double averageRunningTime = (double)runningTime/(double)numGraph;

		Logger.log("average running time: " + averageRunningTime + "(s)" ); 

		Logger.log(System.lineSeparator() + System.lineSeparator() + "Statistics over graph comparison");
		graphCompData.doStatistics();

		Logger.log(System.lineSeparator() + System.lineSeparator() + "Statistics over graph comparison (relaxed)");
		graphCompDataRelax.doStatistics();

		Logger.log(System.lineSeparator() + System.lineSeparator() + "Statistics over graph sequence comparison");
		strGraphCompData.doStatistics();

		FileUtils.writeObjectFile(graphCompData, GlobalConfig.graphComparisonPath);
		FileUtils.writeObjectFile(graphCompDataRelax, GlobalConfig.graphComparisonRelaxPath);


	}

	public static ArrayList<ConnPostInfo> readQueryPath(String queryPath){
		ArrayList<ConnPostInfo> connPosts = new ArrayList<ConnPostInfo>();
		try{
			Scanner sc = new Scanner(new File(queryPath));
			while (sc.hasNextLine()){
				String line = sc.nextLine();
				//				Logger.log(line);
				String[] splits = line.split(":::");
				//				Logger.log("splits: " + Arrays.asList(splits));
				long postID = Long.parseLong(splits[0]);
				ArrayList<CLTData> clts = new ArrayList<CLTData>();
				for (int i=1; i<splits.length; i++){
					String[] splits2 = splits[i].split("//");
					String split = splits2[0];
					if (split.contains(".")){
						int idx = split.indexOf(".");
						String prefix = split.substring(0, idx);
						String postfix = split.substring(idx+1);
						String pqn = prefix;
						String name = postfix;
						String type = "1";
						if (prefix.equals(postfix)){
							name = "var";
							type = "3";
						}
						CLTData clt = new CLTData(String.valueOf(postID), String.valueOf(postID), pqn, name, type);
						clts.add(clt);
					}
					else {
						String pqn = "<unknownClass>";
						String name = split;
						String type = "1";	
						CLTData clt = new CLTData(String.valueOf(postID), String.valueOf(postID), pqn, name, type);
						clts.add(clt);
					}
				}
				ConnPostInfo connPost = new ConnPostInfo(postID, clts);
				connPosts.add(connPost);
			}
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);

		}
		return connPosts; 
	}
}
