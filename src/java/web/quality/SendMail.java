package web.quality;

import api.sys.model.SysCfg;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;

@MultipartConfig
@WebServlet(name = "sendMail", urlPatterns = {"/quality/sendMail"})
public class SendMail extends HttpServlet {

    public static final String LINE_BREAK = "\r\n";

    //correo respuesta reply 
    public static void sendMail(String table, String sub, String msg, String dest, boolean copy, String type, Integer procId, Integer officeId, String poolName, String tz, String module, String replyTo) throws Exception {
        sendMail(table, sub, msg, dest, copy, type, procId, officeId, poolName, tz, null, null, null, null, replyTo);
    }

    public static void sendMail(String table, String sub, String msg, String dest, boolean copy, String type, Integer procId, Integer officeId, String poolName, String tz, String module) throws Exception {
        sendMail(table, sub, msg, dest, copy, type, procId, officeId, poolName, tz, null, null, null);
    }

    public static void sendMail(String table, String sub, String msg, String dest, boolean copy, String type, Integer procId, Integer officeId, String poolName, String tz, String[] fileName, File[] destino, Integer modId) throws Exception {
        sendMail(table, sub, msg, dest, copy, type, procId, officeId, poolName, tz, fileName, destino, modId, null, null);
    }

    public static void sendMail(String table, String sub, String msg, String dest, boolean copy, String type, Integer procId, Integer officeId, String poolName, String tz, String[] fileName, File[] destino, Integer modId, String mailFrom, String replyTo) throws Exception {
        Connection con = MySQLCommon.getConnection(poolName, tz);
        try {
            String html = getHtmlMsg(con, sub, msg);
            if (copy) {
                String q = "INSERT INTO cal_mail(`id`, `emp_id`, `sub`, `message`, `state`, `read`, `reg_date`, `mod_date`, `type`, `proc_id`, `office_id`, `mod_id`) "
                        + "SELECT "//0
                        + "null, "//1
                        + "e.id, "//2
                        + "'" + sub + "' ,"//3
                        + "'" + msg + "' ,"
                        + "'inbox', "//5
                        + "false, "//6
                        + "NOW(), "//7
                        + "NOW(), "//8
                        + (!type.isEmpty() ? "\"" + type + "\", " : "NULL, ")//8
                        + (procId != null ? procId + ", " : "NULL, ")//8
                        + (officeId != null ? officeId + ", " : "NULL, ")//8
                        + (modId != null ? modId + " " : "NULL ")
                        + "FROM "
                        + table + " AS e "
                        + "WHERE e.id in (" + dest + ") AND e.active = 1";
                new MySQLQuery(q).executeUpdate(con);
            }
            String mails = new MySQLQuery("SELECT GROUP_CONCAT(DISTINCT mail SEPARATOR ',') FROM " + table + " WHERE id in (" + dest + ") AND active = 1 AND mail IS NOT NULL AND mail <> ''").getAsString(con);

            if (mails != null) {
                sendMail(con, mails, sub, html, msg, fileName, destino, mailFrom, replyTo);
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    public static void sendMail(MailCfg cfg, String mails, String subject, String html, String plain) throws Exception {
        sendMail(cfg, mails, subject, html, plain, null, null);
    }

    public static void sendMail(Connection con, String mails, String subject, String html, String plain) throws Exception {
        sendMail(con, mails, subject, html, plain, null, null);
    }

    // envio de correo reply 
    public static void sendMail(Connection conn, String mails, String subject, String html, String plain, String[] fileName, File[] locaFile, String mailFrom, String replyTo) throws Exception {
        final MailCfg cfg = MailCfg.select(conn);
        sendMail(cfg, mails, subject, html, plain, fileName, locaFile, mailFrom, replyTo);
    }

    public static void sendMail(Connection conn, String mails, String subject, String html, String plain, String[] fileName, File[] locaFile, String mailFrom) throws Exception {
        final MailCfg cfg = MailCfg.select(conn);
        sendMail(cfg, mails, subject, html, plain, fileName, locaFile, mailFrom, null);
    }

    public static void sendMail(Connection conn, String mails, String subject, String html, String plain, String[] fileName, File[] locaFile) throws Exception {
        final MailCfg cfg = MailCfg.select(conn);
        sendMail(cfg, mails, subject, html, plain, fileName, locaFile);
    }

    public static void sendMail(MailCfg cfg, String mails, String subject, String html, String plain, String[] fileName, File[] locaFile) throws Exception {
        sendMail(cfg, mails, subject, html, plain, fileName, locaFile, null, null);
    }

    public static void sendMail(final MailCfg cfg, String mails, String subject, String html, String plain, String fileNames[], File files[], String mailFrom, String replyTo) throws Exception {
        html = html.replaceAll("\n\r", "\n").replaceAll("\r\n", "\n").replaceAll("\r", "\n").replaceAll("\n", "<br />").replaceAll("\\\\\"", "\"");
        html = htmlLetters(html);
        if (cfg.mailActive) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", (cfg.mailAuth ? "true" : "false"));
            props.put("mail.smtp.starttls.enable", (cfg.mailTls ? "true" : "false"));
            if (cfg.ssl) {
                props.put("mail.smtp.socketFactory.port", cfg.smtpPort);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.ssl.enable", true);
            }
            props.put("mail.smtp.host", cfg.smtpHost);
            props.put("mail.smtp.port", cfg.smtpPort);
            Session session;
            if (cfg.mailAuth) {
                session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(cfg.mailUser, cfg.mailPasswd);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress((mailFrom != null ? mailFrom : cfg.mailFrom) + " <" + cfg.mailUser + ">"));
            message.addRecipients(Message.RecipientType.BCC, mails);
            if (replyTo != null && !replyTo.isEmpty()) {
                String[] parts = replyTo.split(",");
                Address[] reply = new Address[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    reply[i] = new InternetAddress(part);
                }
                message.setReplyTo(reply);
            }
            message.setSubject(subject);
            message.setContent(plain, "text/plain");
            message.setContent(html, "text/html");
            if (fileNames != null && files != null) {
                Multipart multipart = new MimeMultipart();
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(plain, "text/plain");
                messageBodyPart.setContent(html, "text/html");
                multipart.addBodyPart(messageBodyPart);
                for (int i = 0; i < fileNames.length; i++) {
                    addAttachment(multipart, fileNames[i], files[i]);
                }
                message.setContent(multipart);
            }
            int tries = 0;
            while (true) {
                try {
                    tries++;
                    Transport.send(message);
                    break;
                } catch (MessagingException ex) {
                    Logger.getLogger(SendMail.class.getName()).log(Level.INFO, null, ex);
                    if (tries > 3) {
                        throw ex;
                    }
                }
            }
        }
    }

    public static void sendBillMail(final SysCfg cfg, String mails, String subject, String html, String plain, String fileNames[], File files[], String mailFrom, String replyTo) throws Exception {
        
        html = html.replaceAll("\n\r", "\n").replaceAll("\r\n", "\n").replaceAll("\r", "\n").replaceAll("\n", "<br />").replaceAll("\\\\\"", "\"");
        html = getHtmlMsg(LINE_BREAK, subject, html);        
        html = htmlLetters(html);
        if (cfg.billMailActive) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", (cfg.billMailAuth ? "true" : "false"));
            props.put("mail.smtp.starttls.enable", (cfg.billMailTls ? "true" : "false"));
            if (cfg.billMailSsl) {
                props.put("mail.smtp.socketFactory.port", cfg.billSmtpPort);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.ssl.enable", true);
            }
            props.put("mail.smtp.host", cfg.billSmtpHost);
            props.put("mail.smtp.port", cfg.billSmtpPort);
            Session session;
            if (cfg.billMailAuth) {
                session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(cfg.billMailUser, cfg.billMailPasswd);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom != null ? mailFrom : cfg.billMailFrom));
            message.addRecipients(Message.RecipientType.BCC, mails);
            if (replyTo != null && !replyTo.isEmpty()) {
                String[] parts = replyTo.split(",");
                Address[] reply = new Address[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    reply[i] = new InternetAddress(part);
                }
                message.setReplyTo(reply);
            }
            message.setSubject(subject);
            message.setContent(plain, "text/plain");
            message.setContent(html, "text/html");
            if (fileNames != null && files != null) {
                Multipart multipart = new MimeMultipart();
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(plain, "text/plain");
                messageBodyPart.setContent(html, "text/html");
                multipart.addBodyPart(messageBodyPart);
                for (int i = 0; i < fileNames.length; i++) {
                    addAttachment(multipart, fileNames[i], files[i]);
                }
                message.setContent(multipart);
            }
            int tries = 0;
            while (true) {
                try {
                    tries++;
                    Transport.send(message);
                    break;
                } catch (MessagingException ex) {
                    Logger.getLogger(SendMail.class.getName()).log(Level.INFO, null, ex);
                    if (tries > 3) {
                        throw ex;
                    }
                }
            }
        }
    }

    private static void addAttachment(Multipart multipart, String fileName, File localFile) throws Exception {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(localFile);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String sessionId = pars.get("sessionId");
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        String sub = pars.get("sub");
        String msg = pars.get("msg");
        String empIds = pars.get("dest");//emp id's por comas
        String copy = pars.get("copy");//?? para dejar copia en cal_mail
        String type = pars.get("type");//?? para dejar copia en cal_mail
        String sProcId = pars.get("procId");//?? para dejar copia en cal_mail
        String sOfficeId = pars.get("officeId");//?? para dejar copia en cal_mail
        String emails = pars.get("addrs");//lista de direcciones por comas
        String module = pars.get("modId");
        String replyTo = pars.get("replyTo");
        String attId = pars.get("attId");
        String mailFrom = null;
        if (pars.containsKey("mailFrom")) {
            mailFrom = pars.get("mailFrom");
        }

        List<File> lstFiles = new ArrayList<>();
        List<String> lstFilesNames = new ArrayList<>();

        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            SessionLogin.validate(sessionId, con);

            for (int i = 0; pars.containsKey("fileName" + i); i++) {
                File file = File.createTempFile("tmp", ".bin");
                try (FileOutputStream fos = new FileOutputStream(file); InputStream is = request.getPart("file" + i).getInputStream()) {
                    fileManager.copy(is, new BufferedOutputStream(fos));
                }
                lstFiles.add(file);
                lstFilesNames.add(pars.get("fileName" + i));
            }

            if (attId != null) {
                fileManager.PathInfo pInfo = new fileManager.PathInfo(con);
                lstFiles.add(pInfo.getExistingFile(MySQLQuery.getAsInteger(attId)));
                lstFilesNames.add(new MySQLQuery("SELECT file_name FROM bfile WHERE id = " + attId).getAsString(con));
            }
            Integer procId = !sProcId.isEmpty() ? Integer.valueOf(sProcId) : null;
            Integer officeId = !sOfficeId.isEmpty() ? Integer.valueOf(sOfficeId) : null;
            Integer modId = !module.isEmpty() ? Integer.valueOf(module) : null;
            request.getInputStream().close();

            if (empIds != null && !empIds.isEmpty()) {
                try {
                    String[] names = (lstFilesNames.size() > 0 ? lstFilesNames.toArray(new String[0]) : null);
                    File[] files = (lstFiles.size() > 0 ? lstFiles.toArray(new File[0]) : null);
                    sendMail("employee", sub, msg, empIds, copy.equals("1"), type, procId, officeId, poolName, tz, names, files, modId, mailFrom, replyTo);
                    response.setStatus(200);
                } catch (Exception ex) {
                    Logger.getLogger(SendMail.class.getName()).log(Level.SEVERE, null, ex);
                    StackTraceElement[] stack = ex.getStackTrace();
                    StringBuilder sb = new StringBuilder(ex.getClass().toString()).append("\r\n");
                    sb.append(ex.getMessage()).append("\r\n");
                    for (StackTraceElement se : stack) {
                        sb.append(se.toString()).append("\r\n");
                    }
                    response.sendError(500, sb.toString());
                }
            } else {
                String[] names = (lstFilesNames.size() > 0 ? lstFilesNames.toArray(new String[0]) : null);
                File[] files = (lstFiles.size() > 0 ? lstFiles.toArray(new File[0]) : null);
                sendMail(con, emails, sub, getHtmlMsg(con, sub, msg), msg, names, files, mailFrom, replyTo);
            }
        } catch (Exception e) {
            Logger.getLogger(SendMail.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500);
        } finally {
            for (int i = 0; i < lstFiles.size(); i++) {
                lstFiles.get(i).delete();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    public static String htmlLetters(String str) {
        str = str.replaceAll("\\¿", "&iquest;");
        str = str.replaceAll("Á", "&Aacute;");
        str = str.replaceAll("É", "&Eacute;");
        str = str.replaceAll("Í", "&Iacute;");
        str = str.replaceAll("Ó", "&Oacute;");
        str = str.replaceAll("Ú", "&Uacute;");
        str = str.replaceAll("Ñ", "&Ntilde;");
        str = str.replaceAll("á", "&aacute;");
        str = str.replaceAll("é", "&eacute;");
        str = str.replaceAll("í", "&iacute;");
        str = str.replaceAll("ó", "&oacute;");
        str = str.replaceAll("ú", "&uacute;");
        str = str.replaceAll("ñ", "&ntilde;");
        return str;
    }

    public static String readTemplate(String template) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(SendMail.class.getResourceAsStream(template)))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    public static String getHtmlMsg(Connection con, String subject, String msg) throws Exception {
        String signature = new MySQLQuery("SELECT mail_signature_url FROM sys_cfg").getAsString(con);
        return getHtmlMsg(signature, subject, msg);
    }
    
    public static String getHtmlMsg(String signatureURL, String subject, String msg) throws Exception {
        String html = readTemplate("./basicMailTemplate.html");
        html = html.replaceAll("\\{headerTitle\\}", subject);
        html = html.replaceAll("\\{Subject\\}", subject);
        html = html.replaceAll("[\r\n\t]", "");
        if (signatureURL != null) {
            html = html.replaceAll("\\{ent_logo\\}", signatureURL);
        }
        html = html.replaceAll("\\{Mesage\\}", msg);
        return html;
    }
}
