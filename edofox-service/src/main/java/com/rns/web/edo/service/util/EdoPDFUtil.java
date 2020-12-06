package com.rns.web.edo.service.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;

import com.rns.web.edo.service.domain.EdoAdminRequest;
import com.rns.web.edo.service.domain.EdoQuestion;
import com.rns.web.edo.service.domain.ext.EdoPDFCoordinate;

public class EdoPDFUtil {

	public static final String QUESTION_PREFIX = "Q-";
	public static final String SOLUTION_PREFIX = "S-";
	private static final String PATH = "F:\\Resoneuronance\\Edofox\\Document\\Customers\\Athavale classes\\12 Jan\\12th.pdf";
	private static final String OUT = "F:\\Resoneuronance\\Edofox\\Document\\Customers\\Athavale classes\\12 Jan\\12th\\";
	//public static List<Float> yPositions = new ArrayList<Float>();
	//public static List<Float> xPositions = new ArrayList<Float>();
	
	
	/*public static void main(String[] args) throws IOException, SAXException, DocumentException {

		pdfBox(")", new FileInputStream(PATH), OUT, 10);
		// asposePdf();
		// poiPdf();
		//itextPdf();
	}*/

	
	//95.621284 - 759.48
	//95.621284 - 685.54
	
	public static List<EdoQuestion> pdfBox(final EdoAdminRequest request, InputStream is, String outputFolder/*, int buffer, int testId, final Integer from, final Integer to*/) {
		try {
			//String questionNumberSuffix = ".";
			List<EdoQuestion> parsedQuestions = new ArrayList<EdoQuestion>();
			PDDocument document = PDDocument.load(is);

			if (!document.isEncrypted()) {

				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);

				//156.37
				
				PDFTextStripper tStripper = new PDFTextStripper();
				// System.out.println(tStripper.getEndPage());
				final Map<PDPage, List<EdoPDFCoordinate>> coordinates = new HashMap<PDPage, List<EdoPDFCoordinate>>();

				PDFTextStripper customStripper = new PDFTextStripper() {
					int questionCount = 1;

					PDPage pg;
					EdoPDFCoordinate coord = null;
					
					
					@Override
					protected void startPage(PDPage page) throws IOException {
						startOfLine = true;
						this.pg = page;
						System.out.println(page);
						super.startPage(page);
					}

					@Override
					protected void writeLineSeparator() throws IOException {
						startOfLine = true;
						super.writeLineSeparator();
					}

					@Override
					protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
						if (startOfLine) {
							TextPosition firstProsition = textPositions.get(0); //
							//Save the first whitespace for reference so that last question in the page can be correctly cut
							if(StringUtils.isBlank(text)) {
								List<EdoPDFCoordinate> coordList = coordinates.get(pg);
								if(CollectionUtils.isNotEmpty(coordList)) {
									EdoPDFCoordinate lastCoordinate = coordList.get(coordList.size() - 1);
									if(lastCoordinate.getWhiteSpaceY() == null)  {
										lastCoordinate.setWhiteSpaceY(firstProsition.getEndY());
										System.out.println("Setting white space as " + firstProsition.getEndY());
									}
								}
							} else if (StringUtils.contains(text, request.getQuestionPrefix() + questionCount + request.getQuestionSuffix())) {
								//if(withinRange()) {
									
									//Find start X based on question suffix/prefix
									if(StringUtils.isNotBlank(request.getQuestionSuffix())) {
										for(TextPosition position: textPositions) {
											if(position.toString().equals(request.getQuestionSuffix())) {
												firstProsition = position;
												break;
											}
										}
									} else {
										for(TextPosition position: textPositions) {
											if(position.toString().equals(new Integer(questionCount).toString())) {
												firstProsition = position;
												break;
											}
										}
									}
								
									float width = firstProsition.getPageWidth();
									//If width cropping option is selected
									if(StringUtils.equals("true", request.getCropWidth())) {
										//Width of the line is last character X of the first line - first X
										TextPosition lastCharacter = textPositions.get(textPositions.size() - 1);
										width = (lastCharacter.getEndX() + lastCharacter.getWidth()) - firstProsition.getEndX();
										
									}
									
									coord = new EdoPDFCoordinate(firstProsition.getEndX(), firstProsition.getEndY(), firstProsition.getHeight(), width, questionCount);
									//coord.setHeight(firstProsition.getHeight());
									List<EdoPDFCoordinate> coordList = coordinates.get(pg);
									if(coordList == null) {
										coordList = new ArrayList<EdoPDFCoordinate>();
									}
									coordList.add(coord);
									//coord.setPage(pg);
									coordinates.put(pg, coordList);
									System.out.println(questionCount + ":" + coord.getX() + " - " + coord.getY() + " width " + coord.getHeight() + " pg w " + coord.getWidth() +  " for " + text);
								//}
								questionCount++;
							} else if (StringUtils.contains(text, ":Solution:")) {
								coord.setSolutionY(firstProsition.getEndY());
								coord.setWhiteSpaceY(null);
								System.out.println("Solution found at " + firstProsition.getEndY());
							} else if (coord != null) {
								coord.setLastTextY(firstProsition.getEndY());
								//Update width if it's greater than current width
								TextPosition lastCharacter = textPositions.get(textPositions.size() - 1);
								float width = lastCharacter.getEndX() - firstProsition.getEndX();
								if(coord.getWidth() < width) {
									coord.setWidth(width);
								}
							}
							//writeString(String.format("[%s]", firstProsition.getXDirAdj()));
							startOfLine = false;
						}
						super.writeString(text, textPositions);
					}

					boolean startOfLine = true;
				};

				String text = customStripper.getText(document);
				//System.out.println(xPositions + ":" + yPositions);

				String pdfFileInText = tStripper.getText(document);
				// System.out.println("Text:" + st);

				// split by whitespace
				String lines[] = pdfFileInText.split("\\r?\\n");
				for (String line : lines) {
					System.out.println(line);
				}

				/*for(PDPage page: document.getPages()) {
					
				}*/
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				for(int pgNo = 0; pgNo < document.getNumberOfPages(); pgNo++) {
					PDPage page = document.getPage(pgNo);
					List<EdoPDFCoordinate> list = coordinates.get(page);
					if(CollectionUtils.isEmpty(list)) {
						System.out.println("Page empty .....");
						continue;
					}
					for(int i =0; i < list.size(); i ++) {
						if(i == 0 && list.size() > 1) {
							continue;
						}
						EdoPDFCoordinate edoPDFCoordinate = list.get(i);
						
						if(i > 0) {
							// suffix in filename will be used as the file format
						    EdoPDFCoordinate prevCoordinate = list.get(i - 1);
							Integer questionNumber = prevCoordinate.getQuestionNumber();
						    if(!withinRange(request.getFromQuestion(), request.getToQuestion(), questionNumber)) {
						    	continue;
						    }
						    //int buffer = 10;
							float y = edoPDFCoordinate.getY() + edoPDFCoordinate.getHeight() + request.getBuffer();
							float width = prevCoordinate.getWidth();
							float height = prevCoordinate.getY() - edoPDFCoordinate.getY() + request.getBuffer();
							Float x = edoPDFCoordinate.getX();
							Float solutionY = prevCoordinate.getSolutionY();
							//If solution is present at the bottom of the question
							if(solutionY != null) {
								height = prevCoordinate.getY() - solutionY + request.getBuffer();
								solutionY = solutionY + edoPDFCoordinate.getHeight() + request.getBuffer();
								float solutionHeight = solutionY - edoPDFCoordinate.getY() + request.getBuffer();
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, y, width, solutionHeight, questionNumber, x, SOLUTION_PREFIX);
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, solutionY, width, height, questionNumber, x, QUESTION_PREFIX);
							} else {
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, y, width, height, questionNumber, x, QUESTION_PREFIX);
							}
							
							EdoQuestion question = new EdoQuestion();
							question.setQuestionNumber(questionNumber);
							//Timestamp added to avoid caching
							question.setQuestionImageUrl(getQuestionUrl(request.getTest().getId(), questionNumber, EdoConstants.ATTR_TEMP_QUESTION) + "?" + System.currentTimeMillis());
							if(solutionY != null) {
								question.setSolutionImageUrl(getQuestionUrl(request.getTest().getId(), questionNumber, EdoConstants.ATTR_TEMP_SOLUTION) + "?" + System.currentTimeMillis());
							}
							parsedQuestions.add(question);	
						}
						
						if(i == list.size() - 1) {
							if(!withinRange(request.getFromQuestion(), request.getToQuestion(), list.get(i).getQuestionNumber())) {
								continue;
							}
							
							float startY = 0;
							if(edoPDFCoordinate.getWhiteSpaceY() != null) {
								if(edoPDFCoordinate.getLastTextY() != null && edoPDFCoordinate.getLastTextY() < edoPDFCoordinate.getWhiteSpaceY()) {
									startY = edoPDFCoordinate.getLastTextY();
								} else {
									startY = edoPDFCoordinate.getWhiteSpaceY();
								}
								
							} else if (edoPDFCoordinate.getLastTextY() != null) {
								startY = edoPDFCoordinate.getLastTextY();
							}
							//for last item
							float height = edoPDFCoordinate.getY() - startY + edoPDFCoordinate.getHeight() + request.getBuffer();
							
							if(startY > request.getBuffer()) {
								startY = startY - request.getBuffer(); //Add some buffer to starting Y as its last line of PDF
								height = height + request.getBuffer();
							}
							
							//If solution is present at the bottom of the question
							Float solutionY = edoPDFCoordinate.getSolutionY();
							if(solutionY != null) {
								if(solutionY < startY) {
									startY = 0;
								}
								height = edoPDFCoordinate.getY() - solutionY + edoPDFCoordinate.getHeight() + request.getBuffer();
								solutionY = solutionY + edoPDFCoordinate.getHeight() + request.getBuffer();
								float solutionHeight = solutionY - startY + request.getBuffer();
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, startY, edoPDFCoordinate.getWidth(), solutionHeight, list.get(i).getQuestionNumber(), list.get(i).getX(), SOLUTION_PREFIX);
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, solutionY, edoPDFCoordinate.getWidth(), height, list.get(i).getQuestionNumber(), list.get(i).getX(), QUESTION_PREFIX);
							} else {
								cropQuestion(outputFolder, pdfRenderer, pgNo, page, list, i, startY, edoPDFCoordinate.getWidth(), height, list.get(i).getQuestionNumber(), list.get(i).getX(), QUESTION_PREFIX);
								
							}
							
							EdoQuestion lastQuestion = new EdoQuestion();
							lastQuestion.setQuestionNumber(list.get(i).getQuestionNumber());
							lastQuestion.setQuestionImageUrl(getQuestionUrl(request.getTest().getId(), lastQuestion.getQuestionNumber(), EdoConstants.ATTR_TEMP_QUESTION) + "?" + System.currentTimeMillis());
							if(solutionY != null) {
								lastQuestion.setSolutionImageUrl(getQuestionUrl(request.getTest().getId(), lastQuestion.getQuestionNumber(), EdoConstants.ATTR_TEMP_SOLUTION) + "?" + System.currentTimeMillis());
							}
							parsedQuestions.add(lastQuestion);
							
							/*page.setCropBox(new PDRectangle(0, 0, , ));
							//document.save(OUT + "Test_new.pdf");
							BufferedImage bim2 = pdfRenderer.renderImageWithDPI(pgNo, 300, ImageType.RGB);
						    // suffix in filename will be used as the file format
							ImageIOUtil.writeImage(bim2, outputFolder + "Q" + "-" + list.get(i).getQuestionNumber() + ".png", 300);*/
							//System.out.println("Added last question number " + list.get(i).getQuestionNumber());
						}
						//BufferedImage bim = page.convertToImage(BufferedImage.TYPE_INT_RGB, 300);
					    //ImageIOUtil.writeImage(bim, /*pdfFilename +*/ "Q" + edoPDFCoordinate.getQuestionNumber() + ".png", 300);
					}
				}
				
				
				
				/*PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND,true,true); 
				PDFont pdfFont= PDType1Font.HELVETICA_BOLD;
				int fontSize = 14;
				contentStream.beginText();
				contentStream.setFont(pdfFont, fontSize);
				contentStream.newLineAtOffset(95.621284f, 685.54f);
				//contentStream.newLineAtOffset(95.621284f, 686.54f);
				//contentStream.newLineAtOffset(95.621284f, 687.54f);
				contentStream.showText("\\n");
				//contentStream.drawLine(xPositions.get(0), yPositions.get(0), xPositions.get(0) + 50, yPositions.get(0));
				contentStream.endText();
				System.out.println("Done adding ..");
				contentStream.close();
				// Saving the document
				document.save(new File("F:\\Resoneuronance\\Edofox\\Document\\Customers\\Ignitis\\Test_new.pdf"));
*/

				//PDPageContentStream contentStream = new PDPageContentStream(document, page);

				// Begin the Content stream contentStream.beginText();

				// Setting the font to the Content stream
				// contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);

				// Setting the position for the line
				//contentStream.newLineAtOffset(25, 500);

				// String text = "\\n";

				// Adding text in the form of string
				// contentStream.showText(text);

				// Ending the content stream contentStream.endText();

				System.out.println("Content added");

				// Closing the content stream contentStream.close();

				// Closing the document document.close();

			}
			return parsedQuestions;

		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}
	
	private static boolean withinRange(Integer from, Integer to, Integer questionNumber) {
		if(from != null && from > questionNumber) {
			return false;
		}
		if(to != null && (to) < questionNumber) {
			return false;
		}
		return true;
	}


	public static String getQuestionUrl(int testId, Integer questionNumber, String type) {
		return EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_URL) + "getTempImage/" + testId + "/" + questionNumber + "/" + type;
	}

	private static BufferedImage cropQuestion(String outputFolder, PDFRenderer pdfRenderer, int pgNo, PDPage page, List<EdoPDFCoordinate> list, int i,
			float y, float width, float height, Integer questionNumber, Float x, String type) throws IOException {
		
		FileOutputStream fos = null;
		ImageOutputStream ios = null;
		try {
			float xCoord = 0;
			if(x != null) {
				xCoord = x;
			}
			page.setCropBox(new PDRectangle(xCoord, y, width, height));
			//document.save(OUT + "Test_new.pdf");
			BufferedImage bim = pdfRenderer.renderImageWithDPI(pgNo, 300, ImageType.RGB);
			File output = new File(outputFolder);
			if(!output.exists()) {
				output.mkdirs();
			}
			
			//ImageIOUtil.writeImage(bim, outputFolder + QUESTION_PREFIX + questionNumber + ".png", 300);
		
			//Compress image before saving
			ImageWriter writer =  ImageIO.getImageWritersByFormatName("jpg").next();
	        fos = new FileOutputStream(outputFolder + type + questionNumber + ".png");
			ios = ImageIO.createImageOutputStream(fos);
	        writer.setOutput(ios);

	        ImageWriteParam param = writer.getDefaultWriteParam();
	        if (param.canWriteCompressed()){
	            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	            param.setCompressionQuality(0.05f);
	        }

	        writer.write(null, new IIOImage(bim, null, null), param);
			
			System.out.println("Added question number " + questionNumber + " with crop matrix ==> origin " + y + " height " + height);
			return bim;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(ios != null) {
				ios.close();
			}
			if(fos != null) {
				fos.close();
			}
			
		}
		
		return null;
	}
	
	
	/*public static void poiPdf() throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(new File(PATH));

		ParseContext pcontext = new ParseContext();

		// parsing the document using PDF parser
		PDFParser pdfparser = new PDFParser();
		pdfparser.parse(inputstream, handler, metadata, pcontext);

		// getting the content of the document
		System.out.println("Contents of the PDF :" + handler.toString());
		// getting metadata of the document
		System.out.println("Metadata of the PDF:");
		String[] metadataNames = metadata.names();

		for (String name : metadataNames) {
			System.out.println(name + " : " + metadata.get(name));
		}
	}

	public static void itextPdf() throws IOException, DocumentException, PDFException {
		com.itextpdf.text.pdf.PdfReader pdfReader = new com.itextpdf.text.pdf.PdfReader(PATH);
		PdfDocument pdfDoc = new PdfDocument(pdfReader, new PdfWriter(OUT));
		PdfPage page = pdfDoc.getPage(1);
		PdfName key = new PdfName("/Filter");
		ITextExtractionStrategy strategy = new MyExtractionStrategy();
		PdfTextExtractor.getTextFromPage(page, strategy);
		// add content
		// System.out.println(pdfDoc.getFirstPage().);
		pdfDoc.close();

		PdfRectangle rect = new PdfRectangle(55, 0, 1000, 1000);
		com.itextpdf.text.pdf.PdfDictionary pageDict;
		for (int curentPage = 2; curentPage <= pdfReader.getNumberOfPages(); curentPage++) {
		    pageDict = pdfReader.getPageN(curentPage);
		    pageDict.put(com.itextpdf.text.pdf.PdfName.CROPBOX, rect);
		}
		
		//com.itextpdf.text.pdf.PdfDocument pdfDoc = new com.itextpdf.text.pdf.PdfDocument();
		//pdfReader.getdoc
		//com.itextpdf.text.pdf.PdfPage page = pdfReader.
		
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(PATH), new PdfWriter(OUT));
		PdfPage page = pdfDoc.getPage(1);
		//Rectangle2D.Double area = new Rectangle2D.Double(0, 0, 500, 500);
		
		Rectangle rect = new Rectangle(95, 156, 500, 500);
		page.setCropBox(rect);
		 
		// save the cropped document
		//pdfDoc.add
		pdfDoc.close();
		
		//JPDF

		// Open PDF file
		PDFDocument pdfDoc = new PDFDocument(PATH, null);

		// Get first page
		PDFPage page = pdfDoc.getPage(0);

		// Define the new crop box, which is a rectangle that is used to crop content
		// before displaying or printing the page. This rectangle is in PDF native
		// coordinates starting at the bottom left and increasing up to the right.
		// The dimensions are given in PostScript points.
		// 1 inch = 72 points, 1cm = 28.3465 points, 1mm = 2.8346 points
		// width in points
		double crop_width = 500;
		// height in points
		double crop_height = 300;
		// bottom left corner coordinates in points
		double x_1 = 90;
		double y_1 = 685.54 + 5.6;
		java.awt.geom.Rectangle2D.Double area = new java.awt.geom.Rectangle2D.Double(x_1, y_1, crop_width, crop_height);

		// set the new crop box
		page.setCropBox(area);

		// save the cropped document
		pdfDoc.saveDocument(OUT);
		
	}*/

}
