package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;

import com.rns.web.edo.service.domain.EDOAdminAnalytics;
import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoApiStatus;
import com.rns.web.edo.service.domain.EdoComplexOption;
import com.rns.web.edo.service.domain.EdoFeedback;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.EdoServiceRequest;
import com.rns.web.edo.service.domain.EdoServiceResponse;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentSubjectAnalysis;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.EdoTestStudentMap;
import com.rns.web.edo.service.domain.EdoVideoLectureMap;
import com.rns.web.edo.service.domain.jpa.EdoVideoLecture;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

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
	
	public static String convertDate(Date date, String format) {
		try {
			return new SimpleDateFormat(format).format(date);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Date parseDate(String format, String date) {
		try {
			return new SimpleDateFormat(format).parse(date);
		} catch (Exception e) {
			e.printStackTrace();
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

	public static String getStringValue(Integer value, boolean setZero) {
		if (value == null) {
			if(setZero) {
				return "0";
			}
			return "";
		}
		return value.toString();
	}

	public static String getStringValue(BigDecimal value, boolean setZero) {
		if (value == null) {
			if(setZero) {
				return "0";
			}
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
			result = StringUtils.replace(result, "{username}", CommonUtils.getStringValue(student.getRollNo()));
			result = StringUtils.replace(result, "{password}", CommonUtils.getStringValue(student.getPassword()));
			result = StringUtils.replace(result, "{gender}", CommonUtils.getStringValue(student.getGender()));
			result = StringUtils.replace(result, "{phone}", CommonUtils.getStringValue(student.getPhone()));
			result = StringUtils.replace(result, "{examMode}", CommonUtils.getStringValue(student.getExamMode()));
			result = StringUtils.replace(result, "{packages}", CommonUtils.extractPackages(student));
			if (student.getPayment() != null) {
				if (student.getPayment().isOffline()) {
					result = StringUtils.replace(result, "{paymentMode}", "Offline");
					result = StringUtils.replace(result, "{paymentMessage}", "");
				} else {
					result = StringUtils.replace(result, "{paymentMode}", "Online");
					//
					result = StringUtils.replace(result, "{paymentMessage}", "Please complete the payment in order to have full access to {instituteName} courses.");
				}
				result = StringUtils.replace(result, "{paymentId}", CommonUtils.getStringValue(student.getPayment().getPaymentId()));
				result = StringUtils.replace(result, "{transactionId}", CommonUtils.getStringValue(student.getTransactionId(), false));
			} else {
				result = StringUtils.replace(result, "{paymentMode}", "");
				result = StringUtils.replace(result, "{paymentId}", "");
				result = StringUtils.replace(result, "{transactionId}", "");
			}
			if(student.getAnalysis() != null) {
				result = StringUtils.replace(result, "{rank}", CommonUtils.getStringValue(student.getAnalysis().getRank(), false));
				result = StringUtils.replace(result, "{totalStudents}", CommonUtils.getStringValue(student.getAnalysis().getTotalStudents(), false));
				result = StringUtils.replace(result, "{score}", CommonUtils.getStringValue(student.getAnalysis().getScore(), false));
			}
		}
		return result;
	}

	public static String prepareTestNotification(String result, EdoTest test, EDOInstitute institute, String additionalMessage) {
		if (test != null) {
			result = StringUtils.replace(result, "{testName}", CommonUtils.getStringValue(test.getName()));
			result = StringUtils.replace(result, "{solved}", CommonUtils.getStringValue(test.getSolvedCount(), false));
			result = StringUtils.replace(result, "{correctCount}", CommonUtils.getStringValue(test.getCorrectCount(), false));
			result = StringUtils.replace(result, "{score}", CommonUtils.getStringValue(test.getScore(), false));
			result = StringUtils.replace(result, "{totalMarks}", CommonUtils.getStringValue(test.getTotalMarks(), false));
			result = StringUtils.replace(result, "{startDate}", CommonUtils.getStringValue(CommonUtils.convertDate(test.getStartDate(), "MMM dd hh:mm a")));
		}
		result = prepareInstituteNotification(result, institute);
		result = StringUtils.replace(result, "{additionalMessage}", CommonUtils.getStringValue(additionalMessage));
		return result;
	}

	
	public static String prepareInstituteNotification(String result, EDOInstitute institute) {
		if(institute != null) {
			result = StringUtils.replace(result, "{instituteName}", CommonUtils.getStringValue(institute.getName()));
			result = StringUtils.replace(result, "{username}", CommonUtils.getStringValue(institute.getUsername()));
			result = StringUtils.replace(result, "{password}", CommonUtils.getStringValue(institute.getPassword()));
			result = StringUtils.replace(result, "{purchase}", CommonUtils.getStringValue(institute.getPurchase()));
			if(institute.getExpiryDateString() != null && !StringUtils.equals("Free", institute.getPurchase())) {
				result = StringUtils.replace(result, "{expiryMessage}", "Your account will expire on " + institute.getExpiryDateString());	
			} else {
				result = StringUtils.replace(result, "{expiryMessage}", "");	
				
			}
			String appUrl = "https://play.google.com/store/apps/details?id=com.mattersoft.edofoxapp&hl=en_IN&gl=US";
			String webUrl = "https://test.edofox.com";
			if(StringUtils.isNotBlank(institute.getAppUrl())) {
				result = StringUtils.replace(result, "{appUrl}", institute.getAppUrl());	
			} else {
				result = StringUtils.replace(result, "{appUrl}", appUrl);
			}
			if(StringUtils.isNotBlank(institute.getWebUrl())) {
				result = StringUtils.replace(result, "{webUrl}", institute.getWebUrl());	
			} else {
				result = StringUtils.replace(result, "{webUrl}", webUrl);
			}
			//Check for expiry and show expiry message
			
			if(StringUtils.contains(result, "{accountExpiryMessage}")) {
				Date thirtyDaysLater = DateUtils.addDays(new Date(), 30);
				if(institute.getExpiryDate() != null && institute.getExpiryDate().compareTo(thirtyDaysLater) < 0) {
					long diffDays = (institute.getExpiryDate().getTime() - new Date().getTime())/ (1000 * 60 * 60 * 24);
					if(diffDays > 0) {
						result = StringUtils.replace(result, "{accountExpiryMessage}", "<div style=\"display: flex; justify-content: space-between;\">" +
								"<div style=\"display: inline-block; border: 2px solid #eee; border-radius: 10px; padding: 16px; text-align: center;margin: 8px; flex: 1;\">" +
								"<div style=\"color: #f44336;\">Your account will expire in <b>" + diffDays + "</b> days</div></div></div>");
					} else {
						result = StringUtils.replace(result, "{accountExpiryMessage}", "<div style=\"display: flex; justify-content: space-between;\">" +
								"<div style=\"display: inline-block; border: 2px solid #eee; border-radius: 10px; padding: 16px; text-align: center;margin: 8px; flex: 1;\">" +
								"<div style=\"color: #f44336;\">Your account is expired. Please complete the payment in order to proceed </div></div></div>");
					}
					
				} else {
					result = StringUtils.replace(result, "{accountExpiryMessage}", "");
				}
			}
			
			//result = StringUtils.replace(result, "{instituteName}", CommonUtils.getStringValue(institute.getName()));
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
		
		//Check if JEE rule of best of 5 is applicable
		List<String> newFormatSections = sectionsEligibleForNewFormat(test, questions);
		
		
		Integer solvedCount = 0;
		Integer correctCount = 0;
		Integer flaggedCount = 0;
		Integer visitedCount = 0;
		
		BigDecimal score = BigDecimal.ZERO;
		Map<String, Integer> sectionCorrectCount = new HashMap<String, Integer>();
		for (EdoQuestion answered : test.getTest()) {
			if (StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_MATCH, answered.getType())) {
				setComplexAnswer(answered);
			} else if (StringUtils.equalsIgnoreCase(EdoConstants.QUESTION_TYPE_DESCRIPTIVE, answered.getType())) {
				continue;
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
							//if section is eligible for new JEE format
							boolean addScoreToTotal = checkForJeeNewFormat(newFormatSections, sectionCorrectCount, answered, question, questionScore, test);
							
							if(addScoreToTotal) {
								BigDecimal marks = new BigDecimal(questionScore);
								answered.setMarks(marks);
								score = score.add(marks);
								LoggingUtil.logMessage(answered.getQuestionNumber() + "--" + answered.getQn_id() + " -- " + answered.getAnswer() + " -- "
										+ answered.getWeightage() + " -- " + answered.getNegativeMarks() + " " + ":" + answered.getMarks() + " -- " + score);
							} else {
								LoggingUtil.logMessage("Did not add score as best of condition reached " + answered.getQuestionNumber() + "--" + answered.getQn_id() + " -- " + answered.getAnswer() + " -- "
										+ answered.getWeightage() + " -- " + answered.getNegativeMarks() + " " + ":" + answered.getMarks() + " -- " + score);
							}
							
						}
						break;
					}
				}
				solvedCount++;
				if (answered.getFlagged() != null && answered.getFlagged() == 1) {
					flaggedCount++;
				}
			}
			visitedCount++;
		}
		
		test.setVisitedCount(visitedCount);
		test.setCorrectCount(correctCount);
		test.setFlaggedCount(flaggedCount);
		test.setSolvedCount(solvedCount);
		test.setScore(score);

		LoggingUtil.logMessage("Evaluated the test - " + test.getCorrectCount() + " .. " + test.getScore());
	}

	private static boolean checkForJeeNewFormat(List<String> jeeNewFormatSections, Map<String, Integer> sectionCorrectCount, EdoQuestion answered,
			EdoQuestion question, Float questionScore, EdoTest test) {
		String section = StringUtils.isNotBlank(answered.getSection()) ? answered.getSection() : question.getSection();
		if(questionScore > 0 && CollectionUtils.isNotEmpty(jeeNewFormatSections) && section != null && jeeNewFormatSections.contains(section)) {
			Integer count = sectionCorrectCount.get(section) != null ? sectionCorrectCount.get(section) : 0;
			count++;
			Integer limit = EdoConstants.JEE_NEW_FORMAT_BEST_OF_VALUE;
			if(StringUtils.equalsIgnoreCase("NEET", test.getTestUi())) {
				limit = EdoConstants.NEET_NEW_FORMAT_BEST_OF_VALUE;
			}
			if(count <= limit) {
				sectionCorrectCount.put(section, count);
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public static List<String> sectionsEligibleForNewFormat(EdoTest test, List<EdoQuestion> questions) {
		//Apply JEE/NEET new pattern rule of best of 5/ best of 15 for questions in case of JEE UI/ NEET UI and
		if(StringUtils.equalsIgnoreCase("JEEM", test.getTestUi())) {
			
			if(CollectionUtils.isNotEmpty(questions)) {
				Map<String, Integer> numericSectionMap = new HashMap<String, Integer>();
				for (EdoQuestion question : questions) {
					if(StringUtils.isNotBlank(question.getSection()) && StringUtils.equals(question.getType(), EdoConstants.QUESTION_TYPE_NUMBER)) {
						Integer count = 1;
						if(numericSectionMap.get(question.getSection()) != null) {
							count = numericSectionMap.get(question.getSection()) + 1;
						}
						numericSectionMap.put(question.getSection(), count);
					}
				}
				List<String> numericSections = new ArrayList<String>();
				for(Entry<String, Integer> sectionEntry: numericSectionMap.entrySet()) {
					if(sectionEntry.getValue() == EdoConstants.JEE_NEW_FORMAT_TOTAL_QUESTIONS) {
						numericSections.add(sectionEntry.getKey());
					}
				}
				return numericSections;
			}
		} else if (StringUtils.equalsIgnoreCase("NEET", test.getTestUi())) {
			Map<String, Integer> neetSectionMap = new HashMap<String, Integer>();
			for (EdoQuestion question : questions) {
				if(StringUtils.isNotBlank(question.getSection()) && StringUtils.equals(question.getType(), EdoConstants.QUESTION_TYPE_SINGLE)) {
					Integer count = 1;
					if(neetSectionMap.get(question.getSection()) != null) {
						count = neetSectionMap.get(question.getSection()) + 1;
					}
					neetSectionMap.put(question.getSection(), count);
				}
			}
			List<String> neetSections = new ArrayList<String>();
			for(Entry<String, Integer> sectionEntry: neetSectionMap.entrySet()) {
				if(sectionEntry.getValue() == EdoConstants.NEET_NEW_FORMAT_TOTAL_QUESTIONS) {
					neetSections.add(sectionEntry.getKey());
				}
			}
			return neetSections;
		}
				
		
		return null;
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
			Float matchScore = calculateMatchScore(question.getCorrectAnswer(), answered, question);
			Float altScore = null;
			if (StringUtils.isNotBlank(question.getAlternateAnswer())) {
				altScore = calculateMatchScore(question.getAlternateAnswer(), answered, question);
			}
			if (altScore != null && altScore > matchScore) {
				return altScore;
			}
			return matchScore;
		} else if (StringUtils.equals(EdoConstants.QUESTION_TYPE_NUMBER, question.getType()))  {
			//For number, compare decimals (12/07/2021)
			Float numericScore = calculateNumericScore(question.getCorrectAnswer(), answered.getAnswer(), question);
			if(StringUtils.isNotBlank(question.getAlternateAnswer())) {
				String[] altAnswers = StringUtils.split(question.getAlternateAnswer(), ",");
				if(ArrayUtils.isNotEmpty(altAnswers)) {
					for(String ans: altAnswers) {
						Float altScore = calculateNumericScore(ans, answered.getAnswer(), question);
						if(altScore > numericScore) {
							numericScore = altScore;
						}
					}
				}
			}
			return numericScore;
		} else if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(question.getCorrectAnswer()))) {
			return question.getWeightage();
		} else if (StringUtils.isNotBlank(question.getAlternateAnswer())) {
			String[] altAnswers = StringUtils.split(question.getAlternateAnswer(), ",");
			if(ArrayUtils.isNotEmpty(altAnswers)) {
				for(String ans: altAnswers) {
					if(StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(answered.getAnswer()), StringUtils.trimToEmpty(ans))) {
						return question.getWeightage();
					}
				}
			}
			return getWrongScore(question);
		}
		return getWrongScore(question);
	}

	private static Float calculateNumericScore(String correctAns, String answer, EdoQuestion question) {
		if(correctAns == null || answer == null) {
			return null;
		}
		if(!NumberUtils.isNumber(correctAns) || !NumberUtils.isNumber(answer)) {
			//If not a numeric answer, direct compare as string
			if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(correctAns), StringUtils.trimToEmpty(answer))) {
				return question.getWeightage();
			} else {
				return getWrongScore(question);
			}
		} else {
			//Numeric answer, compare as decimal
			Float cor = Float.parseFloat(correctAns);
			Float ans = Float.parseFloat(answer);
			if(cor.floatValue() == ans.floatValue()) {
				return question.getWeightage();
			} else {
				return getWrongScore(question);
			}
		}
	}

	public static boolean isBonus(EdoQuestion question) {
		return StringUtils.equalsIgnoreCase("bonus", question.getCorrectAnswer());
	}

	private static Float calculateMatchScore(String correctAnswer, EdoQuestion answered, EdoQuestion question) {

		if (correctAnswer != null && answered != null && StringUtils.isNotBlank(answered.getAnswer())) {

			//Convert all to lower case
			/*correctAnswer = StringUtils.lowerCase(correctAnswer);
			answered.setAnswer(StringUtils.lowerCase(answered.getAnswer()));
			
			String[] selectedPairs = StringUtils.split(answered.getAnswer(), ",");
			Float count = 0f;
			if (ArrayUtils.isNotEmpty(selectedPairs)) {
				Map<String, Integer> pairCount = new HashMap<String, Integer>();
				for (String selectedPair : selectedPairs) {
					if (!StringUtils.containsIgnoreCase(correctAnswer, selectedPair)) {
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
						//Calculate how many times this key (left column value) appears in correct answer
						int actualCount = calculateActualCount(correctAnswer, pair.getKey());
						if (pair.getValue() != null) {
							if (actualCount == pair.getValue().intValue()) {
								count++;
							} else {
								count--;
							}
						}
					}
				}
				if(question != null && question.getWeightage() != null && count > 0) {
					return count * question.getWeightage();
				} else if (question != null && question.getNegativeMarks() != null && count <= 0) {
					if(question.getNegativeMarks() == 0f) {
						return 0f;
					}
					return count * question.getNegativeMarks();
				}
				return count;
			}*/
			
			//New SIMPLIFIED logic for match column 29/05/21
			String[] correctAnswers = StringUtils.split(correctAnswer, ",");
			//Create list of left column entries
			Map<String, Integer> leftCols = new HashMap<String, Integer>();
			if(ArrayUtils.isNotEmpty(correctAnswers)) {
				for(String ans: correctAnswers) {
					String[] pairSplit = StringUtils.split(ans, "-");
					String leftCol = "";
					if(ArrayUtils.isNotEmpty(pairSplit)) {
						leftCol = pairSplit[0];
						if(leftCols.get(leftCol) == null) {
							leftCols.put(leftCol, 0);
						}
						leftCols.put(leftCol, leftCols.get(leftCol) + 1);
					}
				}
			}
			String[] studentAnswers = StringUtils.split(answered.getAnswer(), ",");
			int matchedCount = 0, unMatchedCount = 0;
			if(ArrayUtils.isNotEmpty(studentAnswers)) {
				Map<String, Integer> matchedLeftCols = new HashMap<String, Integer>();
				Map<String, Integer> unmatchedLeftCols = new HashMap<String, Integer>();
				for(String studentAnswer: studentAnswers) {
					boolean matched = false;
					String[] pairSplit = StringUtils.split(studentAnswer, "-");
					String leftCol = "";
					if(ArrayUtils.isNotEmpty(pairSplit)) {
						leftCol = pairSplit[0];
					}
					for(String correctPair: correctAnswers) {
						if(StringUtils.equalsIgnoreCase(studentAnswer, correctPair)) {
							matched = true;
							break;
						}
					}
					if(matched) {
						//matchedCount++;
						if(matchedLeftCols.get(leftCol) == null) {
							matchedLeftCols.put(leftCol, 0);
						}
						matchedLeftCols.put(leftCol, matchedLeftCols.get(leftCol) + 1);
						
					} else {
						//unMatchedCount++;
						if(unmatchedLeftCols.get(leftCol) == null) {
							unmatchedLeftCols.put(leftCol, 0);
						}
						unmatchedLeftCols.put(leftCol, unmatchedLeftCols.get(leftCol) + 1);						
					}
				}
				
				//Calculate score for each Left col
				if(CollectionUtils.isNotEmpty(leftCols.keySet())) {
					for(Entry<String, Integer> leftCol: leftCols.entrySet()) {
						//If found in unmatched list..wrong
						if(unmatchedLeftCols.keySet().contains(leftCol.getKey())) {
							unMatchedCount++;
							continue;
						}
						//If found in matched list..count also has to match..otherwise wrong
						if(matchedLeftCols.get(leftCol.getKey()) != null) {
							if(matchedLeftCols.get(leftCol.getKey()).intValue() == leftCol.getValue().intValue()) {
								matchedCount++;
							} else {
								unMatchedCount++;
							}
						}
					}
				}
				
			}
			
			float score = 0f;
			if(question.getWeightage() != null && question.getWeightage() > 0f && matchedCount > 0) {
				score = question.getWeightage() * matchedCount;
			}
			if(question.getNegativeMarks() != null && question.getNegativeMarks() > 0f && unMatchedCount > 0) {
				score = score - question.getNegativeMarks() * unMatchedCount;
			}
			return score;
		}
		return null;
	}

	private static int calculateActualCount(String correctAnswer, String key) {
		try {
			if(StringUtils.isNotBlank(correctAnswer) && StringUtils.isNotBlank(key)) {
				String[] correctAnswerValues = StringUtils.split(correctAnswer, ",");
				int count = 0;
				if(ArrayUtils.isNotEmpty(correctAnswerValues)) {
					for(String cols: correctAnswerValues) {
						String[] columns = StringUtils.split(cols, "-");
						if(ArrayUtils.isNotEmpty(columns) && StringUtils.equalsIgnoreCase(columns[0], key)) {
							count++;
						}
					}
					
				}
				return count;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
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
	
	public static List<EdoStudentSubjectAnalysis> getSubjectAnalysis(EdoTest existing, List<EdoTestStudentMap> subjectScores, EdoStudent student, List<String> colSeq) {
		List<EdoStudentSubjectAnalysis> subjectAnalysis = new ArrayList<EdoStudentSubjectAnalysis>();
		for(EdoTestStudentMap map: subjectScores) {
			if(map.getStudent() != null && map.getStudent().getId().intValue() == student.getId().intValue() && map.getSubjectScore() != null ) {
				subjectAnalysis.add(map.getSubjectScore());
				/*if(  !existing.getSubjects().contains(map.getSubjectScore().getSubject()) CollectionUtils.isEmpty(existing.getSubjects())) {
					existing.getSubjects().add(map.getSubjectScore().getSubject());
				}*/
			}
			
		}
		
		setupSubjectAnalysis(existing, subjectAnalysis, colSeq);
		
		return subjectAnalysis;
	}

	public static void setupSubjectAnalysis(EdoTest existing, List<EdoStudentSubjectAnalysis> subjectAnalysis, final List<String> colSeq) {
		if(existing == null || CollectionUtils.isEmpty(existing.getSubjects())) {
			return;
		}

		// Look for subject now found
		for (String subject : existing.getSubjects()) {
			boolean found = false;
			for (EdoStudentSubjectAnalysis map : subjectAnalysis) {
				if (subject.equals(map.getSubject())) {
					found = true;
					break;
				}
			}
			if (!found) {
				EdoStudentSubjectAnalysis analysis = new EdoStudentSubjectAnalysis();
				analysis.setSubject(subject);
				subjectAnalysis.add(analysis);
			}
		}

		Collections.sort(subjectAnalysis, new Comparator<EdoStudentSubjectAnalysis>() {

			public int compare(EdoStudentSubjectAnalysis o1, EdoStudentSubjectAnalysis o2) {
				if(CollectionUtils.isNotEmpty(colSeq)) {
					int index1 = colSeq.indexOf(o1.getSubject());
					int index2 = colSeq.indexOf(o2.getSubject());
					return index1 - index2;
				}
				return o1.getSubject().compareTo(o2.getSubject());
			}
		});
	}

	public static void main(String[] args) {
		/*EdoQuestion question = new EdoQuestion();
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
*/		
		//String languageCode = RakeLanguages.EN;
		
		Date thirtyDaysLater = DateUtils.addDays(new Date(), 30);
		Date actualDate = new Date();
		
		long diffDays = (thirtyDaysLater.getTime() - actualDate.getTime())/(1000 * 60 * 60 * 24);
		
		System.out.println(diffDays);
	}
	
	public static void setQuestionURLs(EdoQuestion question) {
		
		String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL);
		
		String absoluteUrls = EdoPropertyUtil.getProperty(EdoPropertyUtil.ABSOLUTE_IMAGE_URLS);
		if(StringUtils.equals(absoluteUrls, "true") && !StringUtils.contains(question.getQuestionImageUrl(), "home")) {
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
	
	public static String setUrl(String url) {
		if(StringUtils.contains(url, "http")) {
			return url;
		}
		String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME);
		if(StringUtils.isNotBlank(hostUrl)) {
			return hostUrl + url;
		}
		return url;
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
			//LoggingUtil.logMessage("Setting image URL as " + hostName + folderPath);
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
	

	public static BigDecimal calculateStorageUsed(Float value) {
		if(value == null || value == 0) {
			return BigDecimal.ZERO;
		}
		Double storageQuota = new Double(value) / new Double(1024d * 1024d * 1024d);
		BigDecimal bd = new BigDecimal(storageQuota).setScale(5, RoundingMode.HALF_UP);
		return bd;
	}
	
	public static void saveFile(InputStream content, String path, String fileName) {
		FileOutputStream fileOutputStream = null;
		try {
			if(content != null) {
				//Create directory if not present
				File folder = new File(path);
				if(!folder.exists()) {
					folder.mkdirs();
				}
				fileOutputStream = new FileOutputStream(path + fileName);
			    int read;
				byte[] bytes = new byte[1024];
				while ((read = content.read(bytes)) != -1) {
					fileOutputStream.write(bytes, 0, read);
				}
				fileOutputStream.close();
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		} finally {
			if(fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	public static String callExternalApi(String url, JSONObject request, String methodType, String authKey) {
		try {
			ClientConfig config = new DefaultClientConfig();
			config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(config);

			WebResource webResource = client.resource(url);
			LoggingUtil.logMessage("Calling API URL :" + url + " with request:" + request, LoggingUtil.videoLogger);

			//webResource.type(MediaType.TEXT_HTML);
			Builder builder = webResource.type("application/json");
			if(authKey != null) {
				builder.header("Authorization", authKey);
			}
			if(methodType != null && methodType.equals("PUT")) {
				//�X-HTTP-Method-Override�: �PUT�
				builder.header("X-HTTP-Method-Override", methodType);
			}
			
			ClientResponse response = null;
			if (StringUtils.equals("GET", methodType)) {
				response = builder.get(ClientResponse.class);
			} else if (StringUtils.equals(methodType, "PUTACTUAL")) { 
				response = builder.put(ClientResponse.class, request);
			} else {
				response = builder.post(ClientResponse.class, request);
			}
			
			if (response.getStatus() != 200) {
				LoggingUtil.logMessage("Failed in API URL " + url + ": HTTP error code : " + response.getStatus(), LoggingUtil.videoLogger);
			}
			String output = response.getEntity(String.class);
			LoggingUtil.logMessage("Output from " + url + " URL : " + response.getStatus() + ".... \n " + output, LoggingUtil.videoLogger);
			return output;
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
		return null;
	
	}
	
	
	
	public static String prepareClassworkNotification(String result, EdoVideoLecture classwork) {
		if(classwork != null) {
			result = StringUtils.replace(result, "{title}", CommonUtils.getStringValue(classwork.getVideoName()));
			String type = "video";
			if(StringUtils.equals("DOC", classwork.getType())) {
				type = "document";
			}
			result = StringUtils.replace(result, "{contentType}", type);
		}
		return result;
	}
	
	public static String prepareClassworkNotification(String result, EdoVideoLectureMap classwork) {
		if(classwork != null) {
			result = prepareClassworkNotification(result, classwork.getLecture());
			if(classwork.getSubject() != null) {
				result = StringUtils.replace(result, "{subject}", CommonUtils.getStringValue(classwork.getSubject().getSubjectName()));
				if(classwork.getChapter() != null) {
					result = StringUtils.replace(result, "{chapter}", CommonUtils.getStringValue(classwork.getChapter().getChapterName()));
				}
			}
		}
		return result;
	}
	
	public static String escapeQuotes(String value) {
		try {
			return StringUtils.replace(value, "'", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static String prepareFeedbackNotification(String result, EdoQuestion feedbackData, EdoStudent student) {
		if(feedbackData == null){
			return result;
		}
		EdoFeedback feedback = feedbackData.getFeedback();
		if (feedback != null) {
			
			//Fix URLs
			CommonUtils.setQuestionURLs(feedbackData);
			QuestionParser.fixQuestion(feedbackData);
			setupFeedbackAttachment(feedbackData);
			
			if(StringUtils.isBlank(feedback.getAnsweredBy())) {
				feedback.setAnsweredBy("Admin");
			}
			//result = StringUtils.replace(result, "{answeredBy}", CommonUtils.getStringValue(feedback.getAnsweredBy()));
			String doubtType = "individual";
			String detailsUrl = "specific_doubt.php?";
			if(feedback.getQuestionId() != null) {
				result = StringUtils.replace(result, "{doubtFor}", "for question");
				doubtType = "question";
				result = StringUtils.replace(result, "{doubtText}", CommonUtils.getStringValue(feedback.getFeedback()));
				result = StringUtils.replace(result, "{doubtImage}", "");
				detailsUrl = detailsUrl + "doubtId=" + feedback.getQuestionId();
			} else if (feedback.getVideoId() != null) {
				result = StringUtils.replace(result, "{doubtFor}", "for video");
				doubtType = "video";
				result = StringUtils.replace(result, "{doubtText}", CommonUtils.getStringValue(feedback.getFeedback()));
				result = StringUtils.replace(result, "{doubtImage}", "");
				detailsUrl = detailsUrl + "doubtId=" + feedback.getVideoId();
			} else {
				result = StringUtils.replace(result, "{doubtFor}", "");
				result = StringUtils.replace(result, "{doubtText}", CommonUtils.getStringValue(feedback.getFeedback()));
				//Doubt attachment by student
				String doubtImageUrl = CommonUtils.getStringValue(feedback.getAttachment());
				if(StringUtils.isNotBlank(doubtImageUrl)) {
					result = StringUtils.replace(result, "{doubtImage}", "<a href='" + doubtImageUrl + "'> View attachment </a>");
				} else {
					result = StringUtils.replace(result, "{doubtImage}", "");
				}
				detailsUrl = detailsUrl + "doubtId=" + feedback.getId();
			}
			if(student != null) {
				detailsUrl = detailsUrl + "&doubtType=" + doubtType + "&studentId=" + student.getId();
			}
			//Question text or video title
			/*if(StringUtils.isNotBlank(feedbackData.getQuestion())) {
				result = StringUtils.replace(result, "{questionText}", CommonUtils.getStringValue(feedbackData.getQuestion()));
			} else {*/
				result = StringUtils.replace(result, "{questionText}", CommonUtils.getStringValue(feedback.getSourceVideoName()));
			//}
			//Question image
			if(StringUtils.isNotBlank(feedbackData.getQuestionImageUrl())) {
				result = StringUtils.replace(result, "{questionImage}", "<img src='" + feedbackData.getQuestionImageUrl() + "' style='max-height:250px;max-width:400px;' />");
			} else {
				result = StringUtils.replace(result, "{questionImage}", "");
			}
			//Resolution
			result = StringUtils.replace(result, "{resolutionText}", CommonUtils.getStringValue(feedback.getFeedbackResolutionText()));
			if(StringUtils.isNotBlank(feedback.getFeedbackResolutionImageUrl())) {
				result = StringUtils.replace(result, "{resolutionLink}", "<a href='" + CommonUtils.setUrl(CommonUtils.getStringValue(feedback.getFeedbackResolutionImageUrl())) + "' >View resolution attachment</a>");
			} else {
				result = StringUtils.replace(result, "{resolutionLink}",  "");
			}
			if(student != null) {
				result = StringUtils.replace(result, "{actionUrl}", CommonUtils.setUrl(detailsUrl));
			} else {
				result = StringUtils.replace(result, "{actionUrl}", "test.edofox.com");
			}
		}
		return result;
	}
	
	public static void setupFeedbackAttachment(EdoQuestion edoFeedback) {
		EdoFeedback feedback = edoFeedback.getFeedback();
		if(feedback != null && StringUtils.isNotBlank(feedback.getAttachment()) && feedback.getId() != null) {
			String hostUrl = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL);
			feedback.setAttachment(hostUrl + "getImage/" +  feedback.getId() + "/" + EdoConstants.ATTR_DOUBT_IMAGE);
		} else {
			feedback.setAttachment(null);
		}
	}

	public static String createUniversalToken(EdoStudent student) {
		if(student == null || student.getId() == null) {
			return "";
		}
		if(StringUtils.isBlank(student.getName())) {
			return "";
		}
		String firstLetter = StringUtils.substring(student.getName(), 0, 1);
		return DigestUtils.sha256Hex(firstLetter + "_" + student.getId());
	}

	public static boolean isIntEnabled(Integer value) {
		if(value == null || value != 0) {
			return true;
		}
		return false;
	}

	public static void closeStream(InputStream is) {
		try {
			if(is != null) {
				is.close();
			}
		} catch (Exception e) {

		}
	}

	public static BigDecimal subtract(BigDecimal subtractFrom, BigDecimal subtract) {
		try {
			if(subtractFrom == null) {
				subtractFrom = BigDecimal.ZERO;
			}
			if(subtract == null) {
				subtract = BigDecimal.ZERO;
			}
			return subtractFrom.subtract(subtract);
		} catch (Exception e) {
			
		}
		return null;
	}

	public static String prepareInstituteReport(String result, EDOAdminAnalytics analytics) {
		if(analytics != null) {
			result = prepareInstituteNotification(result, analytics.getInstitute());
			result = StringUtils.replace(result, "{activeTests}", getStringValue(analytics.getActiveTests(), true));
			result = StringUtils.replace(result, "{presenty}", getStringValue(analytics.getPresenty(), true));
			result = StringUtils.replace(result, "{absenty}", getStringValue(analytics.getAbsenty(), true));
			result = StringUtils.replace(result, "{testSubmits}", getStringValue(analytics.getTestSubmits(), true));
			result = StringUtils.replace(result, "{studentsAppeared}", getStringValue(analytics.getStudentsAppeared(), true));
			result = StringUtils.replace(result, "{doubtsRaised}", getStringValue(analytics.getDoubtsRaised(), true));
			result = StringUtils.replace(result, "{doubtsResolved}", getStringValue(analytics.getDoubtsResolved(), true));
			result = StringUtils.replace(result, "{doubtsPending}", getStringValue(analytics.getDoubtsRaised(), true));
			//result = StringUtils.replace(result, "{instituteName}", CommonUtils.getStringValue(institute.getName()));
		}
		return result;
	}
	
}
