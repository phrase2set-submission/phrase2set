/**
 * 
 */
package storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import utils.FileUtils;
import utils.Logger;
import config.GlobalConfig;
import datastructure.Graph;
import datastructure.Node;

/**
 * @author anhnt_000
 *
 */
public class NodeGraphDatabase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NodeGraphDatabase nodeGraphDB = new NodeGraphDatabase();
		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(GlobalConfig.graphDatabasePath);
		nodeGraphDB.createMapFromGraphDB(graphDB);
		FileUtils.writeObjectFile(nodeGraphDB, GlobalConfig.nodeGraphDatabasePath);
		
	}
	public HashMap<Integer, ArrayList<Node>> hValNodesMap = new HashMap<Integer, ArrayList<Node>>();
	public HashMap<Node, ArrayList<Graph>> nodeGraphsMap = new HashMap<Node, ArrayList<Graph>>();
	
	public Node findNode(Node node){
		int hVal = node.simpleHashCode();
		if(!hValNodesMap.containsKey(hVal)){
			return node;
		}
		for(Node aNode:hValNodesMap.get(hVal)){
			if(aNode.simpleRoleEquals(node)){
				return aNode;
			}
		}
		return node;
	}
	public void createMapFromGraphDB(GraphDatabase graphDB){
		int countGraph = 0;
		for(Integer hVal:graphDB.h1GraphMaps.keySet()){
			for(Graph graph:graphDB.h1GraphMaps.get(hVal)){
//				if (graph.projectIdxList.length<=1)
//					continue;
				countGraph++;
//				if (countGraph%1000==0){
//					System.out.println(countGraph + "  ");
//					if (countGraph%25000==0){
//						System.out.println();
//					}
//				}
				for(Node node: graph.nodes){
					if (node.getNodeLabel(graphDB.globalData).contains("<unknown"))
						continue;
					int hValue = node.simpleHashCode();
					if(hValNodesMap.containsKey(hValue)){
						boolean isFound = false;
						for(Node aNode:hValNodesMap.get(hValue)){
							if(aNode.simpleRoleEquals(node)){
								nodeGraphsMap.get(aNode).add(graph);
								isFound = true;
								break;
							}
						}
						if(!isFound){
							hValNodesMap.get(hValue).add(node);
							ArrayList<Graph> graphs = new ArrayList<Graph>();
							graphs.add(graph);
							nodeGraphsMap.put(node, graphs);
						}
					}
					else {
						ArrayList<Node> nodes = new ArrayList<Node>();
						nodes.add(node);
						hValNodesMap.put(hValue, nodes);
						ArrayList<Graph> graphs = new ArrayList<Graph>();
						graphs.add(graph);
						nodeGraphsMap.put(node, graphs);
					}
				}
			}
		}
		
//		for (Node node:nodeGraphsMap.keySet()){
//			if(node.getNodeLabel(graphDB.globalData).contains("ControlInfo")){
//				Logger.log("   " +node);
//			}
//		}
	}

	public int getNumContainingGraphs(Node node){
		int hVal = node.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal)){
			return 0;
		}
		for(Node aNode:hValNodesMap.get(hVal)){
			if(aNode.simpleRoleEquals(node))
				return nodeGraphsMap.get(aNode).size();
		}
		return 0;
	}
	
	public int getNumSharedGraphs(Node node1, Node node2){
		int hVal1 = node1.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal1)){
			return 0;
		}
		Node aNode1 = null;
		for(Node aNode:hValNodesMap.get(hVal1)){
			if(aNode.simpleRoleEquals(node1)){
				aNode1 = aNode;
				break;
			}
		}
		if (aNode1==null)
			return 0;
		
		int hVal2 = node2.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal2)){
			return 0;
		}
		Node aNode2 = null;
		for(Node aNode:hValNodesMap.get(hVal2)){
			if(aNode.simpleRoleEquals(node2)){
				aNode2 = aNode;
				break;
			}
		}
		if (aNode2==null)
			return 0;
		
		int countShared = 0;

		ArrayList<Graph> graphs1 = nodeGraphsMap.get(aNode1);
		ArrayList<Graph> graphs2 = nodeGraphsMap.get(aNode2);
		for(Graph graph1:graphs1){
			for(Graph graph2:graphs2){
				if(graph1==graph2){
					countShared++;
					break;
				}
			}

		}
		return countShared;

	}
	
	public int getNumSharedEdgeGraphs(Node node1, Node node2){
		int hVal1 = node1.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal1)){
			return 0;
		}
		Node aNode1 = null;
		for(Node aNode:hValNodesMap.get(hVal1)){
			if(aNode.simpleRoleEquals(node1)){
				aNode1 = aNode;
				break;
			}
		}
		if (aNode1==null)
			return 0;
		
		int hVal2 = node2.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal2)){
			return 0;
		}
		Node aNode2 = null;
		for(Node aNode:hValNodesMap.get(hVal2)){
			if(aNode.simpleRoleEquals(node2)){
				aNode2 = aNode;
				break;
			}
		}
		if (aNode2==null)
			return 0;
		
		int countShared = 0;

		ArrayList<Graph> graphs1 = nodeGraphsMap.get(aNode1);
		ArrayList<Graph> graphs2 = nodeGraphsMap.get(aNode2);
		for(Graph graph1:graphs1){
			if (graph1.nodes.length>2)
				continue;
			for(Graph graph2:graphs2){
				if(graph1==graph2){
					countShared+= graph1.methodIdxList.length;
					break;
				}
			}
			if (countShared>=1)
				break;
		}
		return countShared;

	}
	
	
	public ArrayList<Graph> getContain2Graphs(Node node1){
		int hVal1 = node1.simpleHashCode();
		if (!hValNodesMap.containsKey(hVal1)){
			return new ArrayList<Graph>();
		}
		Node aNode1 = null;
		for(Node aNode:hValNodesMap.get(hVal1)){
			if(aNode.simpleRoleEquals(node1)){
				aNode1 = aNode;
				break;
			}
		}
	
		ArrayList<Graph> graphs1 = new ArrayList<Graph>();
		for (Graph graph:nodeGraphsMap.get(aNode1)){
			if (graph.numNodes()==2)
				graphs1.add(graph);
		}
		
		return graphs1;

	}
}
