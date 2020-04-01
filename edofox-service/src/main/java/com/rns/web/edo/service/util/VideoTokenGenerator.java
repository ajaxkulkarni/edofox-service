package com.rns.web.edo.service.util;

import com.rns.web.edo.service.util.RtcTokenBuilder.Role;

public class VideoTokenGenerator {
	//static String appId = "3c8d25ed1fba4ac882d130e68e88cff6";
	//static String appCertificate = "5492d93cc91d4b93ba07d91d56b98e8e";
	// static String channelName = "TestChannel3";
	// static String userAccount = "2082341273";
	// static int uid = 2082341273;
	static int expirationTimeInSeconds = 3600 * 24;
	//b97d903dd31643229a4fd227fd072529
	//5abfc47d254e4b029f429cdef8fc1832
	
	//Etkl6g-zSB7EpP-Da1zN6yP2t9ao6ed9jArTa0fJfFRRZlT-qq5lIBQbVo0UcB1KUQkJJu_u0oIRMLRCPHynEhW3MwiRtLNTM9yHwwK9H4LXTrX764Ls1ZsCOjDMeufpwVcYgu1A8ku42Zo3UT7PO5TuSi6G_0EdeaqiIOum6T4fgzyHi6ezeRaTvmJYOBUHaU0vekTCVOvC96kUlfuzzuujT3TB-zzBEUaAjpm3ZSmdipCRegdJKCdIkdiRY1KQFKlrJHaZH3Eoi-1-nb0hrA
	//1a5173ac5a47d0ad65e954a76950f887
	
	public static String generateToken(String channelName, int uid, Role selectedRole) {
		RtcTokenBuilder token = new RtcTokenBuilder();
		int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
		String result = token.buildTokenWithUid(EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_APP_ID), EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_APP_SECRET), channelName, uid, selectedRole, timestamp);
		return result;
	}

	public static String generateToken(String channelName, String userAccount, Role selectedRole) {
		RtcTokenBuilder token = new RtcTokenBuilder();
		int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
		String result = token.buildTokenWithUserAccount(EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_APP_ID), EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_APP_SECRET), channelName, userAccount, selectedRole, timestamp);
		return result;
	}

}