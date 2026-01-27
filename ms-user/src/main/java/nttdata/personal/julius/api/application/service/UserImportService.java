package nttdata.personal.julius.api.application.service;

import nttdata.personal.julius.api.application.dto.ImportReportDto;
import nttdata.personal.julius.api.application.dto.UserDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserImportService {

    private final UserService userService;

    public UserImportService(UserService userService) {
        this.userService = userService;
    }

    public ImportReportDto importUsers(MultipartFile file) {
        List<ImportReportDto.ImportError> errors = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = dataFormatter.formatCellValue(row.getCell(0));
                    String email = dataFormatter.formatCellValue(row.getCell(1));
                    String cpf = dataFormatter.formatCellValue(row.getCell(2));
                    String password = dataFormatter.formatCellValue(row.getCell(3));

                    if (email.isBlank() || cpf.isBlank()) {
                        continue;
                    }

                    UserDto dto = new UserDto(name, email, cpf, password);
                    userService.create(dto);

                    successCount++;
                } catch (Exception e) {
                    errors.add(new ImportReportDto.ImportError(i + 1, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar arquivo Excel", e);
        }

        return new ImportReportDto(successCount, errors.size(), errors);
    }
}