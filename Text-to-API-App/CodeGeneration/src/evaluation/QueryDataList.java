/**
 * 
 */
package evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import datastructure.GlobalData;
import datastructure.Node;

/**
 * @author anhnt_000
 *
 */
public class QueryDataList {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public LinkedHashMap<String, ArrayList<Node>> idxNodesMap = new LinkedHashMap<String, ArrayList<Node>>();
	
	public void createIdxNodesMap(String queryListFilePath, GlobalData globalData){
		try{
			Scanner sc = new Scanner(new File(queryListFilePath));
			while (sc.hasNextLine()){
				String tmp =sc.nextLine();
				String[] split = tmp.split(":::");
				String idx = split[0];
				ArrayList<Node> nodes = new ArrayList<Node>();
				for (int i=1; i<split.length;i++){
					String[] splitsmall = split[i].split("//");
					if(!globalData.nodeLabelDict.labelIdxMap.containsKey(splitsmall[1])){
						globalData.nodeLabelDict.addLabel(splitsmall[1]);
					}
					Node node = new  Node(Short.parseShort(splitsmall[0]), globalData.nodeLabelDict.getIdx(splitsmall[1]));
					nodes.add(node);
				}
				idxNodesMap.put(idx, nodes);
			}
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
