package edu.concordia.reflearner.stats;

import static edu.concordia.reflearner.util.DBUtil.DRIVER_CLASS;
import static edu.concordia.reflearner.util.DBUtil.JDBC_URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatisticsWriter {

	public static void writeEdgeStatistics(EdgeStatistics es, String projectName) {
		Connection conn = null;
		try {
			Class.forName(DRIVER_CLASS).newInstance();
			conn = DriverManager.getConnection(JDBC_URL, "root", "Dhar0107");

			PreparedStatement ps = conn.prepareStatement("insert into edgefrequencies values(?, ?, ?, ?);");

			Map<String, int[]> mergedData = mergeFrequencies(es);

			Set<String> edges = mergedData.keySet();

			for (String string : edges) {
				ps.setString(1, string);
				ps.setString(2, projectName);

				int cp[] = mergedData.get(string);

				if (cp != null) {

					ps.setInt(3, cp[0]);
					ps.setInt(4, cp[1]);
				} else {
					ps.setInt(3, 0);
					ps.setInt(4, 0);
				}

				ps.execute();
			}

			ps.close();
			// first insert the extractedMethod frequencies

		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (IllegalAccessException ile) {
			ile.printStackTrace();
		} catch (SQLException sqle) {

			sqle.printStackTrace();
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static Map<String, int[]> mergeFrequencies(EdgeStatistics es) {
		Map<String, int[]> ret = new HashMap<String, int[]>();

		Map<String, Integer> extractedMC = es.getExtractedMethodCount();
		Map<String, Integer> origMC = es.getOrigMethodCount();

		Set<String> edges = extractedMC.keySet();

		for (String string : edges) {
			int[] cp = ret.get(string);
			if (cp != null) {
				cp[0] = extractedMC.get(string).intValue();
			} else {
				cp = new int[2];
				cp[0] = extractedMC.get(string).intValue();
				ret.put(string, cp);
			}
		}

		edges = origMC.keySet();
		for (String string : edges) {
			int[] cp = ret.get(string);
			if (cp != null) {
				cp[1] = origMC.get(string).intValue();
			} else {
				cp = new int[2];
				cp[1] = origMC.get(string).intValue();
				ret.put(string, cp);
			}
		}
		return ret;
	}
}
