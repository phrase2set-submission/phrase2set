package edu.concordia.t2api.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dharani
 *
 */
@SuppressWarnings("unchecked")
public final class AndroidPackageNames {

	public static final String PACKAGE_FILE_CSV = "AllAndroidClasses.csv";
	public static final String SERIALIZED_FILE = "AllAndroidClasses.ser";

	public static final String CONTROL_NODE = "ControlInfo";

	public static Set<String> androidPackageNames = new HashSet<String>();

	static {

		File csvFile = new File(AndroidPackageNames.class.getClassLoader().getResource(PACKAGE_FILE_CSV).getPath());
		File serFile = new File(SERIALIZED_FILE);

		if (!serFile.exists() && !csvFile.exists()) {
			System.err.println(
					"Place Android package names in csv file or the serialized file containing the Set<String> of package names.");
			System.exit(-1);
		}

		if (!serFile.exists()) {

			try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
				String line = null;

				while ((line = br.readLine()) != null) {
					String packageName = line.split(",")[0];
					androidPackageNames.add(packageName);
				}

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIALIZED_FILE))) {
				oos.writeObject(androidPackageNames);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		if (serFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serFile))) {
				androidPackageNames = (Set<String>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		androidPackageNames.add(CONTROL_NODE);
		System.out.println("androidPackageNames " + androidPackageNames);
	}
	
	public static void main(String[] args) {
		new AndroidPackageNames();
	}
}
