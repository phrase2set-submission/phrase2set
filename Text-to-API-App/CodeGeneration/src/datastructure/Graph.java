/**
 * 
 */
package datastructure;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import config.GlobalConfig;
import algorithm.Ullman;
import edu.uci.ics.jung.graph.DirectedGraph;
import storage.GraphDatabase;
import utils.Combination;
import utils.Logger;

/**
 * @author Anh
 *
 */
public class Graph implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6557897223331929283L;


	public Node[] nodes;
	public Edge[] edges;
	public int count = 0;	
	public int hashValue1 = -1;

	public Graph[] parentsGraphs;
	public Graph[] childrenGraphs;

	public int[] projectIdxList = new int[0];
	public int[] methodIdxList = new int[0];


	public Graph(Node[] nodes, Edge[]edges, int count){
		this.nodes = nodes;
		this.edges = edges;
		this.count = count;
		this.hashValue1 = calcH1();
	}


	public Graph(Graph oldGraph, Node addedNode, Edge[] addedEdges, int count){
		int len = oldGraph.nodes.length;
		Node[] tmpNodes = new Node[len+1];
		for(int i=0; i<len; i++){
			tmpNodes[i] = oldGraph.nodes[i];
		}
		tmpNodes[len] = addedNode; 
		this.nodes = tmpNodes;

		int lenEdge = oldGraph.nodes.length;
		int lenAddedEdge = addedEdges.length;
		Edge[] tmpEdges = new Edge[len+lenAddedEdge];
		tmpEdges = Arrays.copyOf(oldGraph.edges, lenEdge + lenAddedEdge);
		for(int i=0;i<lenAddedEdge;i++){
			tmpEdges[len+i] = addedEdges[i]; 
		}
		this.nodes = tmpNodes;
		this.edges = tmpEdges;
		this.count = count;
		this.hashValue1 = calcH1();
	}

	public Graph(ArrayList<Node> nodeList, ArrayList<Edge> edgeList, int count){
		this.nodes = nodeList.toArray(new Node[0]);
		this.edges = edgeList.toArray(new Edge[0]);
		this.count = count;
		this.hashValue1 = calcH1();
	}

	public boolean isANodeStartWithStr(String str, GlobalData globalData){
		for (Node node:nodes){
			if (node.getNodeLabel(globalData).startsWith(str)){
				return true;
			}
		}
		return false;
	}

	public synchronized void removeRedundancy(){
		ArrayList<Edge> newEdges = new ArrayList<Edge>();

		for(Edge edge:edges){
			if (edge==null||edge.sinkNode==null||edge.sourceNode==null)
				continue;
			boolean isFound=false;
			for(Edge nEdge:newEdges){
				if (nEdge==null)
					continue;
				if (edge ==null)
					continue;
				if(nEdge.sinkNode==edge.sinkNode&&nEdge.sourceNode==edge.sourceNode){
					isFound=true;
					break;
				}
			}
			if(!isFound){
				newEdges.add(edge);
			}
		}

		edges = newEdges.toArray(new Edge[0]);
		ArrayList<Node> newNodes = new ArrayList<Node>();
		for(Node node:nodes){
			if (node!=null){

				newNodes.add(node);
			}
		}
		nodes = newNodes.toArray(new Node[0]);
	}

	public void addProject(String projectName, GlobalData globalData){
		int projectIdx = globalData.projectDict.addLabelgetIdx(projectName);
		addProjectIdx(projectIdx);;

	}


	public void addProjectIdx(int projectIdx){
		//If should insert 
		int oldLen = projectIdxList.length;

		if (oldLen==0){
			projectIdxList = new int[1];
			projectIdxList[0] =projectIdx;
		}
		else if (projectIdxList[oldLen-1]<projectIdx){
			int[] newProjectIdxList = new int[oldLen+1];
			System.arraycopy(projectIdxList, 0, newProjectIdxList, 0, oldLen);
			newProjectIdxList[oldLen] = projectIdx;

			projectIdxList = newProjectIdxList;
		}
		else {
			int loc = Arrays.binarySearch(projectIdxList, projectIdx);

			if (loc<0){
				int insertPoint = -loc -1; 
				int[] newProjectIdxList = new int[oldLen+1];
				System.arraycopy(projectIdxList, 0, newProjectIdxList, 0, insertPoint);
				newProjectIdxList[insertPoint] = projectIdx;
				for (int i=insertPoint; i<oldLen;i++){
					newProjectIdxList[i+1] = projectIdxList[i];
				}
				//			projectIdxList = Arrays.copyOf(newProjectIdxList, oldLen+1);
				projectIdxList = newProjectIdxList;

			}
		}

	}

	public void addMethod(String methodName, GlobalData globalData){
		int methodIdx = globalData.methodDict.addLabelgetIdx(methodName);
		addMethodIdx(methodIdx);
	}

	public void addMethodIdx(int methodIdx){

		//		Logger.log("methodIdx: " + methodIdx);
		//		Logger.log("methodIdxList: " + Arrays.toString(methodIdxList));

		int oldLen = methodIdxList.length;
		if (oldLen==0){
			methodIdxList = new int[1];
			methodIdxList[0] =methodIdx;
		}
		else if (methodIdxList[oldLen-1]<methodIdx){
			int[] newMethodIdxList = new int[oldLen+1];

			System.arraycopy(methodIdxList, 0, newMethodIdxList, 0, oldLen);
			newMethodIdxList[oldLen] = methodIdx;
			methodIdxList =newMethodIdxList;

		}
		else {
			int loc = Arrays.binarySearch(methodIdxList, methodIdx);
			//		Logger.log("loc: " + loc);

			//If should insert 
			if (loc<0){
				int insertPoint = (-loc) -1; 
				//			Logger.log("insertPoint: " + insertPoint);


				//			Logger.log("oldLen: " + oldLen);

				int[] newMethodIdxList = new int[oldLen+1];
				//			for (int i=0; i<insertPoint;i++){
				//				newMethodIdxList[i] = methodIdxList[i];
				//			}
				System.arraycopy(methodIdxList, 0, newMethodIdxList, 0, insertPoint);
				newMethodIdxList[insertPoint] = methodIdx;
				for (int i=insertPoint; i<oldLen;i++){
					newMethodIdxList[i+1] = methodIdxList[i];
				}

				methodIdxList =newMethodIdxList;
				//			Logger.log("newMethodIdxList: " + Arrays.toString(methodIdxList));

			}
		}
	}


	public void addChildGraph(Graph childGraph){
		int len = 0;
		if (childrenGraphs!=null)
			len = childrenGraphs.length;
		Graph[] newChildrenGraphs = new Graph[len+1];
		for (int i=0; i<len;i++){
			newChildrenGraphs[i] = childrenGraphs[i];
		}
		newChildrenGraphs[len] = childGraph;
		childrenGraphs = newChildrenGraphs;
	}



	public ArrayList<Graph> getAllAncestorGraphs(int maxGraphSize){
		ArrayList<Graph> allParentGraphs = new ArrayList<>();
		for (Node node:nodes){
			Graph aParent = getAParent(node);
			if (aParent.isConnectedGraph()){
				if (aParent.numNodes()<=maxGraphSize)
					allParentGraphs.add(aParent);
				if (aParent.numNodes()>1){
					allParentGraphs.addAll(aParent.getAllAncestorGraphs(maxGraphSize));
				}
			}
		}
		return allParentGraphs;
	}


	public ArrayList<Graph> getAllParentGraphs(){
		ArrayList<Graph> allParentGraphs = new ArrayList<>();
		HashMap<Node, ArrayList<Edge>> nodeEdgesMap = new HashMap<Node, ArrayList<Edge>>();
		for (Edge edge:edges){
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (nodeEdgesMap.containsKey(source)) {
				nodeEdgesMap.get(source).add(edge);
			}
			else {
				ArrayList<Edge> edges = new ArrayList<Edge>();
				edges.add(edge);
				nodeEdgesMap.put(source, edges);
			}

			if (nodeEdgesMap.containsKey(sink)) {
				nodeEdgesMap.get(sink).add(edge);
			}
			else {
				ArrayList<Edge> edges = new ArrayList<Edge>();
				edges.add(edge);
				nodeEdgesMap.put(sink, edges);
			}
		}
		for (Node node:nodes){
			Graph aParent = getAParent(node);
			if (aParent.isConnectedGraph()){
				allParentGraphs.add(aParent);
			}
		}
		return allParentGraphs;
	}


	public ArrayList<Graph> getAllParentGraphsSimple(){
		ArrayList<Graph> allParentGraphs = new ArrayList<>();
		HashMap<Node, ArrayList<Edge>> nodeEdgesMap = new HashMap<Node, ArrayList<Edge>>();
		for (Edge edge:edges){
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (nodeEdgesMap.containsKey(source)) {
				nodeEdgesMap.get(source).add(edge);
			}
			else {
				ArrayList<Edge> edges = new ArrayList<Edge>();
				edges.add(edge);
				nodeEdgesMap.put(source, edges);
			}

			if (nodeEdgesMap.containsKey(sink)) {
				nodeEdgesMap.get(sink).add(edge);
			}
			else {
				ArrayList<Edge> edges = new ArrayList<Edge>();
				edges.add(edge);
				nodeEdgesMap.put(sink, edges);
			}
		}
		for (Node node:nodes){
			Graph aParent = getAParent(node);
			if (aParent.isConnectedGraph()){
				allParentGraphs.add(aParent);
			}
		}
		return allParentGraphs;
	}

	public Graph getAParent(Node removedNode, HashMap<Node, ArrayList<Edge>> nodeEdgesMap){
		ArrayList<Node> newNodes = new ArrayList<>();
		newNodes.addAll(Arrays.asList(nodes));
		newNodes.remove(removedNode);
		ArrayList<Edge> newEdges = new ArrayList<>();
		newEdges.addAll(Arrays.asList(edges));
		newEdges.removeAll(nodeEdgesMap.get(removedNode));
		return new Graph(newNodes, newEdges, count);
	}

	public Graph getAParent(Node removedNode){
		ArrayList<Node> newNodes = new ArrayList<>();
		for (Node node:nodes){
			if (node!=removedNode){
				newNodes.add(node);
			}
		}
		ArrayList<Edge> newEdges = new ArrayList<>();
		for (Edge edge:edges){
			if ((edge.sinkNode != removedNode)&&(edge.sourceNode!=removedNode)){
				newEdges.add(edge);
			}
		}
		return new Graph(newNodes, newEdges, count);
	}



	public  synchronized  boolean isConnectedGraph(){

		return isConnectedGraph(nodes, edges);
	}

	public  synchronized  boolean isConnectedGraph(Node[] nodes, Edge[] edges){
		if (nodes.length<=0){
			return false;
		}
		ArrayList<Node> spanTreeNodes = getSpanTreeNodes(nodes, edges);
		if (spanTreeNodes.size() != nodes.length){
			return false;
		}
		return true;
	}

	public ArrayList<Node> getSpanTreeNodes(Node[] nodes, Edge[] edges){
		ArrayList<Node> spanTreeNodes = new ArrayList<>();

		//Get all nodes, with each node N, show the nodes having edges with N  
		HashMap<Node, HashSet<Node>> nodeLinkNodeMap = new HashMap<>();
		for (Edge edge:edges){
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

		//Build spantreeNodes
		spanTreeNodes.add(nodes[0]);
		int lastIdx = 0;
		while(true){
			int tmpIdx = spanTreeNodes.size();
			for (int i=lastIdx; i<spanTreeNodes.size(); i++ ){
				Node tmpNode = spanTreeNodes.get(i);
				HashSet<Node> linkNodes = nodeLinkNodeMap.get(tmpNode);
				if (linkNodes==null)
					continue;
				for (Node linkNode:linkNodes){
					if (!spanTreeNodes.contains(linkNode)){
						spanTreeNodes.add(linkNode);
					}
				}
			}
			if (tmpIdx == spanTreeNodes.size()){
				break;
			}
			lastIdx = tmpIdx;
		}
		return spanTreeNodes;
	}




	public int calcH1(){
		return (int)calcNodeHash()*65535 + (int)calcEdgeHash();
	}

	public int calcH2(){
		int tmp = 0;
		for (Node node:nodes){
			if (node==null){
				continue;
			}
			tmp += node.hashCode();
		}
		return tmp;
	}


	public short calcNodeHash(){
		short sum = 0;
		sum += nodes.length*2047;
		for (Node node:nodes){
			if (node != null)
				sum+= node.content;
		}
		return sum;
	}


	public short calcEdgeHash(){
		short sum = 0;
		sum += edges.length*2047;
		for (Edge edge:edges){
			if (edge == null)
				continue;
			if (edge.sourceNode==null||edge.sinkNode==null)
				continue;
			int s1 = edge.sourceNode.content*63;
			int s2 = edge.sinkNode.content;
			sum += s1+s2;
		}
		return sum;
	}


	public boolean roleEquals(Graph otherGraph){
		if (nodes.length != otherGraph.nodes.length)
			return false;
		if (!isNodeListsEqual(nodes, otherGraph.nodes))
			return false;
		if (edges.length != otherGraph.edges.length)
			return false;
		if (!isEdgeListsEqual(edges, otherGraph.edges))
			return false;

		try{
			final DirectedGraph thisG = GraphUtils.createGraph(this);
			final DirectedGraph otherG = GraphUtils.createGraph(otherGraph);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Boolean> handler = executor.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return Ullman.areIsomorph(otherG, thisG);
				}
			});
			executor.shutdown();
			boolean isIso = false;
			try{
				isIso =  handler.get(GlobalConfig.timeout,TimeUnit.SECONDS);
			}
			catch(Exception e){
				handler.cancel(true);
			}
			return isIso;

		}
		catch(Exception e){
			return true;
		}
	}


	public synchronized boolean simpleRoleEquals(Graph otherGraph){
		if (nodes.length != otherGraph.nodes.length)
			return false;
		if (!isNodeListsEqual(nodes, otherGraph.nodes))
			return false;
		if (edges.length != otherGraph.edges.length)
			return false;
		if (!isEdgeListsEqualSimple(edges, otherGraph.edges))
			return false;
		return true;
	}

	public boolean isNodeListsEqual(Node[] nodes1, Node[] nodes2){
		int[] nodeIdxs1 = new int[nodes1.length];
		for (int i= 0; i<nodes1.length;i++){
			if (nodes1[i]==null)
				continue;
			nodeIdxs1[i] = nodes1[i].content;
		}
		Arrays.sort(nodeIdxs1);

		int[] nodeIdxs2 = new int[nodes2.length];
		for (int i=0; i<nodes2.length;i++){
			if (nodes2[i]==null)
				continue;
			nodeIdxs2[i] = nodes2[i].content;
		}
		Arrays.sort(nodeIdxs2);

		return Arrays.equals(nodeIdxs1, nodeIdxs2);
	}
	public synchronized boolean isEdgeListsEqualSimple(Edge[] edges1, Edge[] edges2){
		for (int i=0; i<edges1.length; i++){
			if (edges1[i]==null && edges2[i]!=null || (edges1[i]!=null && edges2[i]==null)){
				return false;
			}
			else if (edges1[i]==null && edges2[i]==null){
				continue;
			}
			else if (edges1[i].sourceNode==null&&edges2[i].sourceNode!=null
					||edges1[i].sourceNode!=null&&edges2[i].sourceNode==null){
				return false;
			}
			else if (edges1[i].sourceNode==null&&edges2[i].sourceNode==null){
				continue;
			}
			else if (edges1[i].sinkNode==null&&edges2[i].sinkNode!=null
					||edges1[i].sinkNode!=null&&edges2[i].sinkNode==null){
				return false;
			}
			else if (edges1[i].sinkNode==null&&edges2[i].sinkNode==null){
				continue;
			}
			else if (edges1[i].sourceNode.content!=edges2[i].sourceNode.content){
				return false;
			}
			else if (edges1[i].sinkNode.content!=edges2[i].sinkNode.content){
				return false;
			}
		}
		return true;//Arrays.equals(edgeRepVals1, edgeRepVals2);
	}

	public synchronized boolean isEdgeListsEqual(Edge[] edges1, Edge[] edges2){
		//		long[] edgeRepVals1 = new long[edges1.length];
		//		for (int i=0; i<edges1.length;i++){
		//			long repVal = edges1[i].getRepValue();
		//			edgeRepVals1[i] = repVal;
		//		}
		//		Arrays.sort(edgeRepVals1);
		//
		//		long[] edgeRepVals2 = new long[edges2.length];
		//		for (int i=0; i<edges2.length; i++){
		//			long repVal = edges2[i].getRepValue();
		//			edgeRepVals2[i] = repVal;
		//		}
		//		Arrays.sort(edgeRepVals2);
		//
		//		return Arrays.equals(edgeRepVals1, edgeRepVals2);
		for (int i=0; i<edges1.length; i++){
			if (edges1[i]==null && edges2[i]!=null || (edges1[i]!=null && edges2[i]==null)){
				return false;
			}
			else if (edges1[i]==null && edges2[i]==null){
				continue;
			}
			else if (edges1[i].sourceNode==null&&edges2[i].sourceNode!=null
					||edges1[i].sourceNode!=null&&edges2[i].sourceNode==null){
				return false;
			}
			else if (edges1[i].sourceNode==null&&edges2[i].sourceNode==null){
				continue;
			}
			else if (edges1[i].sinkNode==null&&edges2[i].sinkNode!=null
					||edges1[i].sinkNode!=null&&edges2[i].sinkNode==null){
				return false;
			}
			else if (edges1[i].sinkNode==null&&edges2[i].sinkNode==null){
				continue;
			}
			else if (edges1[i].sourceNode.content!=edges2[i].sourceNode.content){
				return false;
			}
			else if (edges1[i].sinkNode.content!=edges2[i].sinkNode.content){
				return false;
			}
		}
		return true;//Arrays.equals(edgeRepVals1, edgeRepVals2);
	}




	public boolean memEquals(Graph otherGraph){
		if (nodes == null)
			return false;
		if (otherGraph == null)
			return false;
		if (otherGraph.nodes == null)
			return false;
		if (nodes.length != otherGraph.nodes.length)
			return false;
		if (!isMemNodeListsEqual(nodes, otherGraph.nodes))
			return false;


		return true;
	}

	private boolean isMemNodeListsEqual(Node[] nodes1, Node[] nodes2){

		int count = 0;
		for (Node node1:nodes1){
			for (Node node2:nodes2){
				if (node1 == node2){
					count++;
					continue;
				}
			}
		}

		if (count==nodes1.length)
			return true;

		return false;
	}




	public static ArrayList<int[]> genMaskArrs(int size, int length){
		if (size>length)
			return null;
		ArrayList<ArrayList<Integer>> allMasks = fillMask(length+1, size);
		//		Logger.log("masks: " );

		ArrayList<int[]> allMaskArrs = new ArrayList<>();
		for (ArrayList<Integer> mask:allMasks){
			int[] maskArr = new int[length+1];
			for (int i=0; i<mask.size();i++){
				maskArr[i] = mask.get(i);
			}
			allMaskArrs.add(maskArr);
		}

		//		for (int[] maskArr:allMaskArrs){
		//			printMask(maskArr);
		//		}

		return allMaskArrs;
	}


	public static ArrayList<ArrayList<Integer>> fillMask(int length, int size){
		ArrayList<ArrayList<Integer>> allMasks = new ArrayList<>();
		for (int i=0; i<length-size; i++){

			ArrayList<Integer> mask = new ArrayList<>();
			for (int j=0; j<i;j++){
				mask.add(0); 
			}
			mask.add(1);
			//			Logger.log(mask);

			int newLength = length - i-1;
			//			Logger.log("length: " + length + "    size:" + size  + "     newLength: " + newLength);

			ArrayList<ArrayList<Integer>> tmpMasks = new ArrayList<>();

			if (newLength>=size && size >1){
				//				Logger.log("fillMask: " + newLength + "\\ " + (size-1));
				ArrayList<ArrayList<Integer>> remainMasks = fillMask(newLength, size-1);
				//				Logger.log("remainMasks: " + remainMasks);

				for (ArrayList<Integer> remainMask:remainMasks ){
					//					mask.addAll(remainMask);
					//					allMasks.add(mask);
					ArrayList<Integer> tmpMask = new ArrayList<>();
					tmpMask.addAll(mask);
					tmpMask.addAll(remainMask);
					tmpMasks.add(tmpMask);
				}

			}
			else {
				tmpMasks.add(mask);
			}


			allMasks.addAll(tmpMasks);

			//			Logger.log(mask);
		}
		return allMasks;
	}

	public static void printMask(int[] mask){
		for (int i=0; i<mask.length;i++){
			System.out.print(mask[i] + ", ");
		}
		System.out.println();
	}


	public int numNodes(){
		return nodes.length;
	}

	public ArrayList<Graph> getAllSubGraphs(int maxGraphSize, int maxCountNode, GlobalData globalData){
		ArrayList<Graph> subGraphs = new ArrayList<>();

		LinkedHashMap<Node, LinkedHashSet<Node>> nodeLinkNodeMap = new LinkedHashMap<>();
		LinkedHashMap<Node, LinkedHashSet<Edge>> nodeLinkEdgeMap = new LinkedHashMap<>();

		Node.globalData = globalData;

		for (Edge edge:edges){
			if (edge == null)
				continue;
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (!nodeLinkNodeMap.containsKey(source)){
				nodeLinkNodeMap.put(source, new LinkedHashSet<Node>());
			}
			nodeLinkNodeMap.get(source).add(sink);
			if (!nodeLinkEdgeMap.containsKey(source)){
				nodeLinkEdgeMap.put(source, new LinkedHashSet<Edge>());
			}
			nodeLinkEdgeMap.get(source).add(edge);
		}

		LinkedHashMap<Integer, ArrayList<Graph>> hash2GraphMap = new LinkedHashMap<>();
		for (Node node:nodes){
			TreeMap<Integer, LinkedHashSet<Node>> neighborNodes = getNeighborNodes(node, maxGraphSize, maxCountNode, nodeLinkNodeMap);
			ArrayList<Node> allNodesTmp = new ArrayList<>();
			for (Integer i:neighborNodes.keySet()){
				allNodesTmp.addAll(neighborNodes.get(i));
			}

			ArrayList<Node> allNodes = new ArrayList<>();
			if (allNodesTmp.size()>maxCountNode){
				allNodes.addAll(allNodesTmp.subList(0, maxCountNode));
			}
			else {
				allNodes.addAll(allNodesTmp);
			}
			ArrayList<int[]> allMasks = new ArrayList<>();

			for (int i=1; i<=maxGraphSize; i++){
				Combination comb = new Combination();

				int[] arr = new int[allNodes.size()];
				for (int j=0; j<allNodes.size(); j++){
					arr[j] = j;
				}
				comb.combinations2(arr, i, 0, new int[i]);
				allMasks.addAll(comb.outList);
			}


			for (int[] mask:allMasks){
				ArrayList<Node> nodes = new ArrayList<>();
				for (int point:mask){
					try{
						nodes.add(allNodes.get(point));
					}
					catch(Exception e){
						e.printStackTrace();
						Logger.log("allNodes.size: " + allNodes.size());
						Logger.log(Arrays.toString(mask));
						System.exit(0);
					}
				}

				Graph g = createCompleteSubGraph(nodes, nodeLinkEdgeMap);
				if (g==null)
					continue;
				int hash2Val = g.calcH2();
				if (!hash2GraphMap.containsKey(hash2Val)){
					ArrayList<Graph> graphList = new ArrayList<>();
					graphList.add(g);
					hash2GraphMap.put(hash2Val, graphList);

				}
				else {
					ArrayList<Graph> graphList = hash2GraphMap.get(hash2Val);
					if (!isHaveGraph(graphList, g)){
						graphList.add(g);
					}
				}

			}



		}
		for (Integer hashVal:hash2GraphMap.keySet()){
			subGraphs.addAll(hash2GraphMap.get(hashVal));
		}
		return subGraphs;
	}


	//	public ArrayList<Graph> getAllSubGraphSimple(int maxGraphSize, int maxCountNode, GlobalData globalData){
	//		ArrayList<Graph> subGraphs = new ArrayList<>();
	//
	//		HashMap<Node, HashSet<Node>> nodeLinkNodeMap = new HashMap<>();
	//		HashMap<Node, HashSet<Edge>> nodeLinkEdgeMap = new HashMap<>();
	//
	//		Node.globalData = globalData;
	//
	//		for (Edge edge:edges){
	//			if (edge == null)
	//				continue;
	//			Node source = edge.sourceNode;
	//			Node sink = edge.sinkNode;
	//			if (!nodeLinkNodeMap.containsKey(source)){
	//				nodeLinkNodeMap.put(source, new HashSet<Node>());
	//			}
	//			nodeLinkNodeMap.get(source).add(sink);
	//			if (!nodeLinkEdgeMap.containsKey(source)){
	//				nodeLinkEdgeMap.put(source, new HashSet<Edge>());
	//			}
	//			nodeLinkEdgeMap.get(source).add(edge);
	//		}
	//		
	//		HashMap<Integer, ArrayList<Graph>> hash2GraphMap = new HashMap<>();
	//		for (Node node:nodes){
	//			TreeMap<Integer, HashSet<Node>> neighborNodes = getNeighborNodes(node, maxGraphSize, maxCountNode, nodeLinkNodeMap);
	//			ArrayList<Node> allNodesTmp = new ArrayList<>();
	//			for (Integer i:neighborNodes.keySet()){
	//				allNodesTmp.addAll(neighborNodes.get(i));
	//			}
	//			
	//			ArrayList<Node> allNodes = new ArrayList<>();
	//			if (allNodesTmp.size()>maxCountNode){
	//				allNodes.addAll(allNodesTmp.subList(0, maxCountNode));
	//			}
	//			else {
	//				allNodes.addAll(allNodesTmp);
	//			}
	//			ArrayList<int[]> allMasks = new ArrayList<>();
	//
	//			for (int i=1; i<=maxGraphSize; i++){
	//		    	Combination comb = new Combination();
	//
	//				int[] arr = new int[allNodes.size()];
	//				for (int j=0; j<allNodes.size(); j++){
	//		        	arr[j] = j;
	//		        }
	//				comb.combinations2(arr, i, 0, new int[i]);
	//		        allMasks.addAll(comb.outList);
	//			}
	//			
	//
	//			for (int[] mask:allMasks){
	//				ArrayList<Node> nodes = new ArrayList<>();
	//				for (int point:mask){
	//					try{
	//						nodes.add(allNodes.get(point));
	//					}
	//					catch(Exception e){
	//						e.printStackTrace();
	//						Logger.log("allNodes.size: " + allNodes.size());
	//						Logger.log(Arrays.toString(mask));
	//						System.exit(0);
	//					}
	//				}
	//				
	//				Graph g = createCompleteSubGraphSimple(nodes, nodeLinkEdgeMap);
	//				if (g==null)
	//					continue;
	//				int hash2Val = g.calcH2();
	//				if (!hash2GraphMap.containsKey(hash2Val)){
	//					ArrayList<Graph> graphList = new ArrayList<>();
	//					graphList.add(g);
	//					hash2GraphMap.put(hash2Val, graphList);
	//					
	//				}
	//				else {
	//					ArrayList<Graph> graphList = hash2GraphMap.get(hash2Val);
	//					if (!isHaveGraph(graphList, g)){
	//						graphList.add(g);
	//					}
	//				}
	//				
	//			}
	//
	//
	//			
	//		}
	//		for (Integer hashVal:hash2GraphMap.keySet()){
	//			subGraphs.addAll(hash2GraphMap.get(hashVal));
	//		}
	//		return subGraphs;
	//	}
	//	
	//
	//	public Graph createCompleteSubGraphSimple(ArrayList<Node> nodes, HashMap<Node, HashSet<Edge>> nodeLinkEdgeMap){
	//		ArrayList<Edge> allEdges = new ArrayList<>();
	//		for (Node node:nodes){
	//			HashSet<Edge> linkEdges = nodeLinkEdgeMap.get(node);
	//			if (linkEdges == null)
	//				continue;
	//			for (Edge edge:linkEdges){
	//				if (nodes.contains(edge.sinkNode)){
	//					allEdges.add(edge);
	//				}
	//			}
	//		}
	//		ArrayList<Node> nodeList = new ArrayList<>();
	//		nodeList.addAll(nodes);
	//		Graph subGraph = new Graph(nodeList, allEdges, count);
	//		if (subGraph.isConnectedGraph())
	//			return subGraph;
	//		return null;
	//	}


	public LinkedHashMap<Node, TreeMap<Integer, ArrayList<Path>>> getAllPathsWithConcern(String[] concernedLibs, GlobalData globalData, int maxLen){
		//		LinkedHashMap<Node, ArrayList<Edge> > sourceNodeEdgesMap = new LinkedHashMap<Node, ArrayList<Edge>>();

		ArrayList<Node> allConcernedControlNodes = new ArrayList<Node>();		
		for (Node node:nodes){
			if (node.isConcernedNode(concernedLibs, globalData)){
				allConcernedControlNodes.add(node);
			}
		}


		LinkedHashMap<Node, ArrayList<Node> > sourceNodeSinkNodeMap = new LinkedHashMap<Node, ArrayList<Node>>();

		for (Node node:allConcernedControlNodes){
			//			sourceNodeEdgesMap.put(node, new ArrayList<Edge>());
			sourceNodeSinkNodeMap.put(node, new ArrayList<Node>());
		}

		for (Edge edge:edges){
			if (edge==null)
				continue;
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			//			if (!sourceNodeEdgesMap.containsKey(source)){
			//				sourceNodeEdgesMap.put(source, new ArrayList<Edge>());
			//			}
			//			sourceNodeEdgesMap.get(source).add(edge);
			if (!source.isConcernedNode(concernedLibs, globalData)){
				continue;
			}	
			if (!sink.isConcernedNode(concernedLibs, globalData)){
				continue;
			}	

			if (!sourceNodeSinkNodeMap.containsKey(source)){
				sourceNodeSinkNodeMap.put(source, new ArrayList<Node>());
			}
			sourceNodeSinkNodeMap.get(source).add(sink);
		}

		LinkedHashMap<Node, TreeMap<Integer, ArrayList<Path>>> allSizePathsMap = new LinkedHashMap();
		for (Node node:allConcernedControlNodes) {
			TreeMap<Integer, ArrayList<Path>> sizePathsMap = new TreeMap<Integer, ArrayList<Path>>();
			Path path1 = new Path(null,node,count);
			if (!sizePathsMap.containsKey(1)){
				sizePathsMap.put(1, new ArrayList<Path>());
			}
			sizePathsMap.get(1).add(path1);			
			for (int size=2;size<=maxLen; size++){
				ArrayList<Path> prevPaths = sizePathsMap.get(size-1);
				if (prevPaths == null){
					break;
				}
				//				Logger.log("size: " + size + "///prevPaths: " + prevPaths);
				for (Path prev:prevPaths){
					Node lastPrev = prev.lastNode();
					if (lastPrev!=null){
						ArrayList<Node> sinks = sourceNodeSinkNodeMap.get(lastPrev);
						for (Node sink:sinks){
							if (sinks!=null){
								Path tmp = new Path(prev, sink,count);
								//								putPath(sizePathsMap, size, tmp);
								if (!sizePathsMap.containsKey(size)){
									sizePathsMap.put(size, new ArrayList<Path>());
								}
								sizePathsMap.get(size).add(tmp);
							}
						}
					}
				}
			}
			//			for (Integer size:sizePathsMap.keySet()){
			//				ArrayList<Path> sizePaths = sizePathsMap.get(size);
			//				if (!allSizePathsMap.containsKey(size)){
			//					allSizePathsMap.put(size, new ArrayList<Path>());
			//				}
			//				allSizePathsMap.get(size).addAll(sizePaths);
			//			}
			if (node!=null)
				allSizePathsMap.put(node, sizePathsMap);
		}

		return allSizePathsMap;
	}


	public int getLongestPath(){
		LinkedHashMap<Node, TreeMap<Integer, ArrayList<Path>>> allPaths = getAllPaths(10000);
		int maxLen = 0;
		for (Node node:allPaths.keySet() ){
			TreeMap<Integer, ArrayList<Path>> paths = allPaths.get(node);
			for (int size: paths.keySet()){
				for (Path path:paths.get(size)){
					int mlen = path.nodeList.length;
					if (mlen>maxLen)
						maxLen = mlen;
				}
			}
		}
		return maxLen;
	}
	public LinkedHashMap<Node, TreeMap<Integer, ArrayList<Path>>> getAllPaths(  int maxLen){
		//		LinkedHashMap<Node, ArrayList<Edge> > sourceNodeEdgesMap = new LinkedHashMap<Node, ArrayList<Edge>>();



		LinkedHashMap<Node, ArrayList<Node> > sourceNodeSinkNodeMap = new LinkedHashMap<Node, ArrayList<Node>>();

		for (Edge edge:edges){
			if (edge==null)
				continue;
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;

			if (!sourceNodeSinkNodeMap.containsKey(source)){
				sourceNodeSinkNodeMap.put(source, new ArrayList<Node>());
			}
			sourceNodeSinkNodeMap.get(source).add(sink);
		}

		LinkedHashMap<Node, TreeMap<Integer, ArrayList<Path>>> allSizePathsMap = new LinkedHashMap();
		for (Node node:nodes) {
			TreeMap<Integer, ArrayList<Path>> sizePathsMap = new TreeMap<Integer, ArrayList<Path>>();
			Path path1 = new Path(null,node,count);
			if (!sizePathsMap.containsKey(1)){
				sizePathsMap.put(1, new ArrayList<Path>());
			}
			sizePathsMap.get(1).add(path1);			
			for (int size=2;size<=maxLen; size++){
				ArrayList<Path> prevPaths = sizePathsMap.get(size-1);
				if (prevPaths == null){
					break;
				}
				//				Logger.log("size: " + size + "///prevPaths: " + prevPaths);
				ArrayList<Path> allPath = new ArrayList();
				for (Path prev:prevPaths){
					Node lastPrev = prev.lastNode();
					if (lastPrev!=null){
						ArrayList<Node> sinks = sourceNodeSinkNodeMap.get(lastPrev);
						if (sinks==null)
							break;
						for (Node sink:sinks){
							if (sinks!=null){
								Path tmp = new Path(prev, sink,count);
								//								putPath(sizePathsMap, size, tmp);
								if (!sizePathsMap.containsKey(size)){
									sizePathsMap.put(size, new ArrayList<Path>());
								}
								sizePathsMap.get(size).add(tmp);
								allPath.add(tmp);
							}
						}
					}
				}
				if (allPath.size()<=0)
					break;
			}
			if (node!=null)
				allSizePathsMap.put(node, sizePathsMap);
		}

		return allSizePathsMap;
	}

	//	public void putPath(TreeMap<Integer, ArrayList<Path>> sizePathsMap, int size, Path path){
	//		if (!sizePathsMap.containsKey(size)){
	//			sizePathsMap.put(size, new ArrayList<Path>());
	//		}
	//		sizePathsMap.get(size).add(path);
	//	}


	public boolean isHaveGraph(List<Graph> graphList, Graph aGraph){
		for (Graph graph:graphList){
			if (graph.memEquals(aGraph)){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return "Graph [nNodes=" + nodes.length + "] [nodes=" + Arrays.toString(nodes) + ", edges="
				+ Arrays.toString(edges) + ", count=" + count  +  ", #methods=" + this.methodIdxList.length+ "]";
	}

	//	@Override
	//	public String toString() {
	//		return "Graph [nNodes=" + nodes.length + "]";
	//	}


	public Graph createCompleteSubGraph(ArrayList<Node> nodes, HashMap<Node, LinkedHashSet<Edge>> nodeLinkEdgeMap){
		ArrayList<Edge> allEdges = new ArrayList<>();
		for (Node node:nodes){
			HashSet<Edge> linkEdges = nodeLinkEdgeMap.get(node);
			if (linkEdges == null)
				continue;
			for (Edge edge:linkEdges){
				if (nodes.contains(edge.sinkNode)){
					allEdges.add(edge);
				}
			}
		}
		ArrayList<Node> nodeList = new ArrayList<>();
		nodeList.addAll(nodes);
		Graph subGraph = new Graph(nodeList, allEdges, count);
		if (subGraph.isConnectedGraph())
			return subGraph;
		return null;
	}

	public  TreeMap<Integer, LinkedHashSet<Node>> getNeighborNodes(Node aNode, int maxGraphSize, int maxCountNode,  HashMap<Node, LinkedHashSet<Node>> nodeLinkNodeMap){
		TreeMap<Integer, LinkedHashSet<Node>> neighborNodes = new TreeMap<>();


		LinkedHashSet<Node> visitedNodes = new LinkedHashSet<>();
		LinkedHashSet<Node> nNodes = new LinkedHashSet<>();
		nNodes.add(aNode);
		neighborNodes.put(0, nNodes);
		visitedNodes.add(aNode);


		for (int i=0; i<maxGraphSize; i++){
			LinkedHashSet<Node> curNodes = neighborNodes.get(i);
			if (curNodes==null)
				continue;
			LinkedHashSet<Node> nextNodes = new LinkedHashSet<>();
			for (Node curNode:curNodes){
				HashSet<Node> linkNodes = nodeLinkNodeMap.get(curNode);
				if (linkNodes == null)
					continue;
				for (Node linkNode:linkNodes){
					if (!visitedNodes.contains(linkNode)){
						nextNodes.add(linkNode);
						visitedNodes.add(linkNode);
					}
				}
			}

			if (nextNodes.size()>0)
				neighborNodes.put(i+1, nextNodes);

			if (visitedNodes.size()>maxGraphSize){
				break;
			}


		}
		return neighborNodes;
	}	

	public boolean isConcernedGraph(String[] concernedLibs, GlobalData globalData){
		for (Node node:nodes){
			boolean isNodeConcerned = false;
			String nodeText = node.getNodeLabel(globalData).trim();
			for (String concernedLib:concernedLibs){

				if (nodeText.startsWith(concernedLib)){
					isNodeConcerned = true;
					break;
				}
			}
			if (!isNodeConcerned){
				return false;
			}
		}
		return true;
	}



	public  void fillEdge(GraphDatabase graphDB){
		LinkedHashMap<Node, HashSet<Node>> sourceNodesMap = new LinkedHashMap<Node, HashSet<Node>>();
		LinkedHashMap<Node, HashSet<Node>> sinkNodesMap = new LinkedHashMap<Node, HashSet<Node>>();

		for (Edge edge:edges){
			Node source = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (sourceNodesMap.containsKey(source)){
				sourceNodesMap.get(source).add(sink);
			}
			else {
				HashSet<Node> sinks = new HashSet<>();
				sinks.add(sink);
				sourceNodesMap.put(source, sinks);
			}
			if (sinkNodesMap.containsKey(sink)){
				sinkNodesMap.get(sink).add(source);
			}
			else{
				HashSet<Node> sources = new HashSet<>();
				sources.add(source);
				sinkNodesMap.put(sink, sources);
			}
		}



		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		newEdges.addAll(Arrays.asList(edges));
		//		ArrayList<Edge> addedEdges =s new ArrayList<Edge>();
		for (Node source:sourceNodesMap.keySet()){
			if (!sinkNodesMap.containsKey(source))
				continue;
			HashSet<Node> sinks = sourceNodesMap.get(source);
			HashSet<Node> sources = sinkNodesMap.get(source);
			HashSet<Node> all = new HashSet<Node>();
			all.addAll(sinks);
			all.addAll(sources);
			for (Node aSource:sources){
				for (Node aSink:all){
					if (aSink==aSource)
						continue;
					boolean isFound = false;
					for (Edge e:newEdges){
						if (e.sinkNode==aSource&&e.sourceNode==aSink){
							isFound = true;
							break;
						}
						else if (e.sinkNode==aSink){
							isFound = true;
							break;
						}
					}
					if (isFound){
						continue;
					}
					Edge nEdge = new Edge(aSource, aSink, 1);

					ArrayList<Node> nodeList = new ArrayList<Node>();
					nodeList.add(aSource);
					nodeList.add(aSink);					
					ArrayList<Edge> edgeList = new ArrayList<Edge>();
					edgeList.add(nEdge);
					Graph qGraph = new Graph(nodeList, edgeList, count);
					Graph gr = graphDB.searchGraph(qGraph);

					if (gr!=null){
						if (gr.methodIdxList.length>GlobalConfig.minNConnectedEdge)
							newEdges.add(nEdge);
					}
					//					else 
					//					if (aSource.isTypeEqual(aSink)){
					//						newEdges.add(nEdge);
					//					}
				}
			}

			for (Node aSource:sinks){//sources){
				//				Edge nEdge = new Edge(source, aSink, 1);
				//				newEdges.add(nEdge);
				for (Node aSink:sinks){
					if (aSink==aSource)
						continue;
					boolean isFound = false;
					for (Edge e:newEdges){
						if (e.sinkNode==aSource&&e.sourceNode==aSink){
							isFound = true;
							break;
						}
						else if (e.sinkNode==aSink){
							isFound = true;
							break;
						}
					}
					if (isFound){
						continue;
					}
					Edge nEdge = new Edge(aSource, aSink, 1);

					ArrayList<Node> nodeList = new ArrayList<Node>();
					nodeList.add(aSource);
					nodeList.add(aSink);					
					ArrayList<Edge> edgeList = new ArrayList<Edge>();
					edgeList.add(nEdge);
					Graph qGraph = new Graph(nodeList, edgeList, count);
					Graph gr = graphDB.searchGraph(qGraph);

					if (gr!=null){
						if (gr.methodIdxList.length>GlobalConfig.minNConnectedEdge)
							newEdges.add(nEdge);
					}
					//					else 
					//					if (aSource.isTypeEqual(aSink)){
					//						newEdges.add(nEdge);
					//					}
				}
			}
		}

		HashSet<Node> allUnconnectedNodes = new HashSet<Node>();
		allUnconnectedNodes.addAll(Arrays.asList(nodes));
		allUnconnectedNodes.removeAll(sourceNodesMap.keySet());
		allUnconnectedNodes.removeAll(sinkNodesMap.keySet());

		HashSet<Node> edgedNodes = new HashSet<Node>();
		for (Node aSource:nodes){
			for (Node aSink:allUnconnectedNodes){
				if (aSink==aSource)
					continue;
				boolean isFound = false;
				for (Edge e:newEdges){
					if (e.sinkNode==aSource&&e.sourceNode==aSink){
						isFound = true;
						break;
					}
					//					else if (e.sinkNode==aSink){
					//						isFound = true;
					//						break;
					//					}
				}
				if (isFound){
					continue;
				}
				Edge nEdge = new Edge(aSource, aSink, 1);

				ArrayList<Node> nodeList = new ArrayList<Node>();
				nodeList.add(aSource);
				nodeList.add(aSink);					
				ArrayList<Edge> edgeList = new ArrayList<Edge>();
				edgeList.add(nEdge);
				Graph qGraph = new Graph(nodeList, edgeList, count);
				Graph gr = graphDB.searchGraph(qGraph);

				if (gr!=null){
					if (gr.methodIdxList.length>GlobalConfig.minNConnectedEdge){
						newEdges.add(nEdge);
						edgedNodes.add(aSink);
					}
				}
			}
		}

		allUnconnectedNodes.removeAll(edgedNodes);
		for (Node aSource:allUnconnectedNodes){
			for (Node aSink:nodes){
				if (aSink==aSource)
					continue;
				boolean isFound = false;
				for (Edge e:newEdges){
					if (e.sinkNode==aSource&&e.sourceNode==aSink){
						isFound = true;
						break;
					}
					else if (e.sinkNode==aSink){
						isFound = true;
						break;
					}
				}
				if (isFound){
					continue;
				}
				Edge nEdge = new Edge(aSource, aSink, 1);

				ArrayList<Node> nodeList = new ArrayList<Node>();
				nodeList.add(aSource);
				nodeList.add(aSink);					
				ArrayList<Edge> edgeList = new ArrayList<Edge>();
				edgeList.add(nEdge);
				Graph qGraph = new Graph(nodeList, edgeList, count);
				Graph gr = graphDB.searchGraph(qGraph);

				if (gr!=null){
					if (gr.methodIdxList.length>GlobalConfig.minNConnectedEdge)
						newEdges.add(nEdge);
				}
			}
		}
		edges = newEdges.toArray(new Edge[0]);
		this.removeRedundancy();
	}


	public  void fillEdgeSimple(GraphDatabase graphDB){
		LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap = new  LinkedHashMap<Node, LinkedHashSet<Node>>();
		for (Edge edge:this.edges){
			Node source  = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (sourceSinksMap.containsKey(source)){
				sourceSinksMap.get(source).add(sink);
			}
			else {
				LinkedHashSet<Node> sinks = new LinkedHashSet<Node>();
				sinks.add(sink);
				sourceSinksMap.put(source, sinks);
			}
			if (sinkSourcesMap.containsKey(sink)){
				sinkSourcesMap.get(sink).add(source);
			}
			else {
				LinkedHashSet<Node> sources = new LinkedHashSet<Node>();
				sources.add(source);
				sinkSourcesMap.put(sink, sources);
			}

		}
		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		newEdges.addAll(Arrays.asList(edges));
		for (Node aSource:nodes){
			for (Node aSink:nodes){
				if (aSource==aSink)
					continue;
				if (isHavePath(aSink, aSource, sourceSinksMap, sinkSourcesMap))
					continue;
				boolean isFound = false;
				for (Edge edge:newEdges){
					if (edge.sinkNode==aSource&&edge.sourceNode==aSink){
						isFound = true;
						break;
					}
				}
				if (isFound){
					continue;
				}

				Edge nEdgeF = new Edge(aSource, aSink, 1);
				ArrayList<Node> nodeList = new ArrayList<Node>();
				nodeList.add(aSource);
				nodeList.add(aSink);	
				ArrayList<Edge> edgeListF = new ArrayList<Edge>();
				edgeListF.add(nEdgeF);
				Graph qGraphF = new Graph(nodeList, edgeListF, count);

				Edge nEdgeB = new Edge(aSink, aSource, 1);
				ArrayList<Edge> edgeListB = new ArrayList<Edge>();
				edgeListB.add(nEdgeB);
				Graph qGraphB = new Graph(nodeList, edgeListB, count);
								
				Graph grF = graphDB.searchGraph(qGraphF);
				Graph grB = graphDB.searchGraph(qGraphB);
				
				if (grF!=null&&grB!=null)
					if (grF.methodIdxList!=null&&grB.methodIdxList!=null)
						if(grF.methodIdxList.length<grB.methodIdxList.length&&!isHavePath(aSource, aSink, sourceSinksMap, sinkSourcesMap))
							continue;

				if (grF!=null){
					if (grF.methodIdxList.length>=GlobalConfig.minNConnectedEdge){
						newEdges.add(nEdgeF);

						if (sourceSinksMap.containsKey(aSource)){
							sourceSinksMap.get(aSource).add(aSink);
						}
						else {
							LinkedHashSet<Node> sinks = new LinkedHashSet<Node>();
							sinks.add(aSink);
							sourceSinksMap.put(aSource, sinks);
						}
						if (sinkSourcesMap.containsKey(aSink)){
							sinkSourcesMap.get(aSink).add(aSource);
						}
						else {
							LinkedHashSet<Node> sources = new LinkedHashSet<Node>();
							sources.add(aSource);
							sinkSourcesMap.put(aSink, sources);
						}
					}
				}

			}
		}
		edges = newEdges.toArray(new Edge[0]);
		this.removeRedundancy();
	}


	public int getNumNodeHasEdges(){
		HashSet<Node> foundNodes = new HashSet<Node>();
		for (Edge edge:edges){
			if (edge.sinkNode!=null)
				foundNodes.add(edge.sinkNode);
			if (edge.sourceNode!=null)
				foundNodes.add(edge.sourceNode);
		}
		return foundNodes.size();
	}

	public int getNumConnectedNodes(){
		HashSet<Node> connectedNodes = new HashSet<Node>();
		for (Edge edge:edges){
			if (edge.sourceNode!=null)
				connectedNodes.add(edge.sourceNode);
			if (edge.sinkNode!=null)
				connectedNodes.add(edge.sinkNode);
		}
		return connectedNodes.size();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<int[]> allMaskArrs = genMaskArrs(10, 30);
		for (int[] maskArr:allMaskArrs){
			printMask(maskArr);
		}
	}


	public  Graph removeVarVarEdges(){
		ArrayList<Edge> newEdges = new ArrayList<Edge>();

		for (Edge edge:edges){
			if ((edge.sourceNode.nodeRole==3&&edge.sinkNode.nodeRole==3)||(edge.sourceNode.nodeRole==2&&edge.sinkNode.nodeRole==3))
				continue;
			newEdges.add(edge);

		}		
		return new Graph(nodes,newEdges.toArray(new Edge[0]),count);
	}

	public  Graph removeIsolatedNodes(){
		LinkedHashSet<Node> nonIsolatedNodes = new LinkedHashSet<Node>();
		ArrayList<Edge> usededges = new ArrayList<Edge>();
		for (Edge edge:edges){
			if (edge.sourceNode==null||edge.sinkNode==null)
				continue;
			usededges.add(edge);
			nonIsolatedNodes.add(edge.sourceNode);
			nonIsolatedNodes.add(edge.sinkNode);
		}

		ArrayList<Node> newNodes = new ArrayList<Node>(nonIsolatedNodes);

		return new Graph(newNodes,usededges,count);
	}

	public  Graph removeIsolatedGraphs(){
		LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap = new  LinkedHashMap<Node, LinkedHashSet<Node>>();
		for (Edge edge:this.edges){
			Node source  = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (sourceSinksMap.containsKey(source)){
				sourceSinksMap.get(source).add(sink);
			}
			else {
				LinkedHashSet<Node> sinks = new LinkedHashSet<Node>();
				sinks.add(sink);
				sourceSinksMap.put(source, sinks);
			}
			if (sinkSourcesMap.containsKey(sink)){
				sinkSourcesMap.get(sink).add(source);
			}
			else {
				LinkedHashSet<Node> sources = new LinkedHashSet<Node>();
				sources.add(source);
				sinkSourcesMap.put(sink, sources);
			}

		}



		ArrayList<Node> startNodes = new ArrayList<Node>();
		for (Node node:sourceSinksMap.keySet()){
			if (!sinkSourcesMap.containsKey(node)){
				startNodes.add(node);
			}
		}

		for (Node node:nodes){
			if (!sourceSinksMap.containsKey(node)&&!sinkSourcesMap.containsKey(node)){
				startNodes.add(node);
			}
		}

		//Get all nodes, with each node N, show the nodes having edges with N  
		HashMap<Node, HashSet<Node>> nodeLinkNodeMap = new HashMap<>();
		for (Edge edge:edges){
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

		TreeMap<Integer, ArrayList<ArrayList<Node>>> sizeSpanNodesMap=  new TreeMap<Integer, ArrayList<ArrayList<Node>>>();

		for (Node node:startNodes){
			ArrayList<Node> spanTreeNodes = new ArrayList<>();
			//Build spantreeNodes
			spanTreeNodes.add(node);
			int lastIdx = 0;
			while(true){
				int tmpIdx = spanTreeNodes.size();
				for (int i=lastIdx; i<spanTreeNodes.size(); i++ ){
					Node tmpNode = spanTreeNodes.get(i);
					HashSet<Node> linkNodes = nodeLinkNodeMap.get(tmpNode);
					if (linkNodes==null)
						continue;
					for (Node linkNode:linkNodes){
						if (!spanTreeNodes.contains(linkNode)){
							spanTreeNodes.add(linkNode);
						}
					}
				}
				if (tmpIdx == spanTreeNodes.size()){
					break;
				}
				lastIdx = tmpIdx;
			}

			int size = spanTreeNodes.size();
			if (!sizeSpanNodesMap.containsKey(size)){
				sizeSpanNodesMap.put(size, new  ArrayList<ArrayList<Node>>());
			}
			sizeSpanNodesMap.get(size).add(spanTreeNodes);
		}

		HashSet<Node> allRemainingNodes = new HashSet<Node>();
		if (sizeSpanNodesMap.size()==0)
			return this;
		
//		Logger.log("sizeSpanNodesMap: " + sizeSpanNodesMap);
		for (int size:sizeSpanNodesMap.keySet()){
			if (size<=2)
				continue;
			for (ArrayList<Node> nodes:sizeSpanNodesMap.get(size))
				allRemainingNodes.addAll(nodes);
		}
		
		if (allRemainingNodes.size()==0){
			for (int size:sizeSpanNodesMap.keySet()){
				for (ArrayList<Node> nodes:sizeSpanNodesMap.get(size))
					allRemainingNodes.addAll(nodes);
			}
		}
//		for (ArrayList<Node> nodes:sizeSpanNodesMap.lastEntry().getValue())
//			allRemainingNodes.addAll(nodes);
		ArrayList<Edge> remainingEdges = new ArrayList<Edge>();
		for (Edge edge:this.edges){
			if (allRemainingNodes.contains(edge.sourceNode)&&allRemainingNodes.contains(edge.sinkNode))
				remainingEdges.add(edge);
		}
		return new Graph(allRemainingNodes.toArray(new Node[0]), remainingEdges.toArray(new Edge[0]), count);
	}

	public ArrayList<Node>  getNodeSequenceSimple( GlobalData globalData){
		ArrayList<ArrayList<Node>> nodeSequences = getNodeSequence(globalData);
		ArrayList<Node> nodeSequenceSimple = new ArrayList<Node>();
		for (ArrayList<Node> nodeSequence:nodeSequences){
			nodeSequenceSimple.addAll(nodeSequence);
		}
		return nodeSequenceSimple;
	}
	public ArrayList<ArrayList<Node>>  getNodeSequence( GlobalData globalData){

		LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap = new LinkedHashMap<Node, LinkedHashSet<Node>>();
		LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap = new  LinkedHashMap<Node, LinkedHashSet<Node>>();
		for (Edge edge:this.edges){
			Node source  = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (sourceSinksMap.containsKey(source)){
				sourceSinksMap.get(source).add(sink);
			}
			else {
				LinkedHashSet<Node> sinks = new LinkedHashSet<Node>();
				sinks.add(sink);
				sourceSinksMap.put(source, sinks);
			}
			if (sinkSourcesMap.containsKey(sink)){
				sinkSourcesMap.get(sink).add(source);
			}
			else {
				LinkedHashSet<Node> sources = new LinkedHashSet<Node>();
				sources.add(source);
				sinkSourcesMap.put(sink, sources);
			}

		}



		ArrayList<Node> startNodes = new ArrayList<Node>();
		for (Node node:sourceSinksMap.keySet()){
			if (!sinkSourcesMap.containsKey(node)){
				startNodes.add(node);
			}
		}

		for (Node node:nodes){
			if (!sourceSinksMap.containsKey(node)&&!sinkSourcesMap.containsKey(node)){
				startNodes.add(node);
			}
		}

		ArrayList<ArrayList<Node>> labelSequences = new ArrayList< ArrayList<Node>>();
		//		labelSequence.addAll(startNodes);
		//		LinkedHashSet<Node> observedNodes = new LinkedHashSet<Node>();
		HashSet<Node> observedNodes = new HashSet<Node>();
		TreeMap<Integer, ArrayList<Node>> lengthStartNodesMap = new TreeMap<Integer, ArrayList<Node>>();
		for (Node startNode: startNodes){
			int length = countLongestPath(startNode, sourceSinksMap, sinkSourcesMap);
			if (!lengthStartNodesMap.containsKey(length)){
				lengthStartNodesMap.put(length, new ArrayList<Node>());
			}
			lengthStartNodesMap.get(length).add(startNode);
		}

		//		for (Node startNode: startNodes){
		for (Integer length:lengthStartNodesMap.descendingKeySet())
			for (Node startNode:lengthStartNodesMap.get(length)){
				if (observedNodes.contains(startNode)){
					continue;
				}
				ArrayList<Node> labelSequence = new ArrayList<Node>(); 
				//			labelSequence.add(startNode);
				//			observedNodes.add(startNode);
				labelSequence.addAll(expandFrontiers(startNode, sourceSinksMap, sinkSourcesMap, observedNodes));
				observedNodes.addAll(labelSequence);
				labelSequences.add(labelSequence);

			}
		return labelSequences;
	}

	public ArrayList<Node> expandFrontiers(Node node, LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap, 
			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap, HashSet<Node> observedNodes){
		ArrayList<Node> addedNodes = new ArrayList<Node>();
		LinkedHashSet<Node> frontierNodes = new LinkedHashSet<Node>();
		frontierNodes.add(node);
		addedNodes.add(node);
		while (frontierNodes.size()>0){
			LinkedHashSet<Node> tmp = new LinkedHashSet<Node>();
			for (Node aNode:frontierNodes){
				if (sourceSinksMap.containsKey(aNode)){
					tmp.addAll(sourceSinksMap.get(aNode));
					//					ArrayList<Node> hasStartNodes = new ArrayList<Node>();
					ArrayList<Node> varNodes = new ArrayList<Node>();
					ArrayList<Node> methodNodes = new ArrayList<Node>();
					for (Node aNodeNode:sourceSinksMap.get(aNode)){

						//						boolean isFound = false;
						//						if (sinkSourcesMap.containsKey(aNodeNode)){
						//							for (Node tmpNode:sinkSourcesMap.get(aNodeNode)){
						//								if (tmpNode!=aNode&&!sinkSourcesMap.containsKey(tmpNode)){
						//									isFound = true;
						//									break;
						//								}
						//							}
						//						}
						//						if (isFound)
						//							hasStartNodes.add(aNodeNode);
						//						else 

						if (aNodeNode.nodeRole==3)
							varNodes.add(aNodeNode);
						else 
							methodNodes.add(aNodeNode);
					}
					//					for (Node aNodeNode:hasStartNodes){
					//						addedNodes.remove(aNodeNode);
					//						addedNodes.add(aNodeNode);
					//					}
					for (Node aNodeNode:varNodes){
						addedNodes.remove(aNodeNode);
						addedNodes.add(aNodeNode);
					}
					for (Node aNodeNode:methodNodes){
						addedNodes.remove(aNodeNode);
						addedNodes.add(aNodeNode);
					}
				}
			}

			frontierNodes.clear();
			frontierNodes.addAll(tmp);
		}



		for (int j=0;j<5;j++){
			ArrayList<Node> remainingSources = new ArrayList<Node>();
			remainingSources.addAll(sourceSinksMap.keySet());
			remainingSources.removeAll(addedNodes);
			int sourcesize = remainingSources.size();

			for (int k=0; k<10*sourcesize; k++){
				if (remainingSources.size()==0)
					break;
				Node pNode = remainingSources.get(0);

				boolean isConnected = false;

				for (int i=0; i<addedNodes.size();i++){
					Node cNode = addedNodes.get(i);
					if (sourceSinksMap.get(pNode).contains(cNode)){
						addedNodes.add(i, pNode);
						isConnected = true;
						break;
					}
				}
				remainingSources.remove(0);
				if(!isConnected)
					remainingSources.add(pNode);
			}


			ArrayList<Node> remainingSinks = new ArrayList<Node>();
			remainingSinks.addAll(sinkSourcesMap.keySet());
			remainingSinks.removeAll(addedNodes);
			int sinksize = remainingSinks.size();
			for (int k=0; k<10*sinksize; k++){
				if (remainingSinks.size()==0)
					break;
				Node pNode = remainingSinks.get(0);

				boolean isConnected = false;

				for (int i=0; i<addedNodes.size();i++){
					Node cNode = addedNodes.get(i);
					if (sinkSourcesMap.get(pNode).contains(cNode)){
						addedNodes.add(i+1, pNode);
						isConnected = true;
						break;
					}
				}
				remainingSinks.remove(0);
				if(!isConnected)
					remainingSinks.add(pNode);
			}
		}
		addedNodes.removeAll(observedNodes);
		return addedNodes;
	}




	public boolean isHavePath(Node node1, Node node2, LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap, 
			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap){
		HashSet<Node> frontiers = findFrontiersSimple(node1,  sourceSinksMap,  sinkSourcesMap);
		if (frontiers.contains(node2))
			return true;
		return false;	
	}


	public int countLongestPath(Node node, LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap, 
			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap){
		HashSet<Node> addedNodes = new HashSet<Node>();
		LinkedHashSet<Node> frontierNodes = new LinkedHashSet<Node>();
		frontierNodes.add(node);
		addedNodes.add(node);
		int numNodes = 1;
		while (frontierNodes.size()>0){
			LinkedHashSet<Node> tmp = new LinkedHashSet<Node>();
			for (Node aNode:frontierNodes){
				if (sourceSinksMap.containsKey(aNode)){
					tmp.addAll(sourceSinksMap.get(aNode));
					addedNodes.addAll(sourceSinksMap.get(aNode));
				}
			}

			frontierNodes.clear();
			numNodes++;
			frontierNodes.addAll(tmp);
		}

		return numNodes;
	}

	public HashSet<Node> findFrontiersSimple(Node node, LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap, 
			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap){
		HashSet<Node> addedNodes = new HashSet<Node>();
		LinkedHashSet<Node> frontierNodes = new LinkedHashSet<Node>();
		frontierNodes.add(node);
		addedNodes.add(node);
		while (frontierNodes.size()>0){
			LinkedHashSet<Node> tmp = new LinkedHashSet<Node>();
			for (Node aNode:frontierNodes){
				if (sourceSinksMap.containsKey(aNode)){
					tmp.addAll(sourceSinksMap.get(aNode));
					addedNodes.addAll(sourceSinksMap.get(aNode));
				}
			}

			frontierNodes.clear();
			frontierNodes.addAll(tmp);
		}

		return addedNodes;
	}

	//	public LinkedHashSet<Node> expandFrontiers(Node node, LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap, 
	//			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap, LinkedHashSet<Node> observedNodes){
	//		LinkedHashSet<Node> sinkFrontiers = new LinkedHashSet<Node>();
	//		if(!observedNodes.contains(node)){
	//			sinkFrontiers.add(node);
	//			observedNodes.add(node);
	//		}
	//		
	//		if (sourceSinksMap.containsKey(node)){
	//			for (Node sink:sourceSinksMap.get(node)){
	//				if (!observedNodes.contains(sink)){
	//					LinkedHashSet<Node> sinkFrontier = expandFrontiers(sink, sourceSinksMap, sinkSourcesMap, observedNodes);
	//	//				sinkFrontiers.addAll(sinkFrontier);
	//					for (Node af:sinkFrontier){
	////						if (!observedNodes.contains(af))
	//						{
	//							sinkFrontiers.add(af);
	//							observedNodes.add(af);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		
	//		LinkedHashSet<Node> sourceFrontiers = new LinkedHashSet<Node>();
	//		if (sinkSourcesMap.containsKey(node)){
	//			for (Node source:sinkSourcesMap.get(node)){
	//				if (!observedNodes.contains(source)){
	//					LinkedHashSet<Node> sourceFrontier = expandFrontiers(source, sourceSinksMap, sinkSourcesMap, observedNodes);
	//					for (Node af:sourceFrontier){
	////						if (!sinkFrontiers.contains(af)||!observedNodes.contains(af))
	//						{
	//							sourceFrontiers.add(af);
	//							observedNodes.add(af);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		LinkedHashSet<Node> allNodes = new LinkedHashSet<Node>();
	//		allNodes.addAll(sourceFrontiers);
	//		allNodes.add(node);
	//		allNodes.addAll(sinkFrontiers);
	//		return allNodes;
	//	}

	public int getNumControlStartNode(){

		LinkedHashMap<Node, HashSet<Node>> sourceSinksMap = new LinkedHashMap<Node, HashSet<Node>>();
		LinkedHashMap<Node, HashSet<Node>> sinkSourcesMap = new  LinkedHashMap<Node, HashSet<Node>>();
		for (Edge edge:this.edges){
			Node source  = edge.sourceNode;
			Node sink = edge.sinkNode;
			if (sourceSinksMap.containsKey(source)){
				sourceSinksMap.get(source).add(sink);
			}
			else {
				HashSet<Node> sinks = new HashSet<Node>();
				sinks.add(sink);
				sourceSinksMap.put(source, sinks);
			}
			if (sinkSourcesMap.containsKey(sink)){
				sinkSourcesMap.get(sink).add(source);
			}
			else {
				HashSet<Node> sources = new HashSet<Node>();
				sources.add(source);
				sinkSourcesMap.put(sink, sources);
			}

		}


		ArrayList<Node> startNodes = new ArrayList<Node>();
		for (Node node:sourceSinksMap.keySet()){
			if (!sinkSourcesMap.containsKey(node)){
				startNodes.add(node);
			}
		}

		int numControlStartNode = 0;
		for (Node startNode:startNodes){
			if (startNode.isControlNode(Node.globalData)){
				numControlStartNode++;
			}
		}
		return numControlStartNode;
	}

	public String getNodeSequenceStr(GlobalData globalData){
		ArrayList<ArrayList<Node>> nodeSequences = getNodeSequence(globalData);

		StringBuffer sb = new StringBuffer();
		for(ArrayList<Node> nodeSequence:nodeSequences){
			for (Node node:nodeSequence){
				sb.append(node.getNodeLabelSyn(globalData) + System.lineSeparator());
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
