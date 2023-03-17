package api.ord.writers;

public class FrmAttachments {

    private boolean allowEdit;

    //Siembra
    public static final int SMB_COMO_DOC = 18;
    public static final int SMB_COMO_VISIT = 19;
    public static final int ORD_ACTIV_PQR = 20;
    public static final int SMB_CTR = 87;
    //Modulo de estacionarios
    public static final int EST_TANK_PHOTO = 21;
    public static final int ORD_TANK_CLIENT = 22; //adjuntos a los clientes de estacionarios
    public static final int EST_TANK = 23;
    public static final int EST_CLIENT_DOC = 24;
    public static final int EST_TEMPLATE = 27;
    public static final int EST_MTO_REG = 28;
    public static final int EST_SALE = 126; //SI SE CAMBIA HACERLO TAMBIÉN EN EL MÓVIL
    public static final int EST_VISIT = 127;  //SI SE CAMBIA HACERLO TAMBIÉN EN EL MÓVIL
    //Subsidios
    public static final int DTO_CLIENT = 41;
    public static final int DTO_NO_SIGNATURE = 99;
    public static final int DTO_IMPORT_LOG = 132;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Mantenimiento de Vehículos
    public static final int VEHICLE = 1;
    public static final int MTO_VEH_PHOTO = 37;
    public static final int MTO_ORDER = 38;
    public static final int MTO_CONTRACTOR = 39;
    public static final int MTO_TRIP = 62;
    public static final int MTO_DRIVER = 63;
    public static final int MTO_ING = 65;
    public static final int MTO_DOC_VH = 71;
    public static final int MTO_DOC_DRIVER = 72;
    public static final int MTO_ACCIDENT = 73;
    public static final int MTO_CHEKLIST = 74;//SI SE CAMBIA CAMBIAR EN EL MOVIL
    public static final int MTO_VEH_TYPE = 75;//
    public static final int MTO_CHK_ORDER = 107;//
    public static final int MTO_SIGN_DRIVER = 108;//SI SE CAMBIA CAMBIAR EN EL MOVIL Y EN MtoChkLst EN SERVIDOR
    public static final int MTO_SIGN_FORMATS = 116;
    public static final int MTO_SIGN_MANAGER = 110;
    public static final int MTO_FUEL_IMPORT = 118;
    public static final int MTO_SIGN_CITY_EMP = 133;

    //Talento
    public static final int ACCIDENT = 3;
    public static final int LICENCES = 4;
    public static final int PENALTY = 5;
    public static final int SICKLEAVES = 6;
    public static final int DOCS = 7;
    public static final int EMPLOYEE = 8;
    public static final int CONTRACT = 9;
    public static final int EMPLOYEE_PHOTO = 10;
    public static final int CAL_EXTERNAL_DOC = 11;
    public static final int EMP_NOTE = 76;
    public static final int CONTRACTOR_DOC = 77;
    public static final int PER_TEMPLATE_GENERATOR = 91;
    public static final int PER_CANDIDATE_DOCUMENT = 92;
    public static final int PER_CANDIDATE_PHOTO = 93;
    public static final int PER_CANDIDATE_ANALYSIS_TYPE = 94;
    public static final int PER_CANDIDATE_RESULT = 95;
    public static final int PER_CONTRACT_SUPORT = 97;
    public static final int PER_ELEM_DELIV = 106;
    public static final int PER_SIGN_EMP = 116;
    public static final int PER_SIGN_LIST = 117;
    public static final int PER_FLOW_EXTRA = 131;

    //Calidad
    public static final int QUAL_RW = 12;// estos son 
    public static final int QUAL_RO = 13;
    public static final int QUAL_REQ = 14;
    public static final int CAL_ANALYSIS_TYPE = 15;
    public static final int CAL_MEET = 16;
    public static final int CAL_ANALYSIS = 17;
    public static final int CAL_IND = 25;
    public static final int CAL_AUDIT = 26;
    public static final int CAL_AGREE = 31;
    public static final int CAL_FINDING = 32;
    public static final int CAL_PUBLICATION_IMAGE_PREV = 33;
    public static final int CAL_PUBLICATION_IMAGE_FULL = 34;
    public static final int CAL_PUBLICATION_ATTACH = 35;
    public static final int CAL_PROCESS_MAP_4_3 = 36;
    public static final int CAL_PROCESS_MAP_16_9 = 60;
    public static final int CAL_PLAN_ACT_DONE = 30;
    public static final int CAL_PLAN_ACT_CHECK = 40;
    public static final int CAL_RECORDS = 42;
    public static final int CAL_ACTIONS = 59;
    public static final int CAL_BMB_PATTERN = 61;
    public static final int CAL_RISK_EVALS = 67;
    public static final int CAL_RISK_CONTROLS = 68;
    public static final int CAL_RISK_CASES = 70;

    //Sistema
    public static final int SYS_ICONS = 29;//SI SE CAMBIA CAMBIAR EN SERVIDOR EN MtoChkLst
    public static final int SYS_UPDATE_LOG = 69;
    public static final int SYS_CHAT_ATTACH = 79;
    public static final int SYS_GPS_TILE = 82;
    public static final int SYS_SRV_SERVICE = 111;

    //Crm
    public static final int CRM_CLIENT = 43;
    public static final int CRM_CONTACT_PHOTO = 44;
    public static final int CRM_DOCUMENT = 45;
    public static final int CRM_PQR = 46;
    public static final int CRM_ACT_PQR = 47;
    public static final int CRM_ACT = 64;
    public static final int CRM_PROJECT = 66;
    public static final int CRM_DELIVERY = 90;
    public static final int CRM_POLL_CLIENT = 136;//ULTIMO **********************************
    //Equipos
    public static final int EQS_ACT_MTO_FILE = 48;
    public static final int EQS_PHOTO_EQUI = 49;
    public static final int EQS_ORDER = 50;
    public static final int EQS_DOC = 52;
    public static final int EQS_REPAIR = 53;

    //Proveedores
    public static final int PROVIDER = 2;
    public static final int PROV_DOCUMENT = 51;
    public static final int PROV_ANALYSIS_TYPE = 96;
    public static final int PROV_REQ_PPTAS = 114;
    public static final int PROV_REQ_CTRS = 115;
    public static final int CHL_REQ_FILE = 129;
    public static final int PROV_ITEM = 136;

    //Inventario de Cilindros
    public static final int INV_STORE_DOC = 54;
    public static final int INV_STORE = 55;
    public static final int INV_PLANILLA = 56;
    public static final int INV_MOVEMENT = 57;
    public static final int INV_MOV_INTER = 58;

    //APPS
    public static final int DEPLOY_APP = 78;

    //EMAS
    public static final int EMAS_SUPERV_VISIT = 80;
    public static final int EMAS_RECOL_VISIT = 81;
    public static final int EMAS_RECOL_SIGNATURE = 83;
    //El No. 82 lo tiene la gps_tile
    public static final int EMAS_CLIENT_SIGNATURE = 84;
    public static final int EMAS_CLIENT_FILES = 86;

    //FACTURACION
    public static final int BILL_METER_INSPECTION = 85;
    public static final int BILL_CLIENT = 135;

    //HELP_DESK
    public static final int HLP_ATTACHMENT_REQUEST = 88;

    //SERVICE MANAGERS
    public static final int COM_ATTACHMENT_MANAGERS = 89;

    //PLATAFORMAS Y PORTERIAS
    public static final int GT_E_SIGNED = 100;
    public static final int GT_S_SIGNED = 101;
    public static final int GT_E_SIGNED_GLP = 119;
    public static final int GT_S_SIGNED_GLP = 120;
    public static final int GT_RELOAD = 121;
    public static final int GT_RELOAD_IN = 122;
    public static final int GT_RELOAD_OUT = 123;

    //COMERCIAL
    public static final int TRK_SALE_DOC = 124;
    public static final int TRK_TRANSACTION_DOC = 125;
    public static final int TRK_NIF_ILLEGIBLE = 130;

    //ATENCION AL CLIENTE
    public static final int ORD_PQR_CYL = 102;
    public static final int ORD_PQR_TANK = 103;
    public static final int ORD_PQR_OTHER = 104;
    public static final int ORD_POLL_SIGN = 112;
    public static final int ORD_POLL_TECH = 113;
    public static final int ORD_PQR_COM = 134;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
