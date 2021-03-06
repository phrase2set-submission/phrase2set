package edu.concordia.reflearner.graphs;

import static edu.concordia.reflearner.util.GroumUtil.getMethodGraph;
import static edu.concordia.reflearner.util.GroumUtil.getMethodInfo;
import static edu.concordia.reflearner.util.GroumUtil.getTypeInfo;

import static main.DatabaseBuilderMain.normalizeStr;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import config.DatabaseConfig;
import data.MethodInfo;
import data.TypeInfo;
import datastructure.Edge;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;
import edu.concordia.reflearner.stats.MethodType;
import edu.concordia.reflearner.util.GraphUtil;
import storage.GraphDatabase;
import utils.DataUtils;
import utils.GraphDrawingUtils;

/**
 * Builds the database of method graphs
 * 
 * @author Dharani Kumar Palani
 *
 */
public class RefactoredClassesDatabaseBuilder {

	public static final int MAX_NODES = 8;
	public static final int MIN_NODES = 4;
	public static final int MAX_NEIGHBOUR_NODES = 8;

	private GraphDatabase graphDB;

	public static final String GRAPH_PATH = "";
	public static final boolean isDoCompactMethod = true;

	private String graphFilesLocation;
	private String projectName;
	private MethodType mt;

	private List<Graph> methodGraphs = new ArrayList<Graph>();

	private int numMethods = 0;

	public RefactoredClassesDatabaseBuilder(MethodType mt, String projectName, String graphFilesPath) {
		this.mt = mt;
		this.projectName = projectName;
		this.graphFilesLocation = graphFilesPath;
		graphDB = new GraphDatabase();

	}

	public void writeDatabase(String path) {
		if (path == null || path.isEmpty()) {
			graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePath);
		} else {
			graphDB.storeThisDatabase(path);
		}
	}

	public GraphDatabase getDatabase() {
		return graphDB;
	}

	public void processJavaFile(File location, String methodSignature) {
		// processFile(location.getAbsolutePath(), methodSignature);
		graphDB.buildAllChildSimple();
		graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePath);
	}

	public List<Graph> getMethodGraphs() {
		return methodGraphs;
	}

	/**
	 * Return the status of the processing the Java file to extract GrouM
	 * 
	 * @param filePath
	 * @param nameOfMethod
	 * @param fqcn
	 * @param methodParams
	 * @return
	 */
	public int processFile(String filePath, String nameOfMethod, String fqcn, List<String> methodParams) {
		int status = 0;

		System.out.println("fileName " + filePath + ", fqcn " + fqcn + ", methodSignature " + nameOfMethod);
		TypeInfo ti = getTypeInfo(filePath, fqcn);

		if (ti != null) {
			System.err.println("class found");

			MethodInfo mi = getMethodInfo(ti, nameOfMethod, fqcn, methodParams);
			if (mi != null) {

				Graph methodGraph = getMethodGraph(mi, graphDB.globalData);
				System.out.println("methodGraph.numNodes() " + methodGraph.numNodes());

				if (GraphUtil.hasUnknownClassTypeNodes(methodGraph, graphDB.globalData)) {
					System.err.println("Method has unknown nodes");
					status = -1;
				} else {

					if (methodGraph.numNodes() <= MIN_NODES) {
						System.out.println(
								"Methodgraph is of size <= " + MIN_NODES + ". Hence adding the method graph as is.");

						String combinedName = normalizeStr(fqcn) + "." + normalizeStr(nameOfMethod) + "::" + numMethods;
						numMethods++;
						methodGraphs.add(methodGraph);

						graphDB.addGraphWithOtherData(methodGraph, 1, projectName, combinedName);
					} else {
						if (graphFilesLocation != null && !graphFilesLocation.isEmpty()) {
							System.out
									.println("Creating dot file for the method " + nameOfMethod + " of class " + fqcn);
							String fileName = graphFilesLocation + File.separator + fqcn + "." + nameOfMethod + ".dot";
							GraphDrawingUtils.outputDotFile(methodGraph, fileName, graphDB.globalData);
						}

						String combinedName = normalizeStr(fqcn) + "." + normalizeStr(nameOfMethod) + "::" + numMethods;
						numMethods++;
						methodGraphs.add(methodGraph);
						// methodGraphs.add(methodGraph);

						writeAllMethodSubGraphsToGraphDB(methodGraph, MAX_NODES, MAX_NEIGHBOUR_NODES, combinedName);
					}
				}
			} else {
				System.err.println(nameOfMethod + " method is not found by Recoder");
				status = -2;
			}
		} else {
			System.err.println(fqcn + " class is not found by Recoder");
			status = -3;
		}

		return status;
	}

	public void writeAllMethodSubGraphsToGraphDB(Graph methodGraph, int maxGraphSize, int maxCountNode,
			final String methodName) {
		System.out.println("RefactoredClassesDatabaseBuilder.writeAllMethodSubGraphsToGraphDB() " + methodName);

		ArrayList<Graph> allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode, graphDB.globalData);
		allSubGraphs.removeIf(g -> g.numNodes() < MIN_NODES);
		System.out.println("number of subgraphs " + allSubGraphs.size());

		if (allSubGraphs.isEmpty()) {
			System.err.println("No subgraphs of size " + MIN_NODES + " got generated. Special case graphs");
			// graphDB.addGraphWithOtherData(aGraph, count, methodName,
			// methodName);

			allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode, graphDB.globalData);
			for (Graph graph : allSubGraphs) {
				// String dotStr = GraphDrawingUtils.creatDotStr(graph,
				// graphDB.globalData);
				// System.out.println("subgraph dotStr ");
				// System.out.println(dotStr);
				graphDB.addGraphWithOtherData(graph, 1, projectName, methodName);
			}

		} else {
			// methodGraphs.add(methodGraph);

			for (Graph graph : allSubGraphs) {
				// String dotStr = GraphDrawingUtils.creatDotStr(graph,
				// graphDB.globalData);
				// System.out.println("subgraph dotStr ");
				// System.out.println(dotStr);
				graphDB.addGraphWithOtherData(graph, 1, projectName, methodName);
			}
		}
	}

	private LinkedHashMap<String, Graph> convertAllGroumsToGraphs(LinkedHashMap<String, MethodInfo> methodInfoMap,
			GlobalData globalData) {
		LinkedHashMap<String, Graph> methodGraphMap = new LinkedHashMap<String, Graph>();
		for (String methodName : methodInfoMap.keySet()) {
			MethodInfo methodInfo = methodInfoMap.get(methodName);

			Graph methodGraph = DataUtils.convertGroumToGraph(methodInfo, globalData);
			methodGraph.removeRedundancy();
			methodGraphMap.put(methodName, methodGraph);
		}
		return methodGraphMap;
	}

	public static String getEdgeLabel(Edge edge, GlobalData globalData) {

		Node sourceNode = edge.sourceNode;
		Node sinkNode = edge.sinkNode;

		return sourceNode.getNodeLabel(globalData) + "->" + sinkNode.getNodeLabel(globalData);
	}
}
