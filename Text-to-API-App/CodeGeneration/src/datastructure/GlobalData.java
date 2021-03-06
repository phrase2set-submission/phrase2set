/**
 * 
 */
package datastructure;

import java.io.Serializable;

import utils.Logger;

/**
 * @author Anh
 *
 */
public class GlobalData implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 336164829981805065L;
	
	public LabelIdxDictionary nodeLabelDict = new LabelIdxDictionary();
	public LabelIdxDictionary projectDict = new LabelIdxDictionary();
	public LabelIdxDictionary methodDict = new LabelIdxDictionary(); 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void doStatistics(){
		Logger.log("nodeLabelDict size: " + nodeLabelDict.getSize());
		Logger.log("projectDict size: " + projectDict.getSize());
		Logger.log("methodDict size: " + methodDict.getSize());

	}

}
