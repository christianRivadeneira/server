package model.billing.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Accounts {

    //c Cliente, e Empresa
    public static final int C_CONS = 1;//consumo cliente
    public static final int C_CONS_SUBS = 24;//consumo subsidiado cliente
    public static final int C_REBILL = 32;//refacturaciones *****ULTIMO
    public static final int C_BASI = 2;//cargo basico
    public static final int C_CONTRIB = 22;//contribución
    public static final int C_RECON = 5;//reconexion
    public static final int C_CUOTA_SER_CLI_GLP = 3;//servicios por cliente
    public static final int C_CUOTA_SER_CLI_SRV = 19;//servicios por cliente
    public static final int C_CUOTA_SER_EDI = 4;//servicios edificio
    public static final int C_CUOTA_FINAN_DEU = 27;//cuota de financiación del mes
    public static final int C_CUOTA_INT_CRE = 20;//interes de crédito (srv y finan), no mora, se cobran primero acá y luego pasan a una cartera porque y no direcamente porque no se cobran vencidos.

    public static final int C_CAR_GLP = 7;//cartera mora
    public static final int C_CAR_SRV = 17;
    public static final int C_CAR_FINAN_DEU = 28;//cartera de financiaciones no pagadas
    public static final int C_CAR_CONTRIB = 30;//cartera de contribuciones
    public static final int C_CAR_INTE_CRE = 21;//aquí se coloca los intereses de financiación no pagados, C_CUOTA_INT_CRE
    public static final int C_CAR_OLD = 8;

    public static final int C_INT_GLP = 6;//interes
    public static final int C_INT_SRV = 18;
    public static final int C_INT_FINAN_DEU = 29;// Los interes que se generan de C_CAR_FINAN_DEU 
    public static final int C_INT_CONTRIB = 31;//intereses de contribuciones no pagadas
    public static final int C_INT_OLD = 9;

    public static final int E_ING_OP = 12;//ingresos operativos //no sirve para la validacion de deuda del cliente
    public static final int E_CONTRIB = 23;//contribuciones por pagar al minminas
    public static final int E_SUBS = 25;//subsidios por cobrar al minminas
    public static final int E_INTER = 13;
    public static final int E_AJUST = 14;//ajuste a la 50tena
    public static final int BANCOS = 15;

    public static final int C_ANTICIP = 16;//pagos anticipados
    public static final int C_FINAN_DEU_POR_COBRAR = 26;//financiación por cobrar

    public static final Map<Integer, String> accNames;

    public static final List<Integer> anticipAccs;

    static {
        accNames = new HashMap<>();
        accNames.put(Accounts.C_CONS, "Consumo");
        accNames.put(Accounts.C_CONS_SUBS, "Consumo Subsidiado");
        accNames.put(Accounts.C_CONTRIB, "Contribución de Solidaridad");
        accNames.put(Accounts.C_REBILL, "Refacturación");
        accNames.put(Accounts.C_BASI, "Cargo Fijo");
        accNames.put(Accounts.C_CUOTA_SER_CLI_GLP, "Serv. Cliente Consumo");
        accNames.put(Accounts.C_CUOTA_SER_CLI_SRV, "Serv. Cliente Otros Servicios");
        accNames.put(Accounts.C_CUOTA_FINAN_DEU, "Cuota de Financiación de Deudas");
        accNames.put(Accounts.C_CUOTA_INT_CRE, "Interés de Crédito");
        accNames.put(Accounts.C_CUOTA_SER_EDI, "Servicios Edificio");
        accNames.put(Accounts.C_RECON, "Reconexiónes");
        accNames.put(Accounts.C_CAR_GLP, "Cartera Consumo");
        accNames.put(Accounts.C_CAR_SRV, "Cartera otros Servicios");
        accNames.put(Accounts.C_CAR_FINAN_DEU, "Cartera de Finan. de deudas no pagadas");
        accNames.put(Accounts.C_CAR_CONTRIB, "Cartera Contribuciones");
        accNames.put(Accounts.C_CAR_INTE_CRE, "Cartera Intereses de Crédito");
        accNames.put(Accounts.C_CAR_OLD, "Cartera Anterior");
        accNames.put(Accounts.C_INT_GLP, "Interés Mora Consumo");
        accNames.put(Accounts.C_INT_SRV, "Interés Mora Otros Servicios");
        accNames.put(Accounts.C_INT_FINAN_DEU, "Interés Mora Finan. Deuda");
        accNames.put(Accounts.C_INT_CONTRIB, "Interés Contribuciones");
        accNames.put(Accounts.C_INT_OLD, "Interés Anteriores");
        accNames.put(Accounts.BANCOS, "Bancos");
        accNames.put(Accounts.C_ANTICIP, "Saldo a Favor");
        accNames.put(Accounts.C_FINAN_DEU_POR_COBRAR, "Finan. de Deudas por Cobrar.");

        anticipAccs = new ArrayList<>();
        anticipAccs.add(C_ANTICIP);
    }
}
