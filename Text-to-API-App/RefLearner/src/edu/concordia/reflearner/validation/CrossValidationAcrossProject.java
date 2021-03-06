package edu.concordia.reflearner.validation;

import static edu.concordia.reflearner.util.ClassUtil.getClassName;
import static edu.concordia.reflearner.util.ClassUtil.getExtractedMethodName;
import static edu.concordia.reflearner.util.ValidationUtil.loadRefactoringGraphMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import datastructure.Graph;
import edu.concordia.reflearner.graphs.RefactoredClassesDatabaseBuilder;
import storage.GraphDatabase;
import utils.GraphDrawingUtils;

public class CrossValidationAcrossProject {

	public static final String BASE_DIR = "/home/dharani/concordia/thesis/SLEMR/CrossValidationAcrossProjects";

	private String trainingProject;
	private String tGit;

	private String validationProject;
	private String vGit;

	public CrossValidationAcrossProject() {
		// trainingProject = tp;
		// tGit = tg;

		// validationProject = vp;
		// vGit = vg;

		// String pDirTraining = BASE_DIR + File.separator + tp;
		// String pDirValidation = BASE_DIR + File.separator + vp;

		try {

			GraphDatabase graphDBTraining = GraphDatabase
					.readGraphDatabase("/home/dharani/concordia/thesis/SLEMR/jackson-core/graphDB/graphdatabase.dat");
			// graphDBTraining.buildAllChild();
			// graphDBTraining.buildAllChildSimple();

			GraphDatabase graphDBValidation = GraphDatabase.readGraphDatabase(
					"/home/dharani/concordia/thesis/SLEMR/jackson-databind/graphDB/graphdatabase.dat");
			// graphDBValidation.buildAllChildSimple();

			List<Graph> matchedSubGraphsTraining = new ArrayList<Graph>();
			List<Graph> matchedSubGraphsValidation = new ArrayList<Graph>();
			List<Graph> methodGraphs = null;
			LinkedHashMap<RefactoringGit, Graph> refgVsMethodGraphs = loadRefactoringGraphMap(
					"/home/dharani/concordia/thesis/SLEMR/CrossValidation/jackson-databind/refgVsMethodGraph.ser");

			graphDBTraining.globalData.doStatistics();
			graphDBValidation.globalData.doStatistics();

			// Collection<Graph[]> allSubGraphs =
			// graphDBTraining.h1GraphMaps.values();

			// for (Graph[] graphs : allSubGraphs) {

			// for (Graph graph : graphs) {
			// System.out.println("graph " +
			// GraphDrawingUtils.creatDotStr(graph,
			// graphDBTraining.globalData));
			// }
			// }

			if (refgVsMethodGraphs != null) {
				methodGraphs = new ArrayList<Graph>(refgVsMethodGraphs.values());
				System.out.println("methodGraphs.size() " + methodGraphs.size());
			}

			int numMethodsMatched = 0;
			int numSubGraphsMatched = 0;

			List<RefactoringGit> methodNamesMatched = new ArrayList<RefactoringGit>();

			for (Graph mg : methodGraphs) {

				RefactoringGit refG = CrossValidationWithinProject.getRefactoringGit(mg, refgVsMethodGraphs);

				System.out.println("Extract method details");
				String className = getClassName(refG.getDescription());
				String methodName = getExtractedMethodName(refG.getDescription());
				String commitId = refG.getRevision().getIdCommit();

				System.out.println("className " + className);
				System.out.println("methodName " + methodName);
				System.out.println("commitId " + commitId);
				System.out.println("number of nodes " + mg.nodes.length);

				if (mg.numNodes() <= RefactoredClassesDatabaseBuilder.MIN_NODES) {

					Graph result = graphDBTraining.searchGraph(mg);

					if (result != null) {
						System.err.println("A graph with less than MIN_NODES matched " + result.numNodes());
						// numSubGraphsMatched++;
						// numMethodsMatched++;
						// methodNamesMatched.add(refG);

						// matchedSubGraphs.add(result);
					} else {
						System.err.println("Graph did not match.");
					}
				} else {

					List<Graph> subgraphs = mg.getAllSubGraphs(RefactoredClassesDatabaseBuilder.MAX_NODES,
							RefactoredClassesDatabaseBuilder.MAX_NEIGHBOUR_NODES, graphDBValidation.globalData);
					System.err.println("subgraphs size " + subgraphs.size());
					subgraphs.removeIf(g -> g.numNodes() < RefactoredClassesDatabaseBuilder.MIN_NODES);
					System.err.println("After removal subgraphs size " + subgraphs.size());

					boolean matched = false;

					// if (subgraphs.isEmpty()) {
					// subgraphs =
					// graph.getAllSubGraphs(RefactoredClassesDatabaseBuilder.MAX_NODES,
					// RefactoredClassesDatabaseBuilder.MAX_NEIGHBOUR_NODES,
					// graphDBValidation.globalData);
					// }

					for (Graph querySG : subgraphs) {

						//Graph result = graphDBTraining.searchGraphWithNodeLabels(querySG, graphDBValidation.globalData);
						Graph result = graphDBTraining.searchGraph(querySG);

						if (result != null) {
							System.err.println("subgraphs matched.");
							numSubGraphsMatched++;
							matchedSubGraphsTraining.add(result);
							matchedSubGraphsValidation.add(querySG);
							matched = true;
						} else {
							// System.err.println("Subgraph did not match");
						}
					}

					if (!matched) {
						// System.err.println("subgraphs did not match");

					} else {
						numMethodsMatched++;
						methodNamesMatched.add(refG);
					}
				}
			}

			System.out.println("numMethodsMatched " + numMethodsMatched);
			System.out.println("numSubGraphsMatched " + numSubGraphsMatched);
			System.out.println(methodNamesMatched);

			for (RefactoringGit refactoringGit : methodNamesMatched) {
				System.out.println(refactoringGit.getDescription());
			}

			for (int i = 0; i < matchedSubGraphsTraining.size(); i++) {

				System.err.println("Graph from training DB");
				System.out.println(
						GraphDrawingUtils.creatDotStr(matchedSubGraphsTraining.get(i), graphDBTraining.globalData));
				
				System.err.println("Graph from validation DB");
				System.out.println(
						GraphDrawingUtils.creatDotStr(matchedSubGraphsValidation.get(i), graphDBValidation.globalData));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new CrossValidationAcrossProject();
	}
}
