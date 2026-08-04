package laboratorioxyz.com.ZoneControl.modulo_reportes.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generador de PDF genérico (HU-16/17, gap 1.1 §9) compartido por el export de
 * historial y el archivo periódico. Recibe encabezados + filas para renderizar
 * una tabla con título y subtítulo. itextpdf 5 (Helvetica/WinAnsi soporta
 * acentos del español).
 */
@Component
public class PdfExporter {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    public byte[] exportTable(String title, String subtitle, String[] headers, List<String[]> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, TITLE_FONT));
            if (subtitle != null && !subtitle.isBlank()) {
                document.add(new Paragraph(subtitle, SUBTITLE_FONT));
            }

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100f);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
                cell.setPadding(4);
                table.addCell(cell);
            }
            for (String[] row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, CELL_FONT));
                    cell.setPadding(4);
                    table.addCell(cell);
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el archivo PDF");
        }
    }
}
