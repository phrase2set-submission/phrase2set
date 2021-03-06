package edu.concordia.reflearner.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileFinder {
	public static class Finder extends SimpleFileVisitor<Path> {
		private final PathMatcher matcher;
		private int numMatches = 0;
		private String filePath = null;

		public Finder(String pattern) {
			this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		public void find(Path file) {
			Path name = file.getFileName();
			if ((name != null) && (this.matcher.matches(name))) {
				this.numMatches += 1;
				this.filePath = file.toString();
				System.out.println(file);
			}
		}

		public void done() {
			System.out.println("Matched: " + this.numMatches);
		}

		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return FileVisitResult.CONTINUE;
		}

		public String getFilePath() {
			return this.filePath;
		}
	}

	static void usage() {
		System.err.println("java Find <path> -name \"<glob_pattern>\"");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		String eventBusCheckedOutDir = "/home/dharani/concordia/thesis/refminer/RefactoringMiner/tmp/EventBus";

		Path startingDir = Paths.get(eventBusCheckedOutDir, new String[0]);
		String pattern = "SubscriberMethodFinder.java";

		Finder finder = new Finder(pattern);
		Files.walkFileTree(startingDir, finder);
		finder.done();
	}
}
