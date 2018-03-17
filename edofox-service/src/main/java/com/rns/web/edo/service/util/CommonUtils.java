package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;

public class CommonUtils {
	
	/*public static void closeSession(Session session) {
		if(session == null || !session.isOpen())  {
			return;
		}
		session.close();
		//System.out.println("Session closed!");
	}*/

	public static String convertDate(Date date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String readFile(String contentPath) throws FileNotFoundException {
		File file = getFile(contentPath);
		Scanner scanner = new Scanner(file);
		StringBuilder result = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			result.append(line).append("\n");
		}

		scanner.close();
		return result.toString();
	}

	public static File getFile(String contentPath) {
		ClassLoader classLoader = new CommonUtils().getClass().getClassLoader();
		URL resource = classLoader.getResource(contentPath);
		File file = new File(resource.getFile());
		return file;
	}
	
	
	public static String getStringValue(String value) {
		return StringUtils.isNotEmpty(value) ? value : "";
	}


	public static String getFileExtension(String filePath) {
		String[] tokens = StringUtils.split(filePath, ".");
		if(tokens == null || tokens.length == 0) {
			return null;
		}
		return tokens[tokens.length - 1];
	}

	public static EdoServiceResponse initResponse() {
		EdoServiceResponse response = new EdoServiceResponse();
		okResponse(response);
		return response;
	}

	private static void okResponse(EdoServiceResponse response) {
		response.setStatus(new EdoApiStatus());
	}
	
	public static EdoServiceResponse setResponse(EdoServiceResponse response, String status) {
		if(StringUtils.equals(EdoConstants.RESPONSE_OK, status)) {
			okResponse(response);
		} else {
			response.setStatus(new EdoApiStatus());
			response.getStatus().setStatusCode(-111);
			response.getStatus().setResponseText(status);
		}
		return response;
	}

	
	public static boolean isAmountPresent(BigDecimal amount) {
		if(amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
			return true;
		}
		return false;
	}

	public static Date getFirstDate(Integer year, Integer month) {
		if(year == null || month == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);
		return cal.getTime();
	}
	
	public static Date getLastDate(Integer year, Integer month) {
		if(year == null || month == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public static Integer getCalendarValue(Date date1, int value) {
		if(date1 == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		return cal.get(value);
	}

	public static String getStringValue(Integer value) {
		if(value == null) {
			return "";
		}
		return value.toString();
	}

	public static String getStringValue(BigDecimal value) {
		if(value == null) {
			return "";
		}
		return value.toString();
	}
	
	public static BigDecimal getPercent(Integer divident, Integer divider) {
		if(divident == null || divider == null) {
			return BigDecimal.ZERO;
		}
		if(divider <= 0) {
			return BigDecimal.ZERO;
		}
		BigDecimal value = new BigDecimal(divident).divide(new BigDecimal(divider));
		value = value.round(new MathContext(2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
		return value;
	}
	
	public static String extractPackages(EdoStudent student) {
		String packages = "";
		if(CollectionUtils.isNotEmpty(student.getPackages())) {
			for(EDOPackage pkg: student.getPackages()) {
				packages = packages + pkg.getName() + EdoConstants.COMMA_SEPARATOR;
			}
		}
		packages = StringUtils.removeEnd(packages, EdoConstants.COMMA_SEPARATOR);
		return packages;
	}
	
	public static String prepareStudentNotification(String result, EdoStudent student) {
		if (student != null) {
			result = StringUtils.replace(result, "{name}", CommonUtils.getStringValue(student.getName()));
			//result = StringUtils.replace(result, "{password}", CommonUtils.getStringValue(user.getPassword()));
			result = StringUtils.replace(result, "{gender}", CommonUtils.getStringValue(student.getGender()));
			result = StringUtils.replace(result, "{phone}", CommonUtils.getStringValue(student.getPhone()));
			result = StringUtils.replace(result, "{examMode}", CommonUtils.getStringValue(student.getExamMode()));
			result = StringUtils.replace(result, "{packages}", CommonUtils.extractPackages(student));
			if(student.getPayment() != null) {
				if(student.getPayment().isOffline()) {
					result = StringUtils.replace(result, "{paymentMode}", "Offline");
				} else {
					result = StringUtils.replace(result, "{paymentMode}",  "Online");
				}
				result = StringUtils.replace(result, "{paymentId}", CommonUtils.getStringValue(student.getPayment().getPaymentId()));
				result = StringUtils.replace(result, "{transactionId}", CommonUtils.getStringValue(student.getTransactionId()));
			} else {
				result = StringUtils.replace(result, "{paymentMode}", "");
				result = StringUtils.replace(result, "{paymentId}", "");
				result = StringUtils.replace(result, "{transactionId}", "");
			}
			
		}
		return result;
	}

	
}
