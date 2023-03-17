package utilities.shrinkfiles;

import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageWriteParam;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import static web.fileManager.copy;

public class FileShrinker {

    public static final int TYPE_TIFF_PDF = 1;
    public static final int TYPE_TIFF = 5;
    public static final int TYPE_JPG_COLOR = 2;
    public static final int TYPE_JPG_GRAY = 3;
    public static final int TYPE_NONE = 4;

    private static final int COLOR_SHORT_SIDE = 1080;
    private static final int GRAY_SHORT_SIDE = 1250;
    private static final int BW_SHORT_SIDE = 1500;

    public static String changeFileType(String fname, String ftype) {
        int dotPos = fname.lastIndexOf(".");
        String rta = (dotPos < 0 ? fname : fname.substring(0, dotPos)) + (!ftype.startsWith(".") ? "." : "") + ftype;
        return rta;
    }

    public static ShrunkenFile shrinkFile(File f, String fileName, int outputType) throws Exception {
        if (outputType == TYPE_NONE) {
            ShrunkenFile sf = new ShrunkenFile();
            sf.shrunken = false;
            return sf;
        }

        int type = FileTypes.getType(f);
        ShrunkenFile sf = new ShrunkenFile();
        sf.shrunken = false;
        switch (type) {
            case FileTypes.BMP:
            case FileTypes.GIF:
            case FileTypes.JPG:
            case FileTypes.PNG:
            case FileTypes.TIF: {
                sf.f = File.createTempFile("shrunken", ".bin");
                if (TYPE_TIFF_PDF == outputType) {
                    try (PDDocument dest = new PDDocument(); FileOutputStream fos = new FileOutputStream(sf.f)) {
                        addPage(ImageIO.read(f), outputType, dest);
                        dest.save(fos);
                    }
                    sf.fileName = changeFileType(fileName, ".pdf");
                } else if (TYPE_TIFF == outputType) {
                    BufferedImage img = proccessImage(ImageIO.read(f), outputType);
                    saveAsTIF(img, sf.f);
                    sf.fileName = changeFileType(fileName, ".tiff");
                }
                sf.shrunken = true;
                break;
            }

            case FileTypes.PDF: {
                sf.f = File.createTempFile("shrunken", ".bin");
                sf.fileName = fileName;
                try (PDDocument orig = PDDocument.load(f); FileOutputStream fos = new FileOutputStream(sf.f)) {
                    try {
                        if (orig.isEncrypted()) {
                            throw new Exception("Documento encriptado");
                        }
                        for (PDPage page : orig.getPages()) {
                            Iterator<COSName> it = page.getResources().getXObjectNames().iterator();
                            while (it.hasNext()) {
                                COSName name = it.next();
                                if (page.getResources().isImageXObject(name)) {
                                    PDImageXObject img = (PDImageXObject) page.getResources().getXObject(name);
                                    changeImage(img, orig, outputType);
                                }
                            }
                        }
                        orig.save(fos);
                        sf.shrunken = true;
                    } finally {
                        orig.close();
                    }
                }
                break;
            }
            default:
                sf.shrunken = false;
                break;
        }
        return sf;
    }

    private static void saveAsJpg(BufferedImage img, File f) throws Exception {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (FileOutputStream fos = new FileOutputStream(f); MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(fos)) {
            writer.setOutput(os);
            JPEGImageWriteParam params = new JPEGImageWriteParam(null);
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(0.5f + (img.getType() == BufferedImage.TYPE_INT_RGB ? 0.1f : 0));
            writer.write(null, new IIOImage(img, null, null), params);
        }
    }

    public static void saveAsTIF(BufferedImage img, File f) throws Exception {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("tif").next();
        try (FileOutputStream fos = new FileOutputStream(f); MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(fos)) {
            writer.setOutput(os);
            TIFFImageWriteParam param = (TIFFImageWriteParam) writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("CCITT T.6");
            writer.write(null, new IIOImage(img, null, null), param);
        }
    }

    public static BufferedImage proccessImage(BufferedImage img, int outputType) {
        BufferedImage rta;
        switch (outputType) {
            case TYPE_TIFF_PDF:
                rta = toBlackAndWhite(img);
                break;
            case TYPE_TIFF:
                rta = toBlackAndWhite(img);
                break;
            case TYPE_JPG_COLOR:
                rta = toColor(img);
                break;
            case TYPE_JPG_GRAY:
                rta = toGrayScale(img);
                break;
            case TYPE_NONE:
                rta = img;
                break;
            default:
                throw new RuntimeException("Tipo no reconocido: " + outputType);
        }
        return rta;
    }

    private static PDImageXObject getAsPdfImage(BufferedImage img, PDDocument doc, int outputType) throws Exception {

        File f;
        if (outputType == TYPE_TIFF_PDF && img.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            f = File.createTempFile("imgpdf", ".tif");
            saveAsTIF(img, f);
        } else {
            f = File.createTempFile("imgpdf", ".jpg");
            saveAsJpg(img, f);
        }
        PDImageXObject ret = PDImageXObject.createFromFileByExtension(f, doc);
        FileUtils.forceDelete(f);
        return ret;
    }

    private static void changeImage(PDImageXObject imageInPdf, PDDocument doc, int outputType) throws Exception {
        //imageInPdf.
        //if (!(imageInPdf instanceof PDCcitt)) {
        BufferedImage img = proccessImage(imageInPdf.getImage(), outputType);
        PDImageXObject rep = getAsPdfImage(img, doc, outputType);
        OutputStream os = imageInPdf.getCOSObject().createRawOutputStream();
        InputStream is = rep.getCOSObject().createRawInputStream();
        copy(is, os, true, true);

        COSStream c1 = imageInPdf.getCOSObject();
        COSStream c2 = rep.getCOSObject();

        COSName[] names = new COSName[c1.keySet().size()];
        Iterator<COSName> nit = c1.keySet().iterator();
        for (int i = 0; nit.hasNext(); i++) {
            names[i] = nit.next();
        }
        for (COSName name : names) {
            c1.removeItem(name);
        }
        for (COSName cname : c2.keySet()) {
            c1.setItem(cname, c2.getItem(cname));
        }
        //}
    }

    private static void addPage(BufferedImage img, int outputType, PDDocument dest) throws Exception {
        img = proccessImage(img, outputType);
        PDPage newPage = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
        dest.addPage(newPage);
        try (PDPageContentStream content = new PDPageContentStream(dest, newPage)) {
            PDImageXObject pImg = getAsPdfImage(img, dest, outputType);
            content.drawImage(pImg, 0, 0);
        }
    }

    private static class Size {

        public int width;
        public int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public static boolean needResize(BufferedImage img, int shortSide) {
            return needResize(img.getWidth(), img.getHeight(), shortSide);
        }

        public static boolean needResize(int origWidth, int origHeight, int shortSide) {
            return (origWidth > shortSide || origHeight > shortSide);
        }

        public static Size getResized(int origWidth, int origHeight, int shortSide) {
            if (origHeight > origWidth) {
                //vertical
                return new Size(shortSide, (int) (((double) origHeight / origWidth) * shortSide));
            } else {
                //horizontal
                return new Size((int) (((double) origWidth / origHeight) * shortSide), shortSide);
            }
        }

        public static Size getResized(BufferedImage img, int shortSide) {
            return getResized(img.getWidth(), img.getHeight(), shortSide);
        }
    }

    public static BufferedImage toColor(BufferedImage src) {
        if (!Size.needResize(src, COLOR_SHORT_SIDE)) {
            return src;
        }
        Size resized = Size.getResized(src, COLOR_SHORT_SIDE);
        return Thresholding.getScaledImage(src, BufferedImage.TYPE_INT_RGB, RenderingHints.VALUE_INTERPOLATION_BICUBIC, resized.width, resized.height);
    }

    public static BufferedImage toGrayScale(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY && !Size.needResize(src, GRAY_SHORT_SIDE)) {
            return src;
        }
        Size resized = Size.getResized(src, GRAY_SHORT_SIDE);
        return Thresholding.getScaledImage(src, BufferedImage.TYPE_BYTE_GRAY, RenderingHints.VALUE_INTERPOLATION_BICUBIC, resized.width, resized.height);
    }

    public static BufferedImage toBlackAndWhite(BufferedImage src) {
        if (Size.needResize(src, BW_SHORT_SIDE) || src.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            Size resized = Size.getResized(src, BW_SHORT_SIDE);
            src = Thresholding.getScaledImage(src, BufferedImage.TYPE_BYTE_GRAY, RenderingHints.VALUE_INTERPOLATION_BICUBIC, resized.width, resized.height);
        }
        src = Thresholding.thresholdImage(src);
        return src;
    }
}
