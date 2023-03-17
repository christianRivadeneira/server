package utilities.shrinkfiles;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

public class Thresholding {

    public static BufferedImage getScaledImage(BufferedImage src, int imgType, Object intepType, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, imgType);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, intepType);
        g2.setColor(Color.white);
        g2.fillRect(0, 0, w, h);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    private static int[] getIntData(byte[] data) {
        int[] rta = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = (int) (data[i] & 0xff);
        }
        return rta;
    }

    private static int[] getHistogram(int[] srcData) {
        int histData[] = new int[256];
        for (int i = 0; i < srcData.length; i++) {
            histData[srcData[i]]++;
        }
        return histData;
    }

    private static int getMaxCount(int[] histogram) {
        int maxCount = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > maxCount) {
                maxCount = histogram[i];
            }
        }
        return maxCount;
    }

    private static int getMax(int[] histogram, int maxCount) {
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > maxCount * 0.95f) {
                return i;
            }
        }
        return 0;
    }

    private static int getMin(int[] histogram, int maxCount) {
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > maxCount * 0.001f) {
                return i;
            }
        }
        return 0;
    }

    private static boolean alreadyBw(int[] histogram) {
        if (histogram[0] == 0 || histogram[255] == 0) {
            return false;
        }
        for (int i = 1; i < histogram.length - 1; i++) {
            if (histogram[i] > 0) {
                return false;
            }
        }
        return true;
    }

    private static int[] stretch(int[] data, int minCol, int maxCol) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (int) (((maxCol - minCol) / 255d) * data[i] + minCol);
        }
        return data;
    }

    public static BufferedImage thresholdImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new RuntimeException("La imagen debe ser TYPE_BYTE_GRAY");
        }

        byte[] rawPixels = ((DataBufferByte) image.getData().getDataBuffer()).getData();
        int[] pixels = getIntData(rawPixels);
        int[] hist = getHistogram(pixels);

        int t;
        if (!alreadyBw(hist)) {
            int mc = getMaxCount(hist);
            int maxCol = getMax(hist, mc);
            int minCol = getMin(hist, mc);
            pixels = stretch(pixels, minCol, maxCol);
            t = otsuThreshold(pixels);
        } else {
            t = 128;
        }

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics g = result.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        WritableRaster write = result.getRaster();
        int width = image.getWidth();
        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            if (pixels[pixel] < t) {
                write.setPixel(col, row, new int[]{0});
            }
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static int otsuThreshold(int[] srcData) {
        int ptr = 0;
        int histData[] = new int[256];
        while (ptr < srcData.length) {
            int h = srcData[ptr];
            histData[h]++;
            ptr++;
        }

// Total number of pixels
        int total = srcData.length;

        float sum = 0;
        for (int t = 0; t < 256; t++) {
            sum += t * histData[t];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histData[t];// Weight Background
            if (wB == 0) {
                continue;
            }

            wF = total - wB;// Weight Foreground
            if (wF == 0) {
                break;
            }

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;// Mean Background
            float mF = (sum - sumB) / wF;// Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        return threshold;
    }
}
