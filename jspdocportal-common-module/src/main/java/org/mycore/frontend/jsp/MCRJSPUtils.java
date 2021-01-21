package org.mycore.frontend.jsp;

import java.text.Normalizer;

public class MCRJSPUtils {
    public static String normalizeUmlauts(String input) {
        String result = input.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        result = result.replace("Ä", "AE").replace("Ö", "OE").replace("Ü", "UE");
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        //result = result.replaceAll("[^\\p{ASCII}]", ""); //special sign like /o should go through 
        return result;
    }
}
