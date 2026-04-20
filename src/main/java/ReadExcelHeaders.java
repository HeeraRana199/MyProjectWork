import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcelHeaders {
    public static void main(String[] args) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream("Sample_Crecruit_Data.xlsx"))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            System.out.println("Headers in Sample_Crecruit_Data.xlsx:");
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim();
                if (!header.isEmpty()) {
                    System.out.println("- \"" + header + "\"");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
