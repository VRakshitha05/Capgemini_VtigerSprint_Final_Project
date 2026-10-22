package utilities;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtility {

    // reads one cell from TestData.xlsx (row 0 is the heading row)
    public String getDataFromExcel(String sheetName, int row, int col) {
        try {
            InputStream is = ExcelUtility.class.getClassLoader().getResourceAsStream("TestData.xlsx");
            Workbook wb = WorkbookFactory.create(is);
            Cell cell = wb.getSheet(sheetName).getRow(row).getCell(col);
            String data = "";
            if (cell != null) {
                data = new DataFormatter().formatCellValue(cell).trim();
            }
            wb.close();
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Cannot read TestData.xlsx sheet=" + sheetName + " row=" + row + " col=" + col, e);
        }
    }
}
