package nttdata.personal.julius.api.application.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    private final TransactionRepository repository;

    public ReportService(TransactionRepository repository) {
        this.repository = repository;
    }

    public byte[] generateExcelReport(Long userId) {
        List<Transaction> transactions = repository.findByUserId(userId, 0, 1000);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transações");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Valor Original");
            header.createCell(2).setCellValue("Moeda");
            header.createCell(3).setCellValue("Valor Convertido (BRL)");
            header.createCell(4).setCellValue("Status");
            header.createCell(5).setCellValue("Data");

            int rowIdx = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getAmount() != null ? t.getAmount().doubleValue() : 0);
                row.createCell(2).setCellValue(t.getCurrency() != null ? t.getCurrency() : "-");
                row.createCell(3).setCellValue(t.getConvertedAmount() != null ? t.getConvertedAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(t.getStatus() != null ? t.getStatus().name() : "PENDING");
                row.createCell(5).setCellValue(t.getCreatedAt() != null ? t.getCreatedAt().toString() : "-");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório Excel", e);
        }
    }

    public byte[] generatePdfReport(Long userId) {
        List<Transaction> transactions = repository.findByUserId(userId, 0, 1000);
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Relatório Financeiro - Julius API"));
            document.add(new Paragraph("Usuário ID: " + userId));
            document.add(new Paragraph(" "));

            for (Transaction t : transactions) {
                document.add(new Paragraph(String.format("ID: %d | Valor: %s %s | Status: %s | Data: %s",
                        t.getId(), t.getAmount(), t.getCurrency(), t.getStatus(), t.getCreatedAt())));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }
}
