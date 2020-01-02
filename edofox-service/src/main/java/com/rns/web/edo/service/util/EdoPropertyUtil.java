package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class EdoPropertyUtil {
	
	private static final String PROPERTIES_PATH = "/home/service/properties/edofox.properties";
	public static final String FIREBASE_PROJECT = "firebase.project";
	public static final String FIREBASE_CREDENTIALS = "firebase.credentials.path";
	public static final String HOST_URL = "host.url";
	public static final String HOST_NAME = "host.name";
	public static final String HOST_NAME_RELIANCE = "host.name.reliance";
	public static final String CLIENT_ID = "insta.client.id";
	public static final String CLIENT_SECRET = "insta.client.secret";
	public static final String AUTH_ENDPOINT = "insta.auth.endpoint";//"https://www.instamojo.com/oauth2/token/";
	public static final String API_ENDPOINT = "insta.api.endpoint";//"https://api.instamojo.com/v2/";
	public static final String MAIL_HOST = "mail.server";
	public static final String MAIL_ID = "mail.username";
	public static final String MAIL_PASSWORD = "mail.password";
	public static final String MAIL_PORT = "mail.port";
	public static final String SETTLEMENT_URL = "settlement.url";
	public static final String SETTLEMENT_TOKEN = "settlement.token";

	public static String getProperty(String name) {
		try {
			File file = new File(PROPERTIES_PATH);
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			return StringUtils.trimToEmpty(properties.getProperty(name));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
