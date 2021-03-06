package edu.concordia.reflearner.stats;

import static edu.concordia.reflearner.util.DBUtil.JDBC_URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeSummaryWriter {

	public static int sameEdgeCount = 0;

	public static Map<String, Integer> edgeFrequenciesAcross = new HashMap<String, Integer>();

	public static Connection conn = null;

	public static final String QUERY_EDGE = "select * from globaledgefrequencies where edgename = ?";
	public static final String QUERY_EDGE_PROJECT = "select * from edgefrequencies where projectname = ?";
	public static final String UPDATE_EDGE = "update globaledgefrequencies set freqExtMethod = ?, freqOrigMethod = ? where edgename = ?";
	public static final String INSERT_EDGE = "insert into globaledgefrequencies values (?, ?, ?)";

	public static void mergeFrequenciesAcrossProjects(String projectName) {
		List<EdgeFrequency> allProjectEdges = getEdgeFrequencies(projectName);

		for (EdgeFrequency ecProj : allProjectEdges) {
			EdgeFrequency ecGlobal = getEdgeCount(ecProj.getEdgeName());

			if (ecGlobal != null) {
				System.err.println("Same edge found " + ecGlobal);
				sameEdgeCount++;

				Integer count = edgeFrequenciesAcross.get(ecProj.getEdgeName());
				if (count == null) {
					edgeFrequenciesAcross.put(ecProj.getEdgeName(), new Integer(1));
				} else {
					edgeFrequenciesAcross.put(ecProj.getEdgeName(), new Integer(count.intValue() + 1));
				}

				EdgeFrequency newEC = new EdgeFrequency(ecProj.getEdgeName(), null,
						ecGlobal.getFreqExtMethod() + ecProj.getFreqExtMethod(),
						ecGlobal.getFreqOrigMethod() + ecProj.getFreqOrigMethod());
				updateEdgeCount(newEC);
			} else {
				insertEdgeCount(ecProj);
			}
		}
	}

	public static EdgeFrequency getEdgeCount(String edgeName) {
		EdgeFrequency ec = null;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_EDGE);
			ps.setString(1, edgeName);

			ResultSet rs = ps.executeQuery();

			if (rs != null && rs.next()) {
				ec = new EdgeFrequency(edgeName, rs.getInt("freqExtMethod"), rs.getInt("freqOrigMethod"));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return ec;
	}

	public static void updateEdgeCount(EdgeFrequency ec) {
		try {
			PreparedStatement ps = conn.prepareStatement(UPDATE_EDGE);
			ps.setInt(1, ec.getFreqExtMethod());
			ps.setInt(2, ec.getFreqOrigMethod());
			ps.setString(3, ec.getEdgeName());

			ps.execute();

			if (ps.getUpdateCount() == 0) {
				System.err.println("Error updating data " + ec);
			} else {
				System.out.println("Updated edgecount " + ec);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public static void insertEdgeCount(EdgeFrequency ec) {
		try {
			PreparedStatement ps = conn.prepareStatement(INSERT_EDGE);
			ps.setString(1, ec.getEdgeName());
			ps.setInt(2, ec.getFreqExtMethod());
			ps.setInt(3, ec.getFreqOrigMethod());

			ps.execute();

			if (ps.getUpdateCount() == 0) {
				System.err.println("Error inserting data " + ec);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public static List<EdgeFrequency> getEdgeFrequencies(String projectName) {
		List<EdgeFrequency> ecList = new ArrayList<EdgeFrequency>();

		try {

			PreparedStatement ps = conn.prepareStatement(QUERY_EDGE_PROJECT);
			ps.setString(1, projectName);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ecList.add(new EdgeFrequency(rs.getString("edgename"), projectName, rs.getInt("freqExtMethod"),
						rs.getInt("freqOrigMethod")));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return ecList;
	}

	public static void prepareConnection() {
		if (conn == null) {
			try {
				conn = DriverManager.getConnection(JDBC_URL, "root", "Dhar0107");
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}

	public static void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}
}
