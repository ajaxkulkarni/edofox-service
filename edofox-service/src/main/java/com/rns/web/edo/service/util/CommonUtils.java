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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.rns.web.edo.service.domain.EDOInstitute;
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

	public static String prepareTestNotification(String result, EdoTest test, EDOInstitute institute, String additionalMessage) {
		if (test != null) {
			result = StringUtils.replace(result, "{testName}", CommonUtils.getStringValue(test.getName()));
			result = StringUtils.replace(result, "{solved}", CommonUtils.getStringValue(test.getSolvedCount()));
			result = StringUtils.replace(result, "{correctCount}", CommonUtils.getStringValue(test.getCorrectCount()));
			result = StringUtils.replace(result, "{score}", CommonUtils.getStringValue(test.getScore()));
			result = StringUtils.replace(result, "{totalMarks}", CommonUtils.getStringValue(test.getTotalMarks()));
			if(institute != null) {
				result = StringUtils.replace(result, "{instituteName}", CommonUtils.getStringValue(institute.getName()));
			}
		}
		result = StringUtils.replace(result, "{additionalMessage}", additionalMessage);
		return result;
	}

	public static void calculateTestScore(EdoTest test, List<EdoQuestion> questions) {
		Integer solvedCount = 0;
		Integer correctCount = 0;
		Integer flaggedCount = 0;
		BigDecimal score = BigDecimal.ZERO;
		for (EdoQuestion answered : test.getTest()) {
			if (StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_MATCH, answered.getType())) {
				setComplexAnswer(answered);
			}
			answered.setMarks(BigDecimal.ZERO);
			if (StringUtils.isNotBlank(answered.getAnswer())) {
				for (EdoQuestion question : questions) {
					if (question.getQn_id() != null && answered.getQn_id() != null && question.getQn_id().intValue() == answered.getQn_id().intValue()) {
						Float questionScore = calculateAnswer(answered, question);
						if (questionScore != null) {
							if (questionScore > 0) {
								correctCount++;
							}
							BigDecimal marks = new BigDecimal(questionScore);
							answered.setMarks(marks);
							score = score.add(marks);
							LoggingUtil.logMessage(answered.getQuestionNumber() + "--" + answered.getQn_id() + " -- " + answered.getAnswer() + " -- "
									+ answered.getWeightage() + " -- " + answered.getNegativeMarks() + " " + ":" + answered.getMarks() + " -- " + score);
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
		if (answered != null && CollectionUtils.isNotEmpty(answered.getComplexOptions())) {
			StringBuilder answerBuilder = new StringBuilder();
			for (EdoComplexOption option : answered.getComplexOptions()) {
				if (CollectionUtils.isNotEmpty(option.getMatchOptions())) {
					for (EdoComplexOption match : option.getMatchOptions()) {
						if (match.isSelected()) {
							answerBuilder.append(option.getOptionName()).append("-").append(match.getOptionName()).append(",");
						}
					}
				}
			}
			answered.setAnswer(StringUtils.removeEnd(answerBuilder.toString(), ","));
		}

	}

	public static Float calculateAnswer(EdoQuestion answered, EdoQuestion question) {
		if (question.getWeightage() == null || StringUtils.isBlank(question.getCorrectAnswer())) {
			return null;
		}
		if (StringUtils.equalsIgnoreCase("cancel", question.getCorrectAnswer()) || StringUtils.equalsIgnoreCase("bonus", question.getCorrectAnswer())) {
			return 0f;
		}
		if (StringUtils.contains(question.getType(), EdoConstants.QUESTION_TYPE_MULTIPLE)) {
			String[] correctAnswers = StringUtils.split(question.getCorrectAnswer(), ",");
			String[] selectedAnswers = StringUtils.split(answered.getAnswer(), ",");
			Float compareResult = calculateMultipleTypeScore(correctAnswers, selectedAnswers, question);
			Float alternateResult = null;
			if (StringUtils.isNotBlank(question.getAlternateAnswer())) {
				String[] alternateAnswers = StringUtils.split(question.getAlternateAnswer(), ","); 
				alternateResult = calculateMultipleTypeScore(alternateAnswers, selectedAnswers, question);
			}
			if(alternateResult != null && alternateResult > compareResult) {
				return alternateResult;
			} else {
				return compareResult;
			}
			
		} else if (StringUtils.equals(EdoConstants.QUESTION_TYPE_MATCH, question.getType())) {
			Float matchScore = calculateMatchScore(question.getCorrectAnswer(), answered);
			Float altScore = null;
			if (StringUtils.isNotBlank(question.getAlternateAnswer())) {
				altScore = calculateMatchScore(question.getAlternateAnswer(), answered);
			}
			if (altScore != null && altScore > matchScore) {
				return altScore;
			}
			return matchScore;
		} else if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(question.getCorrectAnswer()))) {
			return question.getWeightage();
		} else if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(question.getAlternateAnswer()))) {
			return question.getWeightage();
		}
		return getWrongScore(question);
	}

	private static Float calculateMatchScore(String correctAnswer, EdoQuestion answered) {

		if (correctAnswer != null && answered != null && StringUtils.isNotBlank(answered.getAnswer())) {

			String[] selectedPairs = StringUtils.split(answered.getAnswer(), ",");
			Float count = 0f;
			if (ArrayUtils.isNotEmpty(selectedPairs)) {
				Map<String, Integer> pairCount = new HashMap<String, Integer>();
				for (String selectedPair : selectedPairs) {
					if (!StringUtils.contains(correctAnswer, selectedPair)) {
						count--;
						continue;
					}
					String[] pair = StringUtils.split(selectedPair, "-");
					if (ArrayUtils.isNotEmpty(pair)) {
						Integer existingCount = pairCount.get(pair[0]);
						if (existingCount == null) {
							existingCount = 0;
						}
						pairCount.put(pair[0], existingCount + 1);
					}
				}
				if (CollectionUtils.isNotEmpty(pairCount.keySet())) {
					for (Entry<String, Integer> pair : pairCount.entrySet()) {
						int actualCount = StringUtils.countMatches(correctAnswer, pair.getKey());
						if (pair.getValue() != null) {
							if (actualCount == pair.getValue().intValue()) {
								count++;
							} else {
								count--;
							}
						}
					}
				}
				return count;
			}
		}
		return null;
	}

	private static Float calculateMultipleTypeScore(String[] correctAnswers, String[] selectedAnswers, EdoQuestion question) {
		if (ArrayUtils.isEmpty(correctAnswers)) {
			return null;
		}
		if (ArrayUtils.isEmpty(selectedAnswers)) {
			return null;
		}
		
		Integer foundCount = 0;
		Integer answerCount = 0;
		Integer solvedCount = 0;
		for (String correctAnswer : correctAnswers) {
			if (StringUtils.isBlank(StringUtils.trimToEmpty(correctAnswer))) {
				continue;
			}
			boolean found = false;
			answerCount++;
			solvedCount = 0;
			for (String selectedAnswer : selectedAnswers) {
				
				if (StringUtils.isBlank(StringUtils.trimToEmpty(selectedAnswer))) {
					continue;
				}
				solvedCount++;
				if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(selectedAnswer), StringUtils.trimToEmpty(correctAnswer))) {
					found = true;
					foundCount++;
				}
			}
			/*if (!found) {
				LoggingUtil.logMessage("Not found for .." + correctAnswer);
				// return false;
			}*/
		}
		if (foundCount == 0) {
			return getWrongScore(question);
		}
		if(answerCount == solvedCount && foundCount == solvedCount) {
			return question.getWeightage();
		}
		if(StringUtils.equalsIgnoreCase("Y", question.getPartialCorrection())) {
			return new Float(1);
		}
		return getWrongScore(question);
	}

	private static Float getWrongScore(EdoQuestion question) {
		if(question.getNegativeMarks() == null) {
			return new Float(0);
		}
		return -question.getNegativeMarks();
	}

	public static void main(String[] args) {
		EdoQuestion question = new EdoQuestion();
		// question.setType(EdoConstants.QUESTION_TYPE_MULTIPLE);
		question.setCorrectAnswer("option2,option3");
		EdoQuestion answer = new EdoQuestion();
		answer.setAnswer("option4");
		question.setPartialCorrection("Y");
		question.setType(EdoConstants.QUESTION_TYPE_MULTIPLE);
		question.setWeightage(3f);
		question.setNegativeMarks(1f);
		System.out.println(calculateAnswer(answer, question));
		// answer.setMarks(new BigDecimal("1").negate());
		// System.out.println(calculateMatchScore(question, answer));
	}

}
