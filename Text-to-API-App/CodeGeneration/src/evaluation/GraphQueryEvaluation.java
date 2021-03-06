/**
 * 
 */
package evaluation;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import query.QueryMain;
import storage.GraphDatabase;
import storage.NodeGraphDatabase;
import utils.GraphDrawingUtils;
import utils.Logger;
import config.GlobalConfig;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;

/**
 * @author anhnt_000
 *
 */
public class GraphQueryEvaluation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		GraphQueryEvaluation gqe = new GraphQueryEvaluation();
//		gqe.createGraphs();
	}

	public TreeMap<Long, Graph>  createGraphs(String queryListPath, GraphDatabase graphDB){
		
		QueryDataList queryDataList = new QueryDataList();
		Logger.log("Loading database");
		queryDataList.createIdxNodesMap(queryListPath, graphDB.globalData);
		QueryMain queryMain = new QueryMain(); 
		Logger.log("createMapFromGraphDB");

		NodeGraphDatabase nodeGraphDB = new NodeGraphDatabase();
		nodeGraphDB.createMapFromGraphDB(graphDB);
		Logger.log("drawing graph");

		Node.globalData = graphDB.globalData;
		TreeMap<Long, Graph> idxGraphsMap = new TreeMap<Long, Graph>();
		int count = 0;
		for (String idx:queryDataList.idxNodesMap.keySet()){
			 
			ArrayList<Node> nodes = queryDataList.idxNodesMap.get(idx);
			count++;
			if (count<0)
				continue;
//			long currTime = System.currentTimeMillis();
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
//				if(gr.numNodes()>=nodes.size())
				{
					filteredGraphsWithScores.put(gr, graphsWithScores.get(gr));
				}
			}

			Graph gr = proceedGraphsWithScores(idx, filteredGraphsWithScores, graphDB.globalData);
			long i = Long.parseLong(idx);
			idxGraphsMap.put(i, gr);
		}
		return idxGraphsMap;
	}
	
	
	
	
	public Graph proceedGraphsWithScores(String idx, LinkedHashMap<Graph, Double> graphsWithScores, GlobalData globalData){
		String graphOutputPath = GlobalConfig.graphOutputPath + idx;
		File graphOutput = new File(graphOutputPath);
		graphOutput.mkdirs();
//		Logger.log("graphsWithScores size: " + graphsWithScores.size());
		TreeMap<Double, ArrayList<Graph>> scoresGraphs = new TreeMap<Double, ArrayList<Graph>>();
		
		for (Graph graph:graphsWithScores.keySet()){
			Double score = graphsWithScores.get(graph);
			if (scoresGraphs.containsKey(score)){
				scoresGraphs.get(score).add(graph);
			}
			else {
				ArrayList<Graph> graphs = new ArrayList<Graph>();
				graphs.add(graph);
				scoresGraphs.put(score, graphs);
			}
		
		}
		ArrayList<Graph> rankedGraphs = new ArrayList<Graph>();
		for (Double score:scoresGraphs.descendingKeySet()){
			ArrayList<Graph> scoreGraph = scoresGraphs.get(score);
			if (scoreGraph.size()<=1)
				rankedGraphs.addAll(scoresGraphs.get(score));
			else {
				TreeMap<Integer, ArrayList<Graph>> nNodesHaveEdgeGraphMap = new TreeMap<>();
				for (Graph gr:scoreGraph){
					int nNodesHaveEdge = gr.getNumNodeHasEdges();
					if (nNodesHaveEdgeGraphMap.containsKey(nNodesHaveEdge)){
						nNodesHaveEdgeGraphMap.get(nNodesHaveEdge).add(gr);
					}
					else {
						ArrayList<Graph> grs = new ArrayList<Graph>();
						grs.add(gr);
						nNodesHaveEdgeGraphMap.put(nNodesHaveEdge, grs);
					}
				}
				for (Integer nNodesHaveEdge:nNodesHaveEdgeGraphMap.descendingKeySet()){
					rankedGraphs.addAll(nNodesHaveEdgeGraphMap.get(nNodesHaveEdge));
				}
			}
		}
		int count =0;
		ArrayList<Graph> grs = new ArrayList<Graph>();
		for (Graph graph:rankedGraphs ){
			boolean isfound = false;
			for (Graph gr:grs){
				if (gr.roleEquals(graph)){
					isfound = true;
					break;
				}
			}
			if (isfound)
				continue;
			grs.add(graph);
			count++;
//			String outputPath = graphOutputPath + "/" + count  + "_"+ graphsWithScores.get(graph) + ".dot"; 
//			GraphDrawingUtils.outputDotFile(graph, outputPath, globalData);
//			
//			String outputGifPath = graphOutputPath + "/" + count + "_"+ graphsWithScores.get(graph) + "_" + graph.getLongestPath() + ".gif"; 
//			GraphDrawingUtils.callConvert(outputPath, outputGifPath);
			
			String outputPath = graphOutputPath + "/" + count  + ".dot"; 
			GraphDrawingUtils.outputDotFile(graph, outputPath, globalData);
			
			String outputGifPath = graphOutputPath + "/" + count  + ".gif";
			GraphDrawingUtils.callConvert(outputPath, outputGifPath);
			
			
			String outputSeqPath = graphOutputPath + "/" + count  + ".txt";
			GraphDrawingUtils.outputsequenceFile(graph, outputSeqPath, globalData);
			
		}
		if (rankedGraphs.size()>0)
			return rankedGraphs.get(0);
		else 
			return null;
	}
	
}
