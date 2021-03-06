/**
 * 
 */
package main;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import storage.GraphDatabase;
import utils.DataUtils;
import utils.Logger;
import config.DatabaseConfig;
import config.GlobalConfig;
import data.MethodInfo;
import data.TypeInfo;
import datastructure.GlobalData;
import datastructure.Graph;

/**
 * @author anhnt
 *
 */
public class DatabaseBuilderMain {
	static int numJavaProjects = 0;
	static int numClasses = 0;
	static int numMethods = 0;
	static long LOCs = 0l;
	public static boolean isDoCompactMethod = true;
	static final int aGraphCount = 1;
	static long addedGraphs = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		try {
//			doMain(DatabaseConfig.androidDirPath);
////			doMain(DatabaseConfig.trainDirPath);
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		doIncremental(DatabaseConfig.trainDirPath, 0);

	}
	
	public static void doIncremental(String mainCodebasePath, int startProjectIdx){
		GraphDatabase graphDB =  GraphDatabase.readGraphDatabase(DatabaseConfig.graphDatabasePathPrune);

//		FileWriter fwDebug = new FileWriter(DatabaseConfig.dataPath + " debug.1txt"); 
		File mainDir = new File(mainCodebasePath);
		File[] children = mainDir.listFiles();
		TreeMap<String, File> nameDirMap = new TreeMap<String, File>();
		
		for (File child:children){
			String name = child.getName();
			nameDirMap.put(name, child);
		}
		
		int countProject = 0;
		
		for (String name:nameDirMap.keySet()){
			File  dir = nameDirMap.get(name);
			countProject++;
			if (countProject<startProjectIdx)
				continue;
			Logger.log(countProject + ":::" + dir);
//			fwDebug.append(countProject + ": " + dir  + System.lineSeparator());
//			fwDebug.flush();
			processProject(dir.getAbsolutePath(), graphDB);
			if (countProject%50==0){
				graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePathPrune + countProject);
			}
		}
//		fwDebug.close();
		graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePathPrune);
		graphDB.buildAllChildSimple();
		graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePathPrune);
	}
	
	public static void doMain(String mainCodebasePath) throws IOException{
		GraphDatabase graphDB = new GraphDatabase();

		FileWriter fwDebug = new FileWriter(DatabaseConfig.dataPath + " debug.txt"); 
		File mainDir = new File(mainCodebasePath);
		File[] children = mainDir.listFiles();
		TreeMap<String, File> nameDirMap = new TreeMap<String, File>();
		
		for (File child:children){
			String name = child.getName();
			nameDirMap.put(name, child);
		}
		
		int countProject = 0;
		
		for (String name:nameDirMap.keySet()){
			File  dir = nameDirMap.get(name);
			countProject++;

			Logger.log(countProject + ":::" + dir);
			fwDebug.append(countProject + ": " + dir  + System.lineSeparator());
			fwDebug.flush();
			processProject(dir.getAbsolutePath(), graphDB);
			if (countProject%50==0){
				graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePath + countProject);
			}
		}
		fwDebug.close();
		
		graphDB.buildAllChildSimple();
		graphDB.storeThisDatabase(DatabaseConfig.graphDatabasePath);
		
	}

	public static void processProject(String project, GraphDatabase graphDB){
		LinkedHashMap<String, MethodInfo> methodInfoMap = new LinkedHashMap<>();

		synchronized (AllMain.class) {
			
			/**
			 * Browse all methods and add their groums and subgroums to database
			 */
			Logger.log("\tbuilding groums");
			JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
			javaGroumVisitor.doMain(project);


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

					//	Logger.log("combinedName: " + combinedName);
					numMethods++;
					LOCs += method.LOCs;
					
					// Logger.log(method.controlNodeList);
					// incGraphDB.addGroumToDatabase(method, project);

					// count++;
					if (isDoCompactMethod){
						DataUtils.compactGroum(method);
					}
					methodInfoMap.put(combinedName, method);
				}
			}

			LinkedHashMap<String, Graph> methodGraphMap = convertAllGroumsToGraphs(methodInfoMap, graphDB.globalData);
			Logger.log("\t\tmethodGraphMap size: " + methodGraphMap.size());
			getAllMethodGraphs(methodGraphMap, GlobalConfig.maxGraphSize, GlobalConfig.maxCountNode, graphDB, project);

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
			if( count%10==0){
				System.out.print(count + " ");
				if( count%500==0){
					System.out.println();
				}
			}
			count++;
			Graph methodGraph = methodGraphMap.get(methodName);
//			Logger.log("methodName: " + methodName + "        NumNodes: " +methodGraph.numNodes());

			ArrayList<Graph> allSubGraphs = methodGraph.getAllSubGraphs(maxGraphSize, maxCountNode, database.globalData);
			
//			allMethodGraphs.put(methodName, allSubGraphs);
			for(Graph graph:allSubGraphs){
//				if (graph.isConcernedGraph(GlobalConfig.concernedLibs, database.globalData)){
			
					database.addGraphWithOtherData(graph, aGraphCount, projectName, methodName);
					addedGraphs ++;
//				}
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
//			Logger.log("methodGraph: " + methodGraph);
			methodGraph.removeRedundancy();
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
	
	
}
