package api.rpt.api.charts;

import static api.rpt.api.charts.MaterialColors.AMBER_100;
import static api.rpt.api.charts.MaterialColors.AMBER_200;
import static api.rpt.api.charts.MaterialColors.AMBER_300;
import static api.rpt.api.charts.MaterialColors.AMBER_500;
import static api.rpt.api.charts.MaterialColors.AMBER_700;
import static api.rpt.api.charts.MaterialColors.AMBER_900;
import static api.rpt.api.charts.MaterialColors.BLUE_100;
import static api.rpt.api.charts.MaterialColors.BLUE_200;
import static api.rpt.api.charts.MaterialColors.BLUE_300;
import static api.rpt.api.charts.MaterialColors.BLUE_500;
import static api.rpt.api.charts.MaterialColors.BLUE_700;
import static api.rpt.api.charts.MaterialColors.BLUE_900;
import static api.rpt.api.charts.MaterialColors.BROWN_100;
import static api.rpt.api.charts.MaterialColors.BROWN_200;
import static api.rpt.api.charts.MaterialColors.BROWN_300;
import static api.rpt.api.charts.MaterialColors.BROWN_500;
import static api.rpt.api.charts.MaterialColors.BROWN_700;
import static api.rpt.api.charts.MaterialColors.BROWN_900;
import static api.rpt.api.charts.MaterialColors.CYAN_100;
import static api.rpt.api.charts.MaterialColors.CYAN_200;
import static api.rpt.api.charts.MaterialColors.CYAN_300;
import static api.rpt.api.charts.MaterialColors.CYAN_500;
import static api.rpt.api.charts.MaterialColors.CYAN_700;
import static api.rpt.api.charts.MaterialColors.CYAN_900;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_100;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_200;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_300;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_500;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_700;
import static api.rpt.api.charts.MaterialColors.DEEP_ORANGE_900;
import static api.rpt.api.charts.MaterialColors.INDIGO_100;
import static api.rpt.api.charts.MaterialColors.INDIGO_200;
import static api.rpt.api.charts.MaterialColors.INDIGO_300;
import static api.rpt.api.charts.MaterialColors.INDIGO_500;
import static api.rpt.api.charts.MaterialColors.INDIGO_700;
import static api.rpt.api.charts.MaterialColors.INDIGO_900;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_100;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_200;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_300;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_500;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_700;
import static api.rpt.api.charts.MaterialColors.LIGHT_GREEN_900;
import static api.rpt.api.charts.MaterialColors.PURPLE_100;
import static api.rpt.api.charts.MaterialColors.PURPLE_200;
import static api.rpt.api.charts.MaterialColors.PURPLE_300;
import static api.rpt.api.charts.MaterialColors.PURPLE_500;
import static api.rpt.api.charts.MaterialColors.PURPLE_700;
import static api.rpt.api.charts.MaterialColors.PURPLE_900;
import static api.rpt.api.charts.MaterialColors.RED_100;
import static api.rpt.api.charts.MaterialColors.RED_200;
import static api.rpt.api.charts.MaterialColors.RED_300;
import static api.rpt.api.charts.MaterialColors.RED_500;
import static api.rpt.api.charts.MaterialColors.RED_700;
import static api.rpt.api.charts.MaterialColors.RED_900;
import static api.rpt.api.charts.MaterialColors.TEAL_100;
import static api.rpt.api.charts.MaterialColors.TEAL_200;
import static api.rpt.api.charts.MaterialColors.TEAL_300;
import static api.rpt.api.charts.MaterialColors.TEAL_500;
import static api.rpt.api.charts.MaterialColors.TEAL_700;
import static api.rpt.api.charts.MaterialColors.TEAL_900;
import controller.Utils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Colors {

    public static final Map<String, Color[]> COLORS = new TreeMap<>();
    public static final Map<String, String> NAMES = new TreeMap<>();

    public static final String ALL = "all";
    public static final String AMBER = "amber";
    public static final String BLUE = "blue";
    public static final String BROWN = "brown";
    public static final String CYAN = "cyan";
    public static final String DEEP_ORANGE = "deep_orange";
    public static final String INDIGO = "indigo";
    public static final String LIGHT_GREEN = "light_green";
    public static final String PURPLE = "purple";
    public static final String RED = "red";
    public static final String TEAL = "teal";

    static {
        COLORS.put(ALL, new Color[]{AMBER_500, BLUE_500, BROWN_500, CYAN_500, DEEP_ORANGE_500, INDIGO_500, LIGHT_GREEN_500, PURPLE_500, RED_500, TEAL_500});
        COLORS.put(AMBER, new Color[]{AMBER_100, AMBER_200, AMBER_300, AMBER_500, AMBER_700, AMBER_900});
        COLORS.put(BLUE, new Color[]{BLUE_100, BLUE_200, BLUE_300, BLUE_500, BLUE_700, BLUE_900});
        COLORS.put(BROWN, new Color[]{BROWN_100, BROWN_200, BROWN_300, BROWN_500, BROWN_700, BROWN_900});
        COLORS.put(CYAN, new Color[]{CYAN_100, CYAN_200, CYAN_300, CYAN_500, CYAN_700, CYAN_900});
        COLORS.put(DEEP_ORANGE, new Color[]{DEEP_ORANGE_100, DEEP_ORANGE_200, DEEP_ORANGE_300, DEEP_ORANGE_500, DEEP_ORANGE_700, DEEP_ORANGE_900});
        COLORS.put(INDIGO, new Color[]{INDIGO_100, INDIGO_200, INDIGO_300, INDIGO_500, INDIGO_700, INDIGO_900});
        COLORS.put(LIGHT_GREEN, new Color[]{LIGHT_GREEN_100, LIGHT_GREEN_200, LIGHT_GREEN_300, LIGHT_GREEN_500, LIGHT_GREEN_700, LIGHT_GREEN_900});
        COLORS.put(PURPLE, new Color[]{PURPLE_100, PURPLE_200, PURPLE_300, PURPLE_500, PURPLE_700, PURPLE_900});
        COLORS.put(RED, new Color[]{RED_100, RED_200, RED_300, RED_500, RED_700, RED_900});
        COLORS.put(TEAL, new Color[]{TEAL_100, TEAL_200, TEAL_300, TEAL_500, TEAL_700, TEAL_900});

        NAMES.put(ALL, "Colorido");
        NAMES.put(AMBER, "Ámbar");
        NAMES.put(BLUE, "Azul");
        NAMES.put(BROWN, "Café");
        NAMES.put(CYAN, "Cian");
        NAMES.put(DEEP_ORANGE, "Naranja");
        NAMES.put(INDIGO, "Índigo");
        NAMES.put(LIGHT_GREEN, "Verde");
        NAMES.put(PURPLE, "Púrpura");
        NAMES.put(RED, "Rojo");
        NAMES.put(TEAL, "Aguamarina");

        Iterator<String> it = COLORS.keySet().iterator();
        while (it.hasNext()) {
            Color[] colors = COLORS.get(it.next());
            for (int j = 0; j < colors.length / 2; j++) {
                if (j % 2 == 0) {
                    swap(colors, j);
                }
            }
        }
    }

    private static void swap(Object[] cs, int i) {
        Object c = cs[i];
        cs[i] = cs[cs.length - 1 - i];
        cs[cs.length - 1 - i] = c;
    }

    public static BufferedImage getImage(String key) throws IOException {
        Color[] colors = COLORS.get(key);
        BufferedImage bi = new BufferedImage(200, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        int tileW = bi.getWidth() / colors.length;
        for (int i = 0; i < colors.length; i++) {
            g.setColor(colors[i]);
            g.fillRect(i * tileW, 0, tileW, bi.getHeight());
        }
        return bi;
    }

}
