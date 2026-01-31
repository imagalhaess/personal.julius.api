package nttdata.personal.julius.api.application.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import nttdata.personal.julius.api.domain.model.Transaction;
import nttdata.personal.julius.api.domain.repository.TransactionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TransactionRepository repository;

    public ReportService(TransactionRepository repository) {
        this.repository = repository;
    }

    public byte[] generateExcelReport(Long userId) {
        List<Transaction> transactions = repository.findByUserId(userId, 0, 1000);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transacoes");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Tipo");
            header.createCell(2).setCellValue("Valor Original");
            header.createCell(3).setCellValue("Moeda");
            header.createCell(4).setCellValue("Valor Convertido (BRL)");
            header.createCell(5).setCellValue("Categoria");
            header.createCell(6).setCellValue("Status");
            header.createCell(7).setCellValue("Data");

            int rowIdx = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getType() != null ? t.getType().name() : "-");
                row.createCell(2).setCellValue(t.getAmount() != null ? t.getAmount().doubleValue() : 0);
                row.createCell(3).setCellValue(t.getCurrency() != null ? t.getCurrency() : "-");
                row.createCell(4).setCellValue(t.getConvertedAmount() != null ? t.getConvertedAmount().doubleValue() : 0);
                row.createCell(5).setCellValue(t.getCategory() != null ? t.getCategory().name() : "-");
                row.createCell(6).setCellValue(t.getStatus() != null ? t.getStatus().name() : "PENDING");
                row.createCell(7).setCellValue(t.getCreatedAt() != null ? t.getCreatedAt().toString() : "-");

                // Calcular totais apenas para transações APPROVED - correção do bug
                if (t.getStatus() != Transaction.TransactionStatus.APPROVED) continue;

                BigDecimal valor = t.getConvertedAmount() != null ? t.getConvertedAmount() : t.getAmount();
                if (valor == null) continue;

                switch (t.getType()) {
                    case INCOME -> totalIncome = totalIncome.add(valor);
                    case EXPENSE -> totalExpense = totalExpense.add(valor);
                    default -> {}
                }
            }

            // Linha em branco
            rowIdx++;

            // Resumo
            Row summaryHeader = sheet.createRow(rowIdx++);
            summaryHeader.createCell(0).setCellValue("RESUMO");

            Row incomeRow = sheet.createRow(rowIdx++);
            incomeRow.createCell(0).setCellValue("Total Entradas (INCOME):");
            incomeRow.createCell(1).setCellValue(totalIncome.doubleValue());

            Row expenseRow = sheet.createRow(rowIdx++);
            expenseRow.createCell(0).setCellValue("Total Saidas (EXPENSE):");
            expenseRow.createCell(1).setCellValue(totalExpense.doubleValue());

            Row balanceRow = sheet.createRow(rowIdx++);
            balanceRow.createCell(0).setCellValue("Saldo:");
            balanceRow.createCell(1).setCellValue(totalIncome.subtract(totalExpense).doubleValue());

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar relatório Excel para userId={}", userId, e);
            throw new RuntimeException("Erro ao gerar relatorio Excel", e);
        }
    }

    public byte[] generatePdfReport(Long userId) {
        List<Transaction> transactions = repository.findByUserId(userId, 0, 1000);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Relatorio Financeiro - Julius API", titleFont));
            document.add(new Paragraph("Usuario ID: " + userId, normalFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Transacoes:", headerFont));
            document.add(new Paragraph(" "));

            for (Transaction t : transactions) {
                String tipo = t.getType() != null ? t.getType().name() : "-";
                String categoria = t.getCategory() != null ? t.getCategory().name() : "-";
                String status = t.getStatus() != null ? t.getStatus().name() : "PENDING";

                document.add(new Paragraph(String.format(
                        "ID: %d | Tipo: %s | Valor: %s %s | Categoria: %s | Status: %s",
                        t.getId(), tipo, t.getAmount(), t.getCurrency(), categoria, status), normalFont));

                // Calcular totais apenas para transações APPROVED - correção de big
                if (t.getStatus() != Transaction.TransactionStatus.APPROVED) continue;

                BigDecimal valor = t.getConvertedAmount() != null ? t.getConvertedAmount() : t.getAmount();
                if (valor == null) continue;

                switch (t.getType()) {
                    case INCOME -> totalIncome = totalIncome.add(valor);
                    case EXPENSE -> totalExpense = totalExpense.add(valor);
                    default -> {}
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("RESUMO", headerFont));
            document.add(new Paragraph(String.format("Total Entradas (INCOME): R$ %.2f", totalIncome), normalFont));
            document.add(new Paragraph(String.format("Total Saidas (EXPENSE): R$ %.2f", totalExpense), normalFont));
            document.add(new Paragraph(String.format("Saldo: R$ %.2f", totalIncome.subtract(totalExpense)), headerFont));

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar relatório PDF para userId={}", userId, e);
            throw new RuntimeException("Erro ao gerar relatorio PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
}
