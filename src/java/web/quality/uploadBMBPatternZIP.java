package web.quality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.IO;
import web.fileManager;
import web.fileManager.PathInfo;

@MultipartConfig
@WebServlet(name = "uploadBMBPatternZIP", urlPatterns = {"/uploadBMBPatternZIP"})
public class uploadBMBPatternZIP extends HttpServlet {

    public static final int CAL_BMB_PATTERN = 61;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;
        Statement st = null;
        FileOutputStream zipFos = null;
        FileOutputStream binFos = null;
        ZipFile zipFile = null;
        try {
            String poolName = IO.convertStreamToString(request.getPart("poolName").getInputStream()).trim().toLowerCase();
            String tz = IO.convertStreamToString(request.getPart("tz").getInputStream());
            String s = IO.convertStreamToString(request.getPart("s").getInputStream());

            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();

            fileManager.PathInfo pi = new fileManager.PathInfo(con);
            clearPatterns(st, pi);

            File f = File.createTempFile("temp", ".zip");
            zipFos = new FileOutputStream(f);
            fileManager.copy(request.getPart("file").getInputStream(), zipFos, true, true);

            zipFile = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            List<String> tags = new ArrayList<>();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String query = "INSERT INTO bfile SET "
                        + "file_name = '" + entry.getName() + "' "
                        + ",description = '' "
                        + ",owner_id = -1 "
                        + ",owner_type = " + CAL_BMB_PATTERN + " "
                        + ",created_by = 1 "
                        + ",updated_by = 1 "
                        + ",created = NOW() "
                        + ",updated = NOW() "
                        + ",keywords = ''";

                int id = insert(st, query);
                File bin = pi.getNewFile(id);
                binFos = new FileOutputStream(bin);
                fileManager.copy(zipFile.getInputStream(entry), binFos, false, true);
                findHighlights(bin, tags);
            }

            for (String tag : tags) {
                st.executeUpdate("INSERT INTO cal_pattern_tag  SET keyword ='" + tag + "'");
            }

            response.setStatus(200);
        } catch (Exception ex) {
            Logger.getLogger(uploadBMBPatternZIP.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(500);
        } finally {
            MySQLSelect.tryClose(zipFile);
            MySQLSelect.tryClose(zipFos);
            MySQLSelect.tryClose(binFos);
            MySQLSelect.tryClose(con);
            MySQLSelect.tryClose(st);
        }
    }

    private static void clearPatterns(Statement st, PathInfo path) throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT id FROM bfile WHERE owner_type = " + CAL_BMB_PATTERN)) {
            while (rs.next()) {
                int bFileId = rs.getInt(1);
                File f = path.getExistingFile(bFileId);
                if (f != null && !f.exists()) {
                    f.delete();
                }
            }
        }
        st.executeUpdate("DELETE FROM bfile WHERE owner_type = " + CAL_BMB_PATTERN);
        st.executeUpdate("TRUNCATE cal_pattern_tag");
    }

    private int insert(Statement st, String query) throws Exception {
        ResultSet rs1 = null;
        try {
            if (st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS) == 0) {
                throw new Exception("Error al insertar la sesión.");
            } else {
                rs1 = st.getGeneratedKeys();
                if (rs1.next()) {
                    return rs1.getInt(1);

                } else {
                    throw new Exception("No se pudo recuperar la llave.");
                }
            }
        } finally {
            MySQLSelect.tryClose(rs1);
        }
    }

    protected static void findHighlights(File f, List<String> highlights) throws Exception {
        ZipFile zipFile = new ZipFile(f);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        SAXBuilder builder = new SAXBuilder();

        List<XMLDoc> docs = new ArrayList<>();
        Pattern headerPatt = Pattern.compile(".*/header[0-9]*\\.xml");
        Pattern footerPatt = Pattern.compile(".*/footer[0-9]*\\.xml");

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            try (InputStream stream = zipFile.getInputStream(entry)) {
                String name = entry.getName();
                if (name.endsWith("/document.xml") || headerPatt.matcher(name).matches() || footerPatt.matcher(name).matches()) {
                    docs.add(new XMLDoc(builder.build(stream), entry));
                }
            }
        }
        for (XMLDoc xMLDoc : docs) {
            findHighlights(xMLDoc, highlights);
        }
        Collections.sort(highlights);
    }

    private static void findHighlights(XMLDoc doc, List<String> highlights) {
        Namespace ns = doc.doc.getRootElement().getNamespace("w");
        findHighlights(doc.doc.getRootElement(), ns, highlights);

    }

    private static void findHighlights(Element e, Namespace ns, List<String> txts) {
        if (e.getName().equals("highlight")) {
            StringBuilder sb = new StringBuilder();
            Element el = e.getParentElement().getParentElement().getChild("t", ns);
            List<Content> content = el.getContent();
            for (Content ct : content) {
                sb.append(ct.getValue());
            }
            String str = sb.toString();
            if (!txts.contains(str)) {
                txts.add(str);
            }
        }
        List<Element> els = e.getChildren();
        for (Element element : els) {
            findHighlights(element, ns, txts);
        }
    }

    private static class XMLDoc {

        Document doc;
        ZipEntry entry;

        public XMLDoc(Document doc, ZipEntry entry) {
            this.doc = doc;
            this.entry = entry;
        }
    }

    static class QualityDocument {

        File f;
        String name;

        public QualityDocument() {
        }

        public QualityDocument(File f, String name) {
            this.f = f;
            this.name = name;
        }
    }

    public static String getFileExt(String name) {
        return name.substring(name.lastIndexOf("."));
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
