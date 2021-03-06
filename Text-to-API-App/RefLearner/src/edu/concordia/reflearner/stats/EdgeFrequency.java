package edu.concordia.reflearner.stats;

public class EdgeFrequency {

	private String edgeName;
	private String projectName;
	private int freqExtMethod;
	private int freqOrigMethod;

	public EdgeFrequency(String en, String pn, int fe, int fo) {
		edgeName = en;
		projectName = pn;
		freqExtMethod = fe;
		freqOrigMethod = fo;
	}

	public EdgeFrequency(String en, int fe, int fo) {
		this(en, null, fe, fo);
	}

	public String getEdgeName() {
		return edgeName;
	}

	public String getProjectName() {
		return projectName;
	}

	public int getFreqExtMethod() {
		return freqExtMethod;
	}

	public int getFreqOrigMethod() {
		return freqOrigMethod;
	}

	@Override
	public String toString() {
		return "EdgeCount [edgeName=" + edgeName + ", projectName=" + projectName + ", freqExtMethod=" + freqExtMethod
				+ ", freqOrigMethod=" + freqOrigMethod + "]";
	}

}
