package com.rns.web.edo.service.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.rns.web.edo.service.domain.EdoStudent;

public class EdoReportUtil {
	
	/*public static InputStream getSalarySlip(ERPUser employee) {
		try {
		    String contentPath = "report/salary_slip.html";
			String result = CommonUtils.readFile(contentPath);
			String cssString = CommonUtils.readFile("report/report.css");
			if (employee != null) {
				result = StringUtils.replace(result, "{name}", CommonUtils.getStringValue(employee.getName()));
				result = StringUtils.replace(result, "{employeeId}", CommonUtils.getStringValue(employee.getRegId()));
				result = StringUtils.replace(result, "{email}", CommonUtils.getStringValue(employee.getEmail()));
				result = StringUtils.replace(result, "{phone}", CommonUtils.getStringValue(employee.getPhone()));
				result = StringUtils.replace(result, "{designation}", CommonUtils.getStringValue(employee.getDesignation()));
				if(employee.getCompany() != null) {
					result = StringUtils.replace(result, "{companyName}", CommonUtils.getStringValue(employee.getCompany().getName()));
					if(employee.getCompany().getFilter() != null) {
						result = StringUtils.replace(result, "{year}", CommonUtils.getStringValue(employee.getCompany().getFilter().getYear()));
						result = StringUtils.replace(result, "{month}", CommonUtils.getStringValue(new DateFormatSymbols().getMonths()[employee.getCompany().getFilter().getMonth()]));
					}
				}
				result = setEmployeeFinancial(employee, result);
			}
			InputStream is = generatePdf(result, cssString);
		    return is;
		    //file.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	}
	*/
	

	public static InputStream generatePdf(String result, String cssString) throws DocumentException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//OutputStream file = new FileOutputStream(new File("Test.pdf"));
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
		document.open();
		//XMLWorkerHelper.getInstance().parseXHtml(writer, document, new StringReader(result));
		//HTMLWorker htmlWorker = new HTMLWorker(document);
		//htmlWorker.parse(new StringReader(result));
		
		CSSResolver cssResolver = new StyleAttrCSSResolver();
		CssFile cssFile = XMLWorkerHelper.getCSS(new ByteArrayInputStream(cssString.getBytes()));
		cssResolver.addCss(cssFile);
		
		// HTML
		HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
 
		// Pipelines
		PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
		HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
		CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);
 
		// XML Worker
		XMLWorker worker = new XMLWorker(css, true);
		XMLParser p = new XMLParser(worker);
		p.parse(new ByteArrayInputStream(result.getBytes()));
		
		document.close();
		InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		return is;
	}
	
	public static void main(String[] args) throws DocumentException, IOException {
		String contentPath = "report/salary_slip.html";
		String result = CommonUtils.readFile(contentPath);
		String cssString = CommonUtils.readFile("report/report.css");
		generatePdf(result, cssString);
	}

	public static String prepareStudentReport(EdoStudent student, String result) {
		if(StringUtils.isNotBlank(result) && student != null) {
			result = StringUtils.replace(result, "{name}", CommonUtils.getStringValue(student.getName()));
			result = StringUtils.replace(result, "{registrationNo}", CommonUtils.getStringValue(student.getRegistrationNo()));
			result = StringUtils.replace(result, "{username}", CommonUtils.getStringValue(student.getUsername()));
			result = StringUtils.replace(result, "{mobileNo}", CommonUtils.getStringValue(student.getPhone()));
			result = StringUtils.replace(result, "{createdAt}", CommonUtils.convertDate(student.getCreatedAt(), "dd-MM-yyyy HH:mm:ss"));
			result = StringUtils.replace(result, "{profilePic}", CommonUtils.getStringValue(student.getProfilePic()));
			return result;
		}
		return null;
	}

}
