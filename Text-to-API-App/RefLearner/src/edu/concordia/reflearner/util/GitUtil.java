package edu.concordia.reflearner.util;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.ExternalProcess;

public class GitUtil {
	
	public static final String DEFAULT_BRANCH_NAME = "master";

	public static void resetRepo(File destDir) throws Exception {
		
		resetRepo(destDir, DEFAULT_BRANCH_NAME);
		
		/*ExternalProcess.execute(destDir, new String[] { "git", "reset", "--hard" });
		ExternalProcess.execute(destDir, new String[] { "git", "clean", "-f", "-d" });
		// ExternalProcess.execute(destDir, new String[] { "git", "checkout",
		// "master" });

		/*
		try {
			ExternalProcess.execute(destDir, new String[] { "git", "checkout", "master" });
		} catch (Exception e) {

			// origin/3.0 for Cassandra java-driver
			ExternalProcess.execute(destDir, new String[] { "git", "reset", "--hard", "origin/3.0" });
			ExternalProcess.execute(destDir, new String[] { "git", "clean", "-f", "-d" });
		}
		*/
	}

	public static void checkout(File destDir, final Repository repo, final GitService gs, final String commitId)
			throws Exception {
		try {
			System.out.println("checking out " + commitId);
			gs.checkout(repo, commitId);
		} catch (Exception e) {
			System.err.println("Exception while checking out. Trying out external git command");
			ExternalProcess.execute(destDir, "git", "checkout", commitId);
		}
	}

	public static void resetRepo(File destDir, final String defaultBranch) {
		ExternalProcess.execute(destDir, new String[] { "git", "reset", "--hard", defaultBranch });
		ExternalProcess.execute(destDir, new String[] { "git", "clean", "-f", "-d" });
		ExternalProcess.execute(destDir, new String[] { "git", "checkout", defaultBranch });
	}
}