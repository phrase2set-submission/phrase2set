package edu.concordia.reflearner.validation;

import static edu.concordia.reflearner.util.ClassUtil.getClassName;
import static edu.concordia.reflearner.util.ClassUtil.getExtractedMethodName;
import static edu.concordia.reflearner.util.DBUtil.getProjectDefaultBranch;
import static edu.concordia.reflearner.util.ValidationUtil.loadRefactoringGraphMap;
import static edu.concordia.reflearner.util.GitUtil.DEFAULT_BRANCH_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.ListUtils;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import datastructure.Graph;
import edu.concordia.reflearner.graphs.RefactoredClassesDatabaseBuilder;
import edu.concordia.reflearner.graphs.RefactoredClassesExtractor;
import edu.concordia.reflearner.stats.MethodType;
import edu.concordia.reflearner.util.GraphUtil;
import storage.GraphDatabase;
import utils.GraphDrawingUtils;

/**
 * Performs cross validation within one project.
 * 
 * @author Dharani Kumar Palani
 *
 */
public class CrossValidationWithinProject {
	public static final String CROSS_VALIDATION_DIR = "/home/dharani/concordia/thesis/SLEMR/CrossValidation";
	public static final String FOLD = "_fold";
	public static final String GRAPH = "graphDB";
	public static final String TEMP = "tmp";
	public static final String PROJECTS = "project";
	public static final String DOTS = "dots";
	public static final int K = 10;

	private String projectName;
	private String gitURL;

	private String projectDir;

	private Map<RefactoringGit, Graph> refgVsMethodGraphs = null;
	private List<Graph> methodGraphs = null;
	private GraphDatabase graphDB = null;

	private boolean useSerializedFile = true;

	private ProjectValidationStatistics ps = null;

	public CrossValidationWithinProject(String pn, String gu) {

		this.projectName = pn;
		this.gitURL = gu;
		ps = new ProjectValidationStatistics(projectName, gitURL);

		projectDir = CROSS_VALIDATION_DIR + File.separator + pn;
		new File(projectDir).mkdir();
		new File(CROSS_VALIDATION_DIR + File.separator + TEMP).mkdir();

		for (int i = 1; i <= K; i++) {
			new File(projectDir + File.separator + i + FOLD).mkdir();
			new File(projectDir + File.separator + i + FOLD + File.separator + GRAPH).mkdir();
			new File(projectDir + File.separator + i + FOLD + File.separator + DOTS).mkdir();
		}

		getMethodGraphs();
		System.out.println("# of methodGraphs " + methodGraphs.size());

		// split now within K graphs
		Collections.shuffle(methodGraphs);

		List<List<Graph>> partitions = ListUtils.partition(methodGraphs, methodGraphs.size() / K);
		System.out.println("partitions " + partitions.size());

		for (int i = 0; i < K; i++) {
			List<Graph> validationSet = partitions.get(i);

			List<Graph> trainingSet = new ArrayList<Graph>();
			for (int j = 0; j < K; j++) {
				if (j != i) {
					trainingSet.addAll(partitions.get(j));
				}
			}

			System.out.println("K == " + (i + 1));
			System.out.println("# trainingSet " + trainingSet.size());
			System.out.println("# validationSet " + validationSet.size());

			GraphDatabase graphDBNew = train(trainingSet);
			System.out.println("Training done. Validation started on " + (i + 1));
			testOnValidationSet(graphDBNew, validationSet, i + 1, trainingSet);
		}

		ps.calculateAndPrintOverallStats();
	}

	private void testOnValidationSet(GraphDatabase graphDBIn, List<Graph> validationSet, int fold,
			List<Graph> trainingSet) {

		Map<Integer, List<Graph>> sgWithFrequencies = new TreeMap<Integer, List<Graph>>(Comparator.reverseOrder());

		StatsPerFold stats = new StatsPerFold();
		int numMethodsMatched = 0;
		int numSubGraphsMatched = 0;

		TreeMap<Integer, Integer> distributionNumNodesVsNumGraphs = new TreeMap<Integer, Integer>(
				Comparator.reverseOrder());

		for (Graph tg : validationSet) {
			// System.out.println(GraphDrawingUtils.creatDotStr(tg,
			// graphDB.globalData));

			RefactoringGit refG = getRefactoringGit(tg, this.refgVsMethodGraphs);

			System.out.println("Extract method details");
			String className = getClassName(refG.getDescription());
			String methodName = getExtractedMethodName(refG.getDescription());
			String commitId = refG.getRevision().getIdCommit();

			System.out.println("className " + className);
			System.out.println("methodName " + methodName);
			System.out.println("commitId " + commitId);

			if (tg.numNodes() < RefactoredClassesDatabaseBuilder.MIN_NODES) {

				Graph result = graphDBIn.searchGraph(tg);

				if (result != null) {
					System.err.println("Subgraph of size " + result.numNodes() + " matched. Ignoring...");
					// numSubGraphsMatched++;
					// numMethodsMatched++;
				} else {
					System.err.println("Graph did not match.");
				}

			} else {

				List<Graph> subgraphs = tg.getAllSubGraphs(RefactoredClassesDatabaseBuilder.MAX_NODES,
						RefactoredClassesDatabaseBuilder.MAX_NEIGHBOUR_NODES, graphDB.globalData);
				subgraphs.removeIf(g -> g.numNodes() < RefactoredClassesDatabaseBuilder.MIN_NODES);

				int count = 0;
				boolean matched = false;

				for (Graph testSG : subgraphs) {
					Graph result = graphDBIn.searchGraph(testSG);
					// Graph result =
					// graphDBIn.searchGraphWithNodeLabels(testSG,
					// graphDB.globalData);

					if (result != null) {
						numSubGraphsMatched++;

						// A graph matched in the database.
						List<Graph> frequencyList = sgWithFrequencies.get(result.count);

						if (frequencyList == null) {
							frequencyList = new ArrayList<Graph>();
							sgWithFrequencies.put(result.count, frequencyList);
						}
						frequencyList.add(result);

						if (refG != null) {

							String dotFile = projectDir + File.separator + fold + FOLD + File.separator + DOTS
									+ File.separator + className + "." + methodName + count++ + ".dot";
							GraphDrawingUtils.outputDotFile(result, dotFile, graphDB.globalData);
							System.err.println("Sub graph matched " + dotFile);
							System.err.println("frequency in database " + result.count);
						}
						matched = true;

						Integer numGraphs = distributionNumNodesVsNumGraphs.get(result.numNodes());
						if (numGraphs == null) {
							distributionNumNodesVsNumGraphs.put(result.numNodes(), new Integer(1));
						} else {
							distributionNumNodesVsNumGraphs.put(result.numNodes(), new Integer(numGraphs + 1));
						}
					}
				}

				if (!matched) {
					System.err.println("subgraphs did not match");

				} else {
					numMethodsMatched++;
				}
			}
		}

		stats.setNumMethodsMatched(numMethodsMatched);
		stats.setTotalMethods(validationSet.size());
		stats.setTotalSubGraphsMatched(numSubGraphsMatched);

		Set<Integer> keySet = sgWithFrequencies.keySet();
		Map<Integer, Integer> frequenciesVsNumGraphs = new TreeMap<Integer, Integer>(Comparator.reverseOrder());

		int count = 0;
		for (Integer integer : keySet) {
			System.out.println("Frequency " + integer);
			List<Graph> graphs = sgWithFrequencies.get(integer);
			System.out.println("Number of graphs " + graphs.size());
			frequenciesVsNumGraphs.put(integer, sgWithFrequencies.get(integer).size());
			
			for(Graph graph : graphs) {
				System.out.println(GraphDrawingUtils.creatDotStr(graph, graphDB.globalData));
				if(count >= 5) {
					break;
				}
				
				count++;
			}
			
			if(count >= 5) {
				break;
			}
			
		}

		stats.setFrequencyVsNumber(frequenciesVsNumGraphs);

		ps.addMatchInfoPerFold(stats);

		System.out.println("Distribution of number of nodes versus number of graphs");
		keySet = distributionNumNodesVsNumGraphs.keySet();
		for (Integer integer : keySet) {
			System.out.println("NumNodes " + integer);
			System.out.println("Number of graphs  " + distributionNumNodesVsNumGraphs.get(integer));
		}
		
		
	}

	private GraphDatabase train(List<Graph> methodGraphs) {

		GraphDatabase graphDBNew = new GraphDatabase();

		Set<Entry<RefactoringGit, Graph>> entry = refgVsMethodGraphs.entrySet();

		for (Graph graph : methodGraphs) {

			for (Entry<RefactoringGit, Graph> entry2 : entry) {

				if (entry2.getValue().equals(graph)) {
					RefactoringGit refG = entry2.getKey();

					List<Graph> subgraphs = graph.getAllSubGraphs(RefactoredClassesDatabaseBuilder.MAX_NODES,
							RefactoredClassesDatabaseBuilder.MAX_NEIGHBOUR_NODES, graphDB.globalData);
					subgraphs.removeIf(g -> g.nodes.length <= RefactoredClassesDatabaseBuilder.MIN_NODES);

					for (Graph sg : subgraphs) {
						graphDBNew.addGraphWithOtherData(sg, 1, projectName,
								getExtractedMethodName(refG.getDescription()));
					}
				}

			}
		}

		return graphDBNew;

		/*
		 * for (Graph graph : methodGraphs) {
		 * 
		 * List<Graph> subgraphs =
		 * graph.getAllSubGraphs(RefactoredClassesDatabaseBuilder.MAX_NODES,
		 * RefactoredClassesDatabaseBuilder.MAX_NEIGHBOUR_NODES,
		 * graphDB.globalData); subgraphs.removeIf(g -> g.nodes.length <=
		 * RefactoredClassesDatabaseBuilder.MIN_NODES);
		 * 
		 * for (Graph sg : subgraphs) { graphDB.addGraph(sg); } }
		 */
	}

	private void getMethodGraphs() {
		if (useSerializedFile) {
			refgVsMethodGraphs = loadRefactoringGraphMap(CROSS_VALIDATION_DIR, projectName);

			if (refgVsMethodGraphs != null) {
				methodGraphs = new ArrayList<Graph>(refgVsMethodGraphs.values());
				/// home/dharani/concordia/thesis/SLEMR/nodebox/graphDB/graphdatabase.dat

				String graphDatabaseLoc = "/home/dharani/concordia/thesis/SLEMR" + File.separator + this.projectName
						+ File.separator + "graphDB/graphdatabase.dat";

				graphDB = GraphDatabase.readGraphDatabase(graphDatabaseLoc);
			}
		} else {
			String project_branch = getProjectDefaultBranch(projectName);

			RefactoredClassesExtractor rce = new RefactoredClassesExtractor(projectDir + File.separator + PROJECTS,
					gitURL, projectName, MethodType.EXTRACTED_METHOD, CROSS_VALIDATION_DIR,
					project_branch != null && !project_branch.isEmpty() ? project_branch : DEFAULT_BRANCH_NAME);
			rce.processExtractMethodRefactorings();

			methodGraphs = rce.getAllMethodGraphs();
			refgVsMethodGraphs = rce.getRefGVsMethodGraph();

			graphDB = rce.getGraphDatabase();
		}
	}

	public static RefactoringGit getRefactoringGit(Graph g, Map<RefactoringGit, Graph> map) {
		RefactoringGit refG = null;
		Set<Entry<RefactoringGit, Graph>> entrySet = map.entrySet();

		for (Entry<RefactoringGit, Graph> entry : entrySet) {
			if (entry.getValue().equals(g)) {
				refG = entry.getKey();
			}
		}

		return refG;
	}

	public static void main(String[] args) {

		// new CrossValidationWithinProject("nodebox",
		// "https://github.com/nodebox/nodebox.git");
		// new CrossValidationWithinProject("mongo-java-driver",
		// "https://github.com/mongodb/mongo-java-driver.git");

//		new CrossValidationWithinProject("Activiti", "https://github.com/Activiti/Activiti.git");
		
		new CrossValidationWithinProject("morphia", "https://github.com/mongodb/morphia.git");

		// new CrossValidationWithinProject("bitcoinj",
		// "https://github.com/bitcoinj/bitcoinj.git");

		// new CrossValidationWithinProject("heritrix3",
		// "https://github.com/internetarchive/heritrix3.git");

		// new CrossValidationWithinProject("browser-android",
		// "https://github.com/brave/browser-android.git");

		// new CrossValidationWithinProject("guava",
		// "https://github.com/google/guava.git");

		// new CrossValidationWithinProject("okhttp",
		// "https://github.com/square/okhttp.git");

		// new CrossValidationWithinProject("jackson-core",
		// "https://github.com/FasterXML/jackson-core.git");

		// new CrossValidationWithinProject("jackson-databind",
		// "https://github.com/FasterXML/jackson-databind.git");
	}
}