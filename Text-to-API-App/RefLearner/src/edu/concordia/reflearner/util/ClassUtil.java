package edu.concordia.reflearner.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A utility to identify and extract class, method details from textual
 * information.
 * 
 * @author Dharani Kumar Palani
 *
 */
public final class ClassUtil {
	public static String getNameOfTheClass(String fqcn) {
		StringTokenizer st = new StringTokenizer(fqcn, ".");
		String className = null;
		while (st.hasMoreTokens()) {
			className = st.nextToken();
		}
		return className;
	}

	public static String getSystemPathOfFile(String className, String projectClonedDir) {
		String pathOfFile = null;
		System.out.println("RefactoredClassesExtractor.getSystemPathOfFile() with className " + className);
		Path startingDir = Paths.get(projectClonedDir, new String[0]);

		StringTokenizer st = new StringTokenizer(className, ".");
		String cn = null;
		String prevCn = "";
		while (st.hasMoreElements()) {
			prevCn = cn;
			cn = st.nextToken();
		}
		try {
			FileFinder.Finder finder = new FileFinder.Finder(cn + ".java");
			Files.walkFileTree(startingDir, finder);
			finder.done();

			pathOfFile = finder.getFilePath();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (pathOfFile == null && prevCn != null) {
			pathOfFile = getSystemPathOfFile(prevCn, projectClonedDir);
		}

		return pathOfFile;
	}

	public static List<String> getParameters(String description) {
		List<String> params = new ArrayList<String>();

		int startIndex = description.indexOf('(');
		int endIndex = description.indexOf(')');

		String strParams = description.substring(startIndex + 1, endIndex);

		StringTokenizer st = new StringTokenizer(strParams, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken().trim();
			if (tok.contains("<") && !tok.contains(">") && st.hasMoreElements()) {
				tok = tok + "," + st.nextToken();
			}
			int firstSpace = tok.indexOf(' ');
			params.add(tok.substring(firstSpace + 1));

			// params.add(tok.split("\\s")[1]);
			params.add(tok);
		}
		return params;
	}

	public static String getExtractedMethodName(String description) {
		String methodName = null;
		StringTokenizer st = new StringTokenizer(description);
		st.nextToken();
		st.nextToken();
		st.nextToken();

		methodName = st.nextToken();
		int indexOfParent = methodName.indexOf('(');
		methodName = methodName.substring(0, indexOfParent);
		// System.out.println("methodName " + methodName);
		return methodName;
	}

	public static String getImpactedMethodName(String description) {
		String methodName = null;

		int index = description.indexOf("from");
		if (index != -1) {
			String subString = description.substring(index + 4, description.length());

			StringTokenizer st = new StringTokenizer(subString);
			st.nextToken();
			String token2 = st.nextToken();

			StringTokenizer st1 = new StringTokenizer(token2, "(");
			methodName = st1.nextToken();
		}
		return methodName;
	}

	public static String getClassName(String description) {
		StringTokenizer st = new StringTokenizer(description);
		String className = null;
		while (st.hasMoreTokens()) {
			className = st.nextToken();
		}
		return className;
	}

	public static String getPathOfFile(String className, String projectRoot) {
		StringBuilder sb = new StringBuilder();
		sb.append(projectRoot);
		className = className.replace('.', File.separatorChar);
		sb.append(File.separatorChar);
		sb.append(className);
		sb.append(".java");

		return sb.toString();
	}

	public static List<String> normalizeParametrizedTypes(List<String> params) {
		List<String> normList = new ArrayList<String>();
		for (String string : params) {
			normList.add(string.replaceAll("\\s+", ""));
		}
		return normList;
	}

	public static void main(String[] args) {
		List<String> params = getParameters(
				"Extract Method\tprivate createNode(attributeMap Map<String,String>, extendFromNode Node, parent Node, nodeRepository NodeRepository) : Node extracted from private parseNode(reader XMLStreamReader, parent Node, nodeRepository NodeRepository) : Node in class nodebox.node.NodeLibrary");
		System.out.println("params " + params);

		params = getParameters(
				"Extract Method\tprivate writeMenuItems(doc Document, parent Element, menuItems List<MenuItem>) : void extracted from private writeNode(doc Document, parent Element, node Node) : void in class nodebox.node.NDBXWriter");
		System.out.println("params " + params);

		params = getParameters(
				"Extract Method\tprivate createImage(objects Iterable<?>, visualizer Visualizer, bounds Rectangle2D, backgroundColor Color) : BufferedImage extracted from public createImage(objects Iterable<?>) : BufferedImage in class nodebox.client.ObjectsRenderer");
		System.out.println("params " + params);

		params = getParameters(
				"Extract Method	private copyOfInternal(graph Graph<N>, copyBuilder GraphBuilder<N>, nodePredicate Predicate<? super N>) : MutableGraph<N> extracted from public copyOf(graph Graph<N>) : MutableGraph<N> in class com.google.common.graph.Graphs");
		System.out.println("params " + params);

		params = normalizeParametrizedTypes(params);
		System.out.println("After normalization");
		System.out.println("params " + params);

		params = getParameters(
				"Extract Method	package createElementSetTestSuite(parentBuilder FeatureSpecificTestSuiteBuilder<?,? extends OneSizeTestContainerGenerator<Collection<E>,E>>) : TestSuite extracted from protected createDerivedSuites(parentBuilder FeatureSpecificTestSuiteBuilder<?,? extends OneSizeTestContainerGenerator<Collection<E>,E>>) : List<TestSuite> in class com.google.common.collect.testing.google.MultisetTestSuiteBuilder");
		System.out.println("params " + params);
		params = normalizeParametrizedTypes(params);
		System.out.println("After normalization");
		System.out.println("params " + params);

	}
}
