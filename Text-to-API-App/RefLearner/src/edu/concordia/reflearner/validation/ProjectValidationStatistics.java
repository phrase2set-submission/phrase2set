package edu.concordia.reflearner.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class ProjectValidationStatistics {

	private int totalRefactoringMethods;
	private int totalMatchedRefactoringMethods;

	private int numSubGraphsMatched;

	private int minMethodsMatchedInFold = 0;
	private int maxMethodsMatchedInFold = 0;
	private int averageMatchedMethodInEachFold;

	private int maxFrequency = 0;
	private int maxMatches = 0;

	private String projectName;
	private String gitURL;

	private List<StatsPerFold> allStats = new ArrayList<StatsPerFold>();

	public ProjectValidationStatistics(final String pn, final String gu) {
		projectName = pn;
		gitURL = gu;
	}

	public void addMatchInfoPerFold(StatsPerFold stats) {
		allStats.add(stats);
	}

	public void calculateAndPrintOverallStats() {

		Entry<Integer, Integer> maxFrequencyEntry = null;
		Entry<Integer, Integer> maxMatchesEntry = null;

		int i = 0;

		for (StatsPerFold stats : allStats) {

			totalRefactoringMethods += stats.getTotalMethods();
			totalMatchedRefactoringMethods += stats.getNumMethodsMatched();

			numSubGraphsMatched += stats.getTotalSubGraphsMatched();

			int max = stats.getMaximumFrequency();

			if (max > maxFrequency) {
				maxFrequency = max;
			}

			max = stats.getMaximumMatches();
			if (max > maxMatches) {
				maxMatches = max;
			}

			Entry<Integer, Integer> entry = stats.getMaximumFrequencyWithNumGraphs();
			if (maxFrequencyEntry != null && entry != null) {
				if (entry.getKey() > maxFrequencyEntry.getKey()) {
					maxFrequencyEntry = entry;
				}
			} else if (maxFrequencyEntry == null && entry != null) {
				maxFrequencyEntry = entry;
			}

			entry = stats.getMaximumMatchedGraphWithFrequency();
			if (maxMatchesEntry != null && entry != null) {
				if (entry.getValue() > maxMatchesEntry.getValue()) {
					maxMatchesEntry = entry;
				}
			} else if (maxMatchesEntry == null && entry != null) {
				maxMatchesEntry = entry;
			}

			int numMethodsMatched = stats.getNumMethodsMatched();

			if (i == 0) {
				minMethodsMatchedInFold = numMethodsMatched;
			} else {
				if (numMethodsMatched < minMethodsMatchedInFold) {
					minMethodsMatchedInFold = numMethodsMatched;
				}
			}

			if (i == 0) {
				maxMethodsMatchedInFold = numMethodsMatched;
			} else {
				if (numMethodsMatched > maxMethodsMatchedInFold) {
					maxMethodsMatchedInFold = numMethodsMatched;
				}
			}
			i++;
		}

		averageMatchedMethodInEachFold = totalMatchedRefactoringMethods / CrossValidationWithinProject.K;

		StringBuilder sb = new StringBuilder();
		sb.append("CROSS VALIDATION REPORT FOR PROJECT " + projectName + " URL " + gitURL);
		sb.append(System.lineSeparator());

		sb.append("Total number of extract method refactorings : " + totalRefactoringMethods);
		sb.append(System.lineSeparator());
		sb.append("Number of extract method matched : " + totalMatchedRefactoringMethods);
		sb.append(System.lineSeparator());

		sb.append("Average number of method matches in each fold : " + averageMatchedMethodInEachFold);
		sb.append(System.lineSeparator());

		sb.append("Number of subgraph matches : " + numSubGraphsMatched);
		sb.append(System.lineSeparator());

		//sb.append("Maximum frequency of subgraph matches : " + maxFrequencyEntry.getKey()
		//		+ ", number of graphs matched for this frequency : " + maxFrequencyEntry.getValue());
		sb.append(System.lineSeparator());

		//sb.append("Maximum number of graphs matched : " + maxMatchesEntry.getValue() + ", frequency of this match is : "
		//		+ maxMatchesEntry.getKey());
		sb.append(System.lineSeparator());

		sb.append("Maximum methods matched in a fold : " + maxMethodsMatchedInFold);
		sb.append(System.lineSeparator());

		sb.append("Minimum methods matched in a fold : " + minMethodsMatchedInFold);
		sb.append(System.lineSeparator());

		System.out.println(sb.toString());
	}

}
