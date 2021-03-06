package edu.concordia.reflearner.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

/**
 * Extracts the method parameters involved in the refactoring
 * 
 * @author dharani kumar palani
 *
 */
public class MethodParameterUtil {

	public static Map<String, List<Refactoring>> commitIdVsRefactorings = new HashMap<String, List<Refactoring>>();

	public static List<String> getMethodParameters(final Repository repo, final File destDir, final String commitId,
			final String methodName, final String className, final String description, final String cloneURL) {

		List<Refactoring> allRefactoringsInCommit = commitIdVsRefactorings.get(commitId);

		if (allRefactoringsInCommit == null || allRefactoringsInCommit.isEmpty()) {

			ParameterExtractionHandler pe = new MethodParameterUtil().new ParameterExtractionHandler(className,
					methodName, description, commitId);

			GitHistoryRefactoringMiner detector = new GitHistoryRefactoringMinerImpl();
			detector.detectAtCommit(repo, cloneURL, commitId, pe);

			return pe.getParameters();
		} else {
			return getParameters(allRefactoringsInCommit, className, methodName, description, commitId);
		}
	}

	public static List<String> getParameters(List<Refactoring> refactorings, final String className,
			final String methodName, final String description, final String commitId) {
		List<String> parameters = new ArrayList<String>();
		for (Refactoring refactoring : refactorings) {

			if (RefactoringType.EXTRACT_OPERATION.equals(refactoring.getRefactoringType())) {
				ExtractOperationRefactoring extractRef = (ExtractOperationRefactoring) refactoring;

				UMLOperation umlo = extractRef.getExtractedOperation();

				if (className.equals(umlo.getClassName())) {
					if (methodName.equals(umlo.getName()) && description.equals(extractRef.toString())) {
						List<UMLType> parameterTypeList = umlo.getParameterTypeList();

						for (UMLType umlType : parameterTypeList) {
							parameters.add(umlType.toString());
						}

						break;
					}
				}
			}
		}

		List<Refactoring> allRefactoringsInCommit = commitIdVsRefactorings.get(commitId);

		if (allRefactoringsInCommit == null || allRefactoringsInCommit.isEmpty()) {
			allRefactoringsInCommit = refactorings;

			commitIdVsRefactorings.put(commitId, allRefactoringsInCommit);
		}

		return parameters;
	}

	class ParameterExtractionHandler extends RefactoringHandler {

		private List<String> parameters = new ArrayList<String>();
		private String methodName;
		private String className;
		private String description;
		private String commitId;

		public ParameterExtractionHandler(final String cn, final String mn, final String desc, final String commitId) {
			className = cn;
			methodName = mn;
			description = desc;
			this.commitId = commitId;
		}

		@Override
		public boolean skipCommit(String commitId) {
			return super.skipCommit(commitId);
		}

		@Override
		public void handleException(String commitId, Exception e) {
			super.handleException(commitId, e);
		}

		@Override
		public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
			super.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
		}

		@Override
		public void handle(RevCommit commitData, List<Refactoring> refactorings) {

			parameters = MethodParameterUtil.getParameters(refactorings, className, methodName, description, commitId);

			/*
			 * for (Refactoring refactoring : refactorings) {
			 * 
			 * if (RefactoringType.EXTRACT_OPERATION.equals(refactoring.
			 * getRefactoringType())) { ExtractOperationRefactoring extractRef =
			 * (ExtractOperationRefactoring) refactoring;
			 * 
			 * UMLOperation umlo = extractRef.getExtractedOperation();
			 * 
			 * if (className.equals(umlo.getClassName())) { if
			 * (methodName.equals(umlo.getName()) &&
			 * description.equals(extractRef.toString())) { List<UMLType>
			 * parameterTypeList = umlo.getParameterTypeList();
			 * 
			 * for (UMLType umlType : parameterTypeList) {
			 * parameters.add(umlType.toString()); }
			 * 
			 * break; } } } }
			 */
		}

		public List<String> getParameters() {
			return parameters;
		}
	}

}
