package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author thanhng
 */
public class SampleAnswerData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3901427181258108213L;
	
	public long answerId;
	/* Id of the question having this as a answer */
	public long parentId;
	
	public String content;
	
	/* Extracted code fragment */
	public String code;
	
	public String title;
	
	/* Just in case <content> is so ambiguous and meaningless */
	public String question;
	
	public PosterData owner;
	
	/* Original core of this post */
	public long score;
	
	/* New weighted score */
	public double weight;
	
	public List<CodeElementData> codeElements = new ArrayList<CodeElementData>();
	
	public SampleAnswerData(long aId, long pId, String cnt, String code, 
			String title, String question, PosterData poster, long score) {
		answerId = aId;
		parentId = pId;
		content = cnt;
		this.code = code;
		this.title = title;
		this.question = question;
		owner = poster;
		this.score = score;
		weight = 0.0;
	}

	public SampleAnswerData() {
		weight = 0.0;
	}
}

class WeightComparator implements Comparator<SampleAnswerData> {
	public int compare(SampleAnswerData sample1, SampleAnswerData sample2) {
		if(sample1.weight < sample2.weight)
			return 1;
		else if(sample1.weight > sample2.weight)
			return -1;
		else
			return 0;
	}	
}

class ScoreComparator implements Comparator<SampleAnswerData> {
	public int compare(SampleAnswerData sample1, SampleAnswerData sample2) {
		int greater = (sample1.score > sample2.score) ? 1 : 0;
		return greater;
	}	
}

class OwnerReputationComparator implements Comparator<SampleAnswerData> {
	public int compare(SampleAnswerData sample1, SampleAnswerData sample2) {
		int greater = sample1.owner.getPosterReputation() > sample1.owner.getPosterReputation() ? 1 :0;
		return greater;
	}	
}