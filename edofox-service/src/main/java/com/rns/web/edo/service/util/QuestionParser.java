package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.rns.web.edo.service.domain.EdoQuestion;

public class QuestionParser {
	
	private static final String ANS_PARSE_KEY = ".Ans";

	public static void main(String[] args) {
		String fileName = "F:\\Resoneuronance\\Edofox\\Document\\Latex\\VL CET 2018\\Biology01.tex";
		Integer previousQuestion = 100;
		Integer testId = null;
		
		System.out.println(parseQuestionPaper(fileName, previousQuestion, "F:\\Resoneuronance\\Edofox\\Document\\Latex\\VL CET 2018\\Solutions\\Biology01.tex").size());
	}

	public static List<EdoQuestion> parseQuestionPaper(String fileName, Integer previousQuestion, String solutionPath) {
		
		List<EdoQuestion> questions = new ArrayList<EdoQuestion>();
		
		BufferedReader reader;
		String question = "", option1 = "", option2 = "", option3 = "", option4 = "";
		Integer step = 0;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			boolean isStartFound = false;
			while (line != null) {
				line = reader.readLine();
				String trimmed = StringUtils.trimToEmpty(line);
				if(StringUtils.isBlank(trimmed)) {
					continue;
				}
				
				if(StringUtils.equalsIgnoreCase("\\\\begin{document}", trimmed) || StringUtils.equalsIgnoreCase("\\\\end{document}", trimmed)) {
					isStartFound = true;
					System.out.println("Found!");
					continue;
				}
				
				if(!validLatex(trimmed)) {
					continue;
				}
				
				if(!isStartFound) {
					continue;
				}
				int indexOfPeriod = StringUtils.indexOf(trimmed, ".");
				if(indexOfPeriod >= 1 && indexOfPeriod < 5) {
					
					String questionNumber = StringUtils.substring(trimmed, 0, indexOfPeriod);
					if(StringUtils.isNumeric(questionNumber)) {
						Integer questionNo = new Integer(questionNumber);
						if( (questionNo - 1) == previousQuestion) {
							LoggingUtil.logMessage("Question number =>" + questionNumber);
							LoggingUtil.logMessage("Question =>" + question);
							LoggingUtil.logMessage("Option 1 =>" + option1);
							LoggingUtil.logMessage("Option 2 =>" + option2);
							LoggingUtil.logMessage("Option 3 =>" + option3);
							LoggingUtil.logMessage("Option 4 =>" + option4);
							
							addQuestion(previousQuestion, solutionPath, questions, question, option1, option2, option3, option4);
							
							question = ""; 
							option1 = ""; 
							option2 = ""; 
							option3 = "";
							option4 = "";
							
							
							previousQuestion = questionNo;
							question = StringUtils.substring(trimmed, indexOfPeriod + 1, trimmed.length());
							System.out.println("Question is =>" + question);
							step = 1;
							continue;
						}
					}
					
				}
				
				int indexOfOption1 = StringUtils.indexOf(trimmed, "(A)");
				if(indexOfOption1 == 0) {
					option1 = option1 + StringUtils.substring(trimmed, 3, trimmed.length());
					step = 2;
					continue;
				}
				
				int indexOfOption2 = StringUtils.indexOf(trimmed, "(B)");
				if(indexOfOption2 == 0) {
					option2 = option2 + StringUtils.substring(trimmed, 3, trimmed.length());
					step = 3;
					continue;
				}
				
				int indexOfOption3 = StringUtils.indexOf(trimmed, "(C)");
				if(indexOfOption3 == 0) {
					option3 = option3 + StringUtils.substring(trimmed, 3, trimmed.length());
					step = 4;
					continue;
				}
				
				int indexOfOption4 = StringUtils.indexOf(trimmed, "(D)");
				if(indexOfOption4 == 0) {
					option4 = option4 + StringUtils.substring(trimmed, 3, trimmed.length());
					step = 5;
					continue;
				}
				
				
				if(step == 1) {
					question = question + trimmed;
				} else if (step == 2) {
					option1 = option1 + trimmed;
				} else if (step == 3) {
					option2 = option2 + trimmed;
				} else if (step == 4) {
					option3 = option3 + trimmed;
				} else if (step == 5) {
					option4 = option4 + trimmed;
				}
				
				
				//System.out.println(trimmed);
				
				//System.out.println("..........");
			}
			reader.close();
			
			addQuestion(previousQuestion, solutionPath, questions, question, option1, option2, option3, option4);
			
		} catch (IOException e) {
			LoggingUtil.logMessage(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		return questions;
	}

	private static boolean validLatex(String trimmed) {
		if(StringUtils.contains(trimmed, "\\\\multicolumn{")) {
			return false;
		}
		if(StringUtils.equalsIgnoreCase(trimmed, "&")) {
			return false;
		}
		if(StringUtils.equals(trimmed, "} &")) {
			return false;
		}
		if(StringUtils.equals(trimmed, "}") || StringUtils.equals(trimmed, "{")) {
			return false;
		}
		return true;
	}

	private static void addQuestion(Integer previousQuestion, String solutionPath, List<EdoQuestion> questions, String question, String option1, String option2,
			String option3, String option4) {
		if(StringUtils.isNotBlank(question)) {
			EdoQuestion edoQuestion = new EdoQuestion();
			edoQuestion.setQuestion(latextCorrect(question));
			edoQuestion.setOption1(latextCorrect(option1));
			edoQuestion.setOption2(latextCorrect(option2));
			edoQuestion.setOption3(latextCorrect(option3));
			edoQuestion.setOption4(latextCorrect(option4));
			edoQuestion.setId(previousQuestion);
			parseSolution(previousQuestion, edoQuestion, solutionPath);
			questions.add(edoQuestion);
		}
	}
	
	private static String latextCorrect(String value) {
		value = StringUtils.replace(value, "{", " { ");
		value = StringUtils.replace(value, "}", " } ");
		value = StringUtils.replace(value,"\\\\begin { array }", "\\\\begin{array}");
		value = StringUtils.replace(value,"\\\\end { array }", "\\\\end{array}");
		value = StringUtils.replace(value,"$", "$ ");
		value = StringUtils.replace(value,"\\ldots { }" , "...");
		value = StringUtils.replace(value, "\\[" , "$ ");
		value = StringUtils.replace(value, "\\]" , "$ ");
		value = StringUtils.replace(value, "\\sin" , "\\sin ");
		value = StringUtils.replace(value, "\\tan" , "\\tan ");
		value = StringUtils.replace(value, "\\cos" , "\\cos ");
		value = StringUtils.replace(value, "\\sec" , "\\sec ");
		value = StringUtils.replace(value, "\\cosec" , "\\cosec ");
		String dashedLine = "\\_\\_";
		for(int i = 0; i < 10; i++) {
			value = StringUtils.replace(value, dashedLine , "$ " + dashedLine + " $ ");
			dashedLine = dashedLine + "\\_";
		}
		return value;
	}
	
	public static void parseSolution(Integer questionNumber, EdoQuestion question, String filePath) {
		BufferedReader reader;
		
		if(StringUtils.isEmpty(filePath)) {
			return;
		}
		
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			String answer = "";
			boolean answerFound = false;
			while (line != null) {
				line = reader.readLine();
				String trimmed = StringUtils.trimToEmpty(line);
				
				if(StringUtils.isBlank(trimmed)) {
					continue;
				}
				
				//int indexOfNextAnswer = StringUtils.indexOf(trimmed, questionNumber + 1 + ANS_PARSE_KEY);
				if(StringUtils.contains(trimmed, ANS_PARSE_KEY) && answerFound) {
					break;
				}
				
				if(answerFound) {
					answer = answer + trimmed;
					continue;
				}
				int indexOfAnswer = StringUtils.indexOf(trimmed, questionNumber + ANS_PARSE_KEY);
				if(indexOfAnswer == 0 || answerFound) {
					answerFound = true;
					String correctAnswer = StringUtils.trimToEmpty(StringUtils.substring(trimmed, questionNumber.toString().length() + ANS_PARSE_KEY.length(), trimmed.length()));
					if(StringUtils.containsIgnoreCase(correctAnswer, "A")) {
						question.setCorrectAnswer(EdoConstants.ATTR_OPTION1);
					} else if (StringUtils.containsIgnoreCase(correctAnswer, "B")) {
						question.setCorrectAnswer(EdoConstants.ATTR_OPTION2);
					} else if (StringUtils.containsIgnoreCase(correctAnswer, "C")) {
						question.setCorrectAnswer(EdoConstants.ATTR_OPTION3);
					} else if (StringUtils.containsIgnoreCase(correctAnswer, "D")) {
						question.setCorrectAnswer(EdoConstants.ATTR_OPTION4);
					}
					System.out.println(question.getId() + " --- Answer:" + question.getCorrectAnswer());
				}
			}
			question.setSolution(latextCorrect(answer));
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
	}

}
