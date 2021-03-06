package edu.concordia.reflearner.stats;

import java.util.HashMap;
import java.util.Map;

public class EdgeStatistics {

	private Map<String, Integer> extractedMethodCount = new HashMap<String, Integer>();
	private Map<String, Integer> origMethodCount = new HashMap<String, Integer>();

	private static EdgeStatistics es = new EdgeStatistics();

	private EdgeStatistics() {
	}

	public static EdgeStatistics getInstance() {
		return es;
	}
	
	public void reset() {
		this.extractedMethodCount.clear();
		this.origMethodCount.clear();
	}
	
	public Map<String, Integer> getExtractedMethodCount() {
		return extractedMethodCount;
	}
	
	public Map<String, Integer> getOrigMethodCount() {
		return origMethodCount;
	}
	
	public void addCount(MethodType mt, String edge) {
		int ec = 0;
		if (MethodType.EXTRACTED_METHOD.equals(mt)) {

			Integer existingCount = extractedMethodCount.get(edge);
			if (existingCount != null) {
				ec = existingCount.intValue();
				ec++;
			} else {
				ec++;
			}

			extractedMethodCount.put(edge, new Integer(ec));
		} else if (MethodType.ORIG_METHOD.equals(mt)) {

			Integer existingCount = origMethodCount.get(edge);
			if (existingCount != null) {
				ec = existingCount.intValue();
				ec++;
			} else {
				ec++;
			}

			origMethodCount.put(edge, new Integer(ec));
		}
	}
}
