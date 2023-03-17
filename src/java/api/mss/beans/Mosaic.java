/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.mss.beans;

import api.mss.api.MssRoundApi;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import web.fileManager.PathInfo;

public class Mosaic {

    public static File mosaic(List<Integer> bfileIds, PathInfo pi, int imgWidth, int cols, Color bgColor) throws Exception {
        File[] fs = new File[bfileIds.size()];

        for (int i = 0; i < bfileIds.size(); i++) {
            fs[i] = pi.getExistingFile(bfileIds.get(i));
        }
        return mosaic(fs, imgWidth, cols, bgColor);
    }

    public static File mosaic(File[] imgs, int imgWidth, int cols, Color bgColor) throws IOException {

        int rows = (int) Math.ceil(imgs.length / ((double) cols));
        int tileWidth = imgWidth / cols;
        int tileHeight = (int) (3d / 4d * tileWidth);
        int imgHeight = tileHeight * rows;

        BufferedImage bi = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();

        for (int i = 0; i < imgs.length; i++) {
            int xp = i % cols;
            xp *= tileWidth;
            int yp = (int) (Math.floor(i / (double) cols) * tileHeight);
            BufferedImage img = ImageIO.read(imgs[i]);
            g2.drawImage(scale(img, tileWidth, tileHeight, bgColor), xp, yp, null);
        }
        File rta = File.createTempFile("mosaic", ".jpg");
        ImageIO.write(bi, "jpg", rta);
        return rta;

    }

    public static BufferedImage scale(BufferedImage src, int width, int height, Color bgColor) {
        BufferedImage rta = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rta.createGraphics();
        double xf = (double) width / src.getWidth();
        double yf = (double) height / src.getHeight();
        double f = Math.min(xf, yf);
        int newW = (int) (src.getWidth() * f);
        int newH = (int) (src.getHeight() * f);
        AffineTransform at = AffineTransform.getTranslateInstance((width - newW) / 2, (height - newH) / 2);
        at.scale(f, f);
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.drawRenderedImage(src, at);
        return rta;
    }

    public static void main(String[] args) {
        File dir = new File("C:\\Users\\alder\\Desktop\\imgs");
        File[] fs = dir.listFiles();
        try {
            File f = mosaic(fs, 1920, 2, Color.WHITE);
            Desktop.getDesktop().open(f);
        } catch (IOException ex) {
            Logger.getLogger(MssRoundApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
