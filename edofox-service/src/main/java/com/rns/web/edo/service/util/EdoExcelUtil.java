package com.rns.web.edo.service.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rns.web.edo.service.domain.EDOInstitute;
import com.rns.web.edo.service.domain.EDOPackage;
import com.rns.web.edo.service.domain.EdoPaymentStatus;
import com.rns.web.edo.service.domain.EdoStudent;

public class EdoExcelUtil {

	private static final CharSequence TITLE_NAME = "Name";
	private static final CharSequence TITLE_PHONE = "Phone";
	private static final CharSequence TITLE_EMAIL = "Email";
	private static final CharSequence TITLE_PKG1 = "Package 1";
	private static final CharSequence TITLE_PKG2 = "Package 2";
	private static final CharSequence TITLE_PKG3 = "Package 3";
	private static final CharSequence TITLE_ROLL = "Roll No";
	private static final CharSequence TITLE_PARENT_PHONE = "Parent Mobile";
	private static final CharSequence TITLE_GENDER = "Gender";
	private static final CharSequence TITLE_DISTRICT = "District";
	private static final CharSequence TITLE_DOB = "DOB";
	private static final CharSequence TITLE_CASTE = "Caste";
	private static final CharSequence TITLE_PASSWORD = "Password";
					


	public static List<EdoStudent> extractStudents(InputStream excel, Integer instituteId, Integer packageId)
			throws InvalidFormatException, IOException, IllegalAccessException, InvocationTargetException {
		List<EdoStudent> students = new ArrayList<EdoStudent>();
		EDOInstitute institute = new EDOInstitute();
		institute.setId(instituteId);
		
		Workbook workbook = WorkbookFactory.create(excel);

		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();

		Integer colName = null, colEmail = null, colPhone = null, colPackage1 = null, colPackage2 = null, colPackage3 = null, colRoll = null;
		Integer colParentPhone = null, colDistrict = null, colGender = null, colDob = null, colCaste = null, colPassword = null;
		for (Row row : sheet) {
			if (row.getRowNum() == 0) { // Title row
				for (Cell cell : row) {
					String cellValue = dataFormatter.formatCellValue(cell);
					System.out.print(cellValue + "\t");
					if (StringUtils.equalsIgnoreCase(cellValue, TITLE_NAME)) {
						colName = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PHONE)) {
						colPhone = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_EMAIL)) {
						colEmail = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PKG1)) {
						colPackage1 = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PKG2)) {
						colPackage2 = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PKG3)) {
						colPackage3 = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_ROLL)) {
						colRoll = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_GENDER)) {
						colGender = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_CASTE)) {
						colCaste = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_DOB)) {
						colDob = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_DISTRICT)) {
						colDistrict = cell.getColumnIndex();
					} else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PARENT_PHONE)) {
						colParentPhone = cell.getColumnIndex();
					}  else if (StringUtils.equalsIgnoreCase(cellValue, TITLE_PASSWORD)) {
						colPassword = cell.getColumnIndex();
					}
				}
			} else {
				if (colName != null || colPhone != null || colRoll != null) {
					/*String phone = "";
					Cell cellValue = row.getCell(colPhone);
					if (cellValue != null) {
						phone = dataFormatter.formatCellValue(cellValue);
					}*/
					EdoStudent student = new EdoStudent();
					student.setName(getCellValue(colName, row));
					student.setPhone(getCellValue(colPhone, row));
					student.setRollNo(getCellValue(colRoll, row));
					student.setEmail(getCellValue(colEmail, row));
					student.setParentMobileNo(getCellValue(colParentPhone, row));
					student.setGender(getCellValue(colGender, row));
					student.setDob(getCellValue(colDob, row));
					student.setCasteCategory(getCellValue(colCaste, row));
					student.setSchoolDistrict(getCellValue(colDistrict, row));
					List<EDOPackage> packages = new ArrayList<EDOPackage>();
					if(packageId == null) {
						if(colPackage1 != null && row.getCell(colPackage1)  != null) {
							EDOPackage p1 = new EDOPackage();
							p1.setId((Double.valueOf(row.getCell(colPackage1).getNumericCellValue())).intValue());
							p1.setInstitute(institute);
							p1.setStatus("Completed");
							packages.add(p1);
						}
						if(colPackage2 != null && row.getCell(colPackage2)  != null) {
							EDOPackage p2 = new EDOPackage();
							p2.setId((Double.valueOf(row.getCell(colPackage2).getNumericCellValue())).intValue());
							p2.setInstitute(institute);
							p2.setStatus("Completed");
							packages.add(p2);
						}
						if(colPackage3 != null && row.getCell(colPackage3)  != null) {
							EDOPackage p3 = new EDOPackage();
							p3.setId((Double.valueOf(row.getCell(colPackage3).getNumericCellValue())).intValue());
							p3.setInstitute(institute);
							p3.setStatus("Completed");
							packages.add(p3);
						}
					} else {
						EDOPackage p1 = new EDOPackage();
						p1.setId(packageId);
						p1.setInstitute(institute);
						p1.setStatus("Completed");
						packages.add(p1);
					}
					
					if(CollectionUtils.isNotEmpty(packages)) {
						student.setPackages(packages);
					}
					EdoPaymentStatus payment = new EdoPaymentStatus();
					payment.setMode("Offline");
					student.setExamMode("Online");
					student.setPayment(payment);
					student.setPassword("12345");
					if(colPassword != null) {
						student.setPassword(getCellValue(colPassword, row));
					}
					students.add(student);
				}
			}
			System.out.println("..........DONE ADDING STUDENTS .......... ");
		}
		
		return students;

	}

	public static String getCellValue(Integer colNumber, Row row) {
		if(colNumber == null || row.getCell(colNumber) == null) {
			return "";
		}
		try {
			return row.getCell(colNumber).getStringCellValue();
		} catch (Exception e) {
			Double numericCellValue = row.getCell(colNumber).getNumericCellValue();
			DecimalFormat df = new DecimalFormat("###");
			return df.format(numericCellValue);
		}
		
	}

	public static void main(String[] args) throws IOException, InvalidFormatException {

		String SAMPLE_XLSX_FILE_PATH = "F:\\Resoneuronance\\Edofox\\Document\\student_data.xlsx";
		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));

		// Retrieving the number of sheets in the Workbook
		System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

		/*
		 * =============================================================
		 * Iterating over all the sheets in the workbook (Multiple ways)
		 * =============================================================
		 */

		// 1. You can obtain a sheetIterator and iterate over it
		Iterator<Sheet> sheetIterator = workbook.sheetIterator();
		System.out.println("Retrieving Sheets using Iterator");
		while (sheetIterator.hasNext()) {
			Sheet sheet = sheetIterator.next();
			System.out.println("=> " + sheet.getSheetName());
		}

		// 2. Or you can use a for-each loop
		System.out.println("Retrieving Sheets using for-each loop");
		/*
		 * for(Sheet sheet: workbook) { System.out.println("=> " +
		 * sheet.getSheetName()); }
		 */

		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();

		// 1. You can obtain a rowIterator and columnIterator and iterate over
		// them
		System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			// Now let's iterate over the columns of the current row
			Iterator<Cell> cellIterator = row.cellIterator();

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				String cellValue = dataFormatter.formatCellValue(cell);
				System.out.print(cellValue + "\t");
			}
			System.out.println();
		}

		// 2. Or you can use a for-each loop to iterate over the rows and
		// columns
		System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
		for (Row row : sheet) {
			for (Cell cell : row) {
				String cellValue = dataFormatter.formatCellValue(cell);
				//if(cell.get)
				System.out.print(cellValue + "\t");
			}
			System.out.println();
		}

		// Closing the workbook
		workbook.close();
	}
}
