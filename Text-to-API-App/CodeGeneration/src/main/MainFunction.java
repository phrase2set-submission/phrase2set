package main;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import config.GlobalConfig;
import data.MethodInfo;
import data.NodeSequenceInfoMap;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import storage.GraphDatabase;
import utils.DataUtils;
import utils.FileUtils;
import utils.Logger;

public class MainFunction {
	static int numJavaProjects = 0;
	static int numClasses = 0;
	static int numMethods = 0;
	static long LOCs = 0l;
	static boolean isDoCompactMethod = true;
	static GraphDatabase database = new GraphDatabase();
	static final int aGraphCount = 1;
	
	public static void main(String[] args){
		
		
		String projectPath = "C:/Research/GraphGeneration/Work/data/db4o";
		String projectName = "db4o";
		LinkedHashMap<String, MethodInfo> methodInfoMap = buildProjectGroums(projectPath, projectName);
		
		
		Logger.log("numJavaProjects: " + numJavaProjects);
		Logger.log("numClasses: " + numClasses);
		Logger.log("numMethods: " + numMethods);
		Logger.log("LOCs: " + LOCs);
		
		String methodInfoMapPath = "C:/Research/GraphGeneration/Work/out_data/methodInfoMap.dat";
		FileUtils.writeObjectFile(methodInfoMap, methodInfoMapPath);
		
		LinkedHashMap<String, Graph> methodGraphMap = convertAllGroumsToGraphs(methodInfoMap, database.globalData);
		String methodGraphMapPath = "C:/Research/GraphGeneration/Work/out_data/methodGraphMap.dat";
		FileUtils.writeObjectFile(methodGraphMap, methodGraphMapPath);
		
		Logger.log("getAllMethodGraphs");
		getAllMethodGraphs(methodGraphMap, GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, database, projectName);
		database.buildAllChild();
		database.doStatistics();
		database.storeThisDatabase(GlobalConfig.graphDatabasePath);
		
	}
	
	
	public static void getAllMethodGraphs
		(LinkedHashMap<String, Graph> methodGraphMap,  int maxGraphSize, int maxCountNode, GraphDatabase database, String projectName){
//		LinkedHashMap<String, ArrayList<Graph>> allMethodGraphs = new LinkedHashMap<>();
		
		for (String methodName:methodGraphMap.keySet()){
			Graph methodGraph = methodGraphMap.get(methodName);
			Logger.log("methodName: " + methodName + "        NumNodes: " +methodGraph.numNodes());

			ArrayList<Graph> allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode, database.globalData);
			
//			allMethodGraphs.put(methodName, allSubGraphs);
			for(Graph graph:allSubGraphs){
				database.addGraphWithOtherData(graph, aGraphCount, projectName, methodName);
			}
		}
		
//		return allMethodGraphs;
	}
	
	
	public static LinkedHashMap<String, Graph> convertAllGroumsToGraphs(LinkedHashMap<String, MethodInfo> methodInfoMap, 
			GlobalData globalData){
		LinkedHashMap<String, Graph> methodGraphMap = new LinkedHashMap<>();
		for (String methodName:methodInfoMap.keySet()){
			MethodInfo methodInfo = methodInfoMap.get(methodName);
			Graph methodGraph = DataUtils.convertGroumToGraph(methodInfo, globalData);
			methodGraphMap.put(methodName, methodGraph);
		}
		return methodGraphMap;
	}
	
	public static LinkedHashMap<String, MethodInfo> buildProjectGroums(String projectPath, String projectName){
		LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<>();
			
		synchronized (MainFunction.class) {
			Logger.log("projectPath: " + projectPath);
			/**
			 * Browse all methods and add their groums and subgroums to database
			 */
			Logger.log("\tbuilding groums");
			JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
			javaGroumVisitor.doMain(projectPath);


			Logger.log("\tadding groums to database");
			List<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
			//			int count = 0;
			if (allTypeList.size()>0)
				numJavaProjects++;
			for (TypeInfo typeInfo:allTypeList){
				String packageName = typeInfo.packageDec;
				String className = typeInfo.typeName;
				numClasses++;
				
				List<MethodInfo> methodList = typeInfo.methodDecList;

				for (MethodInfo method:methodList){
					//					Logger.log(count);
					String methodName = method.methodName;
					
					String combinedName = normalizeStr(projectName) +"::" + 
									normalizeStr(packageName) + "." + normalizeStr(className) + "." + normalizeStr(methodName)
									+"::" + numMethods;
					Logger.log("combinedName: " + combinedName);
					numMethods++;
					LOCs += method.LOCs;
					
					//					Logger.log(method.controlNodeList);
//					incGraphDB.addGroumToDatabase(method, project);

					//					count++;
					if (isDoCompactMethod){
						DataUtils.compactGroum(method);
					}
					methodInfoMap.put(combinedName, method);
				}
			}	
		}
		return methodInfoMap;
	}
	
	
	public static String normalizeStr(String str){
		return str.trim();
	}
	
	public static void cleanup(String projectPath){
		try {
		Logger.log("\tDelete project dir recursively");
		//			FileUtils.deleteDirectoryContent(new File(GlobalConfig.dummyDir));
		org.apache.commons.io.FileUtils.forceDelete(new File(projectPath));
		Thread.sleep(300);
		NodeSequenceInfoMap.clearAll();	

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
