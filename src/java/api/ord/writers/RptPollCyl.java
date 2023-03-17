package api.ord.writers;

import api.ord.model.OrdPollOption;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import java.io.File;
import java.sql.Connection;
import utilities.Dates;

public class RptPollCyl extends RptPollPQR {

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
        infoTab.addCell(cellStyleText.getCell(sdf.format(pqrCyl.creationDate)));
        infoTab.addCell(cellStyleTextBold.getCell("Hora de Captura:"));
        infoTab.addCell(cellStyleText.getCell(sdfHour.format(pqrCyl.registHour)));

        infoTab.addCell(cellStyleTextBold.getCell("Nombres:"));
        infoTab.addCell(cellStyleText.getCell(index.firstName + " " + index.lastName));
        infoTab.addCell(cellStyleTextBold.getCell("Teléfono:"));
        infoTab.addCell(cellStyleText.getCell(index.phones));

        infoTab.addCell(cellStyleTextBold.getCell("Dirección:"));
        infoTab.addCell(cellStyleText.getCell(index.address + " " + (neigh != null ? neigh.name : "")));
        infoTab.addCell(cellStyleTextBold.getCell("Nro Factura:"));
        infoTab.addCell(cellStyleText.getCell(pqrCyl.billNum));

        infoTab.addCell(cellStyleTextBold.getCell("Motivo de la Llamada:"));
        infoTab.addCell(cellStyleText.getCell(reason.description, 3, 1));

        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Llegada:"));
        infoTab.addCell(cellStyleText.getCell(pqrCyl.arrivalDate != null ? Dates.getDateTimeFormat().format(pqrCyl.arrivalDate) : " "));
        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Cierre:"));
        String completeDate = sdf.format(pqrCyl.attentionDate) + " " + Dates.getHourFormat().format(pqrCyl.attentionHour);
        infoTab.addCell(cellStyleText.getCell(completeDate));

        if (pollQuestions.size() > 11) {
            infoTab.addCell(cellStyleTextBold.getCell("Tipo de Regulador:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(11).ordinal)));
            infoTab.addCell(cellStyleTextBold.getCell("Ref de Manguera:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(13).ordinal)));
            infoTab.addCell(cellStyleTextBold.getCell("Marca Regulador:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(12).ordinal)));
            infoTab.addCell(cellStyleTextBold.getCell("Color Manguera:"));
            infoTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(14).ordinal)));
        }

        infoTab.setSpacingAfter(10);
        document.add(infoTab);

        PdfPTable checkCylTab = new PdfPTable(12);
        checkCylTab.setWidths(new float[]{20, 10, 5, 9, 5, 9, 5, 9, 5, 9, 5, 9});
        checkCylTab.setWidthPercentage(100);

        checkCylTab.addCell(cellStyleTextBold.getCell("Cantidad Cilindros:"));
        underlined(cellStyleText, true);
        checkCylTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(0).ordinal)));
        underlined(cellStyleText, false);

        indexPoll++;
        OrdPollOption[] questionCyl = returnOptionsQuestion(pollQuestions.get(1).id);
        checkBoolean = getBoolean(indexPoll);

        for (int i = 0; i < questionCyl.length; i++) {
            checkCylTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            checkCylTab.addCell(cellStyleText.getCell(questionCyl[i].text));
        }
        document.add(checkCylTab);

        PdfPTable info2Tab = new PdfPTable(4);
        info2Tab.setWidths(new float[]{10, 40, 10, 40});
        info2Tab.setWidthPercentage(100);

        info2Tab.addCell(cellStyleTextBold.getCell("Tipo:"));
        info2Tab.addCell(cellStyleText.getCell((index.type.equals("brand") ? "Marca" : (index.type.equals("univ") ? "Provisional" : "App"))));
        info2Tab.addCell(cellStyleTextBold.getCell("Empresa:"));
        info2Tab.addCell(cellStyleText.getCell(enterprise.name));

        info2Tab.addCell(cellStyleTextBold.getCell("Técnico:"));
        info2Tab.addCell(cellStyleText.getCell(tech.firstName + " " + tech.lastName, 3, 1));
        info2Tab.setSpacingAfter(10);
        document.add(info2Tab);
        document.add(separator());

        PdfPTable revTab = new PdfPTable(6);
        revTab.setWidths(new float[]{5, 28, 5, 28, 5, 28});
        revTab.setWidthPercentage(100);

        revTab.addCell(cellStyleTitle.getCell(pollQuestions.get(2).text, 6, 1));

        indexPoll++;
        OrdPollOption[] questionRev = returnOptionsQuestion(pollQuestions.get(2).id);
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < questionRev.length; i++) {
            revTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            revTab.addCell(cellStyleText.getCell(questionRev[i].text));
        }
        revTab.addCell(cellStyleText.getCell(" "));
        revTab.addCell(cellStyleText.getCell(" "));
        revTab.setSpacingAfter(5);
        document.add(revTab);
        document.add(separator());

        PdfPTable rev2Tab = new PdfPTable(2);
        rev2Tab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        rev2Tab.setWidths(new float[]{50, 50});
        rev2Tab.setWidthPercentage(100);

        rev2Tab.addCell(cellStyleTitle.getCell(pollQuestions.get(2).text, 2, 1));

        PdfPTable cylTab = new PdfPTable(4);
        cylTab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        cylTab.setWidths(new float[]{10, 40, 10, 40});
        cylTab.setWidthPercentage(100);

        OrdPollOption[] qCyl = returnOptionsQuestion(pollQuestions.get(3).id);
        cylTab.addCell(cellStyleTitle.getCell(pollQuestions.get(3).text, 4, 1));

        for (int i = 0; i < qCyl.length; i++) {
            indexPoll++;
            checkBoolean = getBoolean(indexPoll);
            cylTab.addCell(checkBoolean > 0 ? check2Cell : checkCell);
            cylTab.addCell(cellStyleText.getCell(qCyl[i].text));
        }
        cylTab.addCell(cellStyleText.getCell(" "));
        cylTab.addCell(cellStyleText.getCell(" "));

        PdfPTable valTab = new PdfPTable(4);
        valTab.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        valTab.setWidths(new float[]{10, 40, 10, 40});
        valTab.setWidthPercentage(100);

        OrdPollOption[] qVal = returnOptionsQuestion(pollQuestions.get(4).id);
        valTab.addCell(cellStyleTitle.getCell(pollQuestions.get(4).text, 4, 1));

        for (int i = 0; i < qVal.length; i++) {
            indexPoll++;
            checkBoolean = getBoolean(indexPoll);
            valTab.addCell(checkBoolean > 0 ? check2Cell : checkCell);
            valTab.addCell(cellStyleText.getCell(qVal[i].text));
        }
        valTab.addCell(cellStyleText.getCell(" "));
        valTab.addCell(cellStyleText.getCell(" "));
        valTab.addCell(cellStyleText.getCell(" "));
        valTab.addCell(cellStyleText.getCell(" "));
        valTab.addCell(cellStyleText.getCell(" "));
        valTab.addCell(cellStyleText.getCell(" "));

        rev2Tab.addCell(cylTab);
        rev2Tab.addCell(valTab);

        document.add(rev2Tab);
        document.add(separator());

        PdfPTable otherTab = new PdfPTable(6);
        otherTab.setWidths(new float[]{5, 28, 5, 28, 5, 28});
        otherTab.setWidthPercentage(100);

        otherTab.addCell(cellStyleTitle.getCell(pollQuestions.get(5).text, 6, 1));

        OrdPollOption[] qOther = returnOptionsQuestion(pollQuestions.get(5).id);

        for (int i = 0; i < qOther.length; i++) {
            indexPoll++;
            checkBoolean = getBoolean(indexPoll);
            otherTab.addCell(checkBoolean > 0 ? check2Cell : checkCell);
            otherTab.addCell(cellStyleText.getCell(qOther[i].text));
        }
        otherTab.setSpacingAfter(5);
        document.add(otherTab);
        document.add(separator());

        PdfPTable concTab = new PdfPTable(5);
        concTab.setWidths(new float[]{40, 5, 15, 5, 35});
        concTab.setWidthPercentage(100);

        concTab.addCell(cellStyleTitle.getCell("Concepto Cliente", 5, 1));

        OrdPollOption[] qConce = returnOptionsQuestion(pollQuestions.get(6).id);

        indexPoll++;
        concTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(6).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qConce.length; i++) {
            concTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            concTab.addCell(cellStyleText.getCell(qConce[i].text));
        }

        OrdPollOption[] qConce2 = returnOptionsQuestion(pollQuestions.get(7).id);

        indexPoll++;
        concTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(7).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qConce2.length; i++) {
            concTab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            concTab.addCell(cellStyleText.getCell(qConce2[i].text));
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

        costs2Tab.addCell(cellStyleText.getCell("Nota: " + cfg.pqrCylNotes, 5, 1));
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
