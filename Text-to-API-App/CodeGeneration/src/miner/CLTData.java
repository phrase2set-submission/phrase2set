/**
 * 
 */
package miner;

import java.io.Serializable;

import config.GlobalConfig;

/**
 * @author anhnt
 *
 */
public class CLTData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 406081903339368945L;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public String threadID = "";
	public String postID = "";
	public String pqn = "";
	public String name = "";
	public String type = "";

	public String getLabel(){
		if (type.equals("1")){
			return pqn+"."+name;
		}
		else if (type.equals("3")){
			return pqn+"#"+name;
		}
		return pqn+name;
	}
	
	public CLTData(String threadID, String postID, String pqn, String name,
			String type) {
		this.threadID = threadID.intern();
		this.postID = postID.intern();
		this.pqn = pqn.intern();
		this.name = name.intern();
		this.type = type.intern();
	}


	public boolean isLegalForSyn(){
		String combine = pqn + name;
		for (String tmp:GlobalConfig.nonLegalStrs){
			if (combine.contains(tmp))
				return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "CLTData [threadID=" + threadID + ", postID=" + postID
				+ ", pqn=" + pqn + ", name=" + name + ", type=" + type + "]";
	}
	
	
}
