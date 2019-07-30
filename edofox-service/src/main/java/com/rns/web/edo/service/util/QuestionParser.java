package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.rns.web.edo.service.domain.EdoChapter;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.ext.ExtDataQuestion;
import com.rns.web.edo.service.domain.ext.ExtDataQuestionChoice;
import com.rns.web.edo.service.domain.ext.ExtDataRoot;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

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
		try {
			if(solution == null || solution.isEmpty() || solution.get(0).children() == null || solution.get(0).children().isEmpty()) {
				return "";
			}
			Element parent = solution.get(0).child(1);
			if(parent.children() == null || parent.children().isEmpty()) {
				return "";
			}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
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
		WebClient webClient = null;
		try {
			/*String url = "https://www.toppr.com/class-12/physics/measurement-and-errors/question-sets/all/?page=3";
			
			String loginUrl = "https://www.toppr.com/login/";
			
			webClient = new WebClient(BrowserVersion.FIREFOX_60);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			//webClient.getOptions().setJavaScriptEnabled(false);
			
			CookieManager manager = new CookieManager();
			
			addCookie(manager, ".toppr.com", "ajs_user_id");
			addCookie(manager, ".toppr.com", "intercom-session-sh7i09tg");
			addCookie(manager, ".toppr.com", "__cfduid");
			addCookie(manager, ".toppr.com", "_fbp");
			addCookie(manager, ".toppr.com", "_gid");
			addCookie(manager, ".toppr.com", "ajs_anonymous_id");
			addCookie(manager, ".toppr.com", "_ga");
			addCookie(manager, ".toppr.com", "session_id");
			addCookie(manager, "www.toppr.com", "NPS_45e50a70_throttle");
			addCookie(manager, "www.toppr.com", "NPS_45e50a70_last_seen");
			addCookie(manager, "www.toppr.com", "AWSALB");
			addCookie(manager, "www.toppr.com", "admin_sessionid");
			
			webClient.setCookieManager(manager);
			
			
			HtmlPage page = webClient.getPage(loginUrl);
			System.out.println("Adding local storage ..");
			addLocalStorage(page, "ajs_group_properties");
			addLocalStorage(page, "ajs_user_traits");
			addLocalStorage(page, "_WE_82618208");
			addLocalStorage(page, "local_ajs_anonymous_id");
			addLocalStorage(page, "ajs_user_id");
			addLocalStorage(page, "intercom-state");
			
			//page = webClient.getPage(url);
			
			System.out.println("Waiting for JS ....");
			webClient.waitForBackgroundJavaScript(10000);
			System.out.println(page.asText());
			
			

			<input placeholder="" name="countryPhone" id="countryPhone" maxlength="10" class="_1_zeR _3yQ8t input-hasError input input-hasError" type="tel" autocomplete="on" value=""/>
			<button class="_3UUnG button-smallHeight button button-shadow">

			 
			
			HtmlAnchor anchor = page.getAnchorByHref("/login/");
			HtmlPage postLogin = anchor.click();
			final String pageAsXml = postLogin.asXml();
			System.out.println(postLogin);
			
			
			HtmlTelInput phone = page.getHtmlElementById("countryPhone");
			phone.setText("9923283604");
			phone.setValueAttribute("9923283604");
			DomNodeList<DomElement> buttons = page.getElementsByTagName("button");
			if(!buttons.isEmpty()) {
				for(DomElement btn: buttons) {
					if(StringUtils.contains(btn.getAttribute("class"), "_3UUnG")) {
						System.out.println("Found login button " + btn.getAttribute("class"));
						WebWindow window = page.getEnclosingWindow();
						HtmlPage newPage = btn.click();
						System.out.println("Waiting for OTP .." + window.getEnclosedPage());
						webClient.waitForBackgroundJavaScript(10000);
						//System.out.println(newPage.asText());
						//page = (HtmlPage) window.getEnclosedPage();
						System.out.println(page.asText());
						break;
					}
				}
			}*/
			
			//getQuestions();
			
			bulkParse(1, 1, "F:\\Resoneuronance\\Edofox\\Document\\QuestionBank\\Physics\\measurement-and-error", "JEE");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(webClient != null) {
				webClient.close();
			}
		}
	}

	private static void addLocalStorage(final HtmlPage page, String key) {
		page.executeJavaScript("window.localStorage.setItem('" + key + "','" + EdoPropertyUtil.getProperty(key) + "');");
		System.out.println("Added local storage for " + key + " = " + EdoPropertyUtil.getProperty(key));
	}

	private static void addCookie(CookieManager manager, String domain, String key) {
		Cookie cookie = new Cookie(domain, key, EdoPropertyUtil.getProperty("ajs_user_id"));
		manager.addCookie(cookie);
	}
	
	public static void getQuestions() {
		String url = "https://www.toppr.com/api/v5.0/class-12/practice/physics/physical-world/question-bank/?page=1&type=single%20correct";
		
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);


		WebResource webResource = client.resource(url);

		/*
		 authority: www.toppr.com
:method: GET
:path: /api/v5.0/class-12/practice/physics/physical-world/question-bank/?page=4&type=single%20correct
:scheme: https
accept: application/json, text/plain, 
accept-encoding: gzip, deflate, br
accept-language: en-US,en;q=0.9
agent-platform: web
agent-platform-version: 4
cookie: __cfduid=d8425c513fca7e167df7fd8c36733c8211562568579; _ga=GA1.2.1805524713.1562568578; _gid=GA1.2.1447940675.1562568578; ajs_group_id=null; ajs_anonymous_id=%22febdab1d-0ed5-4b29-b50c-3189928394ee%22; _fbp=fb.1.1562568579520.1595014898; intercom-id-sh7i09tg=8533d373-7127-4429-8be5-55c34541221f; admin_sessionid=09bdab68bd918319fa7a3a5c7787558c; nextUrl=; sign_up_lead=; tracking_id=; NPS_45e50a70_last_seen=1562576744047; ajs_user_id=10254079; intercom-session-sh7i09tg=S1F1Y3ppRzIwNm52UTJJek1vNFJJSnNsenl6cExva0NNWXNScnJsMlBiUllhUDdtb3AzdElwQ3ljRjBjaWhhcS0tcDVkYUpNQyt4VWU0aHpkVWxwRjhrUT09--74452af66afeec402d3122af23e6e3cea8744af4; AWSALB=U3Apr0g3/gKwS9/MMfI9qSrSaOw/MaCtLndxGqYNF2Wa87qIcXNl1Xexu4qBaKmtunHKQkLjHiuVyEsddl3jRgKOod5tOpF1qNT9N8EFNpuc2YNpuhnKUBjJW03lvL9nLuQzPgJrMHjJ4O529l706cSeyzT+YtH7ofALoYkcfJ/MjKU7j0CxGGS5ioBPeA==; _gat_gtag_UA_42239720_1=1
referer: https://www.toppr.com/class-12/physics/physical-world/question-sets/all/?page=4&type=single%20correct
user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36
accept: application/json, text/plain, 
accept-encoding: gzip, deflate, br
accept-language: en-US,en;q=0.9
agent-platform: web
agent-platform-version: 4
		 */
		
		webResource.header(":authority", "www.toppr.com");
		webResource.header(":method", "GET");
		webResource.header(":path", "/api/v5.0/class-12/practice/physics/physical-world/question-bank/?page=4&type=single%20correct");
		webResource.header(":scheme", "https");
		webResource.header("cookie", "__cfduid=d8425c513fca7e167df7fd8c36733c8211562568579; _ga=GA1.2.1805524713.1562568578; _gid=GA1.2.1447940675.1562568578; ajs_group_id=null; ajs_anonymous_id=%22febdab1d-0ed5-4b29-b50c-3189928394ee%22; _fbp=fb.1.1562568579520.1595014898; intercom-id-sh7i09tg=8533d373-7127-4429-8be5-55c34541221f; admin_sessionid=09bdab68bd918319fa7a3a5c7787558c; nextUrl=; sign_up_lead=; tracking_id=; NPS_45e50a70_last_seen=1562576744047; ajs_user_id=10254079; intercom-session-sh7i09tg=S1F1Y3ppRzIwNm52UTJJek1vNFJJSnNsenl6cExva0NNWXNScnJsMlBiUllhUDdtb3AzdElwQ3ljRjBjaWhhcS0tcDVkYUpNQyt4VWU0aHpkVWxwRjhrUT09--74452af66afeec402d3122af23e6e3cea8744af4; AWSALB=U3Apr0g3/gKwS9/MMfI9qSrSaOw/MaCtLndxGqYNF2Wa87qIcXNl1Xexu4qBaKmtunHKQkLjHiuVyEsddl3jRgKOod5tOpF1qNT9N8EFNpuc2YNpuhnKUBjJW03lvL9nLuQzPgJrMHjJ4O529l706cSeyzT+YtH7ofALoYkcfJ/MjKU7j0CxGGS5ioBPeA=="); //_gat_gtag_UA_42239720_1=1
		webResource.header("referer", "https://www.toppr.com/class-12/physics/physical-world/question-sets/all/?page=4&type=single%20correct");
		webResource.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
		webResource.header("agent-platform", "web");
		webResource.header("agent-platform-version", "4");
		javax.ws.rs.core.Cookie cookie = new javax.ws.rs.core.Cookie("AWSALB", "OsjNLNEeTJXQj");
		webResource.cookie(cookie);
		
		ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

		if (response.getStatus() != 200) {
			System.out.println("Failed : HTTP error code : " + response.getStatus() + " message " + response);
		}
		String output = response.getEntity(String.class);
		LoggingUtil.logMessage("Output from Server for get questions : " + response.getStatus() + ".... \n " + output);
	
		
		//"question_style""single correct" true-false multiple%20correct blank assertion matrix passage true-false

		//
	}
	
	public static List<EdoQuestion> bulkParse(Integer chapterId, Integer subjectId, String path, String exam) throws JsonParseException, JsonMappingException, IOException {
		
		File folder = new File(path);
		List<EdoQuestion> questions = new ArrayList<EdoQuestion>();
		FileInputStream in = null;
		
		try {
			for(File file: folder.listFiles()) {
				System.out.println("Reading file at .. " + file.getAbsolutePath());
				in = new FileInputStream(file);
				String json = IOUtils.toString(in);
				//System.out.println(json);
				ExtDataRoot data = new ObjectMapper().readValue(json, ExtDataRoot.class);
				if(data.getData() != null) {
					if(CollectionUtils.isEmpty(data.getData().getQuestions())) {
						System.out.println("No questions found!!!");
					} else {
						for(ExtDataQuestion q: data.getData().getQuestions()) {
							System.out.println(q);
							if(StringUtils.isBlank(q.getQuestion_style()) || questionStyles.get(q.getQuestion_style()) == null) {
								System.out.println("Skipping ... " + q.getQuestion_style());
								continue;
							}
							
							if(CollectionUtils.isNotEmpty(q.getPassage_child_questions())) {
								//Add multiple questions
								for(ExtDataQuestion subQ: q.getPassage_child_questions()) {
									EdoQuestion edoQuestion = prepareQuestion(chapterId, subjectId, subQ, exam);
									if(edoQuestion != null) {
										edoQuestion.setQuestion(Jsoup.parse(q.getQuestion()).text() + "\n" +  edoQuestion.getQuestion());
										edoQuestion.setType("PASSAGE");
										questions.add(edoQuestion);
									}
								}
							} else if (CollectionUtils.isNotEmpty(q.getChoices()) || StringUtils.equals("blank", q.getQuestion_style())) {
								EdoQuestion edoQuestion = prepareQuestion(chapterId, subjectId, q, exam);
								if(edoQuestion != null) {
									questions.add(edoQuestion);
								}
							}
							
						}
					}
				} else {
					System.out.println("............. Not found ..........");
				}
			}
			System.out.println("................... Total questions added " + questions.size());
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				in.close();
			}
		}
		return questions;
	}

	private static EdoQuestion prepareQuestion(Integer chapterId, Integer subjectId, ExtDataQuestion q, String exam) {
		EdoQuestion edoQuestion = new EdoQuestion();
		edoQuestion.setType(questionStyles.get(q.getQuestion_style()));
		if(edoQuestion.getType() == null) {
			return null;
		}
		edoQuestion.setQuestion(escapeQuotes(Jsoup.parse(q.getQuestion()).text()));
		setOptionAndCorrectAnswer(q, edoQuestion);
		edoQuestion.setSolution(escapeQuotes(Jsoup.parse(q.getSolution()).text()));
		edoQuestion.setSolutionImageUrl(q.getSolution_image());
		edoQuestion.setQuestionImageUrl(q.getQuestion_image());
		//downloadFile(edoQuestion.getQuestionImgeUrl(),  "question.png");
		//downloadFile(edoQuestion.getSolutionImageUrl(), "solution.png");
		EdoChapter chapter = new EdoChapter();
		chapter.setChapterId(chapterId);
		edoQuestion.setChapter(chapter);
		edoQuestion.setSubjectId(subjectId);
		edoQuestion.setLevel(q.getQuestion_level());
		edoQuestion.setReferenceId(q.getQuestion_id());
		edoQuestion.setExamType(exam);
		if(StringUtils.contains(exam, "JEE") || StringUtils.equals("NEET", exam)) {
			edoQuestion.setWeightage(4f);
			edoQuestion.setNegativeMarks(1f);
		}
		if(StringUtils.contains(exam, "CET")) {
			if(subjectId == 2) {
				edoQuestion.setWeightage(2f);
			}
			edoQuestion.setWeightage(1f);
			edoQuestion.setNegativeMarks(0f);
		}
		if(StringUtils.equals("assertion", q.getQuestion_style()) && StringUtils.isBlank(edoQuestion.getQuestion())) {
			edoQuestion.setQuestion(escapeQuotes(Jsoup.parse((q.getAssertion() + "\n" + q.getReason())).text()));
		}
		return edoQuestion;
	}


	public static String escapeQuotes(String text) {
		if(StringUtils.isBlank(text)) {
			return text;
		}
		return StringUtils.replace(text, "'", "''");
	}

	private static void setOptionAndCorrectAnswer(ExtDataQuestion q, EdoQuestion question) {
		if(StringUtils.equals("matrix", q.getQuestion_style())) {
			StringBuilder questionBuilder = new StringBuilder();
			questionBuilder.append("\n").append("List 1 :").append("\n");
			StringBuilder builder = new StringBuilder();
			if(CollectionUtils.isNotEmpty(q.getMx_l1())) {
				int x = 'a';
				for(String op : q.getMx_l1()) {
					char[] ch = Character.toChars(x);
					builder.append(ch).append(",");
					questionBuilder.append(ch[0] + ". " + op).append("\n");
					x++;
				}
				question.setOption1(StringUtils.removeEnd(builder.toString(), ","));
			}
			questionBuilder.append("List 2 :").append("\n");
			builder = new StringBuilder();
			if(CollectionUtils.isNotEmpty(q.getMx_l2())) {
				for(int i = 1; i <= q.getMx_l2().size(); i++) {
					builder.append(i).append(",");
					questionBuilder.append(i + ". " + q.getMx_l2().get(i - 1)).append("\n");
				}
				question.setOption2(StringUtils.removeEnd(builder.toString(), ","));
			}
			question.setQuestion(question.getQuestion() + escapeQuotes(Jsoup.parse(questionBuilder.toString()).text()));
			if(CollectionUtils.isNotEmpty(q.getChoices())) {
				String correctAnswers = q.getChoices().get(0).getChoice();
				if(StringUtils.isNotBlank(correctAnswers)) {
					String[] corrects = StringUtils.split(correctAnswers, ",");
					if(ArrayUtils.isNotEmpty(corrects)) {
						Arrays.sort(corrects, new Comparator<String>() {
					        public int compare(String o1, String o2) {
					            return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
					        }
					    });
						StringBuilder correctAnswer = new StringBuilder();
						int x = 'a';
						for(String correct: corrects) {
							int correctVal = Integer.valueOf(correct) + 1;
							correctVal = correctVal % q.getMx_l2().size();
							if(correctVal == 0) {
								correctVal = q.getMx_l2().size();
							}
							correctAnswer.append(Character.toChars(x)).append("-").append(correctVal).append(",");
							x++;
						}
						question.setCorrectAnswer(StringUtils.removeEnd(correctAnswer.toString(), ","));
					}
				}
				
			}
		} else {
			if(CollectionUtils.isNotEmpty(q.getChoices())) {
				int i = 1;
				StringBuilder correctAnswer = new StringBuilder();
				for(ExtDataQuestionChoice choice: q.getChoices()) {
					String choiceValue = escapeQuotes(choice.getChoice());
					if(i == 1) {
						question.setOption1(choiceValue);
						question.setOption1ImageUrl(choice.getImage());
						if(choice.isIs_right() && !StringUtils.equals("blank", q.getQuestion_style())) {
							correctAnswer.append("option1").append(",");
						}
					} else if(i == 2) {
						question.setOption2(choiceValue);
						question.setOption2ImageUrl(choice.getImage());
						if(choice.isIs_right() && !StringUtils.equals("blank", q.getQuestion_style())) {
							correctAnswer.append("option2").append(",");
						} 
					} else if(i == 3) {
						question.setOption3(choiceValue);
						question.setOption3ImageUrl(choice.getImage());
						if(choice.isIs_right() && !StringUtils.equals("blank", q.getQuestion_style())) {
							correctAnswer.append("option3").append(",");
						}
					} else if(i == 4) {
						question.setOption4(choiceValue);
						question.setOption4ImageUrl(choice.getImage());
						if(choice.isIs_right() && !StringUtils.equals("blank", q.getQuestion_style())) {
							correctAnswer.append("option4").append(",");
						}
					}
					if(choice.isIs_right() && StringUtils.equals("blank", q.getQuestion_style())) {
						correctAnswer.append(choiceValue);
					}
					i++;
				}
				question.setCorrectAnswer(StringUtils.removeEnd(correctAnswer.toString(), ","));
			}
		}
		
	}
	
	public static String downloadFile(String url, String fileName, Integer questionId) throws FileNotFoundException, IOException {
		InputStream in = null;
		FileOutputStream os = null;
		try {
			ClientConfig config = new DefaultClientConfig();
			config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			Client client = Client.create(config);

			WebResource webResource = client.resource(url);
			ClientResponse resp = webResource.get(ClientResponse.class);
			in = resp.getEntityInputStream();
			String filePath = "";
			if (in != null) {
				filePath = EdoConstants.Q_BANK_PATH + questionId + "_" + fileName;
				os = new FileOutputStream(filePath);
				IOUtils.copy(in, os);
			}
			return filePath;
		
		} catch (Exception e) {
			System.out.println("Error downloading file ..");
		} finally {
			if(in != null) {
				in.close();
			}
			if(os != null) {
				os.close();
			}
		}
		return null;
	}
	
	public static void fixQuestion(EdoQuestion question) {
		if(StringUtils.isBlank(question.getQuestion())) {
			return;
		}
		boolean isLatex = false;
		if(StringUtils.contains(question.getQuestion(), "$$")) {
			isLatex = true;
		}
		if(StringUtils.contains(question.getOption1(), "$$")) {
			isLatex = true;
		}
		if(StringUtils.contains(question.getOption2(), "$$")) {
			isLatex = true;
		}
		if(StringUtils.contains(question.getOption3(), "$$")) {
			isLatex = true;
		}
		if(StringUtils.contains(question.getOption4(), "$$")) {
			isLatex = true;
		}
		if(StringUtils.contains(question.getSolution(), "$$")) {
			isLatex = true;
		}
		if(!isLatex) {
			return;
		}
		for(String key: KEYWORDS) {
			String fixedValue = fixValue(question.getQuestion(), key);
			question.setQuestion(fixedValue != null ? fixedValue: question.getQuestion());
			
			fixedValue = fixValue(question.getOption1(), key);
			question.setOption1(fixedValue != null ? fixedValue: question.getOption1());
			
			fixedValue = fixValue(question.getOption2(), key);
			question.setOption2(fixedValue != null ? fixedValue: question.getOption2());
			
			fixedValue = fixValue(question.getOption3(), key);
			question.setOption3(fixedValue != null ? fixedValue: question.getOption3());
			
			fixedValue = fixValue(question.getOption4(), key);
			question.setOption4(fixedValue != null ? fixedValue: question.getOption4());
			
			fixedValue = fixValue(question.getSolution(), key);
			question.setSolution(fixedValue != null ? fixedValue: question.getSolution());
		}
	}

	private static String fixValue(String string, String key) {
		if(!StringUtils.contains(string, "$$")) {
			return StringUtils.remove(string, "'");
		}
		if(!StringUtils.contains(string, key)) {
			return StringUtils.remove(string, "'");
		}
		int startIndex = 0;
		int startOfLatex = 0;
		StringBuilder builder = new StringBuilder();
		while (startOfLatex >= 0) {
			startOfLatex = StringUtils.indexOf(string, "$$", startIndex);
			if(startOfLatex < 0) {
				break;
			}
			builder.append(StringUtils.substring(string, startIndex, startOfLatex));
			int endOfLatex = StringUtils.indexOf(string, "$$", startOfLatex + 2);
			if(endOfLatex > 0) {
				String s = StringUtils.substring(string, startOfLatex, endOfLatex);
				if(StringUtils.isNotBlank(s)) {
					String temp = "";
					if(StringUtils.equals("imes", key)) {
						temp = "t";
					} else if (StringUtils.equals("ightarrow", key) || StringUtils.equals("ight", key)) {
						temp = "r";
					}  else if (StringUtils.equals("frac", key)) {
						if(StringUtils.contains(s, "dfrac")) {
							s = StringUtils.replace(s, "dfrac", "frac");
						}
					}
					String newString = StringUtils.replace(s, key, "\\" + temp + key);
					builder.append(newString);
				}
				builder.append("$$");
				startIndex = endOfLatex + 2;
			} else {
				break;
			}
			
		}
		builder.append(StringUtils.substring(string, startIndex));
		if(!StringUtils.equals(string, builder)) {
			System.out.println("New: " + builder + " Old: " + string);
		}
		return StringUtils.remove(builder.toString(), "'");
	}

	static Map<String, String> questionStyles = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("single correct", "SINGLE");
			put("true-false", "SINGLE");
			put("multiple correct", "MULTIPLE");
			put("blank", "NUMBER");
			put("assertion", "SINGLE");
			put("matrix", "MATCH");
			put("passage", "SINGLE");
		}
	});
	
	public static String[] KEYWORDS = new String [] {"hat", "displaystyle", "dfrac", "frac", "vec","sqrt","circ","imes","mathrm","overrightarrow","pi","left",
			"overline", "widehat", "alpha", "beta", "gamma", "theta", "delta", "eta", "epsilon" , "leq", "geq", "ightarrow", "right", "ight", "harpoons",
			"overset", "underset", "dot", "Delta", "Alpha", "Beta", "Gamma", "Eta", "lambda", "int", "ge", "le", "quad", "ne"};
	
}
