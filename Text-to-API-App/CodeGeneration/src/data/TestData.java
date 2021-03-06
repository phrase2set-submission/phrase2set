package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author thanhng
 *
 */
public class TestData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1299484622222712119L;
	
	/* One answer post data */
	public List<SampleAnswerData> sampleData = new ArrayList<SampleAnswerData>();
	
	/* Store postid (documentid) for removing from training set */
	public HashMap<Long, Long> testSet = new HashMap<Long, Long>();
}