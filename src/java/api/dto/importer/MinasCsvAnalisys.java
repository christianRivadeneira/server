/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.dto.importer;

import java.util.List;

public class MinasCsvAnalisys {

    public int importLogId;
    public List<DtoSaleImportError> errors;

    public MinasCsvAnalisys() {
    }

    public MinasCsvAnalisys(int importLogId, List<DtoSaleImportError> errors) {
        this.importLogId = importLogId;
        this.errors = errors;
    }

}
