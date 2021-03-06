/**
 * 
 */
package datastructure;

import java.io.Serializable;

import config.GlobalConfig;


/**
 * @author Anh
 *
 */
public class Node implements Serializable{
	public static short UNKNOWN = 0; 
	public static short ACTION = 1;
	public static short CONTROL = 2;
	public static short DATA = 3;
	public static GlobalData globalData;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7577539702091908228L;
	
	public short nodeRole;
	public int content;
	
	
	public Node(short nodeRole, int nodeContent) {
		this.nodeRole = nodeRole;
		this.content = nodeContent;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public boolean simpleRoleEquals(Node otherNode) {
		if (nodeRole != otherNode.nodeRole)
			return false;
		if (content != otherNode.content)
			return false;
		return true;
	}
	
	public  synchronized  boolean roleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (nodeRole != other.nodeRole)
			return false;
		if (content != other.content)
			return false;
		return true;
	}
	
	
	public boolean partRoleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
//		if (nodeRole != other.nodeRole)
//			return false;
		
//		if (content != other.content)
//			return false;
		String thisShort = getShortNodeLabel(this);
		String otherShort = getShortNodeLabel(other);
		if (!thisShort.equals(otherShort)){
			return false;
		}
		return true;
	}
	
	public String getShortNodeLabel(Node node){
		String tmp = node.getNodeLabel(globalData);
		if (tmp.contains("#")){
			int lastIdx = tmp.lastIndexOf("#");
			tmp = tmp.substring(lastIdx+1);
		}
		else if (tmp.contains(".")){
			int lastIdx = tmp.lastIndexOf(".");
			tmp = tmp.substring(lastIdx+1);
		}
		return tmp;
	}

	
	public boolean isConcernedNode(GlobalData globalData){
		String nodeLabel = getNodeLabel(globalData);
		
		for (String concernedLib:GlobalConfig.concernedLibs){
			if (nodeLabel.startsWith(concernedLib)){
				return true;
			}
		}
		
		return false;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [nodeRole=");
		builder.append(nodeRole);
		
		builder.append(", content=");
		
		if (globalData==null)
			builder.append(content);
		else
			builder.append(globalData.nodeLabelDict.getLabel(content));
		builder.append("]");
		return builder.toString();
	}
	
	public String getNodeLabel(GlobalData globalData){
		return globalData.nodeLabelDict.getLabel(content);
	}
	
	public String getNodeLabelSyn(GlobalData globalData){
		String label =  globalData.nodeLabelDict.getLabel(content);
		
		if (nodeRole==3){
			int startIdx = label.lastIndexOf("#");
			String prefix = label.substring(0, startIdx);
			return prefix + " var" + prefix;// + " = "; 
		}
		else if (nodeRole == 1){
			
			int startIdx = label.lastIndexOf(".");
			String prefix = label.substring(0, startIdx);
			if (label.endsWith(".new")){
				return "new " + prefix + "(..)";
			}
			return label + "(..)";
		}
		else if (nodeRole==2){
			int start = label.indexOf("=_");
			int end = label.indexOf("_]");
			return label.substring(start+2, end).toLowerCase();
		}
		
		return label;
	}

	
	public boolean isControlNode(GlobalData globalData){
		String nodeLabel = getNodeLabel(globalData);
		if (nodeLabel.startsWith("ControlInfo")){
			return true;
		}
		return false;
	}
	
	public boolean isConcernedNode(String[] concernedLibs, GlobalData globalData){
		String nodeLabel = getNodeLabel(globalData);
		for (String concernedLib:concernedLibs){			
			if (nodeLabel.startsWith(concernedLib)){
				return true;
			}
		}
		return false;
	}
	
	public int simpleHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + content;
		result = prime * result + nodeRole;
		return result;
	}

	
	public boolean isTypeEqual(Node other){
		if (other==null)
			return false;
		String thisLabel = this.getNodeLabel(globalData);
		if (thisLabel==null)
			return false;
		if (!thisLabel.contains(".")&&thisLabel.contains("#"))
			return false;
		String otherLabel = other.getNodeLabel(globalData);
		if (otherLabel==null)
			return false;
		if (!otherLabel.contains(".")&&otherLabel.contains("#"))
			return false;
		
		if (thisLabel.contains("#")&&otherLabel.contains("#"))
			return false;
		int thisLastIdx = 0;
		if (thisLabel.contains(".")){
			thisLastIdx = thisLabel.lastIndexOf(".");
		}
		else if (thisLabel.contains("#")){
			thisLastIdx = thisLabel.lastIndexOf("#");
		}
		String thisPrefix = thisLabel.substring(0, thisLastIdx); 
		
		int otherLastIdx = 0;
		if (otherLabel.contains(".")){
			otherLastIdx = otherLabel.lastIndexOf(".");
		}
		else if (otherLabel.contains("#")){
			otherLastIdx = otherLabel.lastIndexOf("#");
		}
		String otherPrefix = otherLabel.substring(0, otherLastIdx); 
		
		if(!thisPrefix.equals(otherPrefix))
			return false;
		return true;
	}
	

//	@Override
//	public String toString() {
//		return "Node [inEdges=" + Arrays.toString(inEdges) + ", outEdges="
//				+ Arrays.toString(outEdges) + ", nodeRole=" + nodeRole
//				+ ", nodeContent=" + content + ", count=" + count + "]";
//	}


	public boolean isLegalNodeForSyn(){
		if (nodeRole!=2)
			return true;
		String label = this.getNodeLabel(Node.globalData);
		if (label==null)
			return false;
//		if (label.contains("TRY")||label.contains("RETURN")||label.contains("CATCH"))
//			return false;
		
		for (String tmp:GlobalConfig.nonLegalStrs){
			if (label.contains(tmp))
				return false;
		}
		return true;
	}
	
}
