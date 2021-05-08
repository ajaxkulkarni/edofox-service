package com.rns.web.edo.service.util;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;

import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.ext.EdoImpartusResponse;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;

public class EdoLiveUtil {
	
	public static String BASE_URL = "https://a.impartus.com/api/lti/";
	
	public static EdoImpartusResponse adminLogin() {
		String url = BASE_URL + "adminLogin";
		JSONObject request = new JSONObject();
		try {
			request.put("secretKey", EdoPropertyUtil.getProperty(EdoPropertyUtil.IMPARTUS_KEY));
			
			String response = CommonUtils.callExternalApi(url, request, null, null);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
				
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
		
	}
	
	public static EdoImpartusResponse createCourse(String token, EDOPackage pkg) {
		String url = BASE_URL + "courses";
		JSONObject request = new JSONObject();
		try {

			request.put("name", pkg.getName());
			//request.put("code", StringUtils.)
			request.put("externalCourseId", pkg.getId());
			//request.put
			String response = CommonUtils.callExternalApi(url, request, null, "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
		
	}
	
	//2 for host
	//6 for client
	public static EdoImpartusResponse createUser(String token, EdoStudent host, Integer userType) {
		String url = BASE_URL + "users";
		JSONObject request = new JSONObject();
		try {

			request.put("firstName", host.getName());
			if(StringUtils.isBlank(host.getEmail())) {
				host.setEmail(host.getId() + "@email.com");
			}
			request.put("email", host.getEmail());
			request.put("userType", userType);
			request.put("externalUserId", host.getId());
			//request.put
			String response = CommonUtils.callExternalApi(url, request, null, "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
		
	}
	
	public static EdoImpartusResponse createLiveSession(String token, EdoLiveSession live, EdoStudent host) {
		String url = BASE_URL + "schedules";
		JSONObject request = new JSONObject();
		try {

			request.put("date", CommonUtils.convertDate(live.getStartDate()));
			request.put("startTime", CommonUtils.convertDate(live.getStartDate(), "HH:mm"));
			request.put("endTime", CommonUtils.convertDate(live.getEndDate(), "HH:mm"));
			if(live.getCreatedBy() != null) {
				request.put("externalUserId", live.getCreatedBy());
			} else {
				request.put("externalUserId", host.getId());
			}
			request.put("externalCourseId", live.getClassroomId());
			request.put("classroom", "VIRTUAL_CLASSROOM");
			request.put("scheduleType", 3);
			request.put("topic", live.getSessionName());
			request.put("returnURL", EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME) + "live_sessions.php?channelId=" + live.getClassroomId());
			//request.put
			String response = CommonUtils.callExternalApi(url, request, null, "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
	}
	
	public static EdoImpartusResponse scheduleSettings(String token, String scheduleId) {
		String url = BASE_URL + "schedules/settings/" + scheduleId;
		JSONObject request = new JSONObject();
		try {
			request.put("proctoring", 1);
			String response = CommonUtils.callExternalApi(url, request, "PUTACTUAL", "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
	}
	
	public static EdoImpartusResponse join(String token, Integer pkg, Integer user) {
		String url = BASE_URL + "courses/" + pkg + "/users" ;
		JSONObject request = new JSONObject();
		try {

			request.put("externalUserId", user);
			//request.put
			String response = CommonUtils.callExternalApi(url, request, null, "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		 return null;
		
	}
	
	public static EdoImpartusResponse ssoToken(Integer userId) {
		String url = BASE_URL + "login";
		JSONObject request = new JSONObject();
		try {
			request.put("secretKey", EdoPropertyUtil.getProperty(EdoPropertyUtil.IMPARTUS_KEY));
			request.put("externalUserId", userId);
			
			String response = CommonUtils.callExternalApi(url, request, null, null);
			if(response != null) {
				return new ObjectMapper().readValue(response, EdoImpartusResponse.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	public static List<LinkedHashMap<String, Object>> getUsage(Integer scheduledId, String token) {
		String url = BASE_URL + "usage-data/" + scheduledId;
		JSONObject request = new JSONObject();
		try {
			//request.put("secretKey", EdoPropertyUtil.getProperty(EdoPropertyUtil.IMPARTUS_KEY));
			//request.put("externalUserId", userId);
			
			String response = CommonUtils.callExternalApi(url, request, "GET", "adminBearer " + token);
			if(response != null) {
				return new ObjectMapper().readValue(response, List.class);
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

}
