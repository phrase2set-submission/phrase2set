/**
 * 
 */
package query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import config.GlobalConfig;
import recommendation.Recommender;
import storage.GraphDatabase;
import storage.NodeGraphDatabase;
import utils.Logger;
import datastructure.Edge;
import datastructure.Graph;
import datastructure.Node;


/**
 * @author anhnt
 *
 */
public class QueryMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public synchronized LinkedHashMap<Graph, Double>  query(ArrayList<Node> nodes, GraphDatabase  graphDB, NodeGraphDatabase nodeGraphDB){
		long t01 = System.currentTimeMillis();
		int firstNodeIdx = 0;
		if (!GlobalConfig.isUseExternalPivot)
			firstNodeIdx = findMostCommonInContextNode(nodes, nodes, nodeGraphDB);//findMostCommonFirstNode(nodes, nodeGraphDB);
		Node firstNode = nodes.get(firstNodeIdx);
		Logger.log("firstNode: " + firstNode);
		ArrayList<Node> remaining = new ArrayList<Node>();
		remaining.addAll(nodes);
		remaining.remove(firstNodeIdx);
		ArrayList<Node> sequentialNodes = new ArrayList<Node>();
		sequentialNodes.add(firstNode);
		Node lastNode = firstNode;
		int size = remaining.size();
		long t02 = System.currentTimeMillis();
		
	

		for(int i=0; i<size; i++){
//			Logger.log("remaining:" + remaining);
			
			int mostCommonInContextIdx = findMostCommonInContextNodeSimple(lastNode, remaining, nodeGraphDB);//
			if (mostCommonInContextIdx==-1){
//				mostCommonInContextIdx = findMostCommonInContextNode(sequentialNodes, remaining, nodeGraphDB);
				mostCommonInContextIdx = findMostCommonInContextNode(sequentialNodes, remaining, nodeGraphDB);
			}
			
			
//			int mostCommonInContextIdx = findMostCommonNode(remaining, nodeGraphDB);
			Node node = remaining.get(mostCommonInContextIdx);
			lastNode = node;
			sequentialNodes.add(node);
//			Logger.log("node:" + node);

			
			remaining.remove(mostCommonInContextIdx);
		}
////		Logger.log("sequentialNodes: " + sequentialNodes);
//		sequentialNodes.addAll(nodes);
		long t03 = System.currentTimeMillis();
		Logger.log("t02-t01: " + (t02-t01) + " t03-t02: " + (t03-t02));
	
		List<Node> sequentialNodes2 = sequentialNodes;
//		if (sequentialNodes.size()>25)
//			sequentialNodes2 = sequentialNodes.subList(0, 22);
//		Logger.log("sequentialNodes2:" +sequentialNodes2);
		LinkedHashMap<Graph, Double> tmp = createGraphFromSequentialNodes(sequentialNodes2, graphDB);
		return tmp;
	}
	
	public synchronized LinkedHashMap<Graph, Double>  createGraphFromSequentialNodes(List<Node> sequentialNodes, GraphDatabase graphDB){

		ConcurrentHashMap<Graph, Double> parentsWithScores = new ConcurrentHashMap<Graph, Double>();  
		long t1 = System.currentTimeMillis();
		for (Node node:sequentialNodes){
//			Logger.log("node: " + node);
					

			long t11 = System.currentTimeMillis();
			LinkedHashMap<Graph, Double> childrenWithScores = createChildrenFromParents(parentsWithScores, node, graphDB);
//			Logger.log("childrentWithScores: " + childrenWithScores);
			long t12 = System.currentTimeMillis();

			
			parentsWithScores = pruneGraphs(childrenWithScores, graphDB);
			long t13 = System.currentTimeMillis();
//			Logger.log("    t12-t11: " + (t12-t11) + "    t13-t12: " + (t13-t12));

//			Logger.log("parentsWithScores: " + parentsWithScores);

		}
//		Logger.log("sequentialNodes size: " + sequentialNodes.size());
		
		LinkedHashMap<Graph, Double> parentsWithScoresAll = new LinkedHashMap<Graph, Double>();  
		long t2 = System.currentTimeMillis();
		for (Graph gr:parentsWithScores.keySet()){
//			for (int i=0;i<10;i++)
			double score = parentsWithScores.get(gr);
//			for (int i=0;i<5;i++)
//			gr.fillEdge(graphDB);
//			for (int i=0; i<5; i++)
//				gr.fillEdgeSimple(graphDB);
//			Logger.log("gr.nodes.length: " + gr.nodes.length);

			if (gr.nodes.length<sequentialNodes.size())
				continue;
//			gr = gr.removeIsolatedNodes();
			gr.fillEdgeSimple(graphDB);
//			gr.fillEdge(graphDB);
			gr = gr.removeVarVarEdges();
			gr = gr.removeIsolatedNodes();
//			gr = gr.removeIsolatedGraphs();

			double edgeScore =  0;//10*Math.log((double)gr.getNumConnectedNodes()+1);//Math.log(-parent.edges.length + child.edges.length+1);
			edgeScore +=  10*Math.log((double)gr.edges.length+1);//Math.log(-parent.edges.length + child.edges.length+1);
			double controlStartPenalty = 20*Math.log((double)gr.getNumControlStartNode() +1);
			parentsWithScoresAll.put(gr, score+edgeScore - controlStartPenalty);
		}
//		Logger.log("parentsWithScores: " + parentsWithScores);

//		Logger.log("parentsWithScoresAll: " + parentsWithScoresAll);

		long t3 = System.currentTimeMillis();
		Logger.log("t2-t1: " + (t2-t1) + "///t3-t2: " + (t3-t2));
		
		return parentsWithScoresAll;
	}
	
	public ConcurrentHashMap<Graph, Double> pruneGraphs(LinkedHashMap<Graph, Double> graphScoreMap, GraphDatabase graphDB){
		TreeMap<Double, ArrayList<Graph>> scoreGraphsMap = new TreeMap<Double, ArrayList<Graph>>();
		for (Graph graph:graphScoreMap.keySet()){
			Double score = graphScoreMap.get(graph);
			if (scoreGraphsMap.containsKey(score)){
				scoreGraphsMap.get(score).add(graph);
			}
			else {
				ArrayList<Graph> graphs = new ArrayList<Graph>();
				graphs.add(graph);
				scoreGraphsMap.put(score, graphs);
			}
		}
		int totalGraph = 0;
		ConcurrentHashMap<Graph, Double> prunedGraphScoreMap = new ConcurrentHashMap<Graph, Double>();
		for (Double score:scoreGraphsMap.descendingKeySet()){
			for (Graph graph:scoreGraphsMap.get(score)){
//				graph.fillEdge(graphDB);
				prunedGraphScoreMap.put(graph, score);
			}
			totalGraph += scoreGraphsMap.get(score).size();
			if (totalGraph>GlobalConfig.pruneSize){
				break;
			}
		}
		return prunedGraphScoreMap;
	}
	
	
	
	
	
	
	
	
	
	public int findMostCommonFirstNode(ArrayList<Node> nodes, NodeGraphDatabase nodeGraphDB){
		int idx = 0;
		int max = 0;
		for (int i=0; i<nodes.size();i++){
			Node node = nodes.get(i);
			if (node.isControlNode(Node.globalData))
				continue;
			int numContainingGraphs = nodeGraphDB.getNumContainingGraphs(node);
//			Logger.log("ncG: " +numContainingGraphs);
			if(numContainingGraphs>max){
				max = numContainingGraphs;
				idx = i;
			}
		}
		return idx;
	}
	
	public int findMostCommonNode(ArrayList<Node> nodes, NodeGraphDatabase nodeGraphDB){
		int idx = 0;
		int max = 0;
		for (int i=0; i<nodes.size();i++){
			Node node = nodes.get(i);
			int numContainingGraphs = nodeGraphDB.getNumContainingGraphs(node);
//			Logger.log("ncG: " +numContainingGraphs);
			if(numContainingGraphs>max){
				max = numContainingGraphs;
				idx = i;
			}
		}
		return idx;
	}
	
	public int findMostCommonInContextNodeSimple(Node lastNode, ArrayList<Node> nodes, NodeGraphDatabase nodeGraphDB){
		int idx = -1;
		int maxShared = 0;
//		LinkedHashMap<Node, ArrayList<Graph>> contextNodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
//		contextNodeGraphMaps.put(lastNode, nodeGraphDB.getContain2Graphs(lastNode));
		
		
		LinkedHashMap<Node, ArrayList<Graph>>  nodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
		for (Node node:nodes){
			nodeGraphMaps.put(node, nodeGraphDB.getContain2Graphs(node));
		}
		
		for (int i=0; i<nodes.size();i++){
			Node node = nodes.get(i);
			int numContainingGraphs = nodeGraphDB.getNumContainingGraphs(node);
			if(numContainingGraphs==0)
				continue;
//			int aMaxShared = findMaxShared(contextNodes, node, nodeGraphDB);
//			int aMaxShared = findMaxShared(lastNode, node, nodeGraphDB);
//			int aMaxShared = findTotalShared(contextNodeGraphMaps, nodeGraphMaps.get(node), node, nodeGraphDB);
			int aMaxShared =  nodeGraphDB.getNumSharedEdgeGraphs(lastNode, node);
			
			if(aMaxShared>maxShared){
				maxShared = aMaxShared;
				idx = i;
			}
		}
		return idx;
	}
	
	public int findMostCommonInContextNodeWithoutControl(ArrayList<Node> contextNodes, ArrayList<Node> nodes, NodeGraphDatabase nodeGraphDB){
		int idx = 0;
		int maxShared = 0;
		LinkedHashMap<Node, ArrayList<Graph>> contextNodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
		for (Node contextNode:contextNodes){
			contextNodeGraphMaps.put(contextNode, nodeGraphDB.getContain2Graphs(contextNode));
		}
		
		LinkedHashMap<Node, ArrayList<Graph>>  nodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
		for (Node node:nodes){
			nodeGraphMaps.put(node, nodeGraphDB.getContain2Graphs(node));
		}
		
		for (int i=0; i<nodes.size();i++){
			Node node = nodes.get(i);
			if (node.isControlNode(Node.globalData))
				continue;
			int numContainingGraphs = nodeGraphDB.getNumContainingGraphs(node);
			if(numContainingGraphs==0)
				continue;
////			int aMaxShared = findMaxShared(contextNodes, node, nodeGraphDB);
			int aMaxShared = findTotalShared(contextNodeGraphMaps, nodeGraphMaps.get(node), node, nodeGraphDB);
			if (aMaxShared==contextNodes.size()){
				idx = i;
				break;
			}
				
			if(aMaxShared>maxShared){
				maxShared = aMaxShared;
				idx = i;
			}
		}
		return idx;
	}
	
	public int findMostCommonInContextNode(ArrayList<Node> contextNodes, ArrayList<Node> nodes, NodeGraphDatabase nodeGraphDB){
		int idx = 0;
		int maxShared = 0;
		LinkedHashMap<Node, ArrayList<Graph>> contextNodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
		for (Node contextNode:contextNodes){
			contextNodeGraphMaps.put(contextNode, nodeGraphDB.getContain2Graphs(contextNode));
		}
		
		LinkedHashMap<Node, ArrayList<Graph>>  nodeGraphMaps = new LinkedHashMap<Node, ArrayList<Graph>>();
		for (Node node:nodes){
			nodeGraphMaps.put(node, nodeGraphDB.getContain2Graphs(node));
		}
		
		for (int i=0; i<nodes.size();i++){
			Node node = nodes.get(i);
			int numContainingGraphs = nodeGraphDB.getNumContainingGraphs(node);
			if(numContainingGraphs==0)
				continue;
////			int aMaxShared = findMaxShared(contextNodes, node, nodeGraphDB);
			int aMaxShared = findTotalShared(contextNodeGraphMaps, nodeGraphMaps.get(node), node, nodeGraphDB);
			if (aMaxShared==contextNodes.size()){
				idx = i;
				break;
			}
				
			if(aMaxShared>maxShared){
				maxShared = aMaxShared;
				idx = i;
			}
		}
		return idx;
	}
	
	public int findTotalShared(ArrayList<Node> contextNodes, Node node, NodeGraphDatabase nodeGraphDB){
		int totalShared = 0;
		for(Node contextNode:contextNodes){
			if (contextNode==node)
				continue;
//			int shared = nodeGraphDB.getNumSharedGraphs(contextNode, node);
			int shared = nodeGraphDB.getNumSharedEdgeGraphs(contextNode, node);

//			if(shared>maxShared){
//				maxShared = shared;
//			}
			if (shared>=GlobalConfig.minShared){
				totalShared++;
			}
		}
		return totalShared;
	}
	
	
	public int findTotalShared(LinkedHashMap<Node, ArrayList<Graph>> contextNodeGraphMaps ,  ArrayList<Graph> nodeGraph, Node node, NodeGraphDatabase nodeGraphDB){
		int totalShared = 0;
		for(Node contextNode:contextNodeGraphMaps.keySet()){
			if (contextNode==node)
				continue;
//			int shared = nodeGraphDB.getNumSharedEdgeGraphs(contextNode, node);
			int shared = 0;
			for(Graph graph1:contextNodeGraphMaps.get(contextNode)){
				for(Graph graph2:nodeGraph){
					if(graph1==graph2){
						shared+=graph1.methodIdxList.length;
						break;
					}
				}
//				if (shared>=1)
//					break;
			}

			if (shared>=1){
				totalShared++;
			}
		}
		return totalShared;
	}
	
	public int findMaxShared(ArrayList<Node> contextNodes, Node node, NodeGraphDatabase nodeGraphDB){
		int maxShared = 0;
		for(Node contextNode:contextNodes){
			if (contextNode==node)
				continue;
//			int shared = nodeGraphDB.getNumSharedGraphs(contextNode, node);
			int shared = nodeGraphDB.getNumSharedEdgeGraphs(contextNode, node);

			if(shared>maxShared){
				maxShared = shared;
			}
		}
		return maxShared;
	}
	
	public int findMaxShared(Node contextNode, Node node, NodeGraphDatabase nodeGraphDB){
		int maxShared = 0;
			int shared = nodeGraphDB.getNumSharedEdgeGraphs(contextNode, node);

			if(shared>maxShared){
				maxShared = shared;
			}
		
		return maxShared;
	}
	

	
	public synchronized LinkedHashMap<Graph, Double> mergeChildren(ArrayList<ConcurrentHashMap<Graph, Double>> allChildrenWithScores, GraphDatabase graphDB){
		ArrayList<Graph> reps = new ArrayList<Graph>();
		HashSet<Integer> foundSet = new HashSet<Integer>();
		LinkedHashMap<Graph, Double> bestChildrenWithScores = new LinkedHashMap<Graph, Double>();
		for(ConcurrentHashMap<Graph, Double> childrenWithScores:allChildrenWithScores){
			if (childrenWithScores==null)
				continue;
			for(Graph gr:childrenWithScores.keySet()){
				Double score = childrenWithScores.get(gr);
				Graph tmp = gr;
				boolean isFound =false;
				gr.removeRedundancy();
////				for (int i=0; i<5;i++)
////				gr.fillEdge(graphDB);
//
				int hVal = gr.calcH1();
				if (foundSet.contains(hVal)){
					for(Graph rep:reps ){
						if(rep.simpleRoleEquals(gr)){
							tmp = rep;
							isFound = true;
							break;
						}
					}
				}
				if(!isFound){
					reps.add(gr);
					foundSet.add(hVal);
				}
//				
				if(!bestChildrenWithScores.containsKey(tmp)){
					bestChildrenWithScores.put(tmp, score);
				}
				else {
					if(score>bestChildrenWithScores.get(tmp)){
						bestChildrenWithScores.put(tmp, score);
					}
				}
			}
		}
		return bestChildrenWithScores;
	}
	
	public LinkedHashMap<Graph, Double> createChildrenFromParents(ConcurrentHashMap<Graph, Double> parentsWithScores, Node node,
			GraphDatabase graphDB){
		
		LinkedHashMap<Graph, Double> mergedChildrenWithScores = new LinkedHashMap<Graph, Double>();
		if (parentsWithScores.size()==0){
			ArrayList<Node> nodes = new ArrayList<Node>();
			nodes.add(node);
			ArrayList<Edge> edges = new ArrayList<Edge>();
			Graph gr = new Graph(nodes, edges, 1);
			mergedChildrenWithScores.put(gr, 1.0);
		}
		else {
			
			Long t121 = System.currentTimeMillis();

			ArrayList<ConcurrentHashMap<Graph, Double>> allChildrenWithScores = new ArrayList<ConcurrentHashMap<Graph,Double>>();
			synchronized(allChildrenWithScores){
				ArrayList<Runnable> tmp = new ArrayList<Runnable>();
				for(Graph parent:parentsWithScores.keySet()){

					tmp.add(new ChildCalculator(parentsWithScores, graphDB, allChildrenWithScores, parent, node));
					
				}
//				if (tmp.size()==GlobalConfig.maxThreads)
				{
					ExecutorService executor = Executors.newFixedThreadPool(GlobalConfig.maxThreads);
					for (Runnable run:tmp)
						executor.execute(run);
					executor.shutdown();
					try {
						executor.awaitTermination(100, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
//			Logger.log("allChildrenWithScores: "  +allChildrenWithScores);
			Long t122 = System.currentTimeMillis();

//			Logger.log("allChildrenWithScores: " + allChildrenWithScores);
			mergedChildrenWithScores = mergeChildren(allChildrenWithScores, graphDB);
//			Logger.log("mergedChildrenWithScores: "  +mergedChildrenWithScores);

			Long t123 = System.currentTimeMillis();
//			Logger.log("          t122-t121: " + (t122-t121) + "    t123-t122: " + (t123-t122));

		}

//		Logger.log("mergedChildrenWithScores: " + mergedChildrenWithScores);

		return mergedChildrenWithScores;
	}
}

class ChildCalculator implements Runnable{
	ConcurrentHashMap<Graph, Double> parentsWithScores;
	GraphDatabase graphDB;
	ArrayList<ConcurrentHashMap<Graph, Double>> allChildrenWithScores;
	Graph parent;
	Node node;
	
	public ChildCalculator(ConcurrentHashMap<Graph, Double> parentsWithScores,
			GraphDatabase graphDB,
			ArrayList<ConcurrentHashMap<Graph, Double>> allChildrenWithScores,
			Graph parent, Node node) {
		synchronized(parentsWithScores){
			this.parentsWithScores = parentsWithScores;
			this.graphDB = graphDB;
			this.allChildrenWithScores = allChildrenWithScores;
			this.parent = parent;
			this.node = node;
		}
	}

	@Override
	public  void run() {
		// TODO Auto-generated method stub
			
		calculate();
	}
	
	public synchronized void calculate(){
		Long t1221 = System.currentTimeMillis();

		ArrayList<Graph> contextGraphs = parent.getAllSubGraphs( GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, graphDB.globalData);
//		int size = parent.nodes.length;
		Long t1222 = System.currentTimeMillis();

		double score = parentsWithScores.get(parent);
//		Logger.log("contextGraphs: " + contextGraphs);

		for(Graph aParent:contextGraphs){
//			if (parent.nodes.length>2&&aParent.nodes.length<2){
//				continue;
//			}
			double ascore = score + Math.log((double)aParent.edges.length+1);
//			Logger.log("aParent: " + aParent);
			ArrayList<Graph> children = getChildren(aParent, node, graphDB);
//			Logger.log("children: " + children);
			if (children == null){
//				LinkedHashMap<Graph, Double> extendedChildrenWithScores = new LinkedHashMap<Graph, Double>();
//				extendedChildrenWithScores.put(parent, score + GlobalConfig.penaltyForNoConnection);
//				allChildrenWithScores.add(extendedChildrenWithScores);
				Graph aGraph = new Graph(parent, node, new Edge[0],1);
				aGraph.removeRedundancy();
				ConcurrentHashMap<Graph, Double> extendedChildrenWithScores = new ConcurrentHashMap<Graph, Double>();
				extendedChildrenWithScores.put(aGraph, ascore  + GlobalConfig.penaltyForNoConnection);
				allChildrenWithScores.add(extendedChildrenWithScores);
			}
			else {
				ArrayList<Graph> consideredChildren = new ArrayList<Graph>();
				for (Graph gr:children){
					if (gr.methodIdxList.length>=GlobalConfig.minAcceptedAsChild){
						consideredChildren.add(gr);
					}
				}
				if(consideredChildren.size()<=0){
					Graph aGraph = new Graph(parent, node, new Edge[0],1);
					aGraph.removeRedundancy();
//					children.add(aGraph);
					ConcurrentHashMap<Graph, Double> extendedChildrenWithScores = new ConcurrentHashMap<Graph, Double>();
					extendedChildrenWithScores.put(aGraph, ascore  + GlobalConfig.penaltyForNoConnection);
					allChildrenWithScores.add(extendedChildrenWithScores);
//					Logger.log("allChildrenWithScores: " + allChildrenWithScores); 

				}
				else {
					
					ConcurrentHashMap<Graph, Double> childrenWithScores = getScore(consideredChildren, aParent, contextGraphs, node, ascore, graphDB);
//					Logger.log("   childrenWithScores: " + childrenWithScores);
	
					ConcurrentHashMap<Graph, Double> extendedChildrenWithScores = extendChildren(childrenWithScores, parent, aParent, contextGraphs);  
//					Logger.log("   extendedChildrenWithScores: " + extendedChildrenWithScores);
					allChildrenWithScores.add(extendedChildrenWithScores);
				}
			}
		}
		Long t1223 = System.currentTimeMillis();
//		Logger.log("          t1222-t1221: " + (t1222-t1221) + "    t1223-t1222: " + (t1223-t1222));

	}
	
	public synchronized ArrayList<Graph> getChildren(Graph parent, Node node, GraphDatabase graphDB){
		ArrayList<Graph> children = new ArrayList<Graph>();
		Graph qContextGraph = graphDB.searchGraph(parent);
		if(qContextGraph==null)
			return null;
		Graph[] childContextGraphs = qContextGraph.childrenGraphs;
		if(childContextGraphs==null)
			return null;
		for(Graph gr:childContextGraphs){
//			Logger.log(gr);
//			boolean isContain = true;
//			for(Node aNode:gr.nodes){
//				if(aNode.simpleRoleEquals(node)){
//					isContain = true;
//					break;
//				}
//			}

//			boolean isN = false;

//			for (Node aNode:gr.nodes){
//				boolean isFound = false;
//				if (aNode.roleEquals(node)){
//					isFound = true;
//					isN = true;
//				}
//				else {
//					for (Node tNode:parent.nodes){
//						if (tNode==null)
//							continue;
//						if (tNode.roleEquals(aNode)){
//							isFound = true;
//							break;
//						}
//					}
//				}
//				if (!isFound){
//					isContain = false;
//					break;
//				}
//				
//			}
//			if(isContain&&isN){
//				children.add(gr);
//			}
			
			for (Node aNode:gr.nodes){
				if (aNode.roleEquals(node)){
					if (gr.getAParent(aNode).roleEquals(parent)){
						children.add(gr);
						break;
					}
				}
				
			}
					
			
			
		}
		return children;
	}
	
	public synchronized ConcurrentHashMap<Graph, Double> getScore(ArrayList<Graph> children, Graph parent, ArrayList<Graph> contextGraphs,
			Node node, double score, 
			GraphDatabase graphDB){
		ConcurrentHashMap<Graph, Double> scores = new ConcurrentHashMap<Graph, Double>(); 
		for(Graph child:children){
			child.removeRedundancy();
			double regularScore = calcRegularScore(child, parent, contextGraphs, graphDB);
			double fitScore = calcFitScore(child, parent, node);
			double totalScore = regularScore  +fitScore;
			scores.put(child, totalScore);
		}
		return scores;
	}
	
	public synchronized  double calcRegularScore(Graph child, Graph parent,  ArrayList<Graph> contextGraphs, GraphDatabase graphDB){
		Recommender rec = new Recommender();
		return rec.calcScore(child, parent, contextGraphs, graphDB);
	}
	
	public synchronized double calcFitScore(Graph child, Graph parent, Node node){
		return Math.log(((double)parent.nodes.length + 1)/((double)child.nodes.length+1));
	}
	
	public synchronized ConcurrentHashMap<Graph, Double> extendChildren(ConcurrentHashMap<Graph, Double> childrenWithScores, Graph bigParent, Graph aParent,
			ArrayList<Graph> contextGraphs){
		ConcurrentHashMap<Graph, Double> extendedChildren = new ConcurrentHashMap<Graph, Double>();
		for(Graph child:childrenWithScores.keySet()){
			double score = childrenWithScores.get(child);
			LinkedHashSet<Node> usedParentNodes = new LinkedHashSet<Node>();
			ConcurrentHashMap<Node, Node> childParentNodeMap = new ConcurrentHashMap<Node, Node>();
			Node addedNode = getAddedNode(child, aParent);
			for(Node cNode:child.nodes){
				if(cNode!=addedNode){
					for(Node pNode:aParent.nodes){
						if(!usedParentNodes.contains(pNode)){
							if(pNode.simpleRoleEquals(cNode)){
								childParentNodeMap.put(cNode, pNode);
								usedParentNodes.add(pNode);
							}
						}
					}
				}
			}

			Edge[]  edges = child.edges;
			ArrayList<Edge> addedEdges = new ArrayList<Edge>();
			
			
			for(Edge edge:edges){
				if(edge.sinkNode==addedNode){
					Edge aEdge = new Edge(childParentNodeMap.get(edge.sourceNode), addedNode, 1);
					addedEdges.add(aEdge);
				}
				else if(edge.sourceNode==addedNode){
					Edge aEdge = new Edge(addedNode, childParentNodeMap.get(edge.sinkNode), 1);
					addedEdges.add(aEdge);
				}
			}
			ArrayList<Node> allNodes = new ArrayList<Node>();
			for (Node aNode: bigParent.nodes){
				allNodes.add(aNode);
			}
			allNodes.add(addedNode);
			
			ArrayList<Edge> allEdges = new ArrayList<Edge>();
			for (Edge aEdge: bigParent.edges){
				allEdges.add(aEdge);
			}
			allEdges.addAll(addedEdges);
			Graph extendedChild = new Graph(allNodes, allEdges, child.count);
			extendedChildren.put(extendedChild, score);
		}
		
		
		return extendedChildren;
	}
	
	
	public synchronized Node getAddedNode(Graph graph, Graph parentGraph){
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
	
	
}