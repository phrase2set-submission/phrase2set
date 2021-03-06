package edu.concordia.reflearner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import datastructure.Graph;

public class ValidationUtil {

	@SuppressWarnings("unchecked")
	public static Map<RefactoringGit, Graph> loadRefactoringGraphMap(String baseDir, String projectName) {
		String path = baseDir + File.separator + projectName + File.separator + "refgVsMethodGraph.ser";
		System.out.println("path " + path);
		Map<RefactoringGit, Graph> map = null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)))) {

			map = (Map<RefactoringGit, Graph>) ois.readObject();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

		return map;
	}

	public static LinkedHashMap<RefactoringGit, Graph> loadRefactoringGraphMap(String path) {
		LinkedHashMap<RefactoringGit, Graph> map = null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)))) {

			map = (LinkedHashMap<RefactoringGit, Graph>) ois.readObject();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

		return map;
	}
}
