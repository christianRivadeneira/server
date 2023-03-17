package api.ord.writers;

import api.ord.model.OrdPollOption;
import com.lowagie.text.pdf.PdfPTable;
import java.io.File;
import java.sql.Connection;

public class RptPollRepair extends RptPollPQR {

    public File generateReport(Connection ep, Integer pollTypeId, Integer pqrId) throws Exception {

        Integer checkBoolean, indexPoll = 0;
        begin(ep, pollTypeId, pqrId);
        beginDocument();

        loadHead();

        PdfPTable infoTab = new PdfPTable(4);
        infoTab.setWidths(new float[]{22, 40, 18, 20});
        infoTab.setWidthPercentage(100);

        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Registro:"));
        infoTab.addCell(cellStyleText.getCell(sdf.format(pqrRepair.registDate)));
        infoTab.addCell(cellStyleTextBold.getCell("Hora de Registro:"));
        infoTab.addCell(cellStyleText.getCell(sdfHour.format(pqrRepair.registHour)));

        String clientName = "";
        String addr = "";
        String phones = "";
        if (pqrRepair.indexId != null) {
            if (index.neighId != null) {
                addr = index.address + " " + neigh.name;
            } else {
                addr = index.address + " ";
            }
            clientName = (index.firstName != null ? index.firstName : "") + " " + (index.lastName != null ? index.lastName : "");
            phones = index.phones;
        } else if (pqrRepair.buildId != null) {
            if (buildCli != null) {
                addr = (buildCli.address != null ? buildCli.address : "") + " " + (neigt != null ? neigt : "");
            } else {
                addr = neigt + " " + client.address;
            }
            clientName = buildCli.name;
            phones = buildCli.phones;
        } else if (pqrRepair.clientId != null) {
            if (buildCli != null) {
                addr = buildCli.name + " " + buildCli.address + " - Apto " + client.apartament;
            } else {
                addr = neigt + " " + client.address;
            }
            clientName = (client.firstName != null ? client.firstName : "") + " " + (client.lastName != null ? client.lastName : "");
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

        infoTab.addCell(cellStyleTextBold.getCell("Fecha de Atención:"));
        infoTab.addCell(cellStyleText.getCell(sdf.format(pqrRepair.confirmDate)));
        infoTab.addCell(cellStyleTextBold.getCell("Hora de Atención:"));
        infoTab.addCell(cellStyleText.getCell(" "));

        infoTab.addCell(cellStyleTextBold.getCell("Técnico:"));
        infoTab.addCell(cellStyleText.getCell(tech.firstName + " " + tech.lastName, 3, 1));

        infoTab.setSpacingAfter(10);
        document.add(infoTab);

        PdfPTable question1 = new PdfPTable(3);
        question1.setWidths(new float[]{17, 43, 40});
        question1.setWidthPercentage(100);

        longText(question1, cellStyleText, pollQuestions.get(0).text, getText(pollQuestions.get(0).ordinal), 3, 20, true);
        question1.setSpacingAfter(5);

        document.add(question1);

        PdfPTable question2 = new PdfPTable(5);
        question2.setWidths(new float[]{40, 5, 15, 5, 35});
        question2.setWidthPercentage(100);

        question2.addCell(cellStyleTitle.getCell("Concepto Cliente", 5, 1));

        OrdPollOption[] qConce = returnOptionsQuestion(pollQuestions.get(1).id);

        indexPoll++;
        question2.addCell(cellStyleTextBold.getCell(pollQuestions.get(1).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qConce.length; i++) {
            question2.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            question2.addCell(cellStyleText.getCell(qConce[i].text));
        }

        question2.setSpacingAfter(10);
        document.add(question2);

        PdfPTable costsTab = new PdfPTable(4);
        costsTab.setWidths(new float[]{17, 33, 15, 35});
        costsTab.setWidthPercentage(100);

        costsTab.addCell(cellStyleTitle.getCell("Costos Servicio", 4, 1));

        underlined(cellStyleText, true);
        indexPoll++;
        costsTab.addCell(cellStyleTextBold.getCell(pollQuestions.get(2).text));
        costsTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(2).ordinal)));
        indexPoll++;
        costsTab.addCell(cellStyleTextBold.getCell("   " + pollQuestions.get(3).text));
        costsTab.addCell(cellStyleText.getCell(getText(pollQuestions.get(3).ordinal)));
        underlined(cellStyleText, false);
        costsTab.setSpacingAfter(10);

        document.add(costsTab);

        PdfPTable costs2Tab = new PdfPTable(5);
        costs2Tab.setWidths(new float[]{40, 5, 15, 5, 35});
        costs2Tab.setWidthPercentage(100);

        OrdPollOption[] qCosts = returnOptionsQuestion(pollQuestions.get(4).id);

        indexPoll++;
        costs2Tab.addCell(cellStyleTextBold.getCell(pollQuestions.get(4).text));
        checkBoolean = getBoolean(indexPoll);
        for (int i = 0; i < qCosts.length; i++) {
            costs2Tab.addCell(i + 1 == checkBoolean ? check2Cell : checkCell);
            costs2Tab.addCell(cellStyleText.getCell(qCosts[i].text));
        }

        costs2Tab.addCell(cellStyleText.getCell("Nota: " + cfg.pqrOtherNotes, 5, 1));
        costs2Tab.addCell(cellStyleText.getCell("", 5, 1));
        costs2Tab.addCell(cellStyleTitle.getCell("Aceptación: Acepto y comprendo la información de esta asistencia técnica.", 5, 1));
        costs2Tab.addCell(cellStyleTitle.getCell("Recuerde que el mantenimiento de los gasodomesticos y accesorios son de su \npropiedad y de su responsabilidad.", 5, 1));
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
