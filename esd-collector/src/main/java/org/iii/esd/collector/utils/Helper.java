package org.iii.esd.collector.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
    
    public static String toJson(Object ob){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").setPrettyPrinting().create()
                .toJson(ob);
    }

    public static <T> T fromJson(String si, Class<T> classOfT) {
        // System.console().printf("\n==>\n"+si+"\n<==\n");
        try {
            return new Gson().fromJson(si, classOfT);
        } catch (Exception ex) {
            // System.console().printf(ex.getMessage());
            return null;
        }
    }

}