package com.naoty.gradebook.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.naoty.gradebook.exception.PersistenceException;
import com.naoty.gradebook.model.GradeType;
import com.naoty.gradebook.model.Mention;
import com.naoty.gradebook.model.Semestre;
import com.naoty.gradebook.model.Student;
import com.naoty.gradebook.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Génère un bulletin PDF pour un étudiant à un semestre donné.
 * Le tableau présente une colonne par type d'évaluation (DS / TP / EXAM)
 * pour chaque matière applicable à l'étudiant.
 */
public class BulletinService {

    private static final Logger log = LoggerFactory.getLogger(BulletinService.class);

    private final SubjectService subjects;
    private final StatsService stats;

    public BulletinService(SubjectService subjects, StatsService stats) {
        this.subjects = subjects;
        this.stats = stats;
    }

    public void generate(Student student, Semestre semestre, Path output) {
        try {
            Files.createDirectories(output.getParent());
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, new FileOutputStream(output.toFile()));
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(100, 116, 139));
            Font textFont  = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(15, 23, 42));
            Font boldFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(15, 23, 42));

            Paragraph title = new Paragraph("BULLETIN DE NOTES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            doc.add(title);

            Paragraph sub = new Paragraph(
                    student.niveau() + " " + student.formation() + " — Semestre " + semestre.name().substring(1),
                    labelFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(18);
            doc.add(sub);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setSpacingAfter(18);
            addInfo(info, "Étudiant", student.fullName(), labelFont, textFont);
            addInfo(info, "ID",       student.id(),       labelFont, textFont);
            addInfo(info, "Status",   student.status().name(), labelFont, textFont);
            doc.add(info);

            PdfPTable table = new PdfPTable(new float[]{4, 1, 1, 1, 1, 1.4f});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Matière", "Coef.", "DS", "TP", "EXAM", "Moy. / 20"}) {
                PdfPCell c = new PdfPCell(new Phrase(h, labelFont));
                c.setBackgroundColor(new Color(243, 244, 246));
                c.setPadding(8);
                c.setBorderColor(new Color(229, 231, 235));
                table.addCell(c);
            }

            List<Subject> subjectsFor = subjects.findForStudent(student, semestre);
            for (Subject subject : subjectsFor) {
                Map<GradeType, Double> averages = stats.averagesByType(student.id(), subject.id());
                table.addCell(cell(subject.nom(), textFont));
                table.addCell(cell(String.valueOf(subject.coefficient()), textFont));
                table.addCell(cell(format(averages.get(GradeType.DS)),   textFont));
                table.addCell(cell(format(averages.get(GradeType.TP)),   textFont));
                table.addCell(cell(format(averages.get(GradeType.EXAM)), textFont));
                OptionalDouble subAvg = stats.subjectAverage(student.id(), subject);
                table.addCell(cell(subAvg.isPresent() ? String.format("%.2f", subAvg.getAsDouble()) : "—",
                                   boldFont));
            }
            doc.add(table);

            OptionalDouble semAvg = stats.semestreAverage(student, semestre);
            Paragraph summary = new Paragraph();
            summary.setSpacingBefore(20);
            summary.add(new Phrase("Moyenne semestrielle : ", labelFont));
            summary.add(new Phrase(
                    semAvg.isPresent() ? String.format("%.2f / 20", semAvg.getAsDouble()) : "—",
                    boldFont));
            doc.add(summary);

            if (semAvg.isPresent()) {
                Mention m = stats.mention(semAvg.getAsDouble());
                Paragraph mention = new Paragraph();
                mention.add(new Phrase("Mention : ", labelFont));
                mention.add(new Phrase(m.name().replace('_', ' '), textFont));
                doc.add(mention);

                int r = stats.rank(student, semestre);
                if (r > 0) {
                    Paragraph rank = new Paragraph();
                    rank.add(new Phrase("Rang dans la cohorte : ", labelFont));
                    rank.add(new Phrase(String.valueOf(r), textFont));
                    doc.add(rank);
                }
            }

            Paragraph footer = new Paragraph("Édité le " + LocalDate.now(),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(148, 163, 184)));
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(36);
            doc.add(footer);

            doc.close();
            log.info("Bulletin generated: {}", output);
        } catch (IOException e) {
            throw new PersistenceException("Failed to write bulletin " + output, e);
        }
    }

    private static String format(Double value) {
        return value == null ? "—" : String.format("%.2f", value);
    }

    private static void addInfo(PdfPTable table, String label, String value, Font lf, Font tf) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, lf));
        labelCell.setBorder(0);
        labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, tf));
        valueCell.setBorder(0);
        valueCell.setPadding(4);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(6);
        c.setBorderColor(new Color(229, 231, 235));
        return c;
    }
}
