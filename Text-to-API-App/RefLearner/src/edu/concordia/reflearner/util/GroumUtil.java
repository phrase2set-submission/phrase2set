package edu.concordia.reflearner.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import data.MethodInfo;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import groumvisitors.JavaGroumVisitor;
import main.DatabaseBuilderMain;
import utils.DataUtils;

/**
 * Utility to extract information from Recoder and GrouM
 * 
 * @author Dharani Kumar Palani
 */
public class GroumUtil {
	public static final boolean isDoCompactMethod = true;

	public static TypeInfo getTypeInfo(final String projectDir, final String fqcn) {
		TypeInfo type = null;

		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.doMain(projectDir);

		List<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
		for (TypeInfo typeInfo : allTypeList) {
			String packageName = typeInfo.packageDec;
			String className = typeInfo.typeName;
			String fqcnDer = (className != null) && (!className.isEmpty()) ? (packageName + "." + className).trim()
					: null;
			if (fqcn.equals(fqcnDer)) {
				type = typeInfo;
				break;
			}
		}
		return type;
	}

	public static MethodInfo getMethodInfo(final TypeInfo typeInfo, final String methodname, final String projectName,
			final List<String> incomingParams) {
		System.out.println("GroumUtil.getMethodInfo() ");
		System.err.println("methodname " + methodname);

		MethodInfo mi = null;
		// String packageName = typeInfo.packageDec;
		// String className = typeInfo.typeName;

		// int numMethods = 0;

		boolean duplicateMethodNames = false;
		int count = 0;
		for (MethodInfo method : typeInfo.methodDecList) {
			if (method.methodName.equals(methodname)) {
				count++;
			}
		}

		if (count > 1) {
			duplicateMethodNames = true;
		}

		for (MethodInfo method : typeInfo.methodDecList) {
			String mn = method.methodName;

			if (mn.equals(methodname)) {
				
				if(duplicateMethodNames) {
				
					List<String> recoderParams = method.getParamList();
					System.err.println("RECODER PARAMS " + recoderParams);
					System.err.println("INCOMING PARAMS " + incomingParams);
	
					if (Arrays.equals(ClassUtil.normalizeParametrizedTypes(incomingParams).toArray(),
							ClassUtil.normalizeParametrizedTypes(recoderParams).toArray())) {
						mi = method;
						System.err.println("Methods matched in signature.");
	
						// String combinedName =
						// DatabaseBuilderMain.normalizeStr(projectName) + "::"
						// + DatabaseBuilderMain.normalizeStr(packageName) + "."
						// + DatabaseBuilderMain.normalizeStr(className) + "." +
						// DatabaseBuilderMain.normalizeStr(mn)
						// + "::" + numMethods;
	
						// numMethods++;
						DataUtils.compactGroum(method);
	
						break;
					} else {
						System.err.println("Methods did not matched in signature.");
						if (incomingParams.size() != recoderParams.size()) {
							System.err.println("overloaded method " + mn + " with different number of parameters");
						} else {
							System.err.println("overloaded method " + mn + " with different types of parameters");
						}
					}
				} else {
					mi = method;
					DataUtils.compactGroum(method);
					break;
				}
			}
		}
		return mi;
	}

	public static Graph getMethodGraph(TypeInfo typeInfo, String methodSignature, String projectName,
			List<String> methodParams, GlobalData globalData) {
		Graph methodGraph = null;
		String packageName = typeInfo.packageDec;
		String className = typeInfo.typeName;

		LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<String, MethodInfo>();
		List<MethodInfo> methodList = typeInfo.methodDecList;

		int numMethods = 0;
		boolean methodFound = false;
		List<String> paramList1;

		boolean duplicateMethodNames = false;
		int count = 0;
		for (MethodInfo method : methodList) {
			if (method.methodName.equals(methodSignature)) {
				count++;
			}
		}

		if (count > 1) {
			duplicateMethodNames = true;
		}

		for (MethodInfo method : methodList) {
			String mn = method.methodName;

			if (mn.equals(methodSignature)) {

				if (duplicateMethodNames) {

					System.err.println("methodParams " + method.getParamList());
					System.err.println("methodName " + mn);

					paramList1 = ClassUtil.normalizeParametrizedTypes(method.getParamList());
					List<String> paramList2 = ClassUtil.normalizeParametrizedTypes(methodParams);
					System.err.println("RECORDER PARAMS " + paramList1);
					System.err.println("INCOMING PARAMS " + paramList2);
					if (paramList1.size() != paramList2.size()) {
						System.err.println("overloaded method " + mn + " with different number of parameters");
					} else if (!Arrays.equals(paramList1.toArray(), paramList2.toArray())) {
						System.err.println("overloaded method " + mn + " with different types of parameters");
					} else {
						methodFound = true;

						String combinedName = DatabaseBuilderMain.normalizeStr(projectName) + "::"
								+ DatabaseBuilderMain.normalizeStr(packageName) + "."
								+ DatabaseBuilderMain.normalizeStr(className) + "."
								+ DatabaseBuilderMain.normalizeStr(mn) + "::" + numMethods;

						numMethods++;

						DataUtils.compactGroum(method);

						methodInfoMap.put(combinedName, method);
					}
				} else {
					// There is only one method with this name. Do not check for
					// parameters
					methodFound = true;
				}
			}
		}
		if (methodFound) {
			LinkedHashMap<String, Graph> methodGraphMap = convertAllGroumsToGraphs(methodInfoMap, globalData);
			Set<String> keySet = methodGraphMap.keySet();

			for (String string : keySet) {
				methodGraph = methodGraphMap.get(string);
			}
		}
		return methodGraph;
	}

	public static Graph getMethodGraph(MethodInfo mi, GlobalData gd) {
		Graph methodGraph = DataUtils.convertGroumToGraph(mi, gd);
		methodGraph.removeRedundancy();
		return methodGraph;
	}

	public static LinkedHashMap<String, Graph> convertAllGroumsToGraphs(LinkedHashMap<String, MethodInfo> methodInfoMap,
			GlobalData globalData) {
		LinkedHashMap<String, Graph> methodGraphMap = new LinkedHashMap<String, Graph>();
		for (String methodName : methodInfoMap.keySet()) {
			MethodInfo methodInfo = (MethodInfo) methodInfoMap.get(methodName);

			Graph methodGraph = DataUtils.convertGroumToGraph(methodInfo, globalData);
			methodGraph.removeRedundancy();
			methodGraphMap.put(methodName, methodGraph);
		}
		return methodGraphMap;
	}
}
