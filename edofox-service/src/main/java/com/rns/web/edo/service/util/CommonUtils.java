package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.hibernate.Session;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestStudentMap;

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
			if(student.getAnalysis() != null) {
				result = StringUtils.replace(result, "{rank}", CommonUtils.getStringValue(student.getAnalysis().getRank()));
				result = StringUtils.replace(result, "{totalStudents}", CommonUtils.getStringValue(student.getAnalysis().getTotalStudents()));
				result = StringUtils.replace(result, "{score}", CommonUtils.getStringValue(student.getAnalysis().getScore()));
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
		}
		result = prepareInstituteNotification(result, institute);
		result = StringUtils.replace(result, "{additionalMessage}", CommonUtils.getStringValue(additionalMessage));
		return result;
	}

	public static String prepareInstituteNotification(String result, EDOInstitute institute) {
		if(institute != null) {
			result = StringUtils.replace(result, "{instituteName}", CommonUtils.getStringValue(institute.getName()));
		}
		return result;
	}

	public static void calculateTestScore(EdoTest test, List<EdoQuestion> questions) {
		if(test == null || CollectionUtils.isEmpty(test.getTest())) {
			return;
		}
		//Add bonus questions if not solved
		for(EdoQuestion question : questions) {
			if(isBonus(question)) {
				//Find if this is solved
				boolean found = false;
				for (EdoQuestion answered : test.getTest()) {
					if (question.getQn_id() != null && answered.getQn_id() != null && question.getQn_id().intValue() == answered.getQn_id().intValue()) {
						found = true;
						if(answered.getMapId() == null) {
							//Not already added
							answered.setResponse("bonus");
							answered.setAnswer("bonus");
						} else if (StringUtils.isBlank(answered.getAnswer())) {
							answered.setAnswer("bonus");
						}
						break;
					}
				}
				if(!found) {
					EdoQuestion bonus = new EdoQuestion();
					bonus.setResponse("bonus");
					bonus.setQn_id(question.getQn_id());
					bonus.setAnswer("bonus");
					test.getTest().add(bonus);
					LoggingUtil.logMessage("Found unsolved bonus question => " + question.getQn_id());
				}
			}
		}
		
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

	public static void setComplexAnswer(EdoQuestion answered) {
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
	
	public static EdoQuestion getComplexAnswer(EdoQuestion answered) {
		if (answered != null && StringUtils.isNotBlank(answered.getAnswer())) {
			String[] answers = StringUtils.split(answered.getAnswer(), ",");
			if(ArrayUtils.isNotEmpty(answers)) {
				List<EdoComplexOption> options = new ArrayList<EdoComplexOption>();
				for (String answer: answers) {
					String[] keys = StringUtils.split(answer, "-");
					if(ArrayUtils.isNotEmpty(keys)) {
						EdoComplexOption option = new EdoComplexOption();
						option.setOptionName(keys[0]);
						List<EdoComplexOption> selected = new ArrayList<EdoComplexOption>();
						EdoComplexOption selectedOp = new EdoComplexOption();
						selectedOp.setOptionName(keys[1]);
						selected.add(selectedOp);
						option.setMatchOptions(selected);
					}
				}
				answered.setComplexOptions(options);
			}
		}
		return answered;
	}

	public static Float calculateAnswer(EdoQuestion answered, EdoQuestion question) {
		if (question.getWeightage() == null || StringUtils.isBlank(question.getCorrectAnswer())) {
			return null;
		}
		if (StringUtils.equalsIgnoreCase("cancel", question.getCorrectAnswer())) {
			return 0f;
		}
		if(isBonus(question)) {
			LoggingUtil.logMessage("Found a bonus question =>" + answered.getQn_id());
			return question.getWeightage();
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

	public static boolean isBonus(EdoQuestion question) {
		return StringUtils.equalsIgnoreCase("bonus", question.getCorrectAnswer());
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

	public static Float calculateMultipleTypeScore(String[] correctAnswers, String[] selectedAnswers, EdoQuestion question) {
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
			LoggingUtil.logMessage("Not found any correct answered .. " + question.getId() + " : " + question.getQn_id() + " count " + foundCount);
			return getWrongScore(question);
		}
		//answerCount - Total answers
		//solvedCount - Total solved
		//foundCount - Correct and solved
		if(answerCount == solvedCount && foundCount == solvedCount) {
			//all solved and all correct
			LoggingUtil.logMessage("All solved and all are correct .. " + question.getId() + " : " + question.getQn_id() + " counts => " + foundCount + ":" + solvedCount + ":" + answerCount);
			return question.getWeightage();
		}
		
		if(StringUtils.equalsIgnoreCase("Y", question.getPartialCorrection())) {
			//return new Float(1);
			if(solvedCount == foundCount) {
				//Whatever is solved is correct. i.e. 3 options chosen and 3 are correct => score will be 3
				LoggingUtil.logMessage("All solved are correct .. " + question.getId() + " : " + question.getQn_id() + " counts " + solvedCount + ":" + foundCount);
				return new Float(solvedCount);
			}
		}
		return getWrongScore(question);
	}

	private static Float getWrongScore(EdoQuestion question) {
		if(question.getNegativeMarks() == null) {
			return new Float(0);
		}
		return -question.getNegativeMarks();
	}
	
	public static List<EdoStudentSubjectAnalysis> getSubjectAnalysis(EdoTest existing, List<EdoTestStudentMap> subjectScores, EdoStudent student) {
		List<EdoStudentSubjectAnalysis> subjectAnalysis = new ArrayList<EdoStudentSubjectAnalysis>();
		for(EdoTestStudentMap map: subjectScores) {
			if(map.getStudent() != null && map.getStudent().getId().intValue() == student.getId().intValue() && map.getSubjectScore() != null ) {
				subjectAnalysis.add(map.getSubjectScore());
				if( !existing.getSubjects().contains(map.getSubjectScore().getSubject())) {
					existing.getSubjects().add(map.getSubjectScore().getSubject());
				}
			}
		}
		return subjectAnalysis;
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
	
	public static void setQuestionURLs(EdoQuestion question) {
		
		String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL);
		
		String absoluteUrls = EdoPropertyUtil.getProperty(EdoPropertyUtil.ABSOLUTE_IMAGE_URLS);
		if(StringUtils.equals(absoluteUrls, "true")) {
			if(!StringUtils.contains(question.getQuestionImageUrl(), "http") && !StringUtils.contains(question.getQuestionImageUrl(), "https")) {
				question.setQuestionImageUrl("http://" + question.getQuestionImageUrl());
			}
			return;
		}
		
		if(/*EdoConstants.ABSOLUTE_IMAGE_URLS || */StringUtils.contains(question.getQuestionImageUrl(), "public_html")) {
			if(StringUtils.isNotBlank(question.getQuestionImageUrl())) {
				question.setQuestionImageUrl(prepareUrl(question.getQuestionImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getOption1ImageUrl())) {
				question.setOption1ImageUrl(prepareUrl(question.getOption1ImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getOption2ImageUrl())) {
				question.setOption2ImageUrl(prepareUrl(question.getOption2ImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getOption3ImageUrl())) {
				question.setOption3ImageUrl(prepareUrl(question.getOption3ImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getOption4ImageUrl())) {
				question.setOption4ImageUrl(prepareUrl(question.getOption4ImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getMetaDataImageUrl())) {
				question.setMetaDataImageUrl(prepareUrl(question.getMetaDataImageUrl()));
			}
			if(StringUtils.isNotBlank(question.getSolutionImageUrl())) {
				question.setSolutionImageUrl(prepareUrl(question.getSolutionImageUrl()));
			}
		} else {
			Integer qn_id = question.getQn_id() != null ? question.getQn_id() : question.getId();
			if(StringUtils.isNotBlank(question.getQuestionImageUrl()) && !StringUtils.contains(question.getQuestionImageUrl(), "http")) {
				question.setQuestionImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_QUESTION);
			}
			if(StringUtils.isNotBlank(question.getOption1ImageUrl()) && !StringUtils.contains(question.getOption1ImageUrl(), "http")) {
				question.setOption1ImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_OPTION1);
			}
			if(StringUtils.isNotBlank(question.getOption2ImageUrl()) && !StringUtils.contains(question.getOption2ImageUrl(), "http")) {
				question.setOption2ImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_OPTION2);
			}
			if(StringUtils.isNotBlank(question.getOption3ImageUrl()) && !StringUtils.contains(question.getOption3ImageUrl(), "http")) {
				question.setOption3ImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_OPTION3);
			}
			if(StringUtils.isNotBlank(question.getOption3ImageUrl()) && !StringUtils.contains(question.getOption3ImageUrl(), "http")) {
				question.setOption4ImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_OPTION4);
			}
			if(StringUtils.isNotBlank(question.getMetaDataImageUrl())) {
				question.setMetaDataImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_META_DATA);
			}
			if(StringUtils.isNotBlank(question.getSolutionImageUrl()) && !StringUtils.contains(question.getSolutionImageUrl(), "http")) {
				question.setSolutionImageUrl(hostUrl + "getImage/" + qn_id + "/" + EdoConstants.ATTR_SOLUTION);
			}
		}
		
		
	}
	

	public static String prepareUrl(String url) {
		/*if(EdoConstants.ABSOLUTE_IMAGE_URLS) {
			return url;
		}*/
		String folderPath = null;
		//reliancedlp.edofox.com/public_html/testImages/371344/QuestionImg.jpg"
		String hostName = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME);
		if(StringUtils.isBlank(hostName)) {
			hostName = "http://test.edofox.com/";
		}
		if(!StringUtils.contains(url, "reliancedlp")) {
			folderPath = StringUtils.replace(url, "/var/www/edofoxlatur.com/public_html/", "");
		} else {
			hostName = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME_RELIANCE);
			if(StringUtils.isBlank(hostName)) {
				hostName = "http://reliancedlp.edofox.com/";
			}
			folderPath = StringUtils.replace(url, "/var/www/reliancedlp.edofox.com/public_html/", "");
			LoggingUtil.logMessage("Setting image URL as " + hostName + folderPath);
		}
		folderPath = hostName + folderPath;
		return folderPath;
	}
	
	public static Date getStartDate(Date date) {
		if(date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}
	
	public static Date getEndDate(Date date) {
		if(date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, 1);
		return CommonUtils.getStartDate(cal.getTime());
	}

	public static void closeSession(Session session) {
		try {
			if(session != null) {
				session.close();
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
	}
	
	public static void saveJson(EdoServiceRequest request) {
		try {
			String studentSubPath = "/" + request.getStudent().getId() + ".json";
			File folder = new File(EdoConstants.JSON_PATH + request.getTest().getId());
			if(!folder.exists()) {
				folder.mkdirs();
			}
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer();
			writer.writeValue(new File(folder + studentSubPath), request);
			//IOUtils.write(new ObjectMapper().writeValueAsString(request), new FileWriter(folder + studentSubPath));
		} catch (Exception e) {
			LoggingUtil.logError("Error in save JSON .. " + ExceptionUtils.getStackTrace(e), LoggingUtil.saveTestErrorLogger);
		}
	}

	public static Integer prepareTransactionId(Integer transactionId) {
		try {
			long time = System.currentTimeMillis();
			String timeString = String.valueOf(time);
			String substring = StringUtils.substring(timeString, timeString.length() - 3, timeString.length());
			return new Integer(transactionId + substring);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
