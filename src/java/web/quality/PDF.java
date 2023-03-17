/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package web.quality;

import java.io.File;
import java.io.OutputStream;
/*
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 *
 * @author Mario
 */
public class PDF {
    public static File docxToPDF(File inputfilepath) throws Exception {
////        String regex = ".*(calibri|camb|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*";
////        // Windows:
////        // String
////        // regex=".*(calibri|camb|cour|arial|symb|times|Times|zapf).*";        
////        PhysicalFonts.setRegex(regex);
//        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputfilepath);
//
//        // Set up font mapper (optional)
//        Mapper fontMapper = new IdentityPlusMapper();
//        wordMLPackage.setFontMapper(fontMapper);
//
//		// .. example of mapping font Times New Roman which doesn't have certain Arabic glyphs
//        // eg Glyph "ي" (0x64a, afii57450) not available in font "TimesNewRomanPS-ItalicMT".
//        // eg Glyph "ج" (0x62c, afii57420) not available in font "TimesNewRomanPS-ItalicMT".
//        // to a font which does
//        PhysicalFont font
//                = PhysicalFonts.getPhysicalFonts().get("Arial Unicode MS");
//        // make sure this is in your regex (if any)!!!
//        if (font != null) {
//            fontMapper.getFontMappings().put("Times New Roman", font);
//        }
//        fontMapper.getFontMappings().put("Libian SC Regular", PhysicalFonts.getPhysicalFonts().get("SimSun"));
//
//        FOSettings foSettings = Docx4J.createFOSettings();
//        foSettings.setWmlPackage(wordMLPackage);
//        File out = File.createTempFile("pdf", "pdf");
//        OutputStream os = new java.io.FileOutputStream(out);
//        Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
//        return out;
        return null;
    }
}
