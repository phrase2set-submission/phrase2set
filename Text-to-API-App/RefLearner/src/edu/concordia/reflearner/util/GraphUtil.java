package edu.concordia.reflearner.util;

import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;

public class GraphUtil {
	public static boolean hasUnknownClassTypeNodes(Graph graph, GlobalData globalData) {
		boolean unknownFound = false;
		Node[] nodes = graph.nodes;
		Node[] arrayOfNode1;
		int j = (arrayOfNode1 = nodes).length;
		for (int i = 0; i < j; i++) {
			Node node = arrayOfNode1[i];
			if (node.getNodeLabel(globalData).toLowerCase().contains("unknown")) {
				unknownFound = true;
				break;
			}
		}
		return unknownFound;
	}
}
