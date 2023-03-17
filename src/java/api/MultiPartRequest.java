package api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import utilities.Reports;
import utilities.json.JSONDecoder;
import utilities.multipart.FilePart;
import utilities.multipart.MultipartParser;
import utilities.multipart.ParamPart;
import utilities.multipart.Part;
import web.fileManager;

public class MultiPartRequest {

    public HashMap<String, String> params = new HashMap<>();
    public HashMap<String, MultipartFile> files = new HashMap<>();

    public MultiPartRequest(HttpServletRequest request, int maxSizeBytes) throws Exception {
        MultipartParser mp = new MultipartParser(request, maxSizeBytes);
        Part part;
        while ((part = mp.readNextPart()) != null) {
            if (part.isFile()) {
                FilePart filePart = (FilePart) part;
                File file = File.createTempFile("uploaded", ".bin");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fileManager.copy(filePart.getInputStream(), fos);
                }
                files.put(part.getName(), new MultipartFile(file, filePart));
            } else if (part.isParam()) {
                ParamPart pp = (ParamPart) part;
                params.put(pp.getName(), pp.getStringValue("UTF-8"));
            }
        }
    }

    /**
     * Se usa cuando el request contiene un único archivo, de lo contrario hay
     * que usar el Map files
     *
     * @return el archivo
     * @throws java.lang.Exception
     */
    public MultipartFile getFile() throws Exception {
        if (files.size() != 1) {
            return null;
        }
        Iterator<MultipartFile> it = files.values().iterator();
        it.hasNext();
        return it.next();
    }

    public List getList(Class classObj) throws Exception {
        ByteArrayOutputStream baos;
        try (FileInputStream fis = new FileInputStream(getFile().file); GZIPInputStream giz = new GZIPInputStream(fis)) {
            baos = new ByteArrayOutputStream();
            Reports.copy(giz, baos);
            baos.close();
        } catch (Exception ex) {
            throw ex;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new JSONDecoder().getList(bais, classObj);
    }

    public void deleteFiles() {
        Iterator<Map.Entry<String, MultipartFile>> it = files.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().file.delete();            
        }
    }
}
