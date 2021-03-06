/**
 * 
 */
package evaluation;

/**
 * @author anhnt
 *
 */
public class TopKStat {

	int topK = 0;
	int total = 0;
	int totalHit = 0;
	double Accuracy = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public TopKStat(int topK, int total, int totalHit) {
		super();
		this.topK = topK;
		this.total = total;
		this.totalHit = totalHit;
		this.Accuracy = (double)totalHit/(double)total;
	}
	
	

}
