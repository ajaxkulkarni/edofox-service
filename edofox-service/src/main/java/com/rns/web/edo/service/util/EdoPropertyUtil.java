package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class EdoPropertyUtil {
	
	private static final String PROPERTIES_PATH = "/home/service/properties/edofox.properties";
	public static final String FIREBASE_PROJECT = "firebase.project";
	public static final String FIREBASE_CREDENTIALS = "firebase.credentials.path";

	public static String getProperty(String name) {
		try {
			File file = new File(PROPERTIES_PATH);
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			return properties.getProperty(name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
