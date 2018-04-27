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
import java.util.List;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.ArrayUtil;

import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;

public class CommonUtils {

	/*
	 * public static void closeSession(Session session) { if(session == null ||
	 * !session.isOpen()) { return; } session.close();
	 * //System.out.println("Session closed!"); }
	 */

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
		if (tokens == null || tokens.length == 0) {
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
		if (StringUtils.equals(EdoConstants.RESPONSE_OK, status)) {
			okResponse(response);
		} else {
			response.setStatus(new EdoApiStatus());
			response.getStatus().setStatusCode(-111);
			response.getStatus().setResponseText(status);
		}
		return response;
	}

	public static boolean isAmountPresent(BigDecimal amount) {
		if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
			return true;
		}
		return false;
	}

	public static Date getFirstDate(Integer year, Integer month) {
		if (year == null || month == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1);
		return cal.getTime();
	}

	public static Date getLastDate(Integer year, Integer month) {
		if (year == null || month == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public static Integer getCalendarValue(Date date1, int value) {
		if (date1 == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		return cal.get(value);
	}

	public static String getStringValue(Integer value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	public static String getStringValue(BigDecimal value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	public static BigDecimal getPercent(Integer divident, Integer divider) {
		if (divident == null || divider == null) {
			return BigDecimal.ZERO;
		}
		if (divider <= 0) {
			return BigDecimal.ZERO;
		}
		MathContext mc = new MathContext(2, RoundingMode.HALF_UP);
		BigDecimal value = new BigDecimal(divident).divide(new BigDecimal(divider), mc);
		value = value.round(mc).multiply(new BigDecimal(100), mc);
		return value;
	}

	public static String extractPackages(EdoStudent student) {
		String packages = "";
		if (CollectionUtils.isNotEmpty(student.getPackages())) {
			for (EDOPackage pkg : student.getPackages()) {
				packages = packages + pkg.getName() + EdoConstants.COMMA_SEPARATOR;
			}
		}
		packages = StringUtils.removeEnd(packages, EdoConstants.COMMA_SEPARATOR);
		return packages;
	}

	public static String prepareStudentNotification(String result, EdoStudent student) {
		if (student != null) {
			result = StringUtils.replace(result, "{name}", CommonUtils.getStringValue(student.getName()));
			// result = StringUtils.replace(result, "{password}",
			// CommonUtils.getStringValue(user.getPassword()));
			result = StringUtils.replace(result, "{gender}", CommonUtils.getStringValue(student.getGender()));
			result = StringUtils.replace(result, "{phone}", CommonUtils.getStringValue(student.getPhone()));
			result = StringUtils.replace(result, "{examMode}", CommonUtils.getStringValue(student.getExamMode()));
			result = StringUtils.replace(result, "{packages}", CommonUtils.extractPackages(student));
			if (student.getPayment() != null) {
				if (student.getPayment().isOffline()) {
					result = StringUtils.replace(result, "{paymentMode}", "Offline");
				} else {
					result = StringUtils.replace(result, "{paymentMode}", "Online");
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
	
	public static String prepareTestNotification(String result, EdoTest test) {
		if (test != null) {
			result = StringUtils.replace(result, "{testName}", CommonUtils.getStringValue(test.getName()));
			result = StringUtils.replace(result, "{solved}", CommonUtils.getStringValue(test.getSolvedCount()));
			result = StringUtils.replace(result, "{correctCount}", CommonUtils.getStringValue(test.getCorrectCount()));
			result = StringUtils.replace(result, "{score}", CommonUtils.getStringValue(test.getScore()));
			result = StringUtils.replace(result, "{totalMarks}", CommonUtils.getStringValue(test.getTotalMarks()));
		}
		return result;
	}

	public static void calculateTestScore(EdoTest test, List<EdoQuestion> questions) {
		Integer solvedCount = 0;
		Integer correctCount = 0;
		Integer flaggedCount = 0;
		BigDecimal score = BigDecimal.ZERO;
		for (EdoQuestion answered : test.getTest()) {
			
			answered.setMarks(BigDecimal.ZERO);
			if (StringUtils.isNotBlank(answered.getAnswer())) {
				for (EdoQuestion question : questions) {
					if (question.getQn_id() != null &&  answered.getQn_id() != null && question.getQn_id().intValue() == answered.getQn_id().intValue()) {
						Integer questionScore = calculateAnswer(answered, question);
						if (questionScore != null) {
							if(questionScore > 0) {
								correctCount++;
							}
							BigDecimal marks = new BigDecimal(questionScore);
							answered.setMarks(marks);
							score = score.add(marks);
							LoggingUtil.logMessage(answered.getQuestionNumber() + "--" + answered.getQn_id() + " -- " + answered.getAnswer() + " -- " + answered.getWeightage() + " -- " + answered.getNegativeMarks() + " " + ":" + marks);
						} 
						break;
					}
				}
				solvedCount++;
				if (answered.getFlagged() != null && answered.getFlagged() == 1) {
					flaggedCount++;
				}
			}

		}
		test.setCorrectCount(correctCount);
		test.setFlaggedCount(flaggedCount);
		test.setSolvedCount(solvedCount);
		test.setScore(score);
		
		LoggingUtil.logMessage("Evaluated the test - " + test.getCorrectCount() + " .. " + test.getScore());
	}

	private static void setComplexAnswer(EdoQuestion answered) {
		if(answered != null && CollectionUtils.isNotEmpty(answered.getComplexOptions())) {
			StringBuilder answerBuilder = new StringBuilder();
			for(EdoComplexOption option: answered.getComplexOptions()) {
				if(CollectionUtils.isNotEmpty(option.getMatchOptions())) {
					for(EdoComplexOption match: option.getMatchOptions()) {
						if(match.isSelected()) {
							answerBuilder.append(option.getOptionName()).append("-").append(match.getOptionName()).append(",");
						}
					}
				}
			}
			answered.setAnswer(StringUtils.removeEnd(answerBuilder.toString(), ","));
		}
		
	}

	public static Integer calculateAnswer(EdoQuestion answered, EdoQuestion question) {
		if(question.getWeightage() == null || question.getNegativeMarks() == null) {
			return null;
		}
		if(StringUtils.equals(EdoConstants.QUESTION_TYPE_MULTIPLE, question.getType())) {
			String[] correctAnswers = StringUtils.split(question.getCorrectAnswer(), ",");
			String[] selectedAnswers = StringUtils.split(answered.getAnswer(), ",");
			if(compareResults(correctAnswers, selectedAnswers) && compareResults(selectedAnswers, correctAnswers)) {
				return question.getWeightage();
			} 
		} else if(StringUtils.equals(EdoConstants.QUESTION_TYPE_MATCH, question.getType())) {
			return calculateMatchScore(question, answered);
		} else if(StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(question.getCorrectAnswer()))){
			return question.getWeightage();
		}
		return -question.getNegativeMarks();
	}

	private static Integer calculateMatchScore(EdoQuestion question, EdoQuestion answered) {
		if(question.getCorrectAnswer() != null && answered != null && CollectionUtils.isNotEmpty(answered.getComplexOptions())) {
			Integer count = 0;
			for(EdoComplexOption option: answered.getComplexOptions()) {
				if(CollectionUtils.isNotEmpty(option.getMatchOptions())) {
					boolean correct = true;
					Integer matchedCount = 0;
					for(EdoComplexOption match: option.getMatchOptions()) {
						if(match.isSelected()) {
							if(!StringUtils.contains(question.getCorrectAnswer(), option.getOptionName() + "-" + match.getOptionName())) {
								correct = false;
								break;
							}
							matchedCount++;
						}
					}
					int actualCount = StringUtils.countMatches(question.getCorrectAnswer(), option.getOptionName());
					if(correct && actualCount == matchedCount) {
						count++;
					} else {
						count--;
					}
				}
			}
			setComplexAnswer(answered);
			return count;
		}
		
		return null;
	}

	private static boolean compareResults(String[] correctAnswers, String[] selectedAnswers) {
		if(ArrayUtils.isNotEmpty(correctAnswers) && ArrayUtils.isNotEmpty(selectedAnswers)) {
			for(String correctAnswer: correctAnswers) {
				boolean found = false;
				for(String selectedAnswer: selectedAnswers) {
					/*if(!StringUtils.contains(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(correctAnswer))) {
						return false;
					}*/
					if(StringUtils.isBlank(StringUtils.trimToEmpty(selectedAnswer))) {
						continue;
					}
					if(StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(selectedAnswer), StringUtils.trimToEmpty(correctAnswer))) {
						found = true;
						break;
					}
				}
				if(!found) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/*public static void main(String[] args) {
		EdoQuestion question = new EdoQuestion();
		//question.setType(EdoConstants.QUESTION_TYPE_MULTIPLE);
		question.setCorrectAnswer("asd");
		EdoQuestion answer = new EdoQuestion();
		answer.setAnswer("asd");
		answer.setMarks(new BigDecimal("1").negate());
		//System.out.println(checkAnswer(answer, question));
		//System.out.println(answer.getMarks());
		BigDecimal negative = new BigDecimal(-1);
		System.out.println(new BigDecimal(0).add(negative));
	}*/
	
}
