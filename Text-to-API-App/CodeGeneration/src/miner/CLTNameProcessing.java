/**
 * 
 */
package miner;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import utils.Logger;

/**
 * @author anhnt
 *
 */
public class CLTNameProcessing {

	public String pqn ;
	public String name ;
	public String type;

	static ArrayList<String> pqnList = new ArrayList<String>();
	static ArrayList<String> nameList = new ArrayList<String>();
	static ArrayList<String> otherList = new ArrayList<String>();
	
	static HashMap<String, LinkedHashSet<String>> namePQNsMap = new HashMap<String, LinkedHashSet<String>>(); 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public boolean isValid(){
		if (name!=null)
			return true;
		return false;
	}

	public static void createDictionary(HashMap<String, Integer> elementList ){
		HashMap<String, HashMap<String, Integer>> tmpAll = new HashMap<String, HashMap<String,Integer>>();
		for (String element:elementList.keySet()){
			if(!element.contains(".")){
				otherList.add(element);
			}
			else {
				int idx = element.lastIndexOf(".");
				String pqn = element.substring(0, idx);
				pqnList.add(pqn);
				String name = element.substring(idx+1);
				nameList.add(name);
//				if (namePQNsMap.containsKey(name)){
//					namePQNsMap.get(name).add(pqn);
//				}
				if (tmpAll.containsKey(name)){
					tmpAll.get(name).put(pqn, elementList.get(element));
				}
				else {
					HashMap<String, Integer> tmp = new HashMap<String, Integer>();
					tmp.put(pqn, elementList.get(element));
//					namePQNsMap.put(name, tmp);
					tmpAll.put(name, tmp);
				}
			}
		}
		for (String name:tmpAll.keySet()){
			HashMap<String, Integer> tmp = tmpAll.get(name);
			TreeMap<Integer, ArrayList<String>> rpqn = new TreeMap<Integer, ArrayList<String>>();
			for (String pqn:tmp.keySet()){
				int count = tmp.get(pqn);
				if (rpqn.containsKey(count)){
					rpqn.get(count).add(pqn);
				}
				else {
					ArrayList<String>pqns = new ArrayList<String>();
					pqns.add(pqn);
					rpqn.put(count, pqns);
				}
			}
			LinkedHashSet<String> pqnSet = new LinkedHashSet<String>();
			for (Integer count:rpqn.descendingKeySet()){
				pqnSet.addAll(rpqn.get(count));
			}
			namePQNsMap.put(name, pqnSet);
		}
		
	}

	
	public static void outputCLTs(String outputPath){
		try{
			FileWriter fw =  new FileWriter(new File(outputPath));
			fw.append("Qualified Type" + ", Name" + ", Type" + System.lineSeparator());

			for (int i=0; i<pqnList.size();i++){
				String tmpPQN = pqnList.get(i);
				if (tmpPQN.contains("["))
					continue;
				if (tmpPQN.contains("<unknown"))
					continue;
				String tmpName = nameList.get(i);
				String type = "method";
				
				if (tmpName.contains("#")){
					int idx = tmpName.indexOf("#");
					String tmp = tmpName.substring(0, idx);
					tmpName = tmp;
					tmpPQN = tmpPQN +"." + tmp;
					type = "class";
				}
				fw.append(tmpPQN + ", " + tmpName + ", "  + type + System.lineSeparator());
			}
			
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getFullName(){
		return pqn +"." + name;
	}

//	public CLTNameProcessing(String clt_pqn, String clt_name, String clt_type){
//		if (clt_type.equals("method")){
////			LinkedHashSet<String> pqns = new LinkedHashSet<String>();
////			for (int i=0; i<nameList.size(); i++){
////				String name = nameList.get(i);
////				if (name.equals(clt_name)){
////					pqns.add(pqnList.get(i));
////				}
////			}
//			LinkedHashSet<String> pqns = namePQNsMap.get(clt_name);
//			if (pqns!=null&&pqns.size()>0){
//				name = clt_name;
//				if (clt_pqn.equals("!undef!")){
//					pqn = pqns.iterator().next();
//				}
//				else {
//					TreeMap<Double, ArrayList<String>> distPQNsMap = new TreeMap<Double, ArrayList<String>>(); 
//					for(String pqn:pqns){
//						double dist = dist(clt_pqn,pqn);
//						if (distPQNsMap.containsKey(dist)){
//							distPQNsMap.get(dist).add(pqn);
//						}
//						else {
//							ArrayList<String> tmp = new ArrayList<String>();
//							tmp.add(pqn);
//							distPQNsMap.put(dist, tmp);
//						}
//					}
//					pqn = distPQNsMap.get(distPQNsMap.firstKey()).get(0);
//				}
//			}
//
//		}
//	}
	
	
	public CLTNameProcessing(String label, int clt_type){
//		Logger.log("label: " + label + "//clt_type: " + clt_type);
		if (label.contains("#")){
			clt_type=3;
		}
		
		if (clt_type==3){
			int idx = label.lastIndexOf("#");
			String clt_pqn = label.substring(0, idx);
			String clt_name = label.substring(idx+1);
			
			name = "#" + clt_name;
			if (clt_pqn.equals("<unknownClassType>")){
				pqn = null;
			}
			else {
					TreeMap<Double, ArrayList<String>> distPQNsMap = new TreeMap<Double, ArrayList<String>>(); 
					for(String pqn:pqnList){
						if (pqn.contains(".")){
							if (pqn.endsWith("." + clt_pqn)){
								double dist = dist(clt_pqn,pqn);
								if (distPQNsMap.containsKey(dist)){
									distPQNsMap.get(dist).add(pqn);
								}
								else {
									ArrayList<String> tmp = new ArrayList<String>();
									tmp.add(pqn);
									distPQNsMap.put(dist, tmp);
								}
							}
						}
						else {
							if (pqn.endsWith(clt_pqn)){
								double dist = dist(clt_pqn,pqn);
								if (distPQNsMap.containsKey(dist)){
									distPQNsMap.get(dist).add(pqn);
								}
								else {
									ArrayList<String> tmp = new ArrayList<String>();
									tmp.add(pqn);
									distPQNsMap.put(dist, tmp);
								}
							}
						}
					}
					if(distPQNsMap.size()>0)
						pqn = distPQNsMap.get(distPQNsMap.lastKey()).get(0);
					
				
			}
			type = "3";
			
		}
		else if (clt_type==1){
//			Logger.log("namePQNsMap: " + namePQNsMap);
//			Logger.log("pqnList: " + pqnList);

			int idx = label.lastIndexOf(".");
			String clt_pqn = label.substring(0, idx);
			String clt_name = label.substring(idx+1);
			LinkedHashSet<String> pqns = namePQNsMap.get(clt_name);
			if (pqns!=null&&pqns.size()>0){
				name = "." + clt_name;
				if (clt_pqn.equals("<unknownClassType>")||clt_pqn.equals("<unknownMethod>")){
					pqn = pqns.iterator().next();
				}
				else {
//					TreeMap<Double, ArrayList<String>> distPQNsMap = new TreeMap<Double, ArrayList<String>>(); 
//					for(String pqn:pqns){
//						double dist = dist(clt_pqn,pqn);
//						if (distPQNsMap.containsKey(dist)){
//							distPQNsMap.get(dist).add(pqn);
//						}
//						else {
//							ArrayList<String> tmp = new ArrayList<String>();
//							tmp.add(pqn);
//							distPQNsMap.put(dist, tmp);
//						}
//					}
//					pqn = distPQNsMap.get(distPQNsMap.firstKey()).get(0);
					TreeMap<Double, ArrayList<String>> distPQNsMap = new TreeMap<Double, ArrayList<String>>(); 
					for(String pqn:pqnList){
						if (pqn.contains(".")){
							if (pqn.endsWith("." + clt_pqn)){
								double dist = dist(clt_pqn,pqn);
								if (distPQNsMap.containsKey(dist)){
									distPQNsMap.get(dist).add(pqn);
								}
								else {
									ArrayList<String> tmp = new ArrayList<String>();
									tmp.add(pqn);
									distPQNsMap.put(dist, tmp);
								}
							}
						}
						else {
							if (pqn.endsWith(clt_pqn)){
								double dist = dist(clt_pqn,pqn);
								if (distPQNsMap.containsKey(dist)){
									distPQNsMap.get(dist).add(pqn);
								}
								else {
									ArrayList<String> tmp = new ArrayList<String>();
									tmp.add(pqn);
									distPQNsMap.put(dist, tmp);
								}
							}
						}
					}
					if(distPQNsMap.size()>0)
						pqn = distPQNsMap.get(distPQNsMap.lastKey()).get(0);
				}
			}
			type = "1";
			
		}
		else if (clt_type==2){
			pqn = "";
			name = label;
			type="2";
//			Logger.log(pqn + "///" + name + "///" + type);
		}
	}

	public double dist(String clt_pqn, String pqn){
		String lcs = longestSubstring(clt_pqn, pqn);
		return (double)lcs.length()/(double)pqn.length();
	}
	public static String longestSubstring(String str1, String str2) {

		StringBuilder sb = new StringBuilder();
		if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty())
			return "";

		// ignore case
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();

		// java initializes them already with 0
		int[][] num = new int[str1.length()][str2.length()];
		int maxlen = 0;
		int lastSubsBegin = 0;

		for (int i = 0; i < str1.length(); i++) {
			for (int j = 0; j < str2.length(); j++) {
				if (str1.charAt(i) == str2.charAt(j)) {
					if ((i == 0) || (j == 0))
						num[i][j] = 1;
					else
						num[i][j] = 1 + num[i - 1][j - 1];

					if (num[i][j] > maxlen) {
						maxlen = num[i][j];
						// generate substring from str1 => i
						int thisSubsBegin = i - num[i][j] + 1;
						if (lastSubsBegin == thisSubsBegin) {
							//if the current LCS is the same as the last time this block ran
							sb.append(str1.charAt(i));
						} else {
							//this block resets the string builder if a different LCS is found
							lastSubsBegin = thisSubsBegin;
							sb = new StringBuilder();
							sb.append(str1.substring(lastSubsBegin, i + 1));
						}
					}
				}
			}}

		return sb.toString();
	}
}
