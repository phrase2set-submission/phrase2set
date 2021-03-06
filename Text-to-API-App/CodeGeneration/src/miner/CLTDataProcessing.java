/**
 * 
 */
package miner;

import groumvisitors.JavaGroumVisitor;
import groumvisitors.JavaGroumVisitorSimple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.TreeMap;

import storage.GraphDatabase;
import storage.NodeGraphDatabase;
import utils.DataUtils;
import utils.FileUtils;
import utils.Logger;
import config.DatabaseConfig;
import config.GlobalConfig;
import data.MethodInfo;
import data.SampleAnswerData;
import data.TestData;
import data.TypeInfo;
import datastructure.Edge;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;
import datastructure.PostInfoData;

/**
 * @author anhnt
 *
 */
public class CLTDataProcessing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		readCSV(GlobalConfig.cltPath);
		
		
		
//		HashSet<String> elementList = (HashSet<String>) FileUtils.readObjectFile(DatabaseConfig.elementListPath);
//		Logger.log("elementList size:"+elementList.size());
//		
//		FullNameData.createDictionary(elementList);
//		
//		Logger.log("Reading cltTable");
//		ArrayList<CLTData> allTable = (ArrayList<CLTData>) FileUtils.readObjectFile(GlobalConfig.cltTablePath);
//		Logger.log("getPostCLTMap");
//		HashMap<Long, ArrayList<CLTData>> postCLTMap = getPostCLTMap(allTable, elementList);
//		FileUtils.writeObjectFile(postCLTMap, GlobalConfig.postCLTMapPath);
//		
//		proceedPostCLTMap(postCLTMap, GlobalConfig.bigQueryListPath);
		
	}
	
	
	
	public synchronized static TreeMap<Long, ArrayList<Graph>> proceedAll(ArrayList<PostInfoData> posts,  GraphDatabase graphDB, GlobalData globalData, 
			boolean isBuildElementList){
		TreeMap<Long, ArrayList<CLTData>> postCLTMap = new TreeMap<Long, ArrayList<CLTData>>();
		TreeMap<Long, ArrayList<String>> postSnippetsMap = new TreeMap<Long, ArrayList<String>>();
		
		int count=0;
		
		if (isBuildElementList){
			HashMap<String, Integer> elementList0 = proceedNodeGraphDB(graphDB);
			Logger.log("writing elemenlist");
			FileUtils.writeObjectFile(elementList0, DatabaseConfig.elementListPath);
		
		}
		HashMap<String, Integer> elementList = (HashMap<String, Integer>) FileUtils.readObjectFile(DatabaseConfig.elementListPath);
		Logger.log("elementList size:"+elementList.size());
		
		CLTNameProcessing.createDictionary(elementList);
		
		CLTNameProcessing.outputCLTs(GlobalConfig.CLTOutputPath);
		
		Logger.log("Processing posts");
		
		TreeMap<Long, ArrayList<Graph>> postGraphsMap = new TreeMap<Long, ArrayList<Graph>>();
		
		TreeMap<Long, PostInfoData> postIDDataMap = new TreeMap<Long, PostInfoData>();
		for (PostInfoData post:posts){
			postIDDataMap.put(post.id, post);
		}
		
		
		 int countpost = 0;
		 
//		for (PostInfoData post:posts){
		 for (Long id:postIDDataMap.keySet()){
			
			PostInfoData post = postIDDataMap.get(id);
			ArrayList<String> postSnippets = post.getSnippets();
			postSnippetsMap.put(post.id, postSnippets);
			ArrayList<Graph> postGraphs = getGraphsFromSnippets(postSnippets,count, globalData, id);
//			Logger.log("postGraphs: " + postGraphs);

			ArrayList<HashMap<ArrayList<Integer>,ArrayList<CLTData>> > postCLTs = getCLTsFromGraphs(postGraphs, globalData);
//			Logger.log("postCLTs: " + postCLTs);

			
			ArrayList<CLTData> flattenPostCLTs = new ArrayList<CLTData>();

			for (HashMap<ArrayList<Integer>,ArrayList<CLTData>> postCLT:postCLTs){
				
				flattenPostCLTs.addAll(postCLT.values().iterator().next());
			}
//			Logger.log("flattenPostCLTs: " + flattenPostCLTs);
			
			if (flattenPostCLTs.size()>GlobalConfig.maxCLTSize){
				continue;
			}

			if (flattenPostCLTs.size()>0){
				countpost++;
				 if (countpost>GlobalConfig.maxTestSetSize){
					 break;
				 }
				 Logger.log("count post: " + countpost);
				 Logger.log("id: " + id);

				postCLTMap.put(id, flattenPostCLTs);
				
				
				ArrayList<Integer> nodeIdxs = postCLTs.get(0).keySet().iterator().next();
				ArrayList<Graph> postGraphsDel = new ArrayList<Graph>();
				Graph tmp = postGraphs.get(0);
				Node[] nodes = tmp.nodes;
				Edge[] edges = tmp.edges;
				
				ArrayList<Node> storedNodes = new ArrayList<Node>(); 
				for(Integer idx:nodeIdxs){
					storedNodes.add(nodes[idx]);
				}
				ArrayList<Edge> storedEdges = new ArrayList<Edge>();
				for (Edge edge:edges){
					if (storedNodes.contains(edge.sourceNode)&&storedNodes.contains(edge.sinkNode)){
						storedEdges.add(edge);
					}
				}
				Graph newGraph = new Graph(storedNodes, storedEdges, 1);
				postGraphsDel.add(newGraph);
				postGraphsMap.put(id, postGraphsDel);
//				 Logger.log("newGraph: " + newGraph);

			}
			count++;
			
			if (count%100==0){
				System.out.print(count + " ");
			}
			if (count%2000==0){
				System.out.println();
			}
			
		}
		System.out.println();
		

		Logger.log("posts size:"+ posts.size() +"//count: " + count + "///postCLTMap size: " + postCLTMap.size());

		proceedPostCLTMap(postCLTMap, postSnippetsMap, GlobalConfig.bigQueryListPath);
		
//		Logger.log("postGraphsMap: " + postGraphsMap);
		return postGraphsMap;
	}
	
	
	public synchronized static TreeMap<Long, ArrayList<Graph>> proceedAllConnPosts
		   (ArrayList<ConnPostInfo> connPosts, TestData testData, GraphDatabase graphDB, GlobalData globalData, 
			boolean isBuildElementList, String queryListPath){
		TreeMap<Long, ArrayList<CLTData>> postCLTMap = new TreeMap<Long, ArrayList<CLTData>>();
		TreeMap<Long, ArrayList<String>> postSnippetsMap = new TreeMap<Long, ArrayList<String>>();
		
		int count=0;
		
		if (isBuildElementList){
			HashMap<String, Integer> elementList0 = proceedNodeGraphDB(graphDB);
			Logger.log("writing elemenlist");
			FileUtils.writeObjectFile(elementList0, DatabaseConfig.elementListPath);
		
		}
		HashMap<String, Integer> elementList = (HashMap<String, Integer>) FileUtils.readObjectFile(DatabaseConfig.elementListPath);
		Logger.log("elementList size:"+elementList.size());
		
		CLTNameProcessing.createDictionary(elementList);
		
		CLTNameProcessing.outputCLTs(GlobalConfig.CLTOutputPath);
		
		Logger.log("Processing posts");
		
		
		
		 int countpost = 0;
		
		 LinkedHashMap<Long, ConnPostInfo> postIDConnMap = new LinkedHashMap<>();
		 for (ConnPostInfo connPost:connPosts){
			 if(GlobalConfig.blacklistedIDs.contains(connPost.postID))
				 continue;
			 postIDConnMap.put(connPost.postID, connPost);
		 }
//		for (PostInfoData post:posts){
		 Logger.log("postIDConnMap size: " + postIDConnMap.size() );
		 for (Long id:postIDConnMap.keySet()){
			 ConnPostInfo post = postIDConnMap.get(id);
//			 Logger.log("post.clts: " + post.clts);
			 HashMap<ArrayList<Integer>,ArrayList<CLTData>>  postCLTs = getCLTsFromIncompletCLTs(post.clts, globalData);
//				 Logger.log("postCLTs: " + postCLTs);
				 
			 

			
			ArrayList<CLTData> flattenPostCLTs = new ArrayList<CLTData>();
	
			flattenPostCLTs.addAll(postCLTs.values().iterator().next());
			
//			Logger.log("flattenPostCLTs: " + flattenPostCLTs);

			 
			 
			if (flattenPostCLTs.size()>GlobalConfig.maxCLTSize){
				continue;
			}

			if (flattenPostCLTs.size()>0){
				countpost++;
				 if (countpost>GlobalConfig.maxTestSetSize){
					 break;
				 }
				 Logger.log("count post: " + countpost);
				 Logger.log("id: " + id);

				postCLTMap.put(id, flattenPostCLTs);
				

			}
			count++;
			
//			if (count%100==0){
//				System.out.print(count + " ");
//			}
//			if (count%2000==0){
//				System.out.println();
//			}
			
//			if (id==2880348){
//				System.exit(0);
//			}
			
		}
		System.out.println();

//		Logger.log("posts size:"+ posts.size() +"//count: " + count + "///postCLTMap size: " + postCLTMap.size());

		
		
		TreeMap<Long, ArrayList<Graph>> postGraphsMap = new TreeMap<Long, ArrayList<Graph>>();
		
		TreeMap<Long, PostInfoData> postIDDataMap = new TreeMap<Long, PostInfoData>();
		for (SampleAnswerData sample:testData.sampleData){
			long id = sample.answerId;
			ArrayList<String> snippets = new ArrayList<String>();
			snippets.add(sample.code);
			PostInfoData post = new PostInfoData(id, snippets);
			postIDDataMap.put(id, post);
		}
		
		
		TreeMap<Long, ArrayList<CLTData>> newpostCLTMap = new TreeMap<Long, ArrayList<CLTData>>();
		for (Long id:postCLTMap.keySet()){
			PostInfoData post = postIDDataMap.get(id);
			ArrayList<String> postSnippets = post.getSnippets();
			postSnippetsMap.put(post.id, postSnippets);
//			Logger.log("snippet: " + postSnippets);
			ArrayList<Graph> postGraphs = getGraphsFromSnippets(postSnippets,count, globalData, id);
			postGraphsMap.put(id, postGraphs);
			ArrayList<HashMap<ArrayList<Integer>,ArrayList<CLTData>> > postCLTs = getCLTsFromGraphs(postGraphs, globalData);
//			Logger.log("postCLTs: " + postCLTs);

			
			ArrayList<CLTData> flattenPostCLTs = new ArrayList<CLTData>();

			for (HashMap<ArrayList<Integer>,ArrayList<CLTData>> postCLT:postCLTs){
				
				flattenPostCLTs.addAll(postCLT.values().iterator().next());
			}
//			Logger.log("flattenPostCLTs: " + flattenPostCLTs);
			
			ArrayList<CLTData> controlCLTs = new ArrayList<CLTData>();
			ArrayList<CLTData> newCLTs = new ArrayList<CLTData>();
			for (CLTData clt:flattenPostCLTs){
				if (clt.type.equals("2")){
					controlCLTs.add(clt);
				}
				else if (clt.name.equals(".new")){
					newCLTs.add(clt);
				}
			}
			
			if (flattenPostCLTs.size()>GlobalConfig.maxCLTSize){
				continue;
			}

			if (flattenPostCLTs.size()>0){
//				countpost++;
//				 if (countpost>GlobalConfig.maxTestSetSize){
//					 break;
//				 }
//				 Logger.log("count post: " + countpost);
//				 Logger.log("id: " + id);

				
				ArrayList<Integer> nodeIdxs = postCLTs.get(0).keySet().iterator().next();
				ArrayList<Graph> postGraphsDel = new ArrayList<Graph>();
				Graph tmp = postGraphs.get(0);
				Node[] nodes = tmp.nodes;
				Edge[] edges = tmp.edges;
				
				ArrayList<Node> storedNodes = new ArrayList<Node>(); 
				for(Integer idx:nodeIdxs){
					Node node = nodes[idx];
					if (node.isLegalNodeForSyn())
						storedNodes.add(node);
				}
				ArrayList<Edge> storedEdges = new ArrayList<Edge>();
				for (Edge edge:edges){
					if (storedNodes.contains(edge.sourceNode)&&storedNodes.contains(edge.sinkNode)){
						storedEdges.add(edge);
					}
				}
				Graph newGraph = new Graph(storedNodes, storedEdges, 1);
				postGraphsDel.add(newGraph);
				postGraphsMap.put(id, postGraphsDel);
				 Logger.log("idx:" +id+"//newGraph: " + newGraph);

			}
			
			postCLTMap.get(id).addAll(controlCLTs);
			postCLTMap.get(id).addAll(newCLTs);
			
			ArrayList<CLTData> tmp = new ArrayList<CLTData>();

			if (!GlobalConfig.isUSeNonLegalForSyn){
				for (CLTData clt:postCLTMap.get(id)){
					if (clt.isLegalForSyn())
						tmp.add(clt);
				}
			}
			else {
				tmp.addAll(postCLTMap.get(id));
			}
			newpostCLTMap.put(id, tmp);
//			 Logger.log("idx:" +id+"//newGraph: " + postGraphsMap.get(id));


		}

//		Logger.log(postCLTMap.get(2880348l));
//		System.exit(0);


		proceedPostCLTMapSimple(newpostCLTMap,  queryListPath);

		Logger.log("connPosts size: " + connPosts.size());

		Logger.log("postIDConnMap size: " + postIDConnMap.size());
		Logger.log("newpostCLTMap size: " + newpostCLTMap.size()); 
		
		HashSet<Long> remain = new HashSet<Long>();
		remain.addAll(postIDConnMap.keySet());
		remain.removeAll(newpostCLTMap.keySet());
		Logger.log("remain : " + remain); 

		return postGraphsMap;
	}
	
	
	
	
	public static HashMap<ArrayList<Integer>,ArrayList<CLTData>> getCLTsFromIncompletCLTs(ArrayList<CLTData> clts, GlobalData globalData){
		ArrayList<CLTData> allCLTs = new ArrayList<CLTData>();
		ArrayList<Integer> allIdxs = new ArrayList<Integer>();
		for (int i=0; i<clts.size(); i++){			
			String threadID = "-1";
			String postID = "-1";
			CLTData aclt = clts.get(i);
			String label = aclt.getLabel();
			int clt_type = Integer.parseInt(aclt.type);

			
			CLTNameProcessing nameProcessing = new CLTNameProcessing(label, clt_type);
			if (nameProcessing.pqn!=null){
			
				CLTData clt = new CLTData(threadID, postID, nameProcessing.pqn, nameProcessing.name, nameProcessing.type);
//				Logger.log("label: " + label + "///clt_type: " + clt_type + "///added clt: " + clt);

				allCLTs.add(clt);
				allIdxs.add(i);

			}
		}
		HashMap<ArrayList<Integer>,ArrayList<CLTData>> idxsCLTsMap = new HashMap<ArrayList<Integer>, ArrayList<CLTData>>();
		idxsCLTsMap.put(allIdxs, allCLTs);
		return idxsCLTsMap;
	}
	
	public static ArrayList<HashMap<ArrayList<Integer>,ArrayList<CLTData>> > getCLTsFromGraphs(ArrayList<Graph> graphs, GlobalData globalData){
		ArrayList<HashMap<ArrayList<Integer>,ArrayList<CLTData>> > allClts = new ArrayList<HashMap<ArrayList<Integer>,ArrayList<CLTData>> >();
		for (Graph graph:graphs){
			HashMap<ArrayList<Integer>,ArrayList<CLTData>>  clts = getCLTsFromGraph(graph, globalData);
//			if (clts!=null&&clts.size()>0)
				allClts.add(clts);
		}
		return allClts;
	}
	
	public static HashMap<ArrayList<Integer>,ArrayList<CLTData>> getCLTsFromGraph(Graph graph, GlobalData globalData){
		Node[] nodes = graph.nodes;
		ArrayList<CLTData> allCLTs = new ArrayList<CLTData>();
		ArrayList<Integer> allIdxs = new ArrayList<Integer>();
		for (int i=0; i<nodes.length; i++){
			
			Node node = nodes[i];
			String threadID = "-1";
			String postID = "-1";
			String label = node.getNodeLabel(globalData);
			
//			String pqn = "";
//			String name = "";
//			String type = "1";
//			Logger.log("label: " + label);
			int clt_type = node.nodeRole;
//			Logger.log("label: " + label + "///clt_type: " + clt_type );

			
			CLTNameProcessing nameProcessing = new CLTNameProcessing(label, clt_type);
//			if (label.contains("#")){
////				int idx = label.lastIndexOf("#");
//				type = "0";
//				pqn = nameProcessing.pqn ;//label.substring(0, idx);
//				name = nameProcessing.name ;//label.substring(idx+1);
//			}
//			else if (label.contains(".")){
////				int idx = label.lastIndexOf(".");
//				type = "1";
//				pqn = nameProcessing.pqn;//label.substring(0, idx);
//				name = nameProcessing.name;//label.substring(idx+1);
//			}
//			else {
//				type = "2";
//			}
				
			if (nameProcessing.pqn!=null){
			
				CLTData clt = new CLTData(threadID, postID, nameProcessing.pqn, nameProcessing.name, nameProcessing.type);
				allCLTs.add(clt);
				allIdxs.add(i);

			}
		}
		HashMap<ArrayList<Integer>,ArrayList<CLTData>> idxsCLTsMap = new HashMap<ArrayList<Integer>, ArrayList<CLTData>>();
		idxsCLTsMap.put(allIdxs, allCLTs);
		return idxsCLTsMap;
	}
	
	
	public static ArrayList<Graph> getGraphsFromSnippets(ArrayList<String> snippets, int count, GlobalData globalData, long id){
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		for (String snippet:snippets){
			Graph graph =  getGraphFromSnippet(snippet, count, globalData, id);
			if (graph!=null){
				graphs.add(graph);
			}
		}
		return graphs;
	}
	
	public static Graph getGraphFromSnippet(String snippet, int count, GlobalData globalData, long id){
		ArrayList<TypeInfo> allTypeList = new ArrayList<TypeInfo>();
		try{
			allTypeList = doParse1(snippet, count, id);
//			if (allTypeList.size()==0){
//				allTypeList = doParse2(snippet);
//				if (allTypeList.size()==0){
//					allTypeList = doParse3(snippet);
////					if (allTypeList.size()==0){
////						System.exit(0);
////					}
//				}
//			}
		}
		catch(Exception e){
			e.printStackTrace();
//			System.exit(0);
		}
		
//		Logger.log("allTypeList: " + allTypeList);
		if (allTypeList.size()>0){
			if (allTypeList.get(0).methodDecList.size()>0){
				MethodInfo methodInfo = allTypeList.get(0).methodDecList.get(0);
				Graph gr = DataUtils.convertGroumToGraph(methodInfo, globalData);
				return gr;
			}
			
		}
		return null;
	}
	
	public static ArrayList<TypeInfo> doParse3(String snippet) throws IOException{
		String path = GlobalConfig.tmpDir + "Dummy.java"; 
		FileWriter fw = new FileWriter(path);
		String wrappedCode = wrapCode3(snippet);
		fw.append(wrappedCode);
		fw.close();
		
		JavaGroumVisitorSimple  myVisitor = new JavaGroumVisitorSimple();
		myVisitor.doMain(GlobalConfig.tmpDir);
		return  myVisitor.allTypeList;
	}
	public static String wrapCode3(String snippet){
		int idx = 0;
		if (snippet.contains("class"))
			idx = snippet.indexOf("class");
		String tmp = snippet.substring(idx); 	
		StringBuilder sb = new StringBuilder();
		sb.append(tmp + System.lineSeparator());
		return sb.toString();
	}
	
	public static ArrayList<TypeInfo> doParse2(String snippet) throws IOException{
		String path = GlobalConfig.tmpDir + "Dummy.java"; 
		FileWriter fw = new FileWriter(path);
		String wrappedCode = wrapCode2(snippet);
		fw.append(wrappedCode);
		fw.close();
		
		JavaGroumVisitorSimple  myVisitor = new JavaGroumVisitorSimple();
		myVisitor.doMain(GlobalConfig.tmpDir);
		return  myVisitor.allTypeList;
	}
	public static String wrapCode2(String snippet){
		StringBuilder sb = new StringBuilder();
		sb.append("public class Dummy {" + System.lineSeparator());
		sb.append(snippet + System.lineSeparator());
		sb.append("}");
		return sb.toString();
	}
	
	public static ArrayList<TypeInfo> doParse1(String snippet, int count, long id) throws IOException{
		String path = GlobalConfig.tmpDir + "Dummy.java"; 
		FileWriter fw = new FileWriter(path);
		String wrappedCode = wrapCode1(snippet, count);
		fw.append(wrappedCode);
		fw.flush();
		fw.close();
		
		
		
		JavaGroumVisitor  myVisitor = new JavaGroumVisitor();
		myVisitor.doMain(GlobalConfig.tmpDir);
		
		String dummyFilePath = GlobalConfig.dummyDirPath + "dummy_" + id + "_" + count +".java";
		FileWriter fwDummy = new FileWriter(dummyFilePath);
		fwDummy.append(wrappedCode);
		fwDummy.flush();
		fwDummy.close();
		
		return  myVisitor.allTypeList;
	}
	public static String wrapCode1(String snippet, int count){
		StringBuilder sb = new StringBuilder();
		sb.append("public class Dummy {" + System.lineSeparator());
		sb.append("public void method"+count +"() { ");
		sb.append(snippet );
		sb.append(" } ");
		sb.append("}");
		return sb.toString();
	}
	
	
	
	
	
	public static void proceedPostCLTMap(TreeMap<Long, ArrayList<CLTData>> postCLTMap,TreeMap<Long, ArrayList<String>> postSnippetsMap , String bigQueryListPath){
		try{
			FileWriter fwCLT = new FileWriter(bigQueryListPath);
			FileWriter fwPost = new FileWriter(GlobalConfig.postDebugPath);
			
			int countPost=0;
			for (Long postIdx:postCLTMap.keySet()){
				if(postCLTMap.get(postIdx).size()<=0)
					continue;
				countPost++;
				if(countPost>GlobalConfig.maxNumQueries){
					break;
				}
				fwCLT.append(postIdx.toString());
				for(CLTData clt:postCLTMap.get(postIdx)){
					String pqn = clt.pqn;
					String name=clt.name;
					
					String fullName = pqn + name;
					fwCLT.append(":::" + clt.type);
					fwCLT.append("//");
					fwCLT.append(fullName);
				}
				fwCLT.append(System.lineSeparator());
				fwPost.append(postIdx.toString());
				for (String snippet:postSnippetsMap.get(postIdx)){
					String formattedSnippet = snippet.replaceAll("\\s", " ");
					fwPost.append(":::" + formattedSnippet);
				}
				fwPost.flush();
				fwPost.append(System.lineSeparator());
				
			}
			fwCLT.close();
			fwPost.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void proceedPostCLTMapSimple(TreeMap<Long, ArrayList<CLTData>> postCLTMap, String bigQueryListPath){
		try{
			FileWriter fwCLT = new FileWriter(bigQueryListPath);
			
			int countPost=0;
			for (Long postIdx:postCLTMap.keySet()){
				if(postCLTMap.get(postIdx).size()<=0)
					continue;
				countPost++;
				if(countPost>GlobalConfig.maxNumQueries){
					break;
				}
				fwCLT.append(postIdx.toString());
				for(CLTData clt:postCLTMap.get(postIdx)){
					String pqn = clt.pqn;
					String name=clt.name;
					
					String fullName = pqn + name;
					fwCLT.append(":::" + clt.type);
					fwCLT.append("//");
					fwCLT.append(fullName);
				}
				fwCLT.append(System.lineSeparator());
				
			}
			fwCLT.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public static HashMap<String, Integer> getElementList(NodeGraphDatabase nodeGraphDB, GlobalData globalData){
		HashMap<String, Integer> elementList = new HashMap<String, Integer>();
		for(Node node: nodeGraphDB.nodeGraphsMap.keySet()){
			
			String element = node.getNodeLabel(globalData).trim();
			if (element.startsWith("//")||element.startsWith("null.")||element.toLowerCase().contains("dummy"))
				continue;
			
			if (nodeGraphDB.nodeGraphsMap.get(node)==null)
				continue;
			
			elementList.put(element, nodeGraphDB.nodeGraphsMap.get(node).size());
		}
		return elementList;
	}
	
	
	public static HashMap<String, Integer> proceedNodeGraphDB(GraphDatabase graphDB){
//		Logger.log("Loading graphDB");
//		GraphDatabase graphDB = GraphDatabase.readGraphDatabase(DatabaseConfig.graphDatabasePathParentChild);
		Logger.log("createMapFromGraphDB");
		NodeGraphDatabase nodeGraphDB = new NodeGraphDatabase();
		nodeGraphDB.createMapFromGraphDB(graphDB);
//		FileUtils.writeObjectFile(nodeGraphDB, DatabaseConfig.nodeGraphDatabasePath);
		HashMap<String, Integer> elementList = getElementList(nodeGraphDB, graphDB.globalData);		
		return elementList;
	}
	
	public static HashMap<Long, ArrayList<CLTData>> getPostCLTMap(ArrayList<CLTData> allTable, HashSet<String> elementList){
		HashMap<Long, ArrayList<CLTData>> postCLTMap = new HashMap<Long, ArrayList<CLTData>>();
		int count = 0;
		System.out.println();

		for (CLTData aClt:allTable){
			String postStr = aClt.postID;
			Long postID = Long.parseLong(postStr);
			String clt_pqn = aClt.pqn;
			String clt_name = aClt.name;
			String clt_type = aClt.type;
			
			count++;
//			if (count%1000==0){
//				System.out.print(count +" ");
//				if (count%50000==0){
//					System.out.println();
//				}	
//			}
			FullNameData nameData = new FullNameData(clt_pqn, clt_name, clt_type);
			
			if (nameData.isValid()){
				CLTData clt = new CLTData(aClt.threadID, aClt.postID, nameData.pqn, nameData.name, clt_type); 
			
				if(postCLTMap.containsKey(postID)){
					postCLTMap.get(postID).add(clt);
				}
				else {
					ArrayList<CLTData> clts = new ArrayList<CLTData>();
					clts.add(clt);
					postCLTMap.put(postID, clts);
				}
			}
		}
		System.out.println();
		return postCLTMap;
	}
	
	
	
	
	
	
	public static void readCSV(String tablePath){
		try{
			ArrayList<CLTData> allTable = new ArrayList<CLTData>(); 
			
			
			BufferedReader br = new BufferedReader(
				    new InputStreamReader(new FileInputStream(tablePath)), 100000);
			
			String line = null;
			int countLine = 0;
			while ((line = br.readLine()) != null)
			{
				countLine++;
				allTable.add(getCLTLine(line));
				if(countLine%10000==0){
					System.out.print(countLine + " ");
					if(countLine%1000000==0){
						System.out.println();
					}						
				}
			}
			br.close();
			
			Logger.log(System.lineSeparator() + "allTable.size: " + allTable.size());
			FileUtils.writeObjectFile(allTable, GlobalConfig.cltTablePath);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static CLTData getCLTLine(String line){
		String[] split = line.split("&");
		CLTData aCLT = new CLTData(split[0], split[1], split[2], split[3], split[4]);
		return aCLT;
	}
	
}
