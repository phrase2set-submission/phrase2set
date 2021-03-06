/**
 * 
 */
package miner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeMap;

/**
 * @author anhnt
 *
 */
public class FullNameData {

	public String pqn ;
	public String name ;

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

	public static void createDictionary(HashSet<String> elementList ){
		for (String element:elementList){
			if(!element.contains(".")){
				otherList.add(element);
			}
			else {
				int idx = element.lastIndexOf(".");
				String pqn = element.substring(0, idx);
				pqnList.add(pqn);
				String name = element.substring(idx+1);
				nameList.add(name);
				if (namePQNsMap.containsKey(name)){
					namePQNsMap.get(name).add(pqn);
				}
				else {
					LinkedHashSet<String> tmp = new LinkedHashSet<String>();
					tmp.add(pqn);
					namePQNsMap.put(name, tmp);
				}
			}
		}
	}


	public String getFullName(){
		return pqn +"." + name;
	}

	public FullNameData(String clt_pqn, String clt_name, String clt_type){
		if (clt_type.equals("method")){
//			LinkedHashSet<String> pqns = new LinkedHashSet<String>();
//			for (int i=0; i<nameList.size(); i++){
//				String name = nameList.get(i);
//				if (name.equals(clt_name)){
//					pqns.add(pqnList.get(i));
//				}
//			}
			LinkedHashSet<String> pqns = namePQNsMap.get(clt_name);
			if (pqns!=null&&pqns.size()>0){
				name = clt_name;
				if (clt_pqn.equals("!undef!")){
					pqn = pqns.iterator().next();
				}
				else {
					TreeMap<Double, ArrayList<String>> distPQNsMap = new TreeMap<Double, ArrayList<String>>(); 
					for(String pqn:pqns){
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
					pqn = distPQNsMap.get(distPQNsMap.firstKey()).get(0);
				}
			}

		}
	}

	public double dist(String clt_pqn, String pqn){
		String lcs = longestSubstring(clt_pqn, pqn);
		return (double)lcs.length()/(double)clt_pqn.length();
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
