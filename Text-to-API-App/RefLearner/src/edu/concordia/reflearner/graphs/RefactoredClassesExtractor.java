package edu.concordia.reflearner.graphs;

import static edu.concordia.reflearner.util.ClassUtil.getClassName;
import static edu.concordia.reflearner.util.ClassUtil.getExtractedMethodName;
import static edu.concordia.reflearner.util.ClassUtil.getImpactedMethodName;
import static edu.concordia.reflearner.util.ClassUtil.getNameOfTheClass;
import static edu.concordia.reflearner.util.ClassUtil.getSystemPathOfFile;
import static edu.concordia.reflearner.util.DBUtil.getAllExtractMethodRefactorings;
import static edu.concordia.reflearner.util.DBUtil.getConnection;
import static edu.concordia.reflearner.util.DBUtil.getProjectDefaultBranch;
import static edu.concordia.reflearner.util.DBUtil.insertIntoUndetectableTable;
import static edu.concordia.reflearner.util.GitUtil.DEFAULT_BRANCH_NAME;
import static edu.concordia.reflearner.util.GitUtil.checkout;
import static edu.concordia.reflearner.util.GitUtil.resetRepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.util.GitServiceImpl;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import datastructure.Graph;
import edu.concordia.reflearner.stats.MethodType;
import edu.concordia.reflearner.util.MethodParameterUtil;
import storage.GraphDatabase;

/**
 * 
 * This class will identify the list of classes that has undergone extract
 * method refactoring and will process them with GrouMiner.
 * 
 * @author Dharani Kumar Palani
 *
 */
public class RefactoredClassesExtractor {

	public static final int VALID_LIMIT = Integer.MAX_VALUE;

	public static final String GRAPHDB = "graphDB";
	public static final String GRAPHFILE = "graphdatabase.dat";
	public static final String TEMP = "tmp";

	private String projectCheckedOutDir;
	private String cloneURL;

	private Repository repo;
	private RefactoredClassesDatabaseBuilder refDB;
	private MethodType mt;
	private String projectName;
	private String defaultBranch;
	private String baseDir;

	private int validRefactoringsCount = 0;

	private boolean trackUndetectedRefactorings = false;

	// private Map<String, Graph> commitIdVersusMethodGraph = new
	// HashMap<String, Graph>();
	private LinkedHashMap<RefactoringGit, Graph> refgVsMethodGraph = new LinkedHashMap<RefactoringGit, Graph>();

	public RefactoredClassesExtractor(final String pDir, final String cloneURL, final String projectName,
			final MethodType mt, final String baseDir, final String defaultBranch) {
		this.projectCheckedOutDir = pDir;
		this.cloneURL = cloneURL;
		this.mt = mt;
		this.projectName = projectName;
		this.baseDir = baseDir;
		this.defaultBranch = defaultBranch;

		if (!(new File(baseDir).exists())) {
			new File(baseDir).mkdir();
		}

		String graphDir = baseDir + File.separator + projectName + File.separator + GRAPHDB;
		new File(graphDir).mkdir();
		refDB = new RefactoredClassesDatabaseBuilder(mt, projectName, graphDir);
	}

	public GraphDatabase getGraphDatabase() {
		return refDB.getDatabase();
	}

	public void setTrackUndetectableRefactorings(boolean flag) {
		trackUndetectedRefactorings = flag;
	}

	public void serializeMaps(String path) {
		if (new File(path).exists() && new File(path).isDirectory()) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(new File(path + File.separator + "refgVsMethodGraph.ser")));
				oos.writeObject(refgVsMethodGraph);
				oos.flush();
				oos.close();
				System.out.println(
						"Written serialized refactored graphs to " + path + File.separator + "refgVsMethodGraph.ser");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void processExtractMethodRefactorings() {
		GitServiceImpl gs = new GitServiceImpl();
		try {
			repo = gs.cloneIfNotExists(projectCheckedOutDir, cloneURL);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String tempDirPath = baseDir + File.separator + TEMP;
		File tmpDirectory = new File(tempDirPath);
		tmpDirectory.mkdir();
		System.out.println("tmpDirectory " + tmpDirectory);

		Connection conn = getConnection();

		try {
			Map<Long, RefactoringGit> extractMethodRefactorings = getAllExtractMethodRefactorings(this.projectName);
			File destDir = new File(projectCheckedOutDir);

			Set<Long> refactoringIds = extractMethodRefactorings.keySet();
			System.out.println("Number of refactorings " + refactoringIds.size());

			for (Long refId : refactoringIds) {
				if (validRefactoringsCount >= VALID_LIMIT)
					break;
				RefactoringGit refG = extractMethodRefactorings.get(refId);

				resetRepo(destDir, defaultBranch);

				String className = getClassName(refG.getDescription());
				String methodName = null;
				String commitId = null;

				if (MethodType.EXTRACTED_METHOD.equals(mt)) {
					methodName = getExtractedMethodName(refG.getDescription());
					commitId = refG.getRevision().getIdCommit();

				} else if (MethodType.ORIG_METHOD.equals(mt)) {
					methodName = getImpactedMethodName(refG.getDescription());
					commitId = refG.getRevision().getIdCommitParent();
				}

				checkout(destDir, repo, gs, commitId);

				List<String> parameters = MethodParameterUtil.getMethodParameters(repo, destDir, commitId, methodName,
						className, refG.getDescription(), this.cloneURL);
				resetRepo(destDir, defaultBranch);
				checkout(destDir, repo, gs, commitId);

				String fileName = getSystemPathOfFile(className, projectCheckedOutDir);
				System.out.println("fileName " + fileName);

				if (fileName != null) {
					File classFile = new File(fileName);
					if (classFile.exists()) {
						processJavaFile(conn, tempDirPath, fileName, className, methodName, parameters, refG);
					} else {
						System.err.println("Skipping the checkout " + commitId + ", unable to restore the version.");
						if (trackUndetectedRefactorings) {
							insertIntoUndetectableTable(conn, projectName, refG,
									"Unable to checkout to the required version. Error with Git.");
						}
					}
				} else {
					System.err.println("Skipping the className " + className + " as it might be an inner class.");
					if (trackUndetectedRefactorings) {
						insertIntoUndetectableTable(conn, projectName, refG,
								"Refactoring happened within a inner class");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processJavaFile(final Connection conn, final String tempDirPath, final String fileName,
			final String className, final String methodName, List<String> methodParams, RefactoringGit refG)
			throws Exception {
		int status = 0;
		// For now only copying the Java file involved in
		// the refactoring and not the whole project

		File tmpJavaFile = new File(tempDirPath + File.separatorChar + getNameOfTheClass(className) + ".java");
		Files.copy(new File(fileName).toPath(), tmpJavaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		status = refDB.processFile(tempDirPath, methodName, className, methodParams);

		tmpJavaFile.delete();

		if (status < 0) {

			if (trackUndetectedRefactorings) {

				if (status == -1) {
					insertIntoUndetectableTable(conn, projectName, refG,
							"Method graph had unknown nodes. Recoder failed to recognize data types");
				} else if (status == -2) {
					insertIntoUndetectableTable(conn, projectName, refG,
							"Method is not found or signature mismatch between recoder and refminer");
				} else if (status == -3) {
					insertIntoUndetectableTable(conn, projectName, refG, "Class is not found by recoder");
				} else if (status == -4) {
					insertIntoUndetectableTable(conn, projectName, refG, "Method graph has less number of nodes.");
				}
			}
		} else {
			int index = refDB.getMethodGraphs().size();
			Graph methodGraph = refDB.getMethodGraphs().get(index - 1);
			// System.err.println("refG " + refG.hashCode());
			// System.err.println("commitId " +
			// refG.getRevision().getIdCommit());
			// System.err.println("methodGraph " + methodGraph.hashCode());
			// commitIdVersusMethodGraph.put(refG.getRevision().getIdCommit(),
			// methodGraph);
			validRefactoringsCount++;
			refgVsMethodGraph.put(refG, methodGraph);
		}
	}

	/*
	 * public Map<String, Graph> getCommitVsMethodGraph() { return
	 * commitIdVersusMethodGraph; }
	 */

	public Map<RefactoringGit, Graph> getRefGVsMethodGraph() {
		return refgVsMethodGraph;
	}

	public void writeGraphDatabase() {
		refDB.writeDatabase(
				baseDir + File.separator + projectName + File.separator + GRAPHDB + File.separator + GRAPHFILE);
	}

	public List<Graph> getAllMethodGraphs() {
		return this.refDB.getMethodGraphs();
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> projectVsCloneURL = new LinkedHashMap<String, String>();

		if (args != null && args.length == 2) {
			projectVsCloneURL.put(args[0], args[1]);

		} else {
			// GraphDrawingUtils.initMap();

			// projectVsCloneURL.put("okhttp",
			// "https://github.com/square/okhttp.git");
			// projectVsCloneURL.put("EventBus",
			// "https://github.com/greenrobot/EventBus.git");
			// projectVsCloneURL.put("picasso",
			// "https://github.com/square/picasso.git");
			// // projectVsCloneURL.put("RxJava",
			// // "https://github.com/ReactiveX/RxJava.git");
			// projectVsCloneURL.put("storm",
			// "https://github.com/nathanmarz/storm.git");
			// projectVsCloneURL.put("retrofit",
			// "https://github.com/square/retrofit");
			// projectVsCloneURL.put("SlidingMenu",
			// "https://github.com/jfeinstein10/SlidingMenu.git");
			// projectVsCloneURL.put("iosched",
			// "https://github.com/google/iosched.git");

			// projectVsCloneURL.put("nodebox",
			// "https://github.com/nodebox/nodebox.git");

			// projectVsCloneURL.put("bitcoinj",
			// "https://github.com/bitcoinj/bitcoinj.git");

			// projectVsCloneURL.put("Chronicle-Queue",
			// "https://github.com/OpenHFT/Chronicle-Queue.git");

			// Project Chronicle-Queue is removed
			// projectVsCloneURL.put("Chronicle-Queue",
			// "https://github.com/OpenHFT/Chronicle-Queue.git");

			// projectVsCloneURL.put("Lightning-Browser",
			// "https://github.com/anthonycr/Lightning-Browser.git");
			// projectVsCloneURL.put("browser-android",
			// "https://github.com/brave/browser-android.git");

			// projectVsCloneURL.put("heritrix3",
			// "https://github.com/internetarchive/heritrix3.git");

			// projectVsCloneURL.put("Activiti",
			// "https://github.com/Activiti/Activiti.git");

			// projectVsCloneURL.put("guava",
			// "https://github.com/google/guava.git");

			// projectVsCloneURL.put("java-driver",
			// "https://github.com/datastax/java-driver.git");

			// projectVsCloneURL.put("okhttp",
			// "https://github.com/square/okhttp.git");

			// projectVsCloneURL.put("fastjson",
			// "https://github.com/alibaba/fastjson.git");

			// projectVsCloneURL.put("jackson-databind",
			// "https://github.com/FasterXML/jackson-databind.git");

			// projectVsCloneURL.put("jackson-core",
			// "https://github.com/FasterXML/jackson-core.git");

			// projectVsCloneURL.put("neo4j",
			// "https://github.com/neo4j/neo4j.git");

			projectVsCloneURL.put("morphia", "https://github.com/mongodb/morphia.git");

			// projectVsCloneURL.put("arangodb-java-driver",
			// "https://github.com/arangodb/arangodb-java-driver.git");

		}

		String serializeFileLoc = "/home/dharani/concordia/thesis/SLEMR/CrossValidation/";
		String baseCheckoutDir = "/home/dharani/concordia/thesis/SLEMR/RefactoringMiner/tmp/";
		String baseGraphDBDir = "/home/dharani/concordia/thesis/SLEMR";

		Set<String> keySet = projectVsCloneURL.keySet();
		for (String string : keySet) {
			new File(baseGraphDBDir + File.separator + string + File.separator + "graphDB").mkdirs();
			String projectCheckoutDir = baseCheckoutDir + string;
			resetRepo(new File(projectCheckoutDir));
			String project_branch = getProjectDefaultBranch(string);

			RefactoredClassesExtractor rce = new RefactoredClassesExtractor(projectCheckoutDir,
					projectVsCloneURL.get(string), string, MethodType.EXTRACTED_METHOD, baseGraphDBDir,
					project_branch != null && !project_branch.isEmpty() ? project_branch : DEFAULT_BRANCH_NAME);
			rce.setTrackUndetectableRefactorings(false);
			rce.processExtractMethodRefactorings();
			rce.writeGraphDatabase();
			new File(serializeFileLoc + File.separator + string).mkdir();
			rce.serializeMaps(serializeFileLoc + File.separator + string);

			System.out.println("refGVsMethodGraph size " + rce.getRefGVsMethodGraph().size());
		}
	}
}
