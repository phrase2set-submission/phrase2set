/**
 * 
 */
package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author anhnt
 *
 */
public class GraphComparisonData implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5590031812292927824L;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public long numGraphs = 0;
	public long totalNodesActual = 0;
	public long totalNodesSyn = 0;
	public long totalSharedNodes = 0;
	public long totalEdgesActual = 0;
	public long totalEdgesSyn = 0;
	public long totalSharedEdges = 0;
	
	public ArrayList<Long> numNodesActual = new ArrayList<Long>();
	public ArrayList<Long> numNodesSyn = new ArrayList<Long>();
	public ArrayList<Long> numSharedNodes = new ArrayList<Long>();
	public ArrayList<Long> numEdgesActual = new ArrayList<Long>();
	public ArrayList<Long> numEdgesSyn = new ArrayList<Long>();
	public ArrayList<Long> numSharedEdges = new ArrayList<Long>();
	
	public TreeMap<Long, Long> idNumNodesActualMap = new TreeMap<Long, Long>(); 
	public TreeMap<Long, Long> idNumNodesSynMap = new TreeMap<Long, Long>(); 
	public TreeMap<Long, Long> idNumSharedNodesMap = new TreeMap<Long, Long>(); 
	public TreeMap<Long, Long> idNumEdgesActualMap = new TreeMap<Long, Long>(); 
	public TreeMap<Long, Long> idNumEdgesSynMap = new TreeMap<Long, Long>(); 
	public TreeMap<Long, Long> idNumSharedEdgesMap = new TreeMap<Long, Long>(); 
	
	
	public ArrayList<Double> percentSharedNodes = new ArrayList<Double>();
	public ArrayList<Double> percentSharedEdges = new ArrayList<Double>();
	
	
	public void update(long numNodeActual, long numNodeSyn, long numSharedNode, long numEdgeActual, long numEdgeSyn, long numSharedEdge,
			long postid){
		numNodesActual.add(numNodeActual);
		numNodesSyn.add(numNodeSyn);
		numSharedNodes.add(numSharedNode);
		numEdgesActual.add(numEdgeActual);
		numEdgesSyn.add(numEdgeSyn);
		numSharedEdges.add(numSharedEdge);
		
		idNumNodesActualMap.put(postid, numNodeActual);
		idNumNodesSynMap.put(postid, numNodeSyn);
		idNumSharedNodesMap.put(postid, numSharedNode);
		idNumEdgesActualMap.put(postid, numEdgeActual);
		idNumEdgesSynMap.put(postid, numEdgeSyn);
		idNumSharedEdgesMap.put(postid, numSharedEdge);
	}
	
	public void doStatistics(){
		calcAllTotal();
		Logger.log("totalNodesActual: " + totalNodesActual);
		Logger.log("totalNodesSyn: " + totalNodesSyn);
		Logger.log("totalSharedNodes: " + totalSharedNodes);
		Logger.log("totalEdgesActual: " + totalEdgesActual);
		Logger.log("totalEdgesSyn: " + totalEdgesSyn);
		Logger.log("totalSharedEdges: " + totalSharedEdges);

		Logger.log("numNodesActual: " + numNodesActual);
		Logger.log("numNodesSyn: " + numNodesSyn);
		Logger.log("numSharedNodes: " + numSharedNodes);
		Logger.log("numEdgesActual: " + numEdgesActual);
		Logger.log("numEdgesSyn: " + numEdgesSyn);
		Logger.log("numSharedEdges: " + numSharedEdges);
		
		Logger.log("idNumNodesActualMap: " + idNumNodesActualMap);
		Logger.log("idNumNodesSynMap: " + idNumNodesSynMap);
		Logger.log("idNumSharedNodesMap: " + idNumSharedNodesMap);
		Logger.log("idNumEdgesActualMap: " + idNumEdgesActualMap);
		Logger.log("idNumEdgesSynMap: " + idNumEdgesSynMap);
		Logger.log("idNumSharedEdgesMap: " + idNumSharedEdgesMap);
		
		double percentSharedNodesAcc = calcPercentShared(totalNodesActual, totalNodesSyn, totalSharedNodes); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percentSharedNodesAcc: " + percentSharedNodesAcc);
		double percentSharedEdgesAcc = calcPercentShared(totalEdgesActual, totalEdgesSyn, totalSharedEdges); //2*(double)totalSharedEdges/((double)totalEdgesActual + (double)totalEdgesSyn);
		Logger.log("percentSharedEdgesAcc: " + percentSharedEdgesAcc);

//		percentSharedNodes = calcShared(numNodesActual, numNodesSyn, numSharedNodes);
//		double percentSharedNodesAvg = calcAverage(percentSharedNodes);
//		Logger.log("percentSharedNodesAvg: " + percentSharedNodesAvg);
//
//		percentSharedEdges = calcShared(numEdgesActual, numEdgesSyn, numSharedEdges);
//		double percentSharedEdgesAvg = calcAverage(percentSharedEdges);
//		Logger.log("percentSharedEdgesAvg: " + percentSharedEdgesAvg);
		
		Logger.log("node precision: " + (double)totalSharedNodes/(double)totalNodesSyn);

		Logger.log("edge precision: " + (double)totalSharedEdges/(double)totalEdgesSyn);

	}
	

	public void doStatisticsSimple(){
		calcAllTotal();
	
		
		double percentSharedNodesAcc = calcPercentShared(totalNodesActual, totalNodesSyn, totalSharedNodes); // 2*(double)totalSharedNodes/((double)totalNodesActual + (double)totalNodesSyn);
		Logger.log("percentSharedNodesAcc: " + percentSharedNodesAcc);
		double percentSharedEdgesAcc = calcPercentShared(totalEdgesActual, totalEdgesSyn, totalSharedEdges); //2*(double)totalSharedEdges/((double)totalEdgesActual + (double)totalEdgesSyn);
		Logger.log("percentSharedEdgesAcc: " + percentSharedEdgesAcc);
		Logger.log("node precision: " + (double)totalSharedNodes/(double)totalNodesSyn);

		Logger.log("edge precision: " + (double)totalSharedEdges/(double)totalEdgesSyn);


//		percentSharedNodes = calcShared(numNodesActual, numNodesSyn, numSharedNodes);
//		double percentSharedNodesAvg = calcAverage(percentSharedNodes);
//		Logger.log("percentSharedNodesAvg: " + percentSharedNodesAvg);
//
//		percentSharedEdges = calcShared(numEdgesActual, numEdgesSyn, numSharedEdges);
//		double percentSharedEdgesAvg = calcAverage(percentSharedEdges);
//		Logger.log("percentSharedEdgesAvg: " + percentSharedEdgesAvg);

	}
	
	public void showStatistics(){
		Logger.log("  idNumNodesActualMap: " + idNumNodesActualMap.size());
		Logger.log("  idNumNodesSynMap: " + idNumNodesSynMap.size());
		Logger.log("  idNumSharedNodesMap: " + idNumSharedNodesMap.size());
		Logger.log("  idNumEdgesActualMap: " + idNumEdgesActualMap.size());
		Logger.log("  idNumEdgesSynMap: " + idNumEdgesSynMap.size());
		Logger.log("  idNumSharedEdgesMap: " + idNumSharedEdgesMap.size());
		
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
		return (double)numShared/(double)numActual;// 2*(double)numShared/((double)numActual + (double)numSyn);
	}
	
	
	public long calcTotal(ArrayList<Long> numElements){
		long total = 0;
		for (Long numElement:numElements){
			total += numElement;
		}
		return total;
	}
	
	public void calcAllTotal(){
		totalNodesActual = calcTotal(numNodesActual);
		totalNodesSyn = calcTotal(numNodesSyn);
		totalSharedNodes = calcTotal(numSharedNodes);
		totalEdgesActual = calcTotal(numEdgesActual);
		totalEdgesSyn = calcTotal(numEdgesSyn);
		totalSharedEdges = calcTotal(numSharedEdges);
	}
	
	 
}
