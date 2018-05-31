package com.sdyk.ai.crawler.util;

import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class XlxsNumberExtractor {

	public XlxsNumberExtractor() {

	}

	public void loadXlxs() {

		String fileName = "tmp/000-data-499-1000.xlsx";

		try {

			FileInputStream excelFile = new FileInputStream(new File(fileName));
			Workbook workbook = new XSSFWorkbook(excelFile);

			Sheet sheet = workbook.getSheetAt(0);
			/*for(PictureData pic : workbook.getAllPictures()) {
				String ext = pic.suggestFileExtension();
				byte[] data = pic.getData();
				System.out.println(ext);
			}*/

			XSSFDrawing dp = (XSSFDrawing) sheet.createDrawingPatriarch();

			List<XSSFShape> pics = dp.getShapes();

			for(XSSFShape pic : pics) {

				XSSFPicture inpPic = (XSSFPicture) pic;
				XSSFClientAnchor clientAnchor = inpPic.getClientAnchor();
				int col = clientAnchor.getCol1();
				int row = clientAnchor.getRow1();
				System.out.println(row + "\t" + col);

				byte[] pic_data = inpPic.getPictureData().getData();



				sheet.getRow(row).createCell(col + 2);
				sheet.getRow(row).getCell(col + 2).setCellValue("Check");
			}

			FileOutputStream outFile = new FileOutputStream(new File(fileName));
			workbook.write(outFile);
			outFile.close();

			/*System.out.println("col1: " + clientAnchor.getCol1() + ", col2: " + clientAnchor.getCol2() + ", row1: " + clientAnchor.getRow1() + ", row2: " + clientAnchor.getRow2());
			System.out.println("x1: " + clientAnchor.getDx1() + ", x2: " + clientAnchor.getDx2() +  ", y1: " + clientAnchor.getDy1() +  ", y2: " + clientAnchor.getDy2());*/

			/*for (Row currentRow : sheet) {

				// 非空行
				if(currentRow.getCell(0) != null) {

					String value = currentRow.getCell(0).toString();

					// "c" 是默认的第一列名称
					if (!value.equals("c") && value.trim().length() > 0) {

					}
				}
				else {

					break;
				}
			}*/

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		XlxsNumberExtractor extractor = new XlxsNumberExtractor();
		extractor.loadXlxs();
	}
}
