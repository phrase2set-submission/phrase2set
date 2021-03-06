package edu.concordia.reflearner.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RevisionGit;

public class DBUtil {
	public static final String JDBC_URL = "jdbc:mysql://localhost/danilofs-refactoring";
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String QUERY_REVISION = "select * from revisiongit where id = ?";

	public static final String QUERY_EXTRACT_METHOD_REF = "SELECT * FROM refactoringgit where description like "
			+ "'Extract Method%' and revision in (SELECT id FROM revisiongit where project = (select id from projectgit where name = ?))";

	public static final String INSERT_UNDETECTABLE = "insert into undetectable values(?, ?, ?, ?, ?, ?)";

	public static final String QUERY_DEFAULT_BRANCH_OF_PROJECT = "select default_branch from projectgit where name = ?";

	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/danilofs-refactoring", "root", "Dhar0107");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (IllegalAccessException ile) {
			ile.printStackTrace();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		}
		return conn;
	}

	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			} finally {
				// conn = null;
			}
		}
	}

	public static Map<Long, RefactoringGit> getAllExtractMethodRefactorings(String projectName) {
		Map<Long, RefactoringGit> refactoringGitList = null;
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_EXTRACT_METHOD_REF);

			ps.setString(1, projectName);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				refactoringGitList = new LinkedHashMap<Long, RefactoringGit>();
				while (rs.next()) {
					RefactoringGit refG = new RefactoringGit();
					refG.setId(Long.valueOf(rs.getLong("id")));
					refG.setDescription(rs.getString("description"));
					RevisionGit revG = getRevision(conn, rs.getLong("revision"));
					refG.setRevision(revG);

					refactoringGitList.put(refG.getId(), refG);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection(conn);
		return refactoringGitList;
	}

	public static RevisionGit getRevision(final Connection conn, final long revisionId) {
		RevisionGit revG = null;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_REVISION);
			ps.setLong(1, revisionId);

			ResultSet rs = ps.executeQuery();
			if ((rs != null) && (rs.next())) {
				revG = new RevisionGit();
				revG.setId(revisionId);
				revG.setIdCommit(rs.getString("commitId"));
				revG.setIdCommitParent(rs.getString("commitIdParent"));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return revG;
	}

	public static void insertIntoUndetectableTable(final Connection conn, final String projectName,
			final RefactoringGit refG, final String reason) {
		try {
			PreparedStatement ps = conn.prepareStatement(INSERT_UNDETECTABLE);
			ps.setString(1, projectName);
			ps.setString(2, refG.getRevision().getIdCommit());
			ps.setString(3, refG.getRevision().getIdCommitParent());
			ps.setString(4, refG.getDescription());
			ps.setString(5, reason);
			ps.setLong(6, refG.getId());

			System.out.println("Insertion status into undetectable " + ps.execute());

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public static String getProjectDefaultBranch(final String projectName) {
		String defaultBranch = null;
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_DEFAULT_BRANCH_OF_PROJECT);
			ps.setString(1, projectName);

			ResultSet rs = ps.executeQuery();
			if (rs != null && rs.next()) {
				defaultBranch = rs.getString("default_branch");
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		closeConnection(conn);
		return defaultBranch;
	}

	public static Set<String> getAllRefactoredClasses(final String projectName) {

		Set<String> allRefactoredClassNames = new HashSet<String>();

		Map<Long, RefactoringGit> allRefactorings = DBUtil.getAllExtractMethodRefactorings(projectName);

		for (Long id : allRefactorings.keySet()) {
			RefactoringGit refG = allRefactorings.get(id);
			String description = refG.getDescription();

			String splits[] = description.split("\\s+");
			String className = splits[splits.length - 1];

			allRefactoredClassNames.add(className);
		}

		return allRefactoredClassNames;
	}
}
