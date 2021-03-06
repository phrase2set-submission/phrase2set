package evaluation;

import groumvisitors.JavaGroumVisitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import main.AllMain;

import storage.GraphDatabase;
import utils.DataUtils;
import utils.Logger;
import config.GlobalConfig;

import data.MethodInfo;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;

public abstract  class GraphNodeRecCorrectness {
	final static int specialRank = -1;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * return the position in ranked list that has label equal to correctNode. The first position has index equal to 1  
	 * @param correctNode
	 * @param recommendedNodes
	 * @return
	 */

	public  int getRankCorrectNode(Node correctNode, ArrayList<Node> recommendedNodes, GlobalData globalData){
		int rank = specialRank;
		for (int i=0;i<recommendedNodes.size();i++){
			Logger.log("correctNode: " + correctNode.getNodeLabel(globalData) + "    recNode: " + recommendedNodes.get(i).getNodeLabel(globalData)); 
			if(correctNode.simpleRoleEquals(recommendedNodes.get(i))){
				rank = i+1;
				return rank;
			}
		}
		return rank;
	}
	
	
	
	/**
	 *
	 * @param correctNodes
	 * @param recNodesList
	 * @return
	 */
	
	public TreeMap<Integer, Integer>  getAllRankCorrectNodes(ArrayList<Node> correctNodes, ArrayList<ArrayList<Node>> recNodesList, GlobalData globalData ){
		int listSize = correctNodes.size();
		ArrayList<Integer> allRankCorrectNodeList = new  ArrayList<>();
		for (int i=0; i<listSize; i++){
			int rank = getRankCorrectNode(correctNodes.get(i), recNodesList.get(i), globalData);
			Logger.log("rank: " + rank);
			allRankCorrectNodeList.add(rank);
		}
		
		TreeMap<Integer, Integer> allRankCorrectNodes = new TreeMap<>();
		for (Integer rankCorrectNode:allRankCorrectNodeList){
			if (!allRankCorrectNodes.containsKey(rankCorrectNode)){
				allRankCorrectNodes.put(rankCorrectNode, 1);
			}
			else {
				int count = allRankCorrectNodes.get(rankCorrectNode);
				allRankCorrectNodes.put(rankCorrectNode, count + 1);					
			}
		}
		return allRankCorrectNodes;
	}
	
	
	/**
	 * 
	 * @param methodGraph
	 * @return
	 */
	public TreeMap<Integer, Integer> doEValMethod(Graph methodGraph, GraphDatabase graphDB){
		
		ArrayList<Node> correctNodes = getCorrectNodes(methodGraph, graphDB);
//		Logger.log("    correctNodes: " + correctNodes);
		ArrayList<ArrayList<Node>> recNodesList = getRecommendedNodes(methodGraph, correctNodes, graphDB);
//		Logger.log("    recNodesList: " + recNodesList);

		TreeMap<Integer, Integer> allRankCorrectNodes = getAllRankCorrectNodes(correctNodes, recNodesList, graphDB.globalData);
//		Logger.log("    allRankCorrectNodes: " + allRankCorrectNodes);

		return allRankCorrectNodes;
	}
	
	
	/**
	 * 
	 * @param projectPath
	 * @return
	 */
	public TreeMap<Integer, Integer> doEvalProject(String  projectPath, GraphDatabase graphDB){
		ArrayList<Graph> projectMethodGraphs = getProjectMethodGraphs(projectPath, graphDB);
		Logger.log("projectMethodGraphs size: " + projectMethodGraphs.size());
		TreeMap<Integer, Integer> projectRankCorrectNodes = new TreeMap<>();
		for (Graph methodGraph:projectMethodGraphs){
			Logger.log(methodGraph);
			TreeMap<Integer, Integer> methodRankCorrectNodes = doEValMethod(methodGraph, graphDB);
			for (Integer rank:methodRankCorrectNodes.keySet()){
				Integer count = methodRankCorrectNodes.get(rank);
				if (!projectRankCorrectNodes.containsKey(rank)){
					projectRankCorrectNodes.put(rank, count);
				}
				else {
					int curCount = projectRankCorrectNodes.get(rank);
					projectRankCorrectNodes.put(rank, count + curCount);
				}
			}
		}
		return projectRankCorrectNodes;
	}
	
	/**
	 * 
	 * @param projectPath
	 * @return
	 */
	
	public ArrayList<Graph> getProjectMethodGraphs(String projectPath, GraphDatabase graphDB){
		LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<>();

		/**
		 * Browse all methods and add their groums and subgroums to database
		 */
		Logger.log("\tbuilding groums");
		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.doMain(projectPath);

		int numMethods = 0;
		long LOCs = 0;


		List<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
		//			int count = 0;
		for (TypeInfo typeInfo:allTypeList){
			String packageName = typeInfo.packageDec;
			String className = typeInfo.typeName;
			
			List<MethodInfo> methodList = typeInfo.methodDecList;

			for (MethodInfo method:methodList){
				//					Logger.log(count);
				String methodName = method.methodName;
				
				String combinedName = AllMain.normalizeStr(projectPath) +"::" + 
								AllMain.normalizeStr(packageName) + "." + AllMain.normalizeStr(className) + "." + AllMain.normalizeStr(methodName)
								+"::" + numMethods;
				numMethods++;
				LOCs += method.LOCs;
				
				if (AllMain.isDoCompactMethod){
					DataUtils.compactGroum(method);
				}
				methodInfoMap.put(combinedName, method);
			}
		}

		LinkedHashMap<String, Graph> methodGraphMap = AllMain.convertAllGroumsToGraphs(methodInfoMap, graphDB.globalData);
		Logger.log("\t\tmethodGraphMap size: " + methodGraphMap.size());
		return getAllMethodGraphs(methodGraphMap, GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, graphDB, projectPath);
	}
	
	public ArrayList<Graph> getAllMethodGraphs
	(LinkedHashMap<String, Graph> methodGraphMap,  int maxGraphSize, int maxCountNode, GraphDatabase graphDB, String projectName){
		ArrayList<Graph> allMethodGraphs = new ArrayList<>();
		for (String methodName:methodGraphMap.keySet()){
			Graph methodGraph = methodGraphMap.get(methodName);
			allMethodGraphs.add(methodGraph);
		}
	
		return allMethodGraphs;
	}
	
	
	/**
	 * Calculate Quality statistics over ranked nodes 
	 * @param projectRankCorrectNodes
	 */
	public void doStatistics(TreeMap<Integer, Integer> projectRankCorrectNodes){
		int lastTopK = 20;
		TopKStat [] topKStats = new TopKStat[lastTopK+1]; 
		for (int topK=1; topK<=lastTopK; topK++){
			topKStats[topK]= calcTopKAccuracy(projectRankCorrectNodes, topK);
		}
		
		Logger.log("Top-K accuracy: ");
		for (int topK = 1; topK<=lastTopK;topK++){
			System.out.print(String.format("%1.4f",topKStats[topK].Accuracy) + " ");
		}
		
		Logger.log("");
		calcMAP(projectRankCorrectNodes);
	}
	
	/**
	 * calculate top-K accuracy
	 * @param projectRankCorrectNodes
	 */
	public TopKStat calcTopKAccuracy(TreeMap<Integer, Integer> projectRankCorrectNodes, int topK){
		
		Logger.log("    projectRankCorrectNodes: " + projectRankCorrectNodes );
		int totalNum = 0;
		for (Integer rank:projectRankCorrectNodes.keySet()){
			totalNum += projectRankCorrectNodes.get(rank);
		}
		
		int totalHit = 0;
		for (Integer rank:projectRankCorrectNodes.keySet()){
			if (rank<= topK&&rank!=specialRank){
				totalHit += projectRankCorrectNodes.get(rank);
			}
		}
		
		TopKStat topKStat = new TopKStat(topK, totalNum, totalHit);
		return topKStat;
	}
	
	/**
	 * calculate 
	 * @param projectRankCorrectNodes
	 */
	//TODO: implement calcMap
	public void calcMAP(TreeMap<Integer, Integer> projectRankCorrectNodes){
		
	}
	
	//**********************************************
	
	public abstract ArrayList<Node> getCorrectNodes(Graph methodGraph, GraphDatabase graphDB);
	
	public abstract ArrayList<ArrayList<Node>> getRecommendedNodes(Graph methodGraph, ArrayList<Node> correctNodes, GraphDatabase graphDB);
  }
