package org.iii.esd.api;

public class MetaId {


    public static String makeFieldId(Long id) {
        return String.format("RES-%06d-%02d", id, 1);
    }

    public static String increment(String si) {
        try {
            String ss[] = si.split("-");
            Integer n = Integer.parseInt(ss[2]) + 1;
            return ss[0] + "-" + ss[1] + String.format("-%02d", n);    
        }
        catch (Exception ex) {
            return null;
        }
    }


}
