/**
 * 
 */
package storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import config.GlobalConfig;
import utils.Logger;
import datastructure.GlobalData;
import datastructure.Graph;

/**
 * @author Anh
 *
 */

class FastQuery implements Runnable{
	Graph graph;
//	Graph parentGraph;
	ConcurrentHashMap<Integer, Graph[]> h1GraphMapsSimple;
	
	
	public FastQuery(Graph graph, 
//			Graph parentGraph,
			ConcurrentHashMap<Integer, Graph[]> h1GraphMapsSimple) {
		this.graph = graph;
//		this.parentGraph = parentGraph;
		this.h1GraphMapsSimple = h1GraphMapsSimple;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		ArrayList<Graph> allParentGraphs = graph.getAllParentGraphsSimple();
		
		for (Graph parentGraph:allParentGraphs){
			Graph gInDB = searchGraphSimple(parentGraph,h1GraphMapsSimple);
			if (gInDB != null){
				gInDB.addChildGraph(graph);
			}
		}
	}
	
	public Graph searchGraphSimple(Graph queryGraph, ConcurrentHashMap<Integer, Graph[]> h1GraphMapsSimple){
		int h1Val = queryGraph.calcH1();
		if (!h1GraphMapsSimple.containsKey(h1Val)){
			return null;
		}
		else {
			Graph[] graphList =  h1GraphMapsSimple.get(h1Val);
			int loc = getGraphLocationInList(graphList, queryGraph);
			if (loc==-1){
				return null;
			}
			else {
				return graphList[loc];
			}
		}
	}
	
	public int getGraphLocationInList(Graph[] graphList, Graph aGraph){
		for (int i=0; i<graphList.length;i++){
			if (graphList[i].roleEquals(aGraph))
				return i;
		}
		return -1;
	}
}
public class GraphDatabase implements Serializable   {

	private static final long serialVersionUID = -7684875001006068934L;

	/**
	 * 
	 */
	public GlobalData globalData = new GlobalData(); 	
	public HashMap<Integer, Graph[]> h1GraphMaps = new HashMap<>(); 


	public void prune(){
		HashMap<Integer, Graph[]> prunedDB = new HashMap<Integer, Graph[]>();
		for (Integer h1val:h1GraphMaps.keySet()){
			Graph[] tmp = h1GraphMaps.get(h1val);
			ArrayList<Graph> graphs = new ArrayList<Graph>();
			for (Graph graph:tmp){
				if (graph.methodIdxList.length>1){
					graphs.add(graph);
				}
			}
			if (graphs.size()>0){
				prunedDB.put(h1val, graphs.toArray(new Graph[0]));
			}
		}
		h1GraphMaps = prunedDB;
	}
	public void storeThisDatabase(String databasePath){
		try{ 
						
			FileOutputStream fout = new FileOutputStream(databasePath);
			BufferedOutputStream bos = new BufferedOutputStream(fout, 1048576);
			ObjectOutputStream oos = new ObjectOutputStream(bos);   
			oos.writeObject(this);
			oos.close();
			bos.close();
			fout.close();
			
			Logger.log("Finish writing graph database");
		}
		catch(Exception e){
			e.printStackTrace();
			Logger.log("Error writing graph database");
		}
	}

	public static GraphDatabase readGraphDatabase(String databasePath){
		try{
			GraphDatabase graphDB;
			FileInputStream fin = new FileInputStream(databasePath);
			BufferedInputStream bin = new BufferedInputStream(fin, 16777216);
			ObjectInputStream ois = new ObjectInputStream(bin);
			graphDB = (GraphDatabase) ois.readObject();
			ois.close();
			bin.close();
			fin.close();
			return graphDB;

		}catch(Exception ex){
			ex.printStackTrace();
			Logger.log("Error reading graph database");
			return null;
		} 
	} 

	public ArrayList<Graph> getTopMethodGraphsHaveNodeStartWithStr(int topSize, int maxNodes, String str){
		TreeMap<Integer, ArrayList<Graph>> topGraphs = new TreeMap<>();
		for (Integer h1Val:h1GraphMaps.keySet()){
			for (Graph graph: h1GraphMaps.get(h1Val)){
				if (graph.numNodes()>maxNodes)
					continue;
				if (!graph.isANodeStartWithStr(str, this.globalData)){
					continue;
				}
				int mCount = -graph.methodIdxList.length;
				if (topGraphs.size()==0||topGraphs.lastKey()>=mCount){
					if (topGraphs.containsKey(mCount)){
						topGraphs.get(mCount).add(graph);
					}
					else {
						ArrayList<Graph> graphList = new ArrayList<>();
						graphList.add(graph);
						topGraphs.put(mCount, graphList);
					}
				}
				if (topGraphs.size()>topSize){
					topGraphs.remove(topGraphs.lastKey());
				}
			}
		}

		ArrayList<Graph> topGraphList = new ArrayList<>();
		int n =0; 
		for (Integer count:topGraphs.keySet()){
			for (Graph g:topGraphs.get(count)){
				n++;
				topGraphList.add(g);
				if (n>topSize){
					return topGraphList;
				}
			}
		}
		return topGraphList;
	}

	public ArrayList<Graph> getTopGraphsHaveNodeStartWithStr(int topSize, int maxNodes, String str){
		TreeMap<Integer, ArrayList<Graph>> topGraphs = new TreeMap<>();
		for (Integer h1Val:h1GraphMaps.keySet()){
			for (Graph graph: h1GraphMaps.get(h1Val)){
				if (graph.numNodes()>maxNodes)
					continue;
				if (!graph.isANodeStartWithStr(str, this.globalData)){
					continue;
				}
				int mCount = -graph.count;
				if (topGraphs.size()==0||topGraphs.lastKey()>=mCount){
					if (topGraphs.containsKey(mCount)){
						topGraphs.get(mCount).add(graph);
					}
					else {
						ArrayList<Graph> graphList = new ArrayList<>();
						graphList.add(graph);
						topGraphs.put(mCount, graphList);
					}
				}
				if (topGraphs.size()>topSize){
					topGraphs.remove(topGraphs.lastKey());
				}
			}
		}

		ArrayList<Graph> topGraphList = new ArrayList<>();
		int n =0; 
		for (Integer count:topGraphs.keySet()){
			for (Graph g:topGraphs.get(count)){
				n++;
				topGraphList.add(g);
				if (n>topSize){
					return topGraphList;
				}
			}
		}
		return topGraphList;
	}





	public ArrayList<Graph> getTopGraphs(int topSize){
		TreeMap<Integer, ArrayList<Graph>> topGraphs = new TreeMap<>();
		for (Integer h1Val:h1GraphMaps.keySet()){
			for (Graph graph: h1GraphMaps.get(h1Val)){
				int mCount = -graph.count;
				if (topGraphs.size()==0||topGraphs.lastKey()>=mCount){
					if (topGraphs.containsKey(mCount)){
						topGraphs.get(mCount).add(graph);
					}
					else {
						ArrayList<Graph> graphList = new ArrayList<>();
						graphList.add(graph);
						topGraphs.put(mCount, graphList);
					}
				}
				if (topGraphs.size()>topSize){
					topGraphs.remove(topGraphs.lastKey());
				}
			}
		}

		ArrayList<Graph> topGraphList = new ArrayList<>();
		int n =0; 
		for (Integer count:topGraphs.keySet()){
			for (Graph g:topGraphs.get(count)){
				n++;
				topGraphList.add(g);
				if (n>topSize){
					return topGraphList;
				}
			}
		}
		return topGraphList;
	}

	public void buildAllChild(){
		int countGraph = 0;
		for (Integer h1Val:h1GraphMaps.keySet()){
			Graph[] graphArrs = h1GraphMaps.get(h1Val);
			for (Graph graph:graphArrs){
				countGraph++;
				if (countGraph%1000==0){
					System.out.print("g:" + countGraph + ", ");
					if (countGraph%100000==0){
						System.out.println();
					}
				}
				
				ArrayList<Graph> allParentGraphs = graph.getAllParentGraphs();
				for (Graph parentGraph:allParentGraphs){
					Graph gInDB = searchGraph(parentGraph);
					if (gInDB != null){
						gInDB.addChildGraph(graph);
					}
				}
			}
		}
	}

	public void buildAllChildSimple(){
		ConcurrentHashMap<Integer, Graph[]> h1GraphMapsSimple = new ConcurrentHashMap<Integer, Graph[]>();
		int totalGraph = 0;
		for (Integer h1Val:h1GraphMaps.keySet()){
			ArrayList<Graph> repGraph = new ArrayList<Graph>();
			for (Graph graph:h1GraphMaps.get(h1Val)){
				graph.childrenGraphs = null;
				if (graph.methodIdxList.length<=1){
					continue;
				}
				else 
					repGraph.add(graph);
			}
			if (repGraph.size()>0){
				totalGraph += repGraph.size();
				h1GraphMapsSimple.put(h1Val, repGraph.toArray(new Graph[0]));
			}
		}
		Logger.log("totalGraph rep: " + totalGraph);
		int countGraph = 0;
		TreeMap<Integer, ArrayList<Graph>> sizeGraphsMap = new TreeMap<Integer, ArrayList<Graph>>();
		for (Integer h1Val:h1GraphMapsSimple.keySet()){
			Graph[] graphArrs = h1GraphMapsSimple.get(h1Val);
			for (Graph graph:graphArrs){				
				int size = graph.numNodes();
				if (sizeGraphsMap.containsKey(size)){
					sizeGraphsMap.get(size).add(graph);
				}
				else {
					ArrayList<Graph> graphs = new ArrayList<Graph>();
					graphs.add(graph);
					sizeGraphsMap.put(size, graphs);
				}
			}	
		}

		int numThreads = 16;
		ArrayList<Graph> executedGraphs = new ArrayList<Graph>();
		for (Integer size:sizeGraphsMap.keySet()){
			for (Graph graph:sizeGraphsMap.get(size)){
				
				countGraph++;
				if (countGraph%1000==0){
					System.out.print("g:" + countGraph + ", ");
					if (countGraph%50000==0){
						System.out.println();
					}
				}
				if (size == 1){
					continue;
				}
				else if (size<=10){
					ArrayList<Graph> allParentGraphs = graph.getAllParentGraphsSimple();
					
					for (Graph parentGraph:allParentGraphs){
						Graph gInDB = searchGraphSimple(parentGraph,h1GraphMaps);
						if (gInDB != null){
							gInDB.addChildGraph(graph);
						}
					}
					continue;
				}
				executedGraphs.add(graph);
				
				if (executedGraphs.size()==numThreads){
					ExecutorService executor = Executors.newFixedThreadPool(numThreads);
					for(Graph gr:executedGraphs){
						executor.execute(new FastQuery(gr, h1GraphMapsSimple));
					}
					executor.shutdown();
					try {
						executor.awaitTermination(1000, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					executedGraphs.clear();
				}
			}
		}
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for(Graph gr:executedGraphs){
			executor.execute(new FastQuery(gr, h1GraphMapsSimple));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Logger.log(System.lineSeparator() + "Finished building parent-child relationship");
//		for (Integer h1Val:h1GraphMapsSimple.keySet()){
//			Graph[] graphArrs = h1GraphMapsSimple.get(h1Val);
//			ExecutorService executor = Executors.newFixedThreadPool(8);
//			for (Graph graph:graphArrs){				
//				countGraph++;
//				if (countGraph%1000==0){
//					System.out.print("g:" + countGraph + ", ");
//					if (countGraph%100000==0){
//						System.out.println();
//					}
//				}
//				
////				for (Graph parentGraph:allParentGraphs){
////					executor.execute(new FastQuery(graph, parentGraph, h1GraphMapsSimple));
////				}
//				executor.execute(new FastQuery(graph, h1GraphMapsSimple));
//
//				
//			}
//			executor.shutdown();
//			try {
//				executor.awaitTermination(1000, TimeUnit.SECONDS);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	public  synchronized  Graph searchGraph(Graph queryGraph){
		int h1Val = queryGraph.calcH1();
		if (!h1GraphMaps.containsKey(h1Val)){
			return null;
		}
		else {
			Graph[] graphList =  h1GraphMaps.get(h1Val);
			int loc = getGraphLocationInList(graphList, queryGraph);
			if (loc==-1){
				return null;
			}
			else {
				return graphList[loc];
			}
		}
	}

	public Graph searchGraphSimple(Graph queryGraph, HashMap<Integer, Graph[]> h1GraphMapsSimple){
		int h1Val = queryGraph.calcH1();
		if (!h1GraphMapsSimple.containsKey(h1Val)){
			return null;
		}
		else {
			Graph[] graphList =  h1GraphMapsSimple.get(h1Val);
			int loc = getGraphLocationInList(graphList, queryGraph);
			if (loc==-1){
				return null;
			}
			else {
				return graphList[loc];
			}
		}
	}

	public Graph[]  searchChildGraphs(Graph queryGraph){
		Graph g = searchGraph(queryGraph);

		if (g == null)
			return null;
		else
			return g.childrenGraphs;
	}

	public Graph searchGraphSimple(Graph queryGraph, ConcurrentHashMap<Integer, Graph[]> h1GraphMapsSimple){
		int h1Val = queryGraph.calcH1();
		if (!h1GraphMapsSimple.containsKey(h1Val)){
			return null;
		}
		else {
			Graph[] graphList =  h1GraphMapsSimple.get(h1Val);
			int loc = getGraphLocationInList(graphList, queryGraph);
			if (loc==-1){
				return null;
			}
			else {
				return graphList[loc];
			}
		}
	}

	public int getCountGraphPair(Graph queryGraph1, Graph queryGraph2){
		Graph g1 = searchGraph(queryGraph1);
		if (g1 == null){
			return 0;
		}
		Graph g2 = searchGraph(queryGraph2);
		if (g2 == null){
			return 0;
		}

		int[] mIdxList1 = g1.methodIdxList;

		int[] mIdxList2 = g2.methodIdxList;

		int count =0;
		for (int idx1:mIdxList1){
			for (int idx2:mIdxList2){
				if (idx1==idx2)
					count++;
				break;
			}
		}
		return count;
	}



	public boolean addGraphWithOtherData(Graph aGraph, int count, String projectName, String methodName){
		aGraph.count = count;
		aGraph.addProject(projectName, globalData);
		aGraph.addMethod(methodName, globalData);
		return addGraph(aGraph);
	}

	public boolean addGraph(Graph aGraph){
		boolean isAdded = false;
		int h1Val = aGraph.calcH1();
		if(!h1GraphMaps.containsKey(h1Val)){
			Graph[] tmpList = new Graph[1];
			tmpList[0] = aGraph;
			h1GraphMaps.put(h1Val, tmpList);
			isAdded = true;
		}
		else{
			Graph[] tmpList = h1GraphMaps.get(h1Val);
			Graph[] newList = addGraphToList(h1GraphMaps.get(h1Val), aGraph);
			isAdded = (tmpList != newList);
			h1GraphMaps.put(h1Val, newList);
		}
		return isAdded;
	}

	public Graph[] addGraphToList(Graph[] graphList, Graph aGraph){
		int loc = getGraphLocationInList(graphList, aGraph);
		int len = graphList.length;
		if (loc == -1){
			Graph[] newGraphList = new Graph[len+1];
			for (int i=0; i<len; i++){
				newGraphList[i] = graphList[i];
			}
			newGraphList[len] = aGraph;
			return newGraphList;
		}
		else {
			Graph g = graphList[loc];
			g.count += aGraph.count;
			for (int projectIdx:aGraph.projectIdxList){
				g.addProjectIdx(projectIdx);
			}

			for (int methodIdx:aGraph.methodIdxList){
				g.addMethodIdx(methodIdx);
			}

			return graphList;
		}

	}

	
	public int getGraphLocationInList(Graph[] graphList, Graph aGraph){
		for (int i=0; i<graphList.length;i++){
			if (graphList[i].roleEquals(aGraph))
				return i;
		}
		return -1;
	}

	public void doStatistics(){
		Logger.log("****************************\r\nDatabase Statistics: ");
		globalData.doStatistics();
		long numGraphs = countGraphs();
		Logger.log("Number of Graphs: " + numGraphs);
		
		long numRepGraphs = countRepGraphs();
		Logger.log("Number of rep Graphs: " + numRepGraphs);

//		Logger.log("Top Graphs: ");
//		ArrayList<Graph> topGraphList = getTopGraphs(GlobalConfig.topGraphSize);
//		for (Graph graph:topGraphList){
//			Logger.log("    " + graph.count + "::" + graph.toString());
//		}
//
//
//		Logger.log("Top size-1 Graphs contains " + GlobalConfig.containStr + " :");
//		ArrayList<Graph> topSpecGraphList1 = getTopGraphsHaveNodeStartWithStr(GlobalConfig.topGraphSize, 1, GlobalConfig.containStr);
//		for (Graph graph:topSpecGraphList1){
//			Logger.log("    " + graph.count + "::" + graph.toString());
//		}
//
//		Logger.log("Top size-2 Graphs contains " + GlobalConfig.containStr + " :");
//		ArrayList<Graph> topSpecGraphList2 = getTopGraphsHaveNodeStartWithStr(GlobalConfig.topGraphSize, 2, GlobalConfig.containStr);
//		for (Graph graph:topSpecGraphList2){
//			Logger.log("    " + graph.count + "::" + graph.toString());
//		}
//
//		Logger.log("Top size-3 Graphs contains " + GlobalConfig.containStr + " :");
//		ArrayList<Graph> topSpecGraphList3 = getTopGraphsHaveNodeStartWithStr(GlobalConfig.topGraphSize, 3, GlobalConfig.containStr);
//		for (Graph graph:topSpecGraphList3){
//			Logger.log("    " + graph.count + "::" + graph.toString());
//		}

		//		
		//		Logger.log("Top size-5 Graphs contains " + GlobalConfig.containStr + " :");
		//		ArrayList<Graph> topSpecGraphList5 = getTopGraphsHaveNodeStartWithStr(GlobalConfig.topGraphSize, 5, GlobalConfig.containStr);
		//		for (Graph graph:topSpecGraphList5){
		//			Logger.log("    " + graph.count + "::" + graph.toString());
		//		}
		//		


		Logger.log("Top  Graphs contains " + GlobalConfig.containStr + " (according to #methods containing them):");
		ArrayList<Graph> topMethodSpecGraphList = getTopMethodGraphsHaveNodeStartWithStr(GlobalConfig.topGraphSize, GlobalConfig.maxGraphSize, GlobalConfig.containStr);
		for (Graph graph:topMethodSpecGraphList){
			Logger.log("  #methods:  " + graph.methodIdxList.length + "::" + graph.toString());
		}
	}

	public long countGraphs(){
		long numGraphs = 0;
		for (Integer h1Val:h1GraphMaps.keySet()){
			numGraphs += h1GraphMaps.get(h1Val).length;
		}
		return numGraphs;
	}
	public long countRepGraphs(){
		long numGraphs = 0;
		for (Integer h1Val:h1GraphMaps.keySet()){
			for (Graph gr:h1GraphMaps.get(h1Val))
				if(gr.methodIdxList.length>1)
					numGraphs ++;
		}
		return numGraphs;
	}
}
