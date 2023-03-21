package api.bill.writers.bill.netZebra;

public class BillSiteTemplate {

    /**
     * Los archivos .nlbl se abren con ZebraDesigner 3 for Developers (es
     * gratuito), con la opción 'test print' se genera el .prn que contiene el
     * código ZPL que se pega aquí
     *
     *
     * UBICAR LAS BARRAS Y ENCERRAR CON "^FXSBAR^FS" - "^FXEBAR^FS"
     *
     * @return
     */
    public static String getHeaderTemplate() {
        return "^XA\n"
                + "~TA000\n"
                + "~JSN\n"
                + "^LT0\n"
                + "^MNN\n"
                + "^MTT\n"
                + "^PON\n"
                + "^PMN\n"
                + "^LH0,0\n"
                + "^JMA\n"
                + "^PR6,6\n"
                + "~SD15\n"
                + "^JUS\n"
                + "^LRN\n"
                + "^CI27\n"
                + "^PA0,1,1,0\n"
                + "^XZ\n"
                + "^XA\n"
                + "^MMT\n"
                + "^PW799\n"
                + "^LL815\n"
                + "^LS0\n"
                + "^FT38,394^A0N,24,23^FH\\^CI28^FDNombre:^FS^CI27\n"
                + "^FT38,423^A0N,24,23^FH\\^CI28^FDDirección:^FS^CI27\n"
                + "^FT124,393^A0N,23,25^FB646,1,6,R^FH\\^CI28^FD[client_name]^FS^CI27\n"
                + "^FT143,422^A0N,23,25^FB627,1,6,R^FH\\^CI28^FD[client_address]^FS^CI27\n"
                + "^FPH,1^FT38,491^A0N,24,23^FH\\^CI28^FDSector:^FS^CI27\n"
                + "^FT115,489^A0N,23,25^FB291,1,6,R^FH\\^CI28^FD[sector]^FS^CI27\n"
                + "^FT39,624^A0N,23,28^FB735,1,6,C^FH\\^CI28^FD[lbl_consums]^FS^CI27\n"
                + "^LRY^FO39,601^GB735,0,29^FS^LRN\n"
                + "^FT39,687^A0N,20,20^FH\\^CI28^FDcons_1^FS^CI27\n"
                + "^FT146,687^A0N,20,20^FH\\^CI28^FDcons_2^FS^CI27\n"
                + "^FT252,687^A0N,20,20^FH\\^CI28^FDcons_3^FS^CI27\n"
                + "^FT358,687^A0N,20,20^FH\\^CI28^FDcons_4^FS^CI27\n"
                + "^FT464,687^A0N,20,20^FH\\^CI28^FDcons_5^FS^CI27\n"
                + "^FT570,687^A0N,20,20^FH\\^CI28^FDcons_6^FS^CI27\n"
                + "^FT39,661^A0N,20,20^FH\\^CI28^FDrank_1^FS^CI27\n"
                + "^FT145,661^A0N,20,20^FH\\^CI28^FDrank_2^FS^CI27\n"
                + "^FT251,661^A0N,20,20^FH\\^CI28^FDrank_3^FS^CI27\n"
                + "^FT357,661^A0N,20,20^FH\\^CI28^FDrank_4^FS^CI27\n"
                + "^FT463,661^A0N,20,20^FH\\^CI28^FDrank_5^FS^CI27\n"
                + "^FT569,661^A0N,20,20^FH\\^CI28^FDrank_6^FS^CI27\n"
                + "^FT676,687^A0N,20,20^FH\\^CI28^FDcons_7^FS^CI27\n"
                + "^FT675,661^A0N,20,20^FH\\^CI28^FDrank_7^FS^CI27\n"
                + "^FT39,732^A0N,20,20^FB128,1,5,C^FH\\^CI28^FDLectura Actual:^FS^CI27\n"
                + "^FT220,732^A0N,20,20^FB144,1,5,C^FH\\^CI28^FDLectura Anterior:^FS^CI27\n"
                + "^FT221,757^A0N,20,20^FH\\^CI28^FD[last_read]^FS^CI27\n"
                + "^FT39,756^A0N,20,20^FH\\^CI28^FD[cur_read]^FS^CI27\n"
                + "^FT53,250^A0N,18,18^FB318,1,5,C^FH\\^CI28^FDNIT: 891202203-9^FS^CI27\n"
                + "^FT53,267^A0N,18,18^FB318,1,5,C^FH\\^CI28^FDCra 25 No 15 - 29 Pasto - Nariño^FS^CI27\n"
                + "^FT53,284^A0N,18,18^FB318,1,5,C^FH\\^CI28^FDLínea gratuita nal. 018000914080 – #876^FS^CI27\n"
                + "^FT53,302^A0N,18,18^FB318,1,5,C^FH\\^CI28^FDAgentes retenedores de IVA.^FS^CI27\n"
                + "^FO92,45^GFA,4267,5568,32,gN0IAH8gQ0gM0J54gR0gL0MAgQ0gK01I01I5gQ0gK0AH808JA8gO0gJ054J01I5gP0gI02A8J02JAgO0gI01L01515gP0gI0HAL0JA8gN0gI054L0J5gO0gI0AM0JA8gN0gQ01515gO0gQ0KAgN0gQ0J54gN0gQ0KAgN0gQ051514gN0gQ0KA8H08gJ0gQ0J54I04gJ0gP02KAI02gJ0gP01J54gN0gP02KAI0A8gI0gP01J54I01gJ0gP02KAI028gI0gP015151J01gJ0gP0KA8I02AgI0gP0K5J014gI0gO02KA8I02AgI0gO01I515J015gI0gO0LA8I02A8gH0gO0K54J015gI0gO0OA02HAgH0gO05151505H10H1gI0gL08MA8MA8gG0gL014L50I54H54gH0gK02NA2JA2HAgH0gJ0H150K541I50154gH0gI0YAgH0gI0I54L54J54H54gH0gH02UA8IAgH0gH01515151515I15151514gH0gG0gGA8gG0gG0J50L54J541H54gH0g02JA2RA2IAgH0g051H541I51H5H1I50I5gI0Y0KA8WA8gG0Y0J540L54K54I54gH0X02KA2WA8gG0X01515H0151515101515I151gI0X0gIA8AgG0X0I540M54L0J504gG0W02JA2NAL0LAgG0W01H5H0N5M0J514gG0W0SA8K02LAgG0gG0N54L01I5414gG0W0H2PA8L02JA2AgG0W0H15151515151M0151501gH0W02QAM0LA8gG0W01P54L01J5454gG0W02PA8L02JA8A8gG0W01I51I515N0151H501gH0W02OA8M0KA8A8gG0W01N54N0J5401gH0W02OAM02KA02gH0W0H15151515N015151gK0W02NA8M0KA8H08gG0W01M54M01K5gK0X0MA8L0H2KAI08gG0X0L54L0101J54gK0X0LAH8I0IA8KA8H028gG0X0K54K0H541K5gL0X02KAJ0PA8H02gH0X01515K0H1510151514gL0X0KAI0SA80A8gG0X01H54J0J54L505H04gH0X02HAJ02SA828gH0gK01I501H51H541501gI0gJ0KA8OA8A8gH0gJ0J540L50H5H4gI0gI02KA2LA2JAgI0gI0H15150H15151015H1gJ0gH02RA8KAgI0gH01J54L540H545gJ0gH0KA2MA2JA8gI0gH0J501L501H514gJ0gG02JA8MA8KAgJ0gG01I540L54H05454gJ0gG02IA2NAH02HA8gJ0gG0H1H0H1515151J01gL0gG02A8OAI0IA8gJ0gJ0N5K054gK0gH0PAJ02A8gK0gH01H51I515K01gM0gH0OA8J0HAgL0gI0M54J015gM0gI0MAK02AgM0gI0H15151L05gN0gJ0KA8J0HA8gM0gJ01I5L054gN0gJ02IAK02AgO0gS01gP0gR0HA8gO0gR04gQ0gQ02gR0iJ0::::::::J8I08A8H0AHEH80AH8I0J8A8A8AH8J0AFE8J0I8I0AFE8M0I54H01H54H05HFCH0H54H01P54I017HF4J0H5J05HFDM03HFAH03HFE03JF80HFEH03PFEI0JFEI03HF8H03JF8L07HFCH01HFC07JFC07FCH03PFCH017JFI01HFCH07JFCL03HFEH03HFE0KFE8HFE80BPFE80BKF8H03HFEH0KFEL07HFCH01HFC1LF0IFH05QFH07KFCH01HFCH0LFL03HFEH03HFE3LF8IF803QFH0IFBHFEH03HFE03HFABHF8K03HFEH07HFC7HF01HFC7HFH03HF57HF57IF01HFC07HFH07HFE01HF01HFL0IFE80IFEHFE8AHFEIFC03FEABFEABIF83HFH83HF80IFE83FE80HF8K07IFH07KFCH07FDIFC07FC01FC07IF01HFI0HFH07IF01FCH07FCK03HFEH0LF8H03KFE03FE03FE03IF83FEI0HF80JF83FEH03F8K07IFH07KFI01LF03FC01FE07F7FC7FCI0H1H0HF7F01FCH0H1L03FBF80FEIFE8H03LF8BFE03FE0HFBFEHFEJ08H0FEHF83HF8H08L07FDFH0FDJFI01LF07FC01FC07F1FC7FCL01FC7F81HFCO03FBF80FAIFEJ0LF83FE03FE0FE3FEHF8L03FE3F83IFAN07F1F01FD7HFCJ07IF7F43FC01FE1FE1FD7FM01FC3FC17HFDN0HFBF83FBIFE8I0JFBFE3FE03FEBFE0JF80K83FCBFE0JFE8L07F9FC1F1IFCJ0JF1FC5FC01FC1FC1JF801I507FC1FC07JF4L03F8FA3FBIFEJ0JFBFE3FE03FE3FE0JF803IF83F83FE03JFEL07F87C1F1IFCJ07IF1HF1FC01FE1FC07IFH01IF87F01HF017JFL03F8FEBF9IFEJ0JF8HFBFE03FE3FC0JF803IF8HF80FE80BJF8K07F87C7F1IFEJ0JF07F7FC01FE7FC07IF801IFC7F01HFI05IFCK03F8FE3E3IFEJ0JF8JFE03FEHFHAJF803IF8HFABHF8H02IFEK07F07C7F1IFEI01JF07IFC01FE7IF7IF40157F9IF7HFJ017FCK0BF8FEFE3IFE8H03JF83IFE03FENFEI0HFBLFEA8H0HFEK07F87F7C1JFI01JF01IFC01PFCI07FDNFCH01FCK03F83HFE3JF8H03JF83IFE03PFEI0QFAH03FEK07F81HFC1JFCH07FDHFH07HFC01QFI07PFCH01FCK03F83HFE3FEHFE80HFEHF80IFE03QFA0BQFEH03FEK07F81HFC1FC7HF45HFC7FH07HFC01JFH45JFD07JFCH47JFH07FCK03F83HFA3FE3LF8HF803HFE03IFEH02QFAH03JFEBHF8K07F01HF01FC1LF07FH01HFC01IFCI0QFI01KF7HFL0BF83HF80FEAKFE8HF803HFE03IFEI0QF8H03NF8K07F81HF81FC07JFC0HFH01HFC01IFCI07FDJF0IFI01HF7JFDL03F80HF83FE03JF80HF8H0HFE03IF8I0HFEJF8IF8I0HFBJFAL015H0H50174H07HFCH075I0H5H01I5J0H541HFC0I5J0H507IFM00H80I80H8H0AHE8H0I8H0I8H0J8I0I80AE80I8J0I8AHEH8L0iJ0::0HA8N0828U08I0AV08R00H54M0404g04V04R02IAM02828U028H028U0AR01H01M01X01g04R0280AH8K0AH8K08I08L028I0808I08I08I080AH0H8N014O05X01P04O04H04O02AI0A0282A2A202JA02HA02A8H02HA8282A80HA80HA02HA0A82HA02AK01J04010101010515150H14051I0I1010H10I10H15010105051401L0IA80A828IA8A8KA8A8A8A8A80KA8IA2IA2HA8A8A8A8A8A8HAK00H5H040104050504050104051404H0540450404140410145I04I0414L00IA0A02HA0282HA0A02HA02HAJ0280A2HA0A280A282A2AH0AI0A2AL0H015040H1H010H14010H1H0H15J01H04H1H051H04101414H04H0141M0I0A8A0A8A0A8A8A0A82A8028HA8H0280MA80NA8A0JA8L0I05040H14050504040104010H54H05H0451I414045I40H504050414L028028A02HA0282HA0A02HA028H2AH0280A2HAH0280A28I02A8A0A0H28L01H01040H1H010H1405010401I05H01H04H1I01I01K010404041M02A8A8A8IA0A8IA0A828A8AH80A80A8IA8A0A280HA80HA828A8A0HA8L01405040514050514040104040404H0145450404140414145010404041M02IA02HA2A02A2HA0A0282HA0IAH02HA828IA2A0A2HA82HA8KA28L0015H01501H010H1J010H1H0H1J0H1010H101J01H0H1H010H101M00HA8H0A80802HAH80AH080A80HA8I0AH8280A8280AI8H0A80A8HA828L0iJ0\n"
                + "^FT39,457^A0N,24,23^FH\\^CI28^FDDocumento:^FS^CI27\n"
                + "^FT180,456^A0N,23,25^FB203,1,6,R^FH\\^CI28^FD[document]^FS^CI27\n"
                + "^FT583,732^A0N,20,20^FB174,1,5,C^FH\\^CI28^FDConsumo Corregido:^FS^CI27\n"
                + "^FT583,757^A0N,20,20^FH\\^CI28^FD[corr_cons]^FS^CI27\n"
                + "^FT602,98^A0N,23,23^FB174,1,6,R^FH\\^CI28^FD[bill_num]^FS^CI27\n"
                + "^FT384,99^A0N,24,23^FH\\^CI28^FDFactura No:^FS^CI27\n"
                + "^FT386,128^A0N,24,18^FH\\^CI28^FDPeriodo Facturación^FS^CI27\n"
                + "^FT38,550^A0N,24,23^FH\\^CI28^FDCódigo NIU:^FS^CI27\n"
                + "^FT386,186^A0N,24,23^FH\\^CI28^FDFacturas Atrasadas:^FS^CI27\n"
                + "^FT489,127^A0N,23,20^FB287,1,6,R^FH\\^CI28^FD[bill_span]^FS^CI27\n"
                + "^FT152,549^A0N,23,23^FB254,1,6,R^FH\\^CI28^FD[code]^FS^CI27\n"
                + "^FT585,185^A0N,23,23^FB191,1,6,R^FH\\^CI28^FD[months]^FS^CI27\n"
                + "^FPH,1^FT38,786^A0N,17,18^FH\\^CI28^FD[fault_description]^FS^CI27\n"
                + "^FT386,244^A0N,24,23^FH\\^CI28^FDSuspensión Desde:^FS^CI27\n"
                + "^FT602,243^A0N,23,23^FB174,1,6,R^FH\\^CI28^FD[susp_date]^FS^CI27\n"
                + "^FT402,732^A0N,20,20^FB155,1,5,C^FH\\^CI28^FDConsumo Medido:^FS^CI27\n"
                + "^FT402,756^A0N,20,20^FH\\^CI28^FD[med_cons]^FS^CI27\n"
                + "^FT386,273^A0N,24,23^FH\\^CI28^FDPágue Antes de:^FS^CI27\n"
                + "^FT615,272^A0N,23,23^FB161,1,6,R^FH\\^CI28^FD[limit_date]^FS^CI27\n"
                + "^FT386,158^A0N,24,23^FH\\^CI28^FDFecha de Expedición:^FS^CI27\n"
                + "^FT602,156^A0N,23,23^FB174,1,6,R^FH\\^CI28^FD[created]^FS^CI27\n"
                + "^FT27,758^A0B,17,18^FH\\^CI28^FDVigilado por la superintendecia de servicios públicos^FS^CI27\n"
                + "^FT35,354^A0N,23,28^FB735,1,6,C^FH\\^CI28^FDInformación General^FS^CI27\n"
                + "^LRY^FO35,331^GB735,0,29^FS^LRN\n"
                + "^FT421,457^A0N,24,23^FH\\^CI28^FDTeléfono:^FS^CI27\n"
                + "^FT556,456^A0N,23,25^FB213,1,6,R^FH\\^CI28^FD[phone]^FS^CI27\n"
                + "^FPH,1^FT421,490^A0N,24,23^FH\\^CI28^FDEstrato:^FS^CI27\n"
                + "^FT508,489^A0N,23,25^FB260,1,6,R^FH\\^CI28^FD[stratum]^FS^CI27\n"
                + "^FPH,1^FT39,519^A0N,24,23^FH\\^CI28^FDMunicipio del Servicio:^FS^CI27\n"
                + "^FT293,519^A0N,23,25^FB476,1,6,R^FH\\^CI28^FD[city_name]^FS^CI27\n"
                + "^FT421,549^A0N,24,23^FH\\^CI28^FDMedidor:^FS^CI27\n"
                + "^FT515,549^A0N,23,23^FB254,1,6,R^FH\\^CI28^FD[meter_num]^FS^CI27\n"
                + "^FT626,214^A0N,23,23^FB150,1,6,R^FH\\^CI28^FD[bill_ref]^FS^CI27\n"
                + "^FT386,215^A0N,24,23^FH\\^CI28^FDReferencia de Pago:^FS^CI27\n"
                + "^FT35,580^A0N,24,23^FH\\^CI28^FDEstado:^FS^CI27\n"
                + "^FT152,579^A0N,23,23^FB254,1,6,R^FH\\^CI28^FD[client_status]^FS^CI27\n"
                + "^FT386,307^A0N,28,28^FH\\^CI28^FDTotal a Pagar:^FS^CI27\n"
                + "^FT615,306^A0N,28,28^FB161,1,7,R^FH\\^CI28^FD[total]^FS^CI27\n"
                + "^FT602,68^A0N,23,23^FB174,1,6,R^FH\\^CI28^FD[cycle]^FS^CI27\n"
                + "^FT384,69^A0N,24,23^FH\\^CI28^FDCiclo:^FS^CI27\n"
                + "^PQ1,0,1,Y\n"
                + "^XZ\n"
                + "";
    }

    /**
     * UBICAR EL COD DE BARRAS Y ENCERRAR CON variable ;cod_bar"
     */
    public static void getFooterTemplateV(StringBuilder sb, String[] banks) {
        sb.append("^XA\n"
                + "~TA000\n"
                + "~JSN\n"
                + "^LT0\n"
                + "^MNN\n"
                + "^MTT\n"
                + "^PON\n"
                + "^PMN\n"
                + "^LH0,0\n"
                + "^JMA\n"
                + "^PR6,6\n"
                + "~SD15\n"
                + "^JUS\n"
                + "^LRN\n"
                + "^CI27\n"
                + "^PA0,1,1,0\n"
                + "^XZ\n"
                + "^XA\n"
                + "^MMT\n"
                + "^PW799\n"
                + "^LL1199\n"
                + "^LS0\n"
                + "^FO16,35^GFA,194,384,96,F0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0EI0:::\n"
                + "^BY3,3,208^FT265,1151^BCB,,Y,N\n"
                + "^FH\\^FD>;cod_bar^FS\n"
                + "^FT362,1151^A0B,28,28^FH\\^CI28^FDRef. de Pago^FS^CI27\n"
                + "^FT432,1151^A0B,28,28^FH\\^CI28^FDFactura No.^FS^CI27\n"
                + "^FT467,1151^A0B,28,28^FH\\^CI28^FDPeriodo^FS^CI27\n"
                + "^FT502,1151^A0B,28,28^FH\\^CI28^FDPagar Hasta^FS^CI27\n"
                + "^FT537,1151^A0B,28,28^FH\\^CI28^FDTotal^FS^CI27\n"
                + "^FT362,953^A0B,28,28^FH\\^CI28^FD[bill_ref]^FS^CI27\n"
                + "^FT432,953^A0B,28,28^FH\\^CI28^FD[bill_num]^FS^CI27\n"
                + "^FT467,953^A0B,28,28^FH\\^CI28^FD[short_span]^FS^CI27\n"
                + "^FT502,953^A0B,28,28^FH\\^CI28^FD[limit_date]^FS^CI27\n"
                + "^FT537,953^A0B,28,28^FH\\^CI28^FD[total]^FS^CI27\n"
                + "^FO651,813^GFA,3442,5408,16,gR0R07CX0N02803HF8I0AS0N07C03HFCW0N0FC0IFEI0AS0M01FC07IFW0M03FC0JF8H0I2Q0M07FC1JF8V0M0HFC3JF8H0IA8P0M0HFC1HF5FCV0M0HF83FE1FEV0L01FE03FC0FCV0L01FE03FC0FEH0HA8Q0L01FC07FC07CV0L03F807F80FEH0I2Q0L01F807FH07CV0L03F80HF80FEH0H808P0L01FC0HFH0FCV0L03F80HFH0FEH082R0L01FC1HF01FCV0M0FEBFE0BFEH08AH8P0M0JFC17FCV0M0JFE3HF8I0H2Q0M07IFC1HF8V0M0JF83HF8J08Q0M03IF01HFW0M03IF03HF8H08S0N07FC01HFCV0N03F803HFC0JA8P0R01HFCV0R0BHF8K2Q0Q01IFCV0P02JFCV0P07JFCV0O03KF8H020AQ0N01LFW0N0LFEI0A2A8P0M07LFX0M0LFEK0208P0M0JFD7FX0M0JF8FEJ08A08P0M0IF407EX0M0HFAH0FEJ0AH2Q0M0HFCH07EX0M0IF80FEJ0A828P0M0JF07EX0M0JFEFEX0M07LFX0M03KFE8I02A2Q0N05KFCW0O0LF8H0I2Q0O017JFCV0P0BJFCH08A08P0Q07IFCV0Q02IF8I0208P0R01HFCV0Q0H8HFCH08H28P0O041HDHFCV0N03C0JF8H0A2AQ0N07C1JFCV0N0FC0JFCI0A8Q0M01FC1JFCV0M03FE0JF8V0M07FC1IFCW0M0HFC0IFEI0IA8P0M0HFC1FC7FW0M0HF80F83F8H0AH2Q0L01FE01FC1F8V0L01FEH0FC3F8H08S0L01FC01FC1FCV0L03F8H0F80FE02T0L01FC01FC0FCV0L03FCH0A80FEH0IA8P0L01FCK0FCV0L03FCK0FEH0H2AQ0L01FCJ01FCV0M0FEJ03FEI0808P0M0HFJ03FCV0M0HF8I0HFCH0I2Q0M07FCH01HFCV0M0IFHAIF8H0HA28P0M07MFW0M03MFI0AH2Q0M01MFW0N0MFCH08A08P0N07LFCV0N03LF8I0H28P0O07KFCV0O03KFCH0IA8P0P01JFCV0P03JF8H0H2AQ0O01KFCV0O0LF8080I8P0N07KFCW0M03LF8020I2Q0M07LFX0M0LFEI0K8P0M0JF47EX0M0IFA0FEJ0H2R0M0HFCH07FX0M0HFEH0FEJ0IA8P0M0IFH07EX0M0IFE8FEJ02028P0M0JFC7FX0M0LFEJ08H08P0M0LFEX0M0MFAI0802Q0M0FD7KFW0M0FEBKFC0JA8P0M0FC07JFCV0M0FE02JF80J2Q0M0FCH017HFCV0M0FEI0BHFCH0808Q0M0FCI01HFCV0M0NF8V0M0NFCV0::M0NF8V0M0NFCV0M0NFCJ08Q0M0NFCV0M0FELA8H0H2AQ0M0FCgI0M0FEO082H8P0M0FCgI0M0FEP02R0M0FCgI0M0FEO08AH8P0M0FCgI0M0FELA8H0AH2Q0M0NFCV0M0NFCH0A828P0M0NFCV0M0NF8V0M0NFCV0M0NFCH0IAQ0M0NFCV0M0KBIF8H0AH2Q0Q01IFCV0Q0BIF8H08H08P0Q0IFCW0P03IF8W0P07IFX0O03IF8J08028P0O07IFY0N03IFEK0H2AQ0N07IFg0N0IFEM0A8Q0M05IFgG0M0IFEgG0M0IFCgG0M0NFCH0IA8P0M0NFCV0M0NF8H0AS0M0NFCV0M0NFCH08S0M0NFCV0M0NF8V0M0H57IFD54V0N08JFH8I0IA8P0N01JFCX0N07KF8I0A2AQ0N0LFCW0M03LFEI08S0M01LFEW0M03MFW0M07HFH57HFW0M0HFE80BHF8H0A08Q0M0HFCI07FCV0M0HFJ03FCH0H2AQ0L01FEJ01FCV0L01FEJ03FEI0AH8P0L01FCJ01FCV0L01FEK0FEV0L01FCK0FCV0L03FCK0FE28IA8P0L01FCJ01FCV0L03FEJ01FEH0202Q0L01FCJ01FCV0M0FEJ03FEH08H08P0M0HFJ07FCV0M0HF8I0HF80J28P0M07FDH01HF8V0M0IFEJF80JA8P0M07MFW0M03LFEW0M01LFCW0N0LFCI08S0N07KFX0N03JFEJ02S0N01JFCX0M0NF8H0A8A8P0M0NFCV0M0NF8H0H2AQ0M0NFCV0M0NFCI0I8P0M0NFCV0M0NF8H0I2Q0M0HFDK54V0M0HFE80808I0IA8P0M0IFCgG0M0JFEN02Q0M07JFDY0M0BKFE8L08P0N01KFCW0O03KF8J02Q0P01JFCV0Q0BIFCH0IA8P0R05HFCV0R02HF8H0I2Q0R07HFCV0Q0JFCV0P05JFCV0O0BKF8020H2Q0N05KF4W0M0BKFE8H080HA8P0M0KFCY0M0JFAJ020202Q0M0IF4gG0M0HFEL8282808P0M0HFDKFCV0M0NF820202Q0M0NFCV0M0NFC2HA8A8P0M0NFCV0M0NF802A0AQ0M0NFCV0M0HEFIEFE80H808Q0gR0::::::::::Q0IAI8U0gR0Q02A2A02U0gR0M0KA8HA80A8S0gR0L0I2AI2K02S0gR0K02NA8I02AR0gR0J0H2A2A2A2A2AJ028Q0gR0J0H808MA8I08A8P0gR0O0M20J2Q0gR0N0A8MA2JA8O0gR0M02A282A2A2A0H2828O0gR0M02IA8LA2JAO0gR0M0H2AI2AI2A0H282O0gR0M0KA8LA2A8A8N0gR0J02H02A2A2H0A2A2A0A0H2N0gR0H02IA8KAH02JAH8H0AN0gR0H0K20J2I0K2I02N0gR002LA8IAI02JA8H028M0gR002A2A2A2A2A2J0A2A2AI08M0gR00NA8HAJ0KA8H08M0gR02AI2AI2AH2K0AI28H02M0gR0PA8AJ08KAH02M0gR0H2A2A2A2A2A2J0H2A2A2H02M0gR0QA8J028JA8O0gR0H2I0M2J0M2O0gR0A8J0LA8I02LAO0gR0AK0H2A2A2AI0H282A2AO0gR0AL0LA8I0LA8N0gR02M02AI2AI0AI2AH2N0gR0AM0LA8H0HA8JAN0gR02M082A2A2AH02AI2A2N0gR02M0MA802HA2IA8M0gR02M020K2H0I20I2N0gR008L0A8KA8H0HA8IA8M0gR0N0J2A2A2H0H2A2A2N0gR00AL02A8KA802KAN0gR002M0AI2AI20L2N0gR0028L0NAH0HA8A8N0gR0H0AL02A0A2A2AH02A0AO0gR0O0HA8KA802A8AO0gR0O0O2H02Q0gR0O02NAH0AH8O0gR0P0A2A2A2A2T0gR0P0HA8KA8S0gR0P0H20AI2AT0gR0P02MA8S0gR0Q02A2A2A28S0gR0Q0MA8S0gR0Q0M2T0gR0R0A2HA8U0gR0\n"
                + "^FT36,666^A0B,20,20^FH\\^CI28^FDRECAUDADOR^FS^CI27");

        int p = 359;
        for (String bank : banks) {
            sb.append("^FT").append(p).append(",729^A0B,23,25^FH\\^CI28^FD").append(bank).append("^FS^CI27\n");
            p += 20;
        }

        sb.append("^PQ1,0,1,Y\n"
                + "^XZ");
    }

    /**
     * la linea del código de barras debe quedar así: ^FH\^FD>;cod_bar^FS del FO
     * de bancos se toman el x, y, la línea se borra y los valores se se pasan a
     * ^FOX,Y^A0N,17,15^FH\\^CI28^FB400,30,0,L,0^FD luego del for de bancos
     * tiene que ir ^FS^CI27
     */
    public static void getFooterTemplateH(StringBuilder sb, String[] banks) {
        sb.append("^XA\n"
                + "~TA000\n"
                + "~JSN\n"
                + "^LT0\n"
                + "^MNN\n"
                + "^MTT\n"
                + "^PON\n"
                + "^PMN\n"
                + "^LH0,0\n"
                + "^JMA\n"
                + "^PR6,6\n"
                + "~SD15\n"
                + "^JUS\n"
                + "^LRN\n"
                + "^CI27\n"
                + "^PA0,1,1,0\n"
                + "^XZ\n"
                + "^XA\n"
                + "^MMT\n"
                + "^PW799\n"
                + "^LL504\n"
                + "^LS0\n"
                + "^FO16,35^GFA,194,384,96,F0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0HF0EI0:::\n"
                + "^BY2,3,160^FT44,225^BCN,,Y,N\n"
                + "^FH\\^FD>;cod_bar^FS"
                + "^FO38,281^A0N,17,15^FH\\^CI28^FB400,30,0,L,0^FD");

        for (int i = 0; i < banks.length; i++) {
            String bank = banks[i];
            sb.append(bank);
            if (i == 0) {
                sb.append(" ");
            } else if (i < banks.length - 1) {
                sb.append(" | ");
            }
        }
        sb.append("^FS^CI27\n"
                + "^FT502,294^A0N,20,20^FH\\^CI28^FDRef. de Pago^FS^CI27\n"
                + "^FT502,319^A0N,20,20^FH\\^CI28^FDFactura No.^FS^CI27\n"
                + "^FT502,344^A0N,20,20^FH\\^CI28^FDPeriodo^FS^CI27\n"
                + "^FT502,369^A0N,20,20^FH\\^CI28^FDPagar Hasta^FS^CI27\n"
                + "^FT502,394^A0N,20,20^FH\\^CI28^FDTotal^FS^CI27\n"
                + "^FT646,294^A0N,20,20^FB106,1,5,R^FH\\^CI28^FD[bill_ref]^FS^CI27\n"
                + "^FT646,319^A0N,20,20^FB106,1,5,R^FH\\^CI28^FD[bill_num]^FS^CI27\n"
                + "^FT646,344^A0N,20,20^FB106,1,5,R^FH\\^CI28^FD[short_span]^FS^CI27\n"
                + "^FT646,369^A0N,20,20^FB106,1,5,R^FH\\^CI28^FD[limit_date]^FS^CI27\n"
                + "^FT646,394^A0N,20,20^FB106,1,5,R^FH\\^CI28^FD[total]^FS^CI27\n"
                + "^PQ1,0,1,Y\n"
                + "^XZ");
    }

    public static String getBodyTemplate() {
        String body
                = "^XA\n"
                + "^MNN\n"
                + "^MMT\n"
                + "^PW799\n"
                + "^LL@height\n"
                + "^LS0\n"
                + "@content_body"
                //                + "^FT16,111^A0N,23,23^FH\\^CI28^FDParámetros del Servicio^FS^CI27\n"
                //                + "^LRY^FO16,88^GB608,0,29^FS^LRN\n"
                //                + "^FT16,153^A0N,23,25^FH\\^CI28^FDparam_label^FS^CI27\n"
                //                + "^FT439,153^A0N,23,23^FB183,1,6,R^FH\\^CI28^FDparam_value^FS^CI27\n"
                //                + "^FPH,3^FT16,194^A0N,17,18^FH\\^CI28^FDbank_items^FS^CI27\n"
                //                + "^FO17,167^GB607,0,4^FS\n"
                //                + "^FT16,71^A0N,23,25^FH\\^CI28^FDitem_label^FS^CI27\n"
                //                + "^FT439,71^A0N,23,25^FB183,1,6,R^FH\\^CI28^FDitem_value^FS^CI27\n"
                //                + "^FT16,32^A0N,23,23^FH\\^CI28^FDDescripción del Cobro^FS^CI27\n"
                //                + "^LRY^FO16,9^GB608,0,29^FS^LRN\n"
                + "^PQ1,0,1,Y\n"
                + "^XZ";
        return body;

    }
}