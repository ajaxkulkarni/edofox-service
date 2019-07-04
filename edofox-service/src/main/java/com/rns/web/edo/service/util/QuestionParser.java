package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.rns.web.edo.service.domain.EdoQuestion;

public class QuestionParser {
	
	private static final String ANS_PARSE_KEY = ".Ans";

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
			
			LoggingUtil.logMessage("Question number =>" + previousQuestion);
			LoggingUtil.logMessage("Question =>" + edoQuestion.getQuestion());
			LoggingUtil.logMessage("Option 1 =>" + edoQuestion.getOption1());
			LoggingUtil.logMessage("Option 2 =>" + edoQuestion.getOption2());
			LoggingUtil.logMessage("Option 3 =>" + edoQuestion.getOption3());
			LoggingUtil.logMessage("Option 4 =>" + edoQuestion.getOption4());
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
		value = StringUtils.replace(value, "<" , " < ");
		value = StringUtils.replace(value, "\\int" , "\\int ");
		value = StringUtils.replace(value, "\\log" , "\\log ");
		value = StringUtils.replace(value, "<" , " < ");
		value = StringUtils.replace(value, "\\_" , "_");
		/*String dashedLine = "\\_\\_";
		String replacement = "__";
		for(int i = 0; i < 10; i++) {
			value = StringUtils.replace(value, dashedLine , "__" + replacement);
			dashedLine = dashedLine + "\\_";
			replacement = replacement + "__";
		}*/
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
	
	public static EdoQuestion parseHtml(EdoQuestion edoQuestion) {
		/*String blogUrl = "https://www.toppr.com/class-12/physics/units-and-dimensions/";
		Connection connect = Jsoup.connect(blogUrl);
		connect.cookie("session_id", "895510fab9894137ac64501036ed0797");
		connect.cookie("ajs_user_id", "10254079");*/
		//connect.
		//Document doc = connect.get();
		//System.out.println(doc);
		Document doc = Jsoup.parse(edoQuestion.getMetaData());
		//System.out.println(doc);
		Elements scriptTags = doc.getElementsByTag("script");
		for(Element script: scriptTags) {
			script.removeAttr("id");
		}
		System.out.println(scriptTags);
		doc.getElementsByTag("math").remove();
		doc.getElementsByClass("MathJax").remove();
		System.out.println(".....................................");
		String question = "";
		//Parse question premise
		Elements questionElement = doc.getElementsByClass("_3uE9N");
		if(!questionElement.isEmpty()) {
			//Element questionDiv = questionElement.get(0);
			String questionHtml = questionElement.html();
			System.out.println(questionHtml);
			/*int index = 0;
			while(index >= 0) {
				index = StringUtils.indexOf(questionHtml, "<script type=\"math/tex\">", 0);
				if(index >= 0) {
					questionHtml = StringUtils.replaceOnce(questionHtml, "<script type=\"math/tex\">", "$");
					questionHtml = StringUtils.replaceOnce(questionHtml, "</script>", "$");
				}
			}*/
			//System.out.println(Jsoup.parse(questionHtml).text());
			questionHtml = StringUtils.replace(questionHtml, "<script type=\"math/tex\">", "$");
			questionHtml = StringUtils.replace(questionHtml, "</script>", "$");
			System.out.println("==================");
			System.out.println(questionHtml);
			question = Jsoup.parse(questionHtml).text();
			//System.out.println("Premise: " + question);
		}
		
		//Add actual question
		Elements actualQuestions = doc.getElementsByClass("_1M0B9");
		if(!actualQuestions.isEmpty()) {
			String parsedHtml = addMathJax(actualQuestions);
			question = question + Jsoup.parse(parsedHtml).text();
			//System.out.println("Actual Q:" + question);
		}
		
		//Get lists for match the following type question from _97VAo
		Elements lists = doc.getElementsByClass("_97VAo");
		String matchOption1 = "", matchOption2 = "";
		if(!lists.isEmpty()) {
			StringBuilder listBuilder = new StringBuilder();
			//listBuilder.append("List 1").append("\n");
			Element list1 = lists.get(0);
			Elements rows = list1.getElementsByTag("tr");
			if(!rows.isEmpty()) {
				for(Element row: rows) {
					String matchOption = Jsoup.parse(addMathJax(row)).text();
					listBuilder.append(matchOption).append("\n");
					if(row.children().size() > 1) {
						matchOption1 = matchOption1 + StringUtils.split(matchOption, " ")[0] + ",";
					}
				}
			}
			matchOption1 = StringUtils.removeEnd(matchOption1, ",");
			System.out.println("Option 1:" + matchOption1);
			//listBuilder.append("List 2").append("\n");
			Element list2 = lists.get(1);
			Elements list2Rows = list2.getElementsByTag("tr");
			if(!list2Rows.isEmpty()) {
				for(Element row: list2Rows) {
					String matchOption = Jsoup.parse(addMathJax(row)).text();
					listBuilder.append(matchOption).append("\n");
					if(row.children().size() > 1) {
						matchOption2 = matchOption2 + StringUtils.split(matchOption, " ")[0] + ",";
					}
				}
			}
			matchOption2 = StringUtils.removeEnd(matchOption2, ",");
			System.out.println("Option 2:" + matchOption2);
			//System.out.println("List:" + listBuilder);
			question = question + "\n" + listBuilder.toString();
		}
		
		System.out.println("Question: " + question);
		
		//Get question image (If any)
		Elements elements = doc.getElementsByClass("_2v7lu");
		if(!elements.isEmpty()) {
			edoQuestion.setQuestionImageUrl(elements.get(0).attr("src"));
			System.out.println("Q Image: " + elements.get(0).attr("src"));
		}
		//Parse options
		
		String correctAnswer = "";
		String option1 = null, option2 = null, option3 = null, option4 = null;
		Elements optionElements = doc.getElementsByClass("_2qcoI");
		if(!optionElements.isEmpty()) {
			int i = 1;
			for(Element option: optionElements) {
				if(option.hasClass("_2IFsH")) {
					correctAnswer = correctAnswer + "option" + i + ","; 
				}
				Elements optionElement = option.getElementsByClass("_3sXEB");
				String optionHtml = optionElement.html();
				optionHtml = StringUtils.replace(optionHtml, "<script type=\"math/tex\">", "$");
				optionHtml = StringUtils.replace(optionHtml, "</script>", "$");
				String opt = Jsoup.parse(optionHtml).text();
				System.out.println("Option: " + i + ":" + opt);
				if(i == 1) {
					option1 = opt;
				} else if (i == 2) {
					option2 = opt;
				} else if (i == 3) {
					option3 = opt;
				} else if (i == 4) {
					option4 = opt;
				}
				i++;
			}
			correctAnswer = StringUtils.removeEnd(correctAnswer, ",");
			System.out.println("Correct answer .." + correctAnswer);
		} else if (!doc.getElementsByClass("_2JdWN").isEmpty()) {
			optionElements = doc.getElementsByClass("_2JdWN");
			correctAnswer = Jsoup.parse(addMathJax(optionElements)).text();
			System.out.println("Correct answer .." + correctAnswer);
		} else {
			//Match the following
			
			//Correct answer for match the following from ._3gHyx._1bfBQ wrong --> ._3gHyx._30NiZ
			if(StringUtils.isBlank(correctAnswer)) {
				Elements matchOptions = doc.getElementsByClass("_2aVzF");
				StringBuilder builder = new StringBuilder();
				if(!matchOptions.isEmpty()) {
					for(Element matchOp: matchOptions) {
						String parent = matchOp.getElementsByClass("Zh3lG").text();
						Elements childOptions = matchOp.getElementsByClass("_3gHyx");
						for(Element child: childOptions) {
							if(child.hasClass("_1bfBQ")) {
								builder.append(parent).append("-").append(child.text()).append(",");
							}
						}
					}
					correctAnswer = StringUtils.removeEnd(builder.toString(), ",");
					System.out.println("Correct answer .." + correctAnswer);
				}
			}
		}
		
		
		//Parse solution
		Elements solution = doc.getElementsByClass("_2iWgr");
		String finalSolution = "";
		if(!solution.isEmpty()) {
			finalSolution = getSolution(solution);
		} else {
			//_1lGjP
			solution = doc.getElementsByClass("_1lGjP");
			if(!solution.isEmpty()) {
				finalSolution = getSolution(solution);
			} else {
				solution = doc.getElementsByClass("_1wM5e");
				if(!solution.isEmpty()) {
					finalSolution = getSolution(solution);
				} 
			}
		}
		
		if(StringUtils.isBlank(question) || StringUtils.isBlank(correctAnswer)) {
			return null;
		}
		
		edoQuestion.setQuestion(question);
		edoQuestion.setCorrectAnswer(correctAnswer);
		edoQuestion.setOption1(option1);
		edoQuestion.setOption2(option2);
		edoQuestion.setOption3(option3);
		edoQuestion.setOption4(option4);
		if(StringUtils.isNotBlank(matchOption1)) {
			edoQuestion.setOption1(matchOption1);
		}
		if(StringUtils.isNotBlank(matchOption2)) {
			edoQuestion.setOption2(matchOption2);
		}
		edoQuestion.setSolution(finalSolution);
		
		return edoQuestion;
		
		//_202hV - question div class
		//<div class="_2GJzr"><strong>Correct answer</strong></div>
		//_2Lgga _17yZ3 - solution div class
		//_1VlbT _1WDRX Option main div --> _3sXEB actual option --> _2GJzr correct answer div
		//<script type="math/tex" id="MathJax-Element-339"> 
		
		
		//<div class="_2GJzr _145Qo"><strong>Correct answer</strong></div>
		//question div --> _3uE9N
		//actual question --> _1M0B9
		//option div --> _3sXEB
		
		//Multiple questions
		//#div._2qcoI._1WDRX._2IFsH.BS_w2 for right if it has 2IFsH
	}

	private static String addMathJax(Element element) {
		if(element == null) {
			return "";
		}
		String val = StringUtils.replace(element.html(), "<script type=\"math/tex\">", "$");
		val = StringUtils.replace(val, "</script>", "$");
		return val;
	}

	private static String getSolution(Elements solution) {
		Element parent = solution.get(0).child(1);
		int i = 0;
		if(!parent.child(i).isBlock()) {
			i = 1;
		}
		String solutionHtml = parent.child(i).html();
		solutionHtml = StringUtils.replace(solutionHtml, "<script type=\"math/tex\">", "$");
		solutionHtml = StringUtils.replace(solutionHtml, "</script>", "$");
		String solu = Jsoup.parse(solutionHtml).text();
		System.out.println("Solution:" + solu);
		return solu;
	}

	private static String addMathJax(Elements element) {
		if(element.isEmpty()) {
			return "";
		}
		String val = StringUtils.replace(element.html(), "<script type=\"math/tex\">", "$");
		val = StringUtils.replace(val, "</script>", "$");
		return val;
	}

	public static void main(String[] args) {
		/*String fileName = "F:\\Resoneuronance\\Edofox\\Document\\Latex\\VL CET 2018\\06 Math QP.tex";
		Integer previousQuestion = 100;
		Integer testId = null;
		
		System.out.println(parseQuestionPaper(fileName, previousQuestion, "F:\\Resoneuronance\\Edofox\\Document\\Latex\\VL CET 2018\\Solutions\\06 Math Sol.tex").size());
	*/	
		try {
			String html = IOUtils.toString(new FileInputStream("F:\\Resoneuronance\\Edofox\\Document\\sample_toppr_true_false.html"));
			EdoQuestion q = new EdoQuestion();
			q.setMetaData(html);
			parseHtml(q);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
