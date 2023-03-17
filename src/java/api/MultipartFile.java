package api;

import java.io.File;
import java.io.IOException;
import utilities.multipart.FilePart;
import utilities.shrinkfiles.FileTypes;

public class MultipartFile {

    public File file;
    public String fileName;

    public MultipartFile(File file, FilePart part) {
        this.file = file;
        this.fileName = part.getFileName();
    }

    public boolean isXls() throws IOException {
        return FileTypes.getType(file) == FileTypes.OLD_MS_OFFICE;
    }

    public boolean isImage() throws IOException {
        int type = FileTypes.getType(file);
        return type == FileTypes.JPG || type == FileTypes.GIF || type == FileTypes.PNG;
    }

}
