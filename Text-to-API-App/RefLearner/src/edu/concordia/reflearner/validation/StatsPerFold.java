package edu.concordia.reflearner.validation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class StatsPerFold {

	private Map<Integer, Integer> frequencyVsNumber = new TreeMap<Integer, Integer>(Comparator.reverseOrder());

	private int numMethodsMatched;
	private int totalMethods;
	private int totalSubGraphsMatched;

	public StatsPerFold() {

	}

	public void setNumMethodsMatched(int num) {
		numMethodsMatched = num;
	}

	public int getNumMethodsMatched() {
		return numMethodsMatched;
	}

	public int getTotalMethods() {
		return totalMethods;
	}

	public void setTotalMethods(int totalMethods) {
		this.totalMethods = totalMethods;
	}

	public int getTotalSubGraphsMatched() {
		return totalSubGraphsMatched;
	}

	public void setTotalSubGraphsMatched(int num) {
		this.totalSubGraphsMatched = num;
	}

	public Map<Integer, Integer> getFrequencyVsNumber() {
		return frequencyVsNumber;
	}

	public void setFrequencyVsNumber(Map<Integer, Integer> frequencyVsNumber) {
		this.frequencyVsNumber = frequencyVsNumber;
	}

	public int getMaximumFrequency() {
		if (frequencyVsNumber.isEmpty()) {
			return 0;
		}
		return Collections.max(frequencyVsNumber.keySet());
	}

	public int getMaximumMatches() {
		if (frequencyVsNumber.isEmpty()) {
			return 0;
		}
		return Collections.max(frequencyVsNumber.values());
	}

	public Entry<Integer, Integer> getMaximumMatchedGraphWithFrequency() {
		if (frequencyVsNumber.isEmpty()) {
			return null;
		}

		Entry<Integer, Integer> maxEnt = null;
		int maxMatches = getMaximumMatches();
		Set<Entry<Integer, Integer>> entrySet = frequencyVsNumber.entrySet();

		for (Entry<Integer, Integer> entry : entrySet) {
			if (entry.getValue() == maxMatches) {
				maxEnt = entry;
				break;
			}
		}

		return maxEnt;
	}

	public Entry<Integer, Integer> getMaximumFrequencyWithNumGraphs() {
		if (frequencyVsNumber.isEmpty()) {
			return null;
		}

		Entry<Integer, Integer> maxEnt = null;
		int maxFreq = getMaximumFrequency();
		Set<Entry<Integer, Integer>> entrySet = frequencyVsNumber.entrySet();

		for (Entry<Integer, Integer> entry : entrySet) {
			if (entry.getKey() == maxFreq) {
				maxEnt = entry;
				break;
			}
		}

		return maxEnt;
	}
}
