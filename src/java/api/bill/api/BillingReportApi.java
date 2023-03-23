package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillCfg;
import api.bill.model.BillInstance;
import api.bill.model.dto.RptBillPayRequest;
import api.bill.rpt.BillClientReports;
import api.bill.rpt.BillReport;
import api.bill.rpt.cguno.BillCgUnoReports;
import api.bill.rpt.fssri.BillFSSRIReports;
import api.sys.model.SysCfg;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.Dates;
import utilities.mysqlReport.MySQLReport;

@Path("/billingReport")
public class BillingReportApi extends BaseAPI {

    @POST
    @Path("/getRptBillsPaid")
    public Response getRptBillsPaid(RptBillPayRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (bi.isTankInstance()) {
                rep = BillReport.getRptTankBillsPaid(bi, req, conn);
            } else {
                rep = BillReport.getRptNetBillsPaid(bi, req, conn);
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "pagos.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getRptMovClient")
    public Response getRptMovClient(@QueryParam("clientId") Integer clientId, @QueryParam("detailed") boolean detailed) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance bi = getBillInstance();
            MySQLReport rep = BillReport.getMovementsReport(bi, clientId, detailed, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "movimientos.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getRptUserReconn")
    public Response getRptUserReconn(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance bi = getBillInstance();
            MySQLReport rep = BillReport.getRptUserReconn(bi, spanId, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "Clientes con Cobro de Reconexión.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getRtpClientNotes")
    public Response getRtpClientNotes(@QueryParam("cred") boolean cred,
            @QueryParam("bank") boolean bank,
            @QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance bi = getBillInstance();
            MySQLReport rep = BillReport.getRtpClientNotes(bi, cred, bank, spanId, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "Notas del cliente.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getAccountsBalance")
    public Response getAccountsBalance(@QueryParam("spanId") Integer spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep = BillClientReports.getAccountsBalance(getBillInstance(), spanId, conn);
            useDefault(conn);
            File file = rep.write(conn);
            return createResponse(file, "deb_cred.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/clientsBalance")
    public Response getClientsBalance(@QueryParam("spanId") Integer spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep = BillClientReports.getClientsBalance(spanId, getBillInstance(), conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "accounts.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/balanceTest")
    public Response getBalanceTest() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = BillClientReports.getBalanceTest(conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "balanceTest.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/rptBillingParams")
    public Response getBillingParams() throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getBillParams(bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "Parametros.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/rptA1Billing")
    public Response getNetSuiReport(@QueryParam("spanId") int spanId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptA1Billing(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "a1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    @GET 
    @Path("/rptGRC1")
    public Response getNetSuiGRC1(@QueryParam("spanId") int spanId)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRC1(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "grc1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    @GET 
    @Path("/rptGRT1")
    public Response getNetSuiGRT1(@QueryParam("spanId") int spanId)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRT1(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "grt1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    @GET 
    @Path("/rptGRCS2")
    public Response getNetSuiGRCS2(@QueryParam("spanId") int spanId)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRCS2(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "grcs2.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    @GET 
    @Path("/rptGRS1")
    public Response getNetSuiGRS1(@QueryParam("spanId") int spanId)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRS1(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "grs1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    @GET 
    @Path("/rptGRCS1")
    public Response getNetSuiGRCS1(@QueryParam("spnBegin") String begin, @QueryParam("spnEnd") String end)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            //BillInstance bi = getBillInstance();
            //useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRCS1(begin, end, conn);
            
            useDefault(conn);
            return createResponse(rep.write(conn), "grcs1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptC1ServiceFaults")
    public Response getFormatSui(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptC1ServiceFaults(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "c1.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptC2Compensations")
    public Response getRptC2Compensations(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptC2Compensations(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "c2.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptC3SchedFaults")
    public Response getRptC3SchedFaults(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptC3SchedFaults(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "c3.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptT1Stations")
    public Response getRptT1Stations() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptT1Stations(conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "t2.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptT2TechSrvResponse")
    public Response getRptT2TechSrvResponse(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptT2TechSrvResponse(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "t2.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptT3Indicators")
    public Response getRptT3Indicators(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptT3Indicators(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "t3.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptT4Measurements")
    public Response getRptT4Measurements(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptT4Measurements(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "t4.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
/*======================= GRC 6 =========================*/
    @GET 
    @Path("/rptGRC6")
    public Response getNetSuiGRC6(@QueryParam("spanId") int spanId)throws Exception{
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRC6(spanId, bi, sysCfg, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "grc1.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
 /*====================================================*/

    @POST
    @Path("/fssri/format1")
    public Response getFssriFormat1(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF1(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "F1G.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/fssri/format2")
    public Response getFssriFormat2(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF2(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "SubG.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/fssri/format3")
    public Response getFssriFormat3(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF3(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "ConG.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/fssri/format6")
    public Response getFssriFormat6(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans, @QueryParam("nameForm") String nameForm) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF6(year, trimester, showOpenSpans, billCfg, conn,nameForm);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "CExento.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
  
    @POST
    @Path("/fssri/GRC8")
    public Response getGRC8(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans, @QueryParam("nameForm") String nameForm) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF6(year, trimester, showOpenSpans, billCfg, conn, nameForm);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "CExento.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
  /*================  GRF1 ==========================*/  
    
     @POST
    @Path("/fssri/GRF1")
    public Response getGRF1(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans, @QueryParam("nameForm") String nameForm) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getGRF1(year, trimester, showOpenSpans, billCfg, conn, nameForm);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "GRF1xento.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    
  /*================== GRF1=======================*/
    
  
    /*================  GRF2 ==========================*/  
    
     @POST
    @Path("/fssri/GRF2")
    public Response getGRF2(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans, @QueryParam("nameForm") String nameForm) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getGRF2(year, trimester, showOpenSpans, billCfg, conn, nameForm);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "GRF2exento.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    
  /*================== GRF2=======================*/
    @POST
    @Path("/fssri/format7")
    public Response getFssriFormat7(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF7(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "TA.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    } 
    
    @POST
    @Path("/fssri/format8")
    public Response getFssriFormat8(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF8(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "TA.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    } 
    
    @POST
    @Path("/fssri/format7Old")
    public Response getFssriFormat7Old(@QueryParam("year") int year, @QueryParam("trimester") int trimester, @QueryParam("showOpenSpans") boolean showOpenSpans) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                useBillInstance(conn);
                BillCfg billCfg = new BillCfg().select(1, conn);
                if (billCfg.fssri == null || billCfg.fssri.isEmpty()) {
                    throw new Exception("Debe definir el código FSSRI");
                }
                File f = BillFSSRIReports.getF7Old(year, trimester, showOpenSpans, billCfg, conn);
                useDefault(conn);
                return createResponse(f, billCfg.fssri + trimester + year + "TA.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/cguno/terc")
    public Response getCgunoTerc(@QueryParam("date") String strDate) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                Date date = Dates.trimDate(Dates.getSQLDateFormat().parse(strDate));
                useBillInstance(conn);
                BillCfg cfg = new BillCfg().select(1, conn);
                File f = BillCgUnoReports.getTerc(date, cfg, conn);
                useDefault(conn);
                return createResponse(f, "terceros.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/cguno/conv")
    public Response getCgunoConv(@QueryParam("date") String strDate) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.isNetInstance()) {
                Date date = Dates.trimDate(Dates.getSQLDateFormat().parse(strDate));
                useBillInstance(conn);
                BillCfg cfg = new BillCfg().select(1, conn);
                MySQLReport r = BillCgUnoReports.getConverted(date, cfg, this, conn);
                useDefault(conn);
                return createResponse(r.write(conn), "convertidos.xls");
            } else {
                throw new Exception("No disponible en facturación tanques");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/pendingMeasures")
    public Response getPendingMeasures() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport r = BillReport.getRptPendingMeasures(conn);
            useDefault(conn);
            return createResponse(r.write(conn), "pendientes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRC3")
    public Response getRptGRC3(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRC3(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRC3.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRC4")
    public Response getRptGRC4(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRC4(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRC4.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/rptGRTT2")
    public Response getRptGRTT2(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = BillReport.getRptGRTT2(spanId, bi, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "GRTT2.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRCS3")
    public Response getRptGRCS3(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRCS3(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRC3.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRCS7")
    public Response getRptGRCS7(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRCS7(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRCS7.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRCS8")
    public Response getRptGRCS8(@QueryParam("initialDate") String initialDate, @QueryParam("finalDate") String finalDate) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRCS8(initialDate, finalDate, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRCS8.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRCS9")
    public Response getRptGRCS9(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRCS9(spanId, getBillInstance(), conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRCS9.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptGRI1")
    public Response getRptGRI1() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (!bi.isTankInstance()) {
                rep = BillReport.getRptGRI1(conn);
            } else {
                throw new Exception("Error en la instancia");
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "GRI1.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
