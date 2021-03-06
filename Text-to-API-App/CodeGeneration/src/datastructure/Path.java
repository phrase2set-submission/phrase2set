/**
 * 
 */
package datastructure;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Anh
 *
 */
public class Path implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 316960514622316975L;

	public Node[] nodeList;
	
	public int count = 0;
	
	
	
	public Path(Node[] nodeList, int count){
		if (nodeList!=null)
			this.nodeList = Arrays.copyOf(nodeList, nodeList.length);
		this.count = count;
		
	}
	
	public Path(Path prev, Node nextNode, int count){
		if (prev==null||prev.nodeList==null){
			nodeList = new Node[1];
			nodeList[0] = nextNode; 
		}
		else {
			int prevLen = prev.nodeList.length;
			nodeList = Arrays.copyOf(prev.nodeList, prevLen+1);
			nodeList[prevLen] = nextNode;
		}
		this.count = count;
	}
	
	public Node lastNode(){
		if (nodeList!=null)
			return nodeList[nodeList.length-1];
		return null;
	}
	
	
	public boolean roleEqual(Path other){
		if (this.nodeList.length!=other.nodeList.length){
			return false;
		}
		for (int i=0; i<this.nodeList.length; i++){
			if (!this.nodeList[i].roleEquals(other.nodeList[i])){
				return false;
			}
		}
		return true;
	}
	
	
	
	public int getHValue() {
		final int prime = 127;
		int result = 1;
		for (Node node:nodeList){
			result = prime * result + node.content;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (!Arrays.equals(nodeList, other.nodeList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Path [nodeList=");
		builder.append(Arrays.toString(nodeList));
		builder.append("]");
		return builder.toString();
	}


}
