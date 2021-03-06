/**
 * 
 */
package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import datastructure.Edge;
import datastructure.GlobalData;
import datastructure.Graph;
import datastructure.Node;

/**
 * @author anhnt_000
 *
 */
public class GraphDrawingUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	

	public static String creatDotStr(Graph gr, GlobalData globalData){
		StringBuffer sb = new StringBuffer();
		sb.append("digraph mygraph { "+System.lineSeparator());
		int countNode = 0; 
		HashMap<Node, String> nodeNameMap = new HashMap<Node, String>();
		for(Node node:gr.nodes){
			String nodeName = "node" + countNode;
			nodeNameMap.put(node, nodeName);
			String shape = "box";
			if(node.nodeRole==2){
				shape = "diamond";
			}
			sb.append( nodeName + " [label=\"" + node.getNodeLabel(globalData) +"\", shape=" + shape +"];" + System.lineSeparator());
			countNode++;
		}
		
		
		
//		Logger.log("nodeNameMap: " + nodeNameMap);
		for (Edge edge:gr.edges){
			if(edge == null){
				continue;
			}
			Node sourceNode = edge.sourceNode;
//			Logger.log("sourceNode: " + sourceNode + "///" + nodeNameMap.get(sourceNode));

			Node sinkNode = edge.sinkNode;
//			Logger.log("sinkNode: " + sinkNode + "///" + nodeNameMap.get(sinkNode));

			sb.append(nodeNameMap.get(sourceNode) + "->" + nodeNameMap.get(sinkNode) + ";" + System.lineSeparator());
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	public static void callConvert(String dotPath, String gifPath){
		try {
			String cmd = "dot -Tgif " + dotPath + " -o "+gifPath;
			Process p = Runtime.getRuntime().exec(cmd);
		    p.waitFor();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static  void outputDotFile(Graph gr, String outputPath, GlobalData globalData){
		try{
			FileWriter fw = new FileWriter(outputPath);
			fw.append(creatDotStr(gr, globalData));
			fw.flush();
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static  void outputsequenceFile(Graph gr, String outputPath, GlobalData globalData){
		try{
			FileWriter fw = new FileWriter(outputPath);
			fw.append(gr.getNodeSequenceStr(globalData));
//			fw.append(creatDotStr(gr, globalData));
			fw.flush();
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	

}
