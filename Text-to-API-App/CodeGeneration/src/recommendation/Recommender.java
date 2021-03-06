/**
 * 
 */
package recommendation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import storage.GraphDatabase;
import utils.Logger;

import config.GlobalConfig;

import datastructure.Edge;
import datastructure.Graph;
import datastructure.Node;
import evaluation.EvalConfig;
import evaluation.GraphNodeRecCorrectness;

/**
 * @author anhnt
 *
 */
public class Recommender  extends GraphNodeRecCorrectness{

	public static double alpha = 0.0001;
	public static int extendSize = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Recommender aRecommender = new Recommender();
		aRecommender.doMain(GlobalConfig.graphDatabasePath,EvalConfig.testProjectPath);


	}
	
	
	public void doMain(String databasePath, String testProjectPath){
		Logger.log("Reading Graph Database: " + databasePath);
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(databasePath);
		Logger.log("Building parent-child relations: " );
		graphDB.buildAllChild();
		Logger.log("Doing Evaluation");
		TreeMap<Integer, Integer> projectRankCorrectNodes = doEvalProject(testProjectPath, graphDB);
		doStatistics(projectRankCorrectNodes);
		
	}
	
	
	
	/**
	 * Return scored Graphs according to current method's Graph and currentNode
	 * @param methodGraph
	 * @param currentNode
	 * @return
	 */
	public TreeMap<Double, ArrayList<Graph>>  processGraphWithContext(Graph methodGraph, Node currentNode, GraphDatabase graphDB){
		ArrayList<Graph>  contextGraphs = getContextGraphs(methodGraph, currentNode, extendSize, graphDB);

		HashMap<Graph, Double> allChildGraphWithScore = getAllChildGraphsWithScore(contextGraphs, graphDB);

		TreeMap<Double, ArrayList<Graph>> rankedScoreChildGraphsMap = new TreeMap<>();
		for (Graph childGraph:allChildGraphWithScore.keySet()){
			Double score = allChildGraphWithScore.get(childGraph);
			if (!rankedScoreChildGraphsMap.containsKey(score)){
				ArrayList<Graph> sameScoreList = new ArrayList<>();
				sameScoreList.add(childGraph);
				rankedScoreChildGraphsMap.put(score, sameScoreList);
			}
			else{
				rankedScoreChildGraphsMap.get(score).add(childGraph);
			}
		}
		return rankedScoreChildGraphsMap;
	}
	
	
	/**
	 * Return scored Nodes according to current method's Graph and currentNode
	 * @param methodGraph
	 * @param currentNode
	 * @return
	 */
	public TreeMap<Double, ArrayList<Node>>  processGraphNodeWithContext(Graph methodGraph, Node currentNode, GraphDatabase graphDB){
		ArrayList<Graph>  contextGraphs = getContextGraphs(methodGraph, currentNode, extendSize, graphDB);
		Logger.log("    contextGraphs: " + contextGraphs);
		HashMap<Node, Double> allChildGraphWithScore = getAllChildNodesWithScore(contextGraphs, graphDB);
		Logger.log("    allChildGraphWithScore: " + allChildGraphWithScore);

		TreeMap<Double, ArrayList<Node>> rankedScoreChildGraphsMap = new TreeMap<>();
		for (Node childGraph:allChildGraphWithScore.keySet()){
			Double score = allChildGraphWithScore.get(childGraph);
			if (!rankedScoreChildGraphsMap.containsKey(score)){
				ArrayList<Node> sameScoreList = new ArrayList<>();
				sameScoreList.add(childGraph);
				rankedScoreChildGraphsMap.put(score, sameScoreList);
			}
			else{
				rankedScoreChildGraphsMap.get(score).add(childGraph);
			}
		}
		return rankedScoreChildGraphsMap;
	}


	/**
	 * Get the list of all graphs that surrounding the focused node. Those graphs are subgraphs of the overall graph
	 * @param methodGraph
	 * @param currentNode
	 * @return
	 */
	public ArrayList<Graph> getContextGraphs(Graph methodGraph, Node currentNode, int extendSize, GraphDatabase graphDB){

		//Get all nodes, with each node N, show the nodes having edges with N  
		HashMap<Node, HashSet<Node>> nodeLinkNodeMap = new HashMap<>();
		for (Edge edge:methodGraph.edges){
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (!nodeLinkNodeMap.containsKey(source)){
				nodeLinkNodeMap.put(source, new HashSet<Node>());
			}
			nodeLinkNodeMap.get(source).add(sink);

			if (!nodeLinkNodeMap.containsKey(sink)){
				nodeLinkNodeMap.put(sink, new HashSet<Node>());
			}
			nodeLinkNodeMap.get(sink).add(source);
		}
		
		
		//Build the list of all nodes surrounding the focused point
		TreeMap<Integer, HashSet<Node>> extendNodesMap = new TreeMap<>();
		HashSet<Node> allObservedNodes = new HashSet<>();
  		
		HashSet<Node> nodes0 = new HashSet<>();
		nodes0.add(currentNode);
		extendNodesMap.put(0, nodes0);
		allObservedNodes.add(currentNode);
		
		for (int i=1; i<=extendSize; i++){
			HashSet<Node> nodesPrev = extendNodesMap.get(i-1);
			HashSet<Node> nodesi = new HashSet<>();
			for (Node node:nodesPrev){
				if (nodeLinkNodeMap.containsKey(node)){
					HashSet<Node> linkNodes = nodeLinkNodeMap.get(node);
					for (Node linkNode:linkNodes){
						if (!allObservedNodes.contains(linkNode)){
							allObservedNodes.add(linkNode);
							nodesi.add(linkNode);
						}
					}
				}
			}
			extendNodesMap.put(i, nodesi);
		}

		//remove focused node
		allObservedNodes.remove(currentNode);
		Logger.log("allObservedNodes: " + allObservedNodes);
		
		//Get Surrrounding graphs
		
		HashMap<Node, HashSet<Node>> node1LinkNodeMap = new HashMap<>();
		for (Edge edge:methodGraph.edges){
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (!node1LinkNodeMap.containsKey(source)){
				node1LinkNodeMap.put(source, new HashSet<Node>());
			}
			node1LinkNodeMap.get(source).add(sink);

		
		}
		ArrayList<Graph> contextGraphs = new ArrayList<>();
		for (Node node:allObservedNodes ){
			HashSet<Node> limitNodes = new HashSet<>();
			limitNodes.addAll(allObservedNodes);
			limitNodes.remove(node);
			ArrayList<Graph> allGraphs = getAllGraphs(node, limitNodes, node1LinkNodeMap, graphDB);
			contextGraphs.addAll(allGraphs);
		}
		return contextGraphs;
	}

	public ArrayList<Graph> getAllGraphs(Node node, HashSet<Node> limitNodes, HashMap<Node, HashSet<Node>> node1LinkNodeMap, GraphDatabase graphDB ){
		ArrayList<Graph> allGraphs = new ArrayList<>();
		
		ArrayList<Node> nodeList = new ArrayList<>();
		nodeList.add(node);
		ArrayList<Edge> edgeList = new ArrayList<>();
		Graph aGraph = new Graph(nodeList, edgeList, 1);
		allGraphs.add(aGraph);
		
		HashSet<Node>observedNodes = new HashSet<>();
		observedNodes.add(node);
		HashSet<Edge> observedEdges = new HashSet<>();

		do{
			int lastSize = observedNodes.size();
			HashSet<Node>observedNodesTmp = new HashSet<>();
			observedNodesTmp.addAll(observedNodes);
			for (Node aNode:observedNodesTmp){
				if (node1LinkNodeMap.containsKey(aNode)){
					HashSet<Node> linkNodes = node1LinkNodeMap.get(aNode);
					for (Node linkNode:linkNodes){
						if (limitNodes.contains(linkNode))
							continue;
						if (!observedNodes.contains(linkNode)){
							HashSet<Node> oldObservedNodes = new HashSet<>();
							oldObservedNodes.addAll(observedNodes);
							observedNodes.add(linkNode);
							for (Node oldObservedNode:oldObservedNodes){
								if(node1LinkNodeMap.containsKey(oldObservedNode)){
									if (node1LinkNodeMap.get(oldObservedNode).contains(linkNode)){
										Edge edge = new Edge(oldObservedNode, linkNode, 1);
										observedEdges.add(edge);
										ArrayList<Edge> edges = new ArrayList<>();
										edges.addAll(observedEdges);
										ArrayList<Node> nodes = new ArrayList<>();
										nodes.addAll(observedNodes);
										Graph graph = new Graph(nodes, edges, 1);
										if (graph.isConcernedGraph(GlobalConfig.concernedLibs, graphDB.globalData)){

											allGraphs.add(graph);
										}
									}
								}
							}
						}
					}
				}
			}
			if (lastSize==observedNodes.size()){
				break;
			}
		}
		while(true);
		return allGraphs;
	}

	

	public HashMap<Node, Double> getAllChildNodesWithScore(ArrayList<Graph> contextGraphs, GraphDatabase graphDB){
		HashMap<Integer, HashMap<Node, Double>>hashGraphScoresMap = new HashMap<Integer, HashMap<Node,Double>>();
		for(Graph contextGraph:contextGraphs){
			Graph dbGraph = graphDB.searchGraph(contextGraph);
//			Graph[] childGraphs = graphDB.searchChildGraphs(contextGraph);// contextGraph.childrenGraphs;
			Logger.log("         dbGraph: " + dbGraph );
			Graph[] childGraphs = null;
			if (dbGraph != null){
				childGraphs = dbGraph.childrenGraphs;

			}
		
			if (childGraphs!=null){
				Logger.log("         childGraphs: " + Arrays.asList(childGraphs ));

				for (Graph childGraph:childGraphs){
					addGraphNode(hashGraphScoresMap, childGraph, contextGraph,  contextGraphs, graphDB);
				}
			}
		}

		HashMap<Node, Double> allChildGraphsWithScore = new HashMap<>();
		for (Integer h1Val:hashGraphScoresMap.keySet()){
			allChildGraphsWithScore.putAll(hashGraphScoresMap.get(h1Val));
		}
		return allChildGraphsWithScore;
		
	}
	
	public void addGraphNode(HashMap<Integer, HashMap<Node, Double>>hashGraphScoresMap, Graph graph, 
			Graph parentGraph, ArrayList<Graph> contextGraphs, 
			GraphDatabase graphDB){
		int h1Val = graph.calcH1();
		double score = calcScore(graph, parentGraph, contextGraphs, graphDB);
		if (!hashGraphScoresMap.containsKey(h1Val)){
			HashMap<Node, Double> graphs = new HashMap<Node, Double>();
			graphs.put(getAddedNode(graph, parentGraph), score);
			hashGraphScoresMap.put(h1Val, graphs);
		}
		else {
			boolean isFound = false;
			HashMap<Node, Double> graphs = hashGraphScoresMap.get(h1Val);
			for (Node agraph:graphs.keySet()){
				if (agraph.equals(graphs)){
					isFound = true;
					if (graphs.get(agraph)<score){
						graphs.remove(agraph);
						graphs.put(agraph, score);
					}
					break;
				}
			}
			if (!isFound){
				graphs.put(getAddedNode(graph,parentGraph), score);
			}
		}
	}
	
	
	public Node getAddedNode(Graph graph, Graph parentGraph){
		for (Node node:graph.nodes){
			Graph aParent = graph.getAParent(node);
			if (aParent.isConnectedGraph()){
				if (aParent.roleEquals(parentGraph)){
					return node;
				}
			}
		}
		return null;
	}
	
	
	
	public HashMap<Graph, Double> getAllChildGraphsWithScore(ArrayList<Graph> contextGraphs, GraphDatabase graphDB){
		HashMap<Integer, HashMap<Graph, Double>>hashGraphScoresMap = new HashMap<Integer, HashMap<Graph,Double>>();
		for(Graph contextGraph:contextGraphs){
			Graph[] childGraphs = contextGraph.childrenGraphs;
			for (Graph childGraph:childGraphs){
				addGraph(hashGraphScoresMap, childGraph, contextGraph,  contextGraphs, graphDB);
			}
		}

		HashMap<Graph, Double> allChildGraphsWithScore = new HashMap<>();
		for (Integer h1Val:hashGraphScoresMap.keySet()){
			allChildGraphsWithScore.putAll(hashGraphScoresMap.get(h1Val));
		}
		return allChildGraphsWithScore;
	}

	public synchronized double calcScore( Graph childGraph, Graph parentGraph, ArrayList<Graph> contextGraphs, GraphDatabase graphDB){
		double score = 0.0;
		Graph childGraphInDB = graphDB.searchGraph(childGraph);
		Graph parentGraphInDB = graphDB.searchGraph(parentGraph);
		
//		int totalNum = 0;
		score += calcSmoothScore(parentGraphInDB, childGraphInDB, alpha);
//		totalNum++;
		for (Graph contextGraph:contextGraphs ){
			if (contextGraph == parentGraph)
				continue;
			Graph contextGraphInDB = graphDB.searchGraph(contextGraph);
			score += calcSmoothScore(childGraphInDB, contextGraphInDB, alpha);
//			totalNum++;
		}
//		score += Math.log(((double)childGraph.methodIdxList.length+1)/1000000);
		return score;
	}


	public synchronized  double calcSmoothScore(Graph graph, Graph contextGraph, double alpha){
		if (graph==null||contextGraph==null)
			return 0.0;
		
		int numGraphMethods = graph.methodIdxList.length;
		int numCoAppear = 0;
		for (int i=0; i<numGraphMethods;i++){
			int methodIdx = graph.methodIdxList[i];
			if(contextGraph==null)
				continue;
			if(contextGraph.methodIdxList==null)
				continue;
			int loc = Arrays.binarySearch(contextGraph.methodIdxList, methodIdx);
			if (loc>=0){
				numCoAppear++;
			}
		}
		double smoothScore = Math.log(((double)numCoAppear + alpha)/((double)numGraphMethods + alpha));
//		double smoothScore = Math.log(((double)numCoAppear + 1)/((double)numGraphMethods + 2));

		return smoothScore;
	}





	public void addGraph(HashMap<Integer, HashMap<Graph, Double>>hashGraphScoresMap, Graph graph, Graph parentGraph, ArrayList<Graph> contextGraphs, 
			GraphDatabase graphDB){
		int h1Val = graph.calcH1();
		double score = calcScore(graph, parentGraph, contextGraphs, graphDB);
		if (!hashGraphScoresMap.containsKey(h1Val)){
			HashMap<Graph, Double> graphs = new HashMap<Graph, Double>();
			graphs.put(graph, score);
			hashGraphScoresMap.put(h1Val, graphs);
		}
		else {
			boolean isFound = false;
			HashMap<Graph, Double> graphs = hashGraphScoresMap.get(h1Val);
			for (Graph agraph:graphs.keySet()){
				if (agraph.equals(graphs)){
					isFound = true;
					if (graphs.get(agraph)<score){
						graphs.remove(agraph);
						graphs.put(agraph, score);
					}
					break;
				}
			}
			if (!isFound){
				graphs.put(graph, score);
			}
		}
	}



	@Override
	public ArrayList<Node> getCorrectNodes(Graph methodGraph, GraphDatabase graphDB) {
		// TODO Auto-generated method stub
		ArrayList<Node> correctNodes = new ArrayList<>();
		
		for (Node node:methodGraph.nodes){
			if (node.isConcernedNode(graphDB.globalData)){
				correctNodes.add(node);
			}
		}
		return correctNodes;
	}



	@Override
	public ArrayList<ArrayList<Node>> getRecommendedNodes(Graph methodGraph, ArrayList<Node> correctNodes, GraphDatabase graphDB) {
		// TODO Auto-generated method stub
		ArrayList<ArrayList<Node>> recommendedNodes = new ArrayList<>();
//		for (Node node:methodGraph.nodes){
		for (Node node:correctNodes){
			Logger.log("   node: " + node);

//			TreeMap<Double, ArrayList<Graph>> rankedScoreChildGraphsMap = processGraphWithContext(methodGraph, node, graphDB);
//			ArrayList<Graph> rankedGraphs = getRankedGraphs(rankedScoreChildGraphsMap);
			TreeMap<Double, ArrayList<Node>> rankedScoreChildGraphsMap = processGraphNodeWithContext(methodGraph, node, graphDB);
			ArrayList<Node> rankedNodes = getRankedNodes(rankedScoreChildGraphsMap);
			recommendedNodes.add(rankedNodes);
		}
		return recommendedNodes;
	}
	
	
	/**
	 * 
	 * @param rankedScoreChildGraphsMap
	 * @return
	 */
	public ArrayList<Graph> getRankedGraphs(TreeMap<Double, ArrayList<Graph>> rankedScoreChildGraphsMap){
		ArrayList<Graph> rankedGraphs = new ArrayList<>();
		for (Double score: rankedScoreChildGraphsMap.descendingKeySet()){
			ArrayList<Graph> graphs = rankedScoreChildGraphsMap.get(score);
			rankedGraphs.addAll(graphs);
		}
		return rankedGraphs;
	}
	
	
	/**
	 * 
	 * @param rankedScoreChildGraphsMap
	 * @return
	 */
	public ArrayList<Node> getRankedNodes(TreeMap<Double, ArrayList<Node>> rankedScoreChildGraphsMap){
		ArrayList<Node> rankedGraphs = new ArrayList<>();
		for (Double score: rankedScoreChildGraphsMap.descendingKeySet()){
			ArrayList<Node> graphs = rankedScoreChildGraphsMap.get(score);
			
			for (Node node:graphs){
				boolean isFound = false;
				for (Node rankedNode:rankedGraphs){
					if (node.roleEquals(rankedNode)){
						isFound = true;
						break;
					}
				}
				if (!isFound){
					rankedGraphs.add(node);
				}
			}
			
		}
		return rankedGraphs;
	}

	
//	public 



	//	public ArrayList<Graph> getAllChildGraphs(ArrayList<Graph> contextGraphs){
	//		HashMap<Integer, ArrayList<Graph>> hashGraphsMap = new HashMap<>();
	//		for(Graph contextGraph:contextGraphs){
	//			Graph[] childGraphs = contextGraph.childrenGraphs;
	//			for (Graph childGraph:childGraphs){
	//				addGraph(hashGraphsMap, childGraph);
	//			}
	//		}
	//		
	//		
	//		ArrayList<Graph> allChildGraphs = new ArrayList<>();
	//
	//		for (int h1Val:hashGraphsMap.keySet()){
	//			allChildGraphs.addAll(hashGraphsMap.get(h1Val));
	//		}
	//		return allChildGraphs;
	//	}

	//	public void addGraph(HashMap<Integer, ArrayList<Graph>> hashGraphsMap, Graph graph){
	//		int h1Val = graph.calcH1();
	//		if (!hashGraphsMap.containsKey(h1Val)){
	//			ArrayList<Graph> graphs = new ArrayList<>();
	//			graphs.add(graph);
	//			hashGraphsMap.put(h1Val, graphs);
	//		}
	//		else {
	//			boolean isFound = false;
	//			ArrayList<Graph> graphs = hashGraphsMap.get(h1Val);
	//			for (Graph agraph:graphs){
	//				if (agraph.equals(graphs)){
	//					isFound = true;
	//					break;
	//				}
	//			}
	//			if (!isFound){
	//				graphs.add(graph);
	//			}
	//		}
	//	}


}
