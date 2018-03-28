package com.rns.web.edo.service.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rns.web.edo.service.domain.EdoStudent;

public class EdoExcelUtil {

	public List<EdoStudent> extarctStudents(String filePath) {
		List<EdoStudent> students = new ArrayList<EdoStudent>();
		return students;
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
