/**
 * 
 */
package utils;

import java.util.ArrayList;

/**
 * @author anhnt
 *
 */
public class StringGraphComparisonData {


	public long numGraphs = 0;
	public long total1GramActual = 0;
	public long total1GramSyn = 0;
	public long total1GramShared = 0;
	public long total2GramActual = 0;
	public long total2GramSyn = 0;
	public long total2GramShared = 0;
	public long total3GramActual = 0;
	public long total3GramSyn = 0;
	public long total3GramShared = 0;
	public long total4GramActual = 0;
	public long total4GramSyn = 0;
	public long total4GramShared = 0;
	
	public ArrayList<Long> num1GramsActual = new ArrayList<Long>();
	public ArrayList<Long> num1GramsSyn = new ArrayList<Long>();
	public ArrayList<Long> num1GramsShared = new ArrayList<Long>();
	public ArrayList<Long> num2GramsActual = new ArrayList<Long>();
	public ArrayList<Long> num2GramsSyn = new ArrayList<Long>();
	public ArrayList<Long> num2GramsShared = new ArrayList<Long>();
	public ArrayList<Long> num3GramsActual = new ArrayList<Long>();
	public ArrayList<Long> num3GramsSyn = new ArrayList<Long>();
	public ArrayList<Long> num3GramsShared = new ArrayList<Long>();
	public ArrayList<Long> num4GramsActual = new ArrayList<Long>();
	public ArrayList<Long> num4GramsSyn = new ArrayList<Long>();
	public ArrayList<Long> num4GramsShared = new ArrayList<Long>();
	
	
	
	public ArrayList<Double> percent1GramShared = new ArrayList<Double>();
	public ArrayList<Double> percent2GramShared = new ArrayList<Double>();
	public ArrayList<Double> percent3GramShared = new ArrayList<Double>();
	public ArrayList<Double> percent4GramShared = new ArrayList<Double>();
	
	
	public void update(long num1GramActual, long num1GramSyn, long num1GramShared,
			long num2GramActual, long num2GramSyn, long num2GramShared,
			long num3GramActual, long num3GramSyn, long num3GramShared,
			long num4GramActual, long num4GramSyn, long num4GramShared){
		num1GramsActual.add(num1GramActual);
		num1GramsSyn.add(num1GramSyn);
		num1GramsShared.add(num1GramShared);
		num2GramsActual.add(num2GramActual);
		num2GramsSyn.add(num2GramSyn);
		num2GramsShared.add(num2GramShared);
		num3GramsActual.add(num3GramActual);
		num3GramsSyn.add(num3GramSyn);
		num3GramsShared.add(num3GramShared);
		num4GramsActual.add(num4GramActual);
		num4GramsSyn.add(num4GramSyn);
		num4GramsShared.add(num4GramShared);
	}

	public void doStatistics(){
		calcAllTotal();
		double percent1GramSharedAcc = calcPercentShared(total1GramActual, total1GramSyn, total1GramShared); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percent1GramSharedAcc: " + percent1GramSharedAcc);
		double percent2GramSharedAcc = calcPercentShared(total2GramActual, total2GramSyn, total2GramShared); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percent2GramSharedAcc: " + percent2GramSharedAcc);
		double percent3GramSharedAcc = calcPercentShared(total3GramActual, total3GramSyn, total3GramShared); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percent3GramSharedAcc: " + percent3GramSharedAcc);
		double percent4GramSharedAcc = calcPercentShared(total4GramActual, total4GramSyn, total4GramShared); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percent4GramSharedAcc: " + percent4GramSharedAcc);
		
		
		
		percent1GramShared = calcShared(num1GramsActual, num1GramsSyn, num1GramsShared);
		double percent1GramtSharedAvg = calcAverage(percent1GramShared);
		Logger.log("percen1GramtSharedAvg: " + percent1GramtSharedAvg);

		percent2GramShared = calcShared(num2GramsActual, num2GramsSyn, num2GramsShared);
		double percent2GramtSharedAvg = calcAverage(percent2GramShared);
		Logger.log("percen2GramtSharedAvg: " + percent2GramtSharedAvg);

		percent3GramShared = calcShared(num3GramsActual, num3GramsSyn, num3GramsShared);
		double percent3GramtSharedAvg = calcAverage(percent3GramShared);
		Logger.log("percen3GramtSharedAvg: " + percent3GramtSharedAvg);
		
		percent4GramShared = calcShared(num4GramsActual, num4GramsSyn, num4GramsShared);
		double percent4GramtSharedAvg = calcAverage(percent4GramShared);
		Logger.log("percen4GramtSharedAvg: " + percent4GramtSharedAvg);
		
	}
	
	
	public double calcAverage(ArrayList<Double> vals){
		double total =0;
		for (Double val:vals){
			total += val;
		}
		return total/(double)vals.size();
	}
	
	
	
	public ArrayList<Double> calcShared(ArrayList<Long> numActuals, ArrayList<Long> numSyns, ArrayList<Long> numShareds){
		int size = numActuals.size();
		
		ArrayList<Double> percentShareds = new ArrayList<Double>();  
		for (int i=0; i<size; i++){
			long numActual = numActuals.get(i);
			long numSyn = numSyns.get(i);
			long numShared = numShareds.get(i);
			double percentShared =  calcPercentShared(numActual, numSyn, numShared);
			percentShareds.add(percentShared);
		}
		return percentShareds;
	}
	
	public double calcPercentShared(Long numActual, Long numSyn, Long numShared){
		if (numActual+numSyn==0)
			return 1.0;
//		return  2*(double)numShared/((double)numActual + (double)numSyn);
		return  (double)numShared/((double)numActual);

	}
	
	
	public long calcTotal(ArrayList<Long> numElements){
		long total = 0;
		for (Long numElement:numElements){
			total += numElement;
		}
		return total;
	}
	
	public void calcAllTotal(){
		total1GramActual = calcTotal(num1GramsActual);
		total1GramSyn = calcTotal(num1GramsSyn);
		total1GramShared = calcTotal(num1GramsShared);
		total2GramActual = calcTotal(num2GramsActual);
		total2GramSyn = calcTotal(num2GramsSyn);
		total2GramShared = calcTotal(num2GramsShared);
		total3GramActual = calcTotal(num3GramsActual);
		total3GramSyn = calcTotal(num3GramsSyn);
		total3GramShared = calcTotal(num3GramsShared);
		total4GramActual = calcTotal(num4GramsActual);
		total4GramSyn = calcTotal(num4GramsSyn);
		total4GramShared = calcTotal(num4GramsShared);
	}
}
