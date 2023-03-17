package api.ord.writers;

import api.ord.model.OrdPollOption;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.io.File;
import java.sql.Connection;
import utilities.Dates;

public class RptPollTank extends RptPollPQR {

    public File generateReport(Connection ep, Integer pollTypeId, Integer pqrId) throws Exception {
        Integer checkBoolean, indexPoll = 0;
        begin(ep, pollTypeId, pqrId);
        beginDocument();

        loadHead();

        PdfPTable infoTab = new PdfPTable(4);
        infoTab.setWidths(new float[]{22, 40, 18, 20});
        infoTab.setWidthPercentage(100);

        cellStyleText.setFontSize(10);
        cellStyleTextBold.setFontSize(10);
        cellStyleTitle.setFontSize(10);

        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Captura:"));
        infoTab.addCell(cellStyleText.getCell(sdf.format(pqrTank.registDate)));
        infoTab.addCell(cellStyleTextBold.getCell("Hora de Captura:"));
        infoTab.addCell(cellStyleText.getCell(sdfHour.format(pqrTank.registHour)));

        String clientName;
        String addr;
        String phones;
        if (pqrTank.buildId != null) {
            clientName = build.name;
            addr = build.address;
            phones = build.phones;
        } else {
            clientName = client.firstName + " " + client.lastName;
            if (buildCli != null) {
                addr = buildCli.address + " " + buildCli.name;
            } else {
                addr = neigt + " " + client.address;
            }

            phones = client.phones;
        }
        infoTab.addCell(cellStyleTextBold.getCell("Nombres:"));
        infoTab.addCell(cellStyleText.getCell(clientName));
        infoTab.addCell(cellStyleTextBold.getCell("Teléfono:"));
        infoTab.addCell(cellStyleText.getCell(phones));

        infoTab.addCell(cellStyleTextBold.getCell("Dirección:"));
        infoTab.addCell(cellStyleText.getCell(addr, 3, 1));

        infoTab.addCell(cellStyleTextBold.getCell("Motivo de la Llamada:"));
        infoTab.addCell(cellStyleText.getCell(reason.description, 3, 1));

        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Llegada:"));
        infoTab.addCell(cellStyleText.getCell(pqrTank.arrivalDate != null ? Dates.getDateTimeFormat().format(pqrTank.arrivalDate) : " "));

        String completeDate = sdf.format(pqrTank.attentionDate) + " " + Dates.getHourFormat().format(pqrTank.attentionHour);
        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Cierre:"));
        infoTab.addCell(cellStyleText.getCell(completeDate));

        infoTab.addCell(cellStyleTextBold.getCell("Técnico:"));
        infoTab.addCell(cellStyleText.getCell(tech.firstName + " " + tech.lastName));
        infoTab.addCell(cellStyleTextBold.getCell("Nro Factura:"));
        infoTab.addCell(cellStyleText.getCell(pqrTank.billNum));

        if (pollQuestions.size() > 11) {
            infoTab.addCell(cellStyleTextBold.getCell("Tipo de Regulador:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(11).ordinal)));
            infoTab.addCell(cellStyleTextBold.getCell("Marca Regulador:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(12).ordinal)));
        }

        infoTab.addCell(cellStyleText.getCell(" ", 4, 1));
        infoTab.addCell(cellStyleTitle.getCell("Resultado o Concepto Técnico", 4, 1));
        infoTab.setSpacingAfter(5);
        document.add(infoTab);
        document.add(separator());

        PdfPTable revTab = new PdfPTable(6);
        revTab.setWidths(new float[]{5, 28, 5, 28, 5, 28});
        revTab.setWidthPercentage(100);

        revTab.addCell(cellStyleTitle.getCell(pollQuestions.get(0).text, 6, 1));

        OrdPollOption[] questionRev = returnOptionsQuestion(pollQuestions.get(0).id);
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < questionRev.length; i++) {
            revTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            revTab.addCell(cellStyleText.getCell(questionRev[i].text));
        }

        document.add(revTab);

        PdfPTable descTab = new PdfPTable(3);
        descTab.setWidths(new float[]{23, 37, 40});
        descTab.setWidthPercentage(100);

        indexPoll++;
        longText(descTab, cellStyleText, pollQuestions.get(1).text, getText(pollQuestions.get(1).ordinal), 3, 20, false);
        descTab.setSpacingAfter(5);

        document.add(descTab);
        document.add(separator());

        PdfPTable fugaTab = new PdfPTable(6);
        fugaTab.setWidths(new float[]{5, 28, 5, 28, 5, 28});
        fugaTab.setWidthPercentage(100);

        OrdPollOption[] qFuga = returnOptionsQuestion(pollQuestions.get(2).id);
        fugaTab.addCell(cellStyleTitle.getCell(pollQuestions.get(2).text, 6, 1));

        indexPoll++;
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qFuga.length; i++) {
            fugaTab.addCell(checkBoolean == i + 1 ? check2Cell : checkCell);
            fugaTab.addCell(cellStyleText.getCell(qFuga[i].text));
        }
        document.add(fugaTab);

        PdfPTable desc2Tab = new PdfPTable(3);
        desc2Tab.setWidths(new float[]{23, 37, 40});
        desc2Tab.setWidthPercentage(100);

        indexPoll++;
        longText(desc2Tab, cellStyleText, pollQuestions.get(3).text, getText(pollQuestions.get(3).ordinal), 3, 20, false);
        desc2Tab.setSpacingAfter(5);

        document.add(desc2Tab);
        document.add(separator());

        PdfPTable fugaGTab = new PdfPTable(6);
        fugaGTab.setWidths(new float[]{5, 28, 5, 28, 5, 28});
        fugaGTab.setWidthPercentage(100);

        OrdPollOption[] qFugaG = returnOptionsQuestion(pollQuestions.get(4).id);
        fugaGTab.addCell(cellStyleTitle.getCell(pollQuestions.get(4).text, 6, 1));

        indexPoll++;
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qFugaG.length; i++) {
            fugaGTab.addCell(checkBoolean == i + 1 ? check2Cell : checkCell);
            fugaGTab.addCell(cellStyleText.getCell(qFugaG[i].text));
        }

        PdfPTable cualTab = new PdfPTable(2);
        cualTab.setWidths(new float[]{20, 80});
        cualTab.setWidthPercentage(100);
        cualTab.setComplete(true);

        cualTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(5).text != null ? pollQuestions.get(5).text : ""));
        underlined(cellStyleText, true);
        cualTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(5).ordinal)));
        underlined(cellStyleText, false);
        indexPoll++;
        PdfPCell cell = new PdfPCell(cualTab);
        cell.setColspan(4);
        cell.setBorder(0);

        fugaGTab.addCell(cell);
        document.add(fugaGTab);

        PdfPTable desc3Tab = new PdfPTable(3);
        desc3Tab.setWidths(new float[]{23, 37, 40});
        desc3Tab.setWidthPercentage(100);

        indexPoll++;
        longText(desc3Tab, cellStyleText, pollQuestions.get(6).text, getText(pollQuestions.get(6).ordinal), 3, 20, false);
        desc3Tab.setSpacingAfter(5);

        document.add(desc3Tab);
        document.add(separator());

        PdfPTable concTab = new PdfPTable(5);
        concTab.setWidths(new float[]{40, 5, 15, 5, 35});
        concTab.setWidthPercentage(100);

        concTab.addCell(cellStyleTitle.getCell("Concepto Cliente", 5, 1));

        OrdPollOption[] qConce = returnOptionsQuestion(pollQuestions.get(7).id);

        indexPoll++;
        concTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(7).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qConce.length; i++) {
            concTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            concTab.addCell(cellStyleText.getCell(qConce[i].text));
        }

        concTab.setSpacingAfter(5);
        document.add(concTab);
        document.add(separator());

        PdfPTable costsTab = new PdfPTable(4);
        costsTab.setWidths(new float[]{17, 33, 15, 35});
        costsTab.setWidthPercentage(100);

        costsTab.addCell(cellStyleTitle.getCell("Costos Servicio", 4, 1));

        underlined(cellStyleText, true);
        indexPoll++;
        costsTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(8).text));
        costsTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(8).ordinal)));
        indexPoll++;
        costsTab.addCell(cellStyleTextBold.getCell("   " + pollQuestions.get(9).text));
        costsTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(9).ordinal)));
        underlined(cellStyleText, false);
        costsTab.setSpacingAfter(5);

        document.add(costsTab);

        PdfPTable costs2Tab = new PdfPTable(5);
        costs2Tab.setWidths(new float[]{40, 5, 15, 5, 35});
        costs2Tab.setWidthPercentage(100);

        OrdPollOption[] qCosts = returnOptionsQuestion(pollQuestions.get(10).id);

        indexPoll++;
        costs2Tab.addCell(cellStyleTextBold.getCell(pollQuestions.get(10).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qCosts.length; i++) {
            costs2Tab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            costs2Tab.addCell(cellStyleText.getCell(qCosts[i].text));
        }

        costs2Tab.addCell(cellStyleText.getCell("Nota: " + cfg.pqrTankNotes, 5, 1));
        costs2Tab.addCell(cellStyleText.getCell("", 5, 1));
        costs2Tab.addCell(cellStyleTitle.getCell("Acepto y comprendo la información de esta PQRs por atención de emergencias.", 5, 1));
        costs2Tab.addCell(cellStyleTitle.getCell("Recuerde que el mantenimiento de los gasodomésticos y accesorios son de su propiedad y de su responsabilidad.", 5, 1));
        costs2Tab.setSpacingAfter(5);

        document.add(costs2Tab);

        PdfPTable notesTab = new PdfPTable(3);
        notesTab.setWidths(new float[]{15, 45, 40});
        notesTab.setWidthPercentage(100);

        longText(notesTab, cellStyleText, "Informe Final", poll.notes, 3, 15, true);
        notesTab.setSpacingAfter(10);

        document.add(notesTab);

        loadSignatures();

        return endDocument();

    }
}
