package edu.concordia.t2api.android;

import static edu.concordia.reflearner.validation.AllMain.normalizeStr;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bson.Document;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import data.MethodInfo;
import data.NodeSequenceInfoMap;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;
import edu.concordia.reflearner.util.GitUtil;
import edu.concordia.reflearner.util.GraphUtil;
import edu.concordia.reflearner.validation.AllMain;
import edu.concordia.t2api.android.db.MongoManager;
import groumvisitors.JavaGroumVisitor;
import storage.GraphDatabase;
import utils.DataUtils;
import utils.GraphDrawingUtils;
import utils.Logger;

public class AndroidGraphCollector {

	public static final String GRAPHDB_FILE = "/home/dharani/concordia/thesis/CodeToTask/android_graph_database.dat";
	public static final String CHECKOUT_DIR = "/home/dharani/concordia/thesis/CodeToTask/gitprojects/";
	public static final String DOTS_FILES = "DOT_FILES";
	public static final int QUERY_LIMIT = 2;

	public static int NUM_CLASSES = 0;
	public static int NUM_METHODS = 0;
	public static int NUM_GRAPHS = 0;

	public static final int MIN_GRAPH_SIZE = 3;
	public static final int MAX_GRAPH_SIZE = 8;
	public static final int MAX_NEIGHBOUR_NODES = 20;

	public static final String[] ANDROID_PACKAGE_NAMES = AndroidPackageNames.androidPackageNames.toArray(new String[0]);

	public static void main(String[] args) {

		MongoCollection<Document> col = MongoManager.getDatabase().getCollection(MongoManager.COLLECTION_PROJECTS);
		// Document query = new Document("analyzed", false);
		// FindIterable<Document> result = col.find(query).sort(new
		// Document("stargazers_count", -1))
		// .projection(Projections.include(new String[] { "clone_url",
		// "full_name", "name", "default_branch" }))
		// .limit(QUERY_LIMIT);

		Document query = new Document("clone_url", "https://github.com/JohnPersano/SuperToasts.git");
		// Document query = new Document("clone_url",
		// "https://github.com/Todd-Davies/ProgressWheel.git");
		// Document query = new Document("clone_url",
		// "https://github.com/google/iosched.git");
		FindIterable<Document> result = col.find(query);

		result.forEach(new Consumer<Document>() {
			@Override
			public void accept(Document d) {
				System.out.println("d " + d);
				try {
					final String path = checkoutProject(d.getString("name"), d.getString("clone_url"),
							d.getString("default_branch"));

					new File(path + File.separator + DOTS_FILES).mkdir();

					collectGraphs(path, d.getString("name"));

					col.updateOne(new Document("clone_url", d.getString("clone_url")),
							new Document("$set", new Document("analyzed", Boolean.TRUE)));

					try {
						// Logger.log("\tDelete project dir recursively");
						// FileUtils.deleteDirectoryContent(new
						// File(GlobalConfig.dummyDir));
						// org.apache.commons.io.FileUtils.forceDelete(new
						// File(GlobalConfig.dummyDir));
						// Thread.sleep(300);
						NodeSequenceInfoMap.clearAll();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static String checkoutProject(final String projectName, final String clone_url, final String defaultBranch)
			throws Exception {

		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.cloneIfNotExists(CHECKOUT_DIR + File.separator + projectName, clone_url);
		GitUtil.resetRepo(new File(CHECKOUT_DIR + File.separator + projectName), defaultBranch);
		return CHECKOUT_DIR + File.separator + projectName;
	}

	public static synchronized void collectGraphs(final String path, final String project) {

		JavaGroumVisitor jgv = new JavaGroumVisitor();
		jgv.doMain(path);

		GraphDatabase graphDB = null;
		if (new File(GRAPHDB_FILE).exists()) {
			graphDB = GraphDatabase.readGraphDatabase(GRAPHDB_FILE);
		} else {
			graphDB = new GraphDatabase();
		}

		final List<TypeInfo> allTypeList = jgv.allTypeList;

		for (TypeInfo typeInfo : allTypeList) {
			String packageName = typeInfo.packageDec;
			packageName = packageName != null ? packageName.trim() : "";
			String className = typeInfo.typeName;
			className = className != null ? className.trim() : "";

			System.err.println("packageName " + packageName);
			System.err.println("className " + className);

			NUM_CLASSES++;

			List<MethodInfo> methodList = typeInfo.methodDecList;
			LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<>();

			for (MethodInfo method : methodList) {
				// Logger.log(count);
				String methodName = method.methodName;
				String combinedName = normalizeStr(project) + "::" + normalizeStr(className) + "."
						+ normalizeStr(methodName) + "::" + NUM_METHODS;
				NUM_METHODS++;

				DataUtils.compactGroum(method);
				methodInfoMap.put(combinedName, method);
			}

			LinkedHashMap<String, Graph> methodGraphMap = AllMain.convertAllGroumsToGraphs(methodInfoMap,
					graphDB.globalData);
			Logger.log("\t\tmethodGraphMap size: " + methodGraphMap.size());
			if (className.equals("com.todddavies.components.progressbar.ProgressWheel")) {
				Set<String> keyset = methodGraphMap.keySet();
				for (String string : keyset) {
					System.err.println("methodName " + string);
					System.err.println(GraphDrawingUtils.creatDotStr(methodGraphMap.get(string), graphDB.globalData));
				}
			}
			addGraphsToDatabase(methodGraphMap, MAX_GRAPH_SIZE, MAX_NEIGHBOUR_NODES, graphDB, project, methodInfoMap);

		}

		graphDB.storeThisDatabase(GRAPHDB_FILE);
	}

	public static void addGraphsToDatabase(LinkedHashMap<String, Graph> methodGraphMap, int maxGraphSize,
			int maxCountNode, GraphDatabase database, String projectName,
			LinkedHashMap<String, MethodInfo> methodInfoMap) {
		// LinkedHashMap<String, ArrayList<Graph>> allMethodGraphs = new
		// LinkedHashMap<>();
		int count = 1;
		for (String methodName : methodGraphMap.keySet()) {
			System.out.print(count + " ");
			if (count % 100 == 0) {
				System.out.println();
			}
			count++;
			MethodInfo mi = methodInfoMap.get(methodName);
			Graph methodGraph = methodGraphMap.get(methodName);

			ArrayList<Graph> allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode,
					database.globalData);

			// allMethodGraphs.put(methodName, allSubGraphs);
			for (Graph graph : allSubGraphs) {

				if (graph.numNodes() >= MIN_GRAPH_SIZE) {
					if (!GraphUtil.hasUnknownClassTypeNodes(graph, database.globalData)) {
						if (isGraphComposedOfOnlyAndroidAPI(graph, database.globalData)) {
							boolean isAdded = database.addGraphWithOtherData(graph, 1, projectName, methodName);
							if (isAdded) {
								final String dotPath = CHECKOUT_DIR + File.separator + projectName + File.separator
										+ DOTS_FILES + File.separator + "graph_" + NUM_GRAPHS + ".dot";
								GraphDrawingUtils.outputDotFile(graph, dotPath, database.globalData);
								NUM_GRAPHS++;
							}
						}
					}
				}
			}
		}
	}

	public static boolean isGraphComposedOfOnlyAndroidAPI(Graph graph, GlobalData globalData) {
		boolean isAndroidAPIGraph = true;
		Node[] nodes = graph.nodes;
		BitSet allControlNodes = new BitSet(nodes.length);

		int bitIndex = 0;
		for (Node node : nodes) {
			boolean isAndroidAPINode = false;

			String nodeText = node.getNodeLabel(globalData);
			if (node.nodeRole == 3) {
				int startIdx = nodeText.lastIndexOf("#");
				nodeText = nodeText.substring(0, startIdx);
			}

			if (nodeText.contains(AndroidPackageNames.CONTROL_NODE)) {
				allControlNodes.set(bitIndex);
			} else {
				allControlNodes.clear(bitIndex);
			}

			for (String concernedLib : ANDROID_PACKAGE_NAMES) {
				if (nodeText.startsWith(concernedLib)) {
					isAndroidAPINode = true;
					break;
				}
			}
			if (!isAndroidAPINode) {
				isAndroidAPIGraph = false;
				break;
			}
			bitIndex++;
		}

		if (isAndroidAPIGraph) {
			// not efficient, just a hack
			boolean isAllBitSet = true;
			for (int i = 0; i < nodes.length; i++) {
				if (!allControlNodes.get(i)) {
					isAllBitSet = false;
					break;
				}
			}
			// if (allControlNodes.length() == allControlNodes.nextClearBit(0))
			// {
			if (isAllBitSet) {
				// System.err.println("All control nodes graph");
				// System.err.println(GraphDrawingUtils.creatDotStr(graph,
				// globalData));
				isAndroidAPIGraph = false;
			}
		}

		return isAndroidAPIGraph;
	}

}
