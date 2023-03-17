package model.billing.constants;

import java.util.SortedMap;
import java.util.TreeMap;

public class Transactions {

    public static final int CAUSA = 1;
    public static final int CAUSA_SERV_OTHER = 23;
    public static final int CAUSA_SERV_CHECK = 17;
    public static final int CAUSA_SERV_CONN = 18;
    public static final int CAUSA_SERV_SUSP_RECONN = 19;
    public static final int CAUSA_SERV_CUT_RECONN = 26;

    public static final int CAUSA_INTE_GLP = 2;//interés mora
    public static final int CAUSA_INTE_SRV = 13;//interés mora

    public static final int CAUSA_FINAN_FEE = 25;

    //intereses de financiación
    public static final int CAUSA_INTE_CRE_OTHER = 14;
    public static final int CAUSA_INTE_CRE_CHECK = 20;
    public static final int CAUSA_INTE_CRE_CONN = 21;
    public static final int CAUSA_INTE_CRE_SUSP_RECONN = 22;
    public static final int CAUSA_INTE_CRE_CUT_RECONN = 27;//ULTIMA

    public static final int CAUSA_CART = 3;
    public static final int CAUSA_SUBSIDY = 16;
    public static final int N_FINAN = 24;
    public static final int N_DEBIT = 4;
    public static final int N_CREDIT = 5;
    public static final int N_AJ_DEBIT = 6;
    public static final int N_AJ_CREDIT = 7;
    public static final int PAGO_BANCO = 8;
    public static final int N_DEU_ANTE = 9;
    public static final int N_ANTICIP = 10;
    public static final int PAGO_ANTICIP = 11;
    public static final int DTO_EDIF = 12;
    public static final int BK_BALANCE = 15;

    public static final SortedMap<Integer, String> tNames;

    static {
        tNames = new TreeMap<>();
        tNames.put(Transactions.CAUSA_CART, "Paso a Cartera");
        tNames.put(Transactions.CAUSA_INTE_GLP, "Causación Interés Mora Consumo");
        tNames.put(Transactions.CAUSA_INTE_SRV, "Causación Interés Mora otros Servicios");
        tNames.put(Transactions.CAUSA_SUBSIDY, "Causación de Subsidio");

        tNames.put(Transactions.CAUSA, "Causación");
        tNames.put(Transactions.CAUSA_SERV_OTHER, "Causación de Servicios Generales");
        tNames.put(Transactions.CAUSA_SERV_CHECK, "Causación de Servicios Revisión");
        tNames.put(Transactions.CAUSA_SERV_CONN, "Causación de Servicios Conexión");
        tNames.put(Transactions.CAUSA_SERV_SUSP_RECONN, "Causación de Servicios Reconexión por Suspensión");
        tNames.put(Transactions.CAUSA_SERV_CUT_RECONN, "Causación de Servicios Reconexión por Corte");

        tNames.put(Transactions.CAUSA_INTE_CRE_OTHER, "Causación Interés de Crédito General");
        tNames.put(Transactions.CAUSA_INTE_CRE_CHECK, "Causación Interés de Crédito Revisión");
        tNames.put(Transactions.CAUSA_INTE_CRE_CONN, "Causación Interés de Crédito Conexión");
        tNames.put(Transactions.CAUSA_INTE_CRE_SUSP_RECONN, "Causación Interés de Crédito Reconexión por Suspensión");
        tNames.put(Transactions.CAUSA_INTE_CRE_CUT_RECONN, "Causación Interés de Crédito Reconexión por Corte");

        tNames.put(Transactions.N_AJ_CREDIT, "Nota de Ajuste");
        tNames.put(Transactions.N_AJ_DEBIT, "Nota de Ajuste");
        tNames.put(Transactions.N_ANTICIP, "Nota de Saldo a Favor");
        tNames.put(Transactions.N_CREDIT, "Nota crédito");
        tNames.put(Transactions.N_DEBIT, "Nota debíto");
        tNames.put(Transactions.N_DEU_ANTE, "Nota deuda Anterior");
        tNames.put(Transactions.N_FINAN, "Nota de Financiación");
        tNames.put(Transactions.PAGO_ANTICIP, "Saldo a Favor");
        tNames.put(Transactions.PAGO_BANCO, "Pago Bancos");
        tNames.put(Transactions.DTO_EDIF, "Descuento por Edificios");
    }

}
