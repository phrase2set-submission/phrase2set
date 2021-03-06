/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import datastructure.Edge;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;


/**
 * @author anhnt
 *
 */
public class GraphComparisonUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	
	public static void compareGraphWithStrSeq(Graph actualGraph, Graph synGraph, StringGraphComparisonData strGraphCompData, GlobalData globalData){
		ArrayList<Node> nodeSequenceActual = actualGraph.getNodeSequenceSimple( globalData);
		ArrayList<Node> nodeSequenceSyn = synGraph.getNodeSequenceSimple( globalData);

		int num1GramActual = getNumNGram(nodeSequenceActual, 1);
		int num2GramActual = getNumNGram(nodeSequenceActual, 2);
		int num3GramActual = getNumNGram(nodeSequenceActual, 3);
		int num4GramActual = getNumNGram(nodeSequenceActual, 4);
		
		int num1GramSyn = getNumNGram(nodeSequenceSyn, 1);
		int num2GramSyn = getNumNGram(nodeSequenceSyn, 2);
		int num3GramSyn = getNumNGram(nodeSequenceSyn, 3);
		int num4GramSyn = getNumNGram(nodeSequenceSyn, 4);

		int num1GramShared =getNumNGramShared(nodeSequenceActual, nodeSequenceSyn, 1);
		int num2GramShared =getNumNGramShared(nodeSequenceActual, nodeSequenceSyn, 2);
		int num3GramShared =getNumNGramShared(nodeSequenceActual, nodeSequenceSyn, 3);
		int num4GramShared =getNumNGramShared(nodeSequenceActual, nodeSequenceSyn, 4);
		
		strGraphCompData.update(num1GramActual, num1GramSyn, num1GramShared, 
				num2GramActual, num2GramSyn, num2GramShared, 
				num3GramActual, num3GramSyn, num3GramShared, 
				num4GramActual, num4GramSyn, num4GramShared);
		
		
	}
	
	
	public static int getNumNGram(ArrayList<Node> sequences, int nGram){
		int numgrams = sequences.size() - nGram + 1;
		if (numgrams<0)
			numgrams = 0;
		return numgrams;
	}
	
	public static int getNumNGramShared(ArrayList<Node> nodeSequenceActual, ArrayList<Node> nodeSequenceSyn, int nGram){
		if (nGram>nodeSequenceActual.size())
			return 0;
		if (nGram>nodeSequenceSyn.size())
			return 0;
		ArrayList<ArrayList<Node>> nGramsActual = getNGrams(nodeSequenceActual, nGram);
		ArrayList<ArrayList<Node>> nGramsSyn = getNGrams(nodeSequenceSyn, nGram);
		return getNumShare(nGramsActual, nGramsSyn);
	}
	
	public static int getNumShare(ArrayList<ArrayList<Node>> nGramsActual , ArrayList<ArrayList<Node>> nGramsSyn){
		int num = 0;
		HashSet<ArrayList<Node>> foundNGram = new HashSet<ArrayList<Node>>();
		for (ArrayList<Node> nGramActual:nGramsActual){
			for (ArrayList<Node> nGramSyn:nGramsSyn){
//				if (!foundNGram.contains(nGramSyn)){
					if (isEqual(nGramActual, nGramSyn)){
						num++;
						foundNGram.addAll(nGramsSyn);
						break;
					}
//				}
			}
		}
		return num;
	}
	
	public static boolean  isEqual(ArrayList<Node> nGramActual, ArrayList<Node> nGramSyn){
		for (int i=0; i<nGramActual.size();i++){
			Node nActual = nGramActual.get(i);
			Node nSyn = nGramSyn.get(i);
			if (nActual==null){
				if (nSyn==null){
					continue;
				}
				else {
					return false;
				}
			}
			else {
				if (nSyn==null){
					return false;
				}
				else if (!nSyn.partRoleEquals(nActual)){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static ArrayList<ArrayList<Node>> getNGrams(ArrayList<Node> nodeSequence, int nGram){
		ArrayList<ArrayList<Node>> all =new ArrayList<ArrayList<Node>>();
		for (int i=0; i<nodeSequence.size()-nGram+1;i++){
			ArrayList<Node> tmp = new ArrayList<Node>();
			tmp.addAll(nodeSequence.subList(i, i+nGram));
			all.add(tmp);
		}
		return all;
	}
	
	

	public static void compareGraphWithshape(Graph actualGraph, Graph synGraph, GraphComparisonData graphCompData, long postid){
		Node[] actualNodes = actualGraph.nodes;
		Edge[] actualEdges = actualGraph.edges;
		Node[] synNodes = synGraph.nodes;
		Edge[] synEdges = synGraph.edges;
		
//		Logger.log("actualNodes: " +Arrays.asList(actualNodes));
//		Logger.log("synNodes: " +Arrays.asList(synNodes));

		if (synGraph!=null){
			long numNodeActual = actualNodes.length;
			long numNodeSyn = synNodes.length;
			ArrayList<Integer> foundNodeIdxs = new ArrayList<>();
			for (Node node:actualNodes){
				for (int i=0;i<synNodes.length; i++){
//					if (!foundNodeIdxs.contains(i)){
						if (synNodes[i].partRoleEquals(node)){
							foundNodeIdxs.add(i);
							break;
						}
//					}
				}
			}
			long numSharedNode = foundNodeIdxs.size();
			
			long numEdgeActual = actualEdges.length;
			long numEdgeSyn = synEdges.length;
			ArrayList<Integer> foundEdgeIdxs = new ArrayList<Integer>();
			for (Edge edge:actualEdges){
				for (int i=0; i<synEdges.length;i++){
//					if (!foundEdgeIdxs.contains(i)){
						if (synEdges[i].partRoleEquals(edge)){
							foundEdgeIdxs.add(i);
							break;
						}
//					}
				}
			}
			long numSharedEdge = foundEdgeIdxs.size();
			graphCompData.update(numNodeActual, numNodeSyn, numSharedNode, numEdgeActual, numEdgeSyn, numSharedEdge, postid);
		}
		else {
			long numNodeActual = actualNodes.length;
			long numNodeSyn = 0;
			long numSharedNode = 0;
			long numEdgeActual = actualEdges.length;
			long numEdgeSyn = 0;

			long numSharedEdge = 0;

			graphCompData.update(numNodeActual, numNodeSyn, numSharedNode, numEdgeActual, numEdgeSyn, numSharedEdge, postid);

		}
	}
	
	public static void compareGraphWithShapeRelax(Graph actualGraph, Graph synGraph, GraphComparisonData graphCompData, long postid){
		Node[] actualNodes = actualGraph.nodes;
		Edge[] actualEdges = actualGraph.edges;
		Node[] synNodes = synGraph.nodes;
		Edge[] synEdges = synGraph.edges;
		
//		Logger.log("actualNodes: " +Arrays.asList(actualNodes));
//		Logger.log("synNodes: " +Arrays.asList(synNodes));

		if (synGraph!=null){
			long numNodeActual = actualNodes.length;
			long numNodeSyn = synNodes.length;
			ArrayList<Integer> foundNodeIdxs = new ArrayList<>();
			for (Node node:actualNodes){
				for (int i=0;i<synNodes.length; i++){
//					if (!foundNodeIdxs.contains(i)){
						if (synNodes[i].partRoleEquals(node)){
							foundNodeIdxs.add(i);
							break;
						}
//					}
				}
			}
			long numSharedNode = foundNodeIdxs.size();
			
			long numEdgeActual = actualEdges.length;
			long numEdgeSyn = synEdges.length;
			ArrayList<Integer> foundEdgeIdxs = new ArrayList<Integer>();
			
			LinkedHashMap<Node, LinkedHashSet<Node>> sourceSinksMap = new LinkedHashMap<Node, LinkedHashSet<Node>>();
			LinkedHashMap<Node, LinkedHashSet<Node>> sinkSourcesMap = new  LinkedHashMap<Node, LinkedHashSet<Node>>();
			for (Edge edge:synGraph.edges){
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
			
			long numSharedEdge = 0;
			for (Edge edge:actualEdges){
				Node sourceNode = edge.sourceNode;
				Node sinkNode = edge.sinkNode;
				boolean isFound = false;
				for (Node node1:synGraph.nodes){
					if (node1.roleEquals(sourceNode)){
						for (Node node2:synGraph.nodes){
							if (node2.roleEquals(sinkNode)){
								if(synGraph.isHavePath(node1, node2, sourceSinksMap, sinkSourcesMap)){
									isFound = true;
									numSharedEdge++;
									break;
								}
							}
						}
					}
					if (isFound)
						break;
				}
				if (!isFound)
					for (int i=0; i<synEdges.length;i++){
	//					if (!foundEdgeIdxs.contains(i)){
							if (synEdges[i].partRoleEquals(edge)){
								foundEdgeIdxs.add(i);
								break;
							}
	//					}
					}
			}
			
			numSharedEdge += foundEdgeIdxs.size();
			graphCompData.update(numNodeActual, numNodeSyn, numSharedNode, numEdgeActual, numEdgeSyn, numSharedEdge, postid);
		}
		else {
			long numNodeActual = actualNodes.length;
			long numNodeSyn = 0;
			long numSharedNode = 0;
			long numEdgeActual = actualEdges.length;
			long numEdgeSyn = 0;

			long numSharedEdge = 0;

			graphCompData.update(numNodeActual, numNodeSyn, numSharedNode, numEdgeActual, numEdgeSyn, numSharedEdge, postid);

		}
	}
}
