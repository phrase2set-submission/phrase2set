package datastructure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LabelIdxDictionary implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 199249251418374025L;
	
	
	public final static int OutOfDictIdx = -1;
	public final static String OutOfDictLabel = null;
	
	public LinkedHashMap<String, Integer> labelIdxMap = new  LinkedHashMap<>();
	public HashMap<Integer, String> idxLabelMap = new HashMap<>();
	
	public void addLabel(String label){
		if(!labelIdxMap.containsKey(label)){
			int idx = labelIdxMap.size();
			labelIdxMap.put(label, idx);
			idxLabelMap.put(idx, label);
		}
	}
	
	public int addLabelgetIdx(String label){
		if(!labelIdxMap.containsKey(label)){
			int idx = labelIdxMap.size();
			labelIdxMap.put(label, idx);
			idxLabelMap.put(idx, label);
			return idx;
		}
		else {
			return labelIdxMap.get(label);
		}
	}
	
	public int getIdx(String label){
		if (labelIdxMap.containsKey(label)){
			return labelIdxMap.get(label);
		}
		else {
			return OutOfDictIdx;
		}
	}
	
	public String getLabel(int idx){
		if (idxLabelMap.containsKey(idx)){
			return idxLabelMap.get(idx);
		}
		else{
			return OutOfDictLabel;
		}
	}
	
	public int getSize(){
		return this.labelIdxMap.size();
	}
}
