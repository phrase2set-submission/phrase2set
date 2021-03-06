/**
 * 
 */
package datastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
//import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * @author anhnt
 *
 */
public class GraphUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public static DirectedSparseGraph createGraph(datastructure.Graph eg){
		DirectedSparseGraph g = new DirectedSparseGraph();
		Map<Node, Vertex> nodeVertexMap = new HashMap<Node, Vertex>();
		for (Node n:eg.nodes){
			Vertex v = new DirectedSparseVertex();
			nodeVertexMap.put(n, v);			
			g.addVertex(v);
		}
		for (Edge e:eg.edges){
			Node source = e.sourceNode;
			Node sink = e.sinkNode;
			Vertex gov = nodeVertexMap.get(source);
			Vertex dep = nodeVertexMap.get(sink);
			g.addEdge(new DirectedSparseEdge(gov, dep));
		}
		return g;
		
	}
	
//	public static DirectedGraph createGraph(Graph g){
//		DirectedSparseGraph<Vertex,edu.ucdenver.ccp.esm.Edge> graph = new DirectedSparseGraph<Vertex,edu.ucdenver.ccp.esm.Edge>();
//		Map<Node, Vertex> nodeVertexMap = new HashMap<Node, Vertex>();
//		for (Node n:g.nodes){
//			Vertex v = new Vertex("n-" + String.valueOf(n.content) +"/n");
//			v.setCompareForm( (v.getWord() + " " + v.getTag()).toLowerCase() );
//			v.setGeneralizedPOS(v.getTag());	
//			nodeVertexMap.put(n, v);			
//			graph.addVertex(v);
//		}
//		
//		for (Edge e:g.edges){
//			Node source = e.sourceNode;
//			Node sink = e.sinkNode;
//			Vertex gov = nodeVertexMap.get(source);
//			Vertex dep = nodeVertexMap.get(sink);
//			edu.ucdenver.ccp.esm.Edge ed = new edu.ucdenver.ccp.esm.Edge(gov, "dep", dep);
//			graph.addEdge(ed, gov, dep);
//		}
//		
//		return graph;
//	}
	
//	/**
//	 * Create graphs from dependency representation
//	 * @param r : input dependency representation separated by ";"
//	 * @return created dependency graph
//	 */
//	public static DirectedGraph createGraph(String r) {
//		DirectedSparseGraph<Vertex,Edge> graph = new DirectedSparseGraph<Vertex,Edge>();
//		Map<String, Vertex> tokenToNode = new HashMap<String, Vertex>();
//		/** dr: a single dependency representation */
//		for ( String dr : r.split("\\s*;\\s*") ) {
//			if ( ! dr.matches("^\\S+\\(\\S+\\s*,\\s*\\S+\\)\\s*$") )
//		    	throw new RuntimeException("The dependency representation: "
//						+ dr + " is not valid. Please check.");	
//		    Matcher md = Pattern.compile("^(\\S+)\\((\\S+)\\s*,\\s*(\\S+)\\)\\s*$").matcher(dr);
//		    md.find();
//		    String label = md.group(1);  
//		    String g = md.group(2); 
//		    String d = md.group(3);
//
//		    Vertex gov;
//		    if(!tokenToNode.containsKey(g)) {
//		        gov = new Vertex(g);
//		        graph.addVertex(gov);
//		        tokenToNode.put(g, gov);
//		    }
//		    else { gov = tokenToNode.get(g); }
//		    
//		    Vertex dep;
//		    if(!tokenToNode.containsKey(d)) {
//		        dep = new Vertex(d);
//		        graph.addVertex(dep);
//		        tokenToNode.put(d, dep);
//		    }   
//		    else { dep = tokenToNode.get(d); }
//		    
//		    Edge govToDep = new Edge(gov, label, dep);		    
//		    graph.addEdge(govToDep, gov, dep);
//		}	
//		
//		/** generates lemma for each node */
//		generateLemmas(graph.getVertices());
//		
//		return graph;
//   }
}
