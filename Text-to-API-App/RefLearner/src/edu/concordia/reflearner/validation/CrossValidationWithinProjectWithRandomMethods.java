package edu.concordia.reflearner.validation;

import static edu.concordia.reflearner.util.DBUtil.getAllRefactoredClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import data.MethodInfo;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import edu.concordia.reflearner.util.GraphUtil;
import edu.concordia.reflearner.util.GroumUtil;
import groumvisitors.JavaGroumVisitorSimple;

public class CrossValidationWithinProjectWithRandomMethods {

	public static final int THRESHOLD_LOC = 10;

	public static void main(String[] args) {
		String projectName = "jackson-core";
		String projectPath = "/home/dharani/concordia/thesis/SLEMR/RefactoringMiner/tmp/jackson-core";

		Set<String> allClassesRefactored = getAllRefactoredClasses(projectName);
		System.out.println("allClassesRefactored.size() " + allClassesRefactored.size());

		List<Graph> randomMethodGraphs = getGraphOfRandomMethods(projectPath, allClassesRefactored,
				allClassesRefactored.size(), new GlobalData());
		
		System.out.println("randomMethodGraphs.size() " + randomMethodGraphs.size());
	}

	public static List<Graph> getGraphOfRandomMethods(final String projectDir,
			final Set<String> classesThatGotRefactored, int numGraphsToAdd, GlobalData gd) {
		System.out.println("CrossValidationWithinProjectWithRandomMethods.getGraphOfRandomMethods() " + numGraphsToAdd + ", projectDir " + projectDir);
		List<Graph> randomMethodGraphs = new ArrayList<Graph>();
		List<String> allClasses = new ArrayList<String>();

		JavaGroumVisitorSimple javaGroumVisitor = new JavaGroumVisitorSimple();
		javaGroumVisitor.doMain(projectDir);

		List<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
		System.out.println("allTypeList " + allTypeList.size());

		for (TypeInfo typeInfo : allTypeList) {
			String packageName = typeInfo.packageDec;
			String className = typeInfo.typeName;

			if (randomMethodGraphs.size() >= numGraphsToAdd)
				break;

			if (packageName != null && !packageName.isEmpty() && className != null && !className.isEmpty()) {

				String fqcn = packageName.trim() + "." + className.trim();

				if (!classesThatGotRefactored.contains(fqcn)) {
					allClasses.add(packageName.trim() + "." + className.trim());

					List<MethodInfo> methods = typeInfo.methodDecList;

					// First fit of method meeting the required criteria
					for (MethodInfo mi : methods) {
						String methodSig = mi.getFullMethodSignature();
						if (!methodSig.contains("get") && !methodSig.contains("set") && !methodSig.contains("is")
								&& mi.LOCs >= THRESHOLD_LOC) {

							Graph graph = GroumUtil.getMethodGraph(mi, gd);

							if (!GraphUtil.hasUnknownClassTypeNodes(graph, gd)) {
								randomMethodGraphs.add(graph);
								break;
							}
						}
					}
				}
			}
		}

		return randomMethodGraphs;
	}
}
