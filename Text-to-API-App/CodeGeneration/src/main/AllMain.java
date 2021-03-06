package main;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import config.GlobalConfig;
import data.MethodInfo;
import data.NodeSequenceInfoMap;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;
import repository.SnapshotCreation;
import storage.GraphDatabase;
import utils.DataUtils;
import utils.FileUtils;
import utils.Logger;

public class AllMain {
	static int numJavaProjects = 0;
	static int numClasses = 0;
	static int numMethods = 0;
	static long LOCs = 0l;
	public static boolean isDoCompactMethod = true;
	static GraphDatabase database = new GraphDatabase();
	static final int aGraphCount = 1;
	static long addedGraphs = 0;
	
	public static void main(String[] args){
		
		
//		String projectPath = "C:/Research/GraphGeneration/Work/data/db4o";
//		String projectName = "db4o";
//		LinkedHashMap<String, MethodInfo> methodInfoMap = buildProjectGroums(projectPath, projectName);
//		
//		

//		
//		String methodInfoMapPath = "C:/Research/GraphGeneration/Work/out_data/methodInfoMap.dat";
//		FileUtils.writeObjectFile(methodInfoMap, methodInfoMapPath);
//		
//		LinkedHashMap<String, Graph> methodGraphMap = convertAllGroumsToGraphs(methodInfoMap, database.globalData);
//		String methodGraphMapPath = "C:/Research/GraphGeneration/Work/out_data/methodGraphMap.dat";
//		FileUtils.writeObjectFile(methodGraphMap, methodGraphMapPath);
//		
//		Logger.log("getAllMethodGraphs");
//		getAllMethodGraphs(methodGraphMap, GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, database, projectName);
//		database.buildAllChild();
//		database.doStatistics();
//		database.storeThisDatabase(GlobalConfig.graphDatabasePath);

		processAllProjects(database);
		
	}
	
	public static void processAllProjects(GraphDatabase graphDB){
		List<String> projects = getProjectsFromDirDat(GlobalConfig.projectDataDir);
		Logger.log("Number of projects: " +  projects.size());
		
		int countProject = 1;
		int countSlot=1;
		for (String project:projects){
			
			GlobalConfig.dummyDir = GlobalConfig.slotDummyDir + countProject + "/";
			Logger.log("countProject: " + countProject);
			
			if (countProject==188||countProject==207||countProject>2000)
			{
				countProject++;
				continue;
			}
			else{
				countProject++;

			}
			
			if (countProject%100==0){
				countSlot++;
				GlobalConfig.slotDummyDir = GlobalConfig.mainDummyDir + "slot" + countSlot +"/";
				Logger.log("store tmp database");
				graphDB.storeThisDatabase(GlobalConfig.graphDatabasePath + countProject);

			}
			
//			if (countProject<=750)
//			{
//				countProject++;
//				continue;
//			}
//			else{
//				countProject++;
//
//			}
			
			processProject(project, graphDB);
		}
		
		Logger.log("numJavaProjects: " + numJavaProjects);
		Logger.log("numClasses: " + numClasses);
		Logger.log("numMethods: " + numMethods);
		Logger.log("LOCs: " + LOCs);
		
		Logger.log("buildAllChildren");
		graphDB.buildAllChild();
		Logger.log("doStatistics");
		graphDB.doStatistics();
		Logger.log("storeThisDatabase");
		graphDB.storeThisDatabase(GlobalConfig.graphDatabasePath);
	}
	
	
	
	public static void processProject(String project, GraphDatabase graphDB){
		LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<>();

		new File(GlobalConfig.dummyDir).mkdirs();
		synchronized (AllMain.class) {
			
			new File(GlobalConfig.dummyDir).mkdirs();
			Logger.log("\r\nproject: " + project);
			//			Logger.logDebugBis("\r\nproject: " + project);
			Logger.log("\tcreating dummy dir");
			TreeMap<String, String> fileContentMap = SnapshotCreation.readData(GlobalConfig.projectDataDir, project);
			SnapshotCreation.buildDummyDir(GlobalConfig.dummyDir, fileContentMap);
			/**
			 * Browse all methods and add their groums and subgroums to database
			 */
			Logger.log("\tbuilding groums");
			JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
			javaGroumVisitor.doMain(GlobalConfig.dummyDir);


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
					
					String combinedName = normalizeStr(project) +"::" + 
									normalizeStr(packageName) + "." + normalizeStr(className) + "." + normalizeStr(methodName)
									+"::" + numMethods;
//					Logger.log("combinedName: " + combinedName);
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

			LinkedHashMap<String, Graph> methodGraphMap = convertAllGroumsToGraphs(methodInfoMap, database.globalData);
			Logger.log("\t\tmethodGraphMap size: " + methodGraphMap.size());
			getAllMethodGraphs(methodGraphMap, GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, graphDB, project);

		}
		try {
			Logger.log("\tDelete project dir recursively");
			//			FileUtils.deleteDirectoryContent(new File(GlobalConfig.dummyDir));
			org.apache.commons.io.FileUtils.forceDelete(new File(GlobalConfig.dummyDir));
			Thread.sleep(300);
			NodeSequenceInfoMap.clearAll();	

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.log("addedGraphs: " + addedGraphs);
	}


	
	public static List<String> getProjectsFromDirDat(String dirDatPath){
		TreeSet<String> projectList = new TreeSet<String>();
		File dirDat = new File(dirDatPath);
		if (dirDat.exists()){
			File[] subs = dirDat.listFiles();
			for (File sub:subs){
				String name = sub.getName();
				if (name.endsWith(".dat")){
					projectList.add(name.substring(0,name.length()-4));
				}
			}
		}
		ArrayList<String> projects = new ArrayList<>();
		projects.addAll(projectList);
		return projects;
	}
	
	
	public static void getAllMethodGraphs
		(LinkedHashMap<String, Graph> methodGraphMap,  int maxGraphSize, int maxCountNode, GraphDatabase database, String projectName){
//		LinkedHashMap<String, ArrayList<Graph>> allMethodGraphs = new LinkedHashMap<>();
		int count = 1;
		for (String methodName:methodGraphMap.keySet()){
			System.out.print(count + " ");
			if( count%100==0){
				System.out.println();
			}
			count++;
			Graph methodGraph = methodGraphMap.get(methodName);
//			Logger.log("methodName: " + methodName + "        NumNodes: " +methodGraph.numNodes());

			ArrayList<Graph> allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode, database.globalData);
			
//			allMethodGraphs.put(methodName, allSubGraphs);
			for(Graph graph:allSubGraphs){
				if (graph.isConcernedGraph(GlobalConfig.concernedLibs, database.globalData)){
			
					database.addGraphWithOtherData(graph, aGraphCount, projectName, methodName);
					addedGraphs ++;
				}
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
			
		synchronized (AllMain.class) {
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
//					Logger.log("combinedName: " + combinedName);
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
		if(str ==null)
			return "";
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
