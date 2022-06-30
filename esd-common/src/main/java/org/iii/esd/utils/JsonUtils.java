package org.iii.esd.utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtils {
    private JsonUtils() {}


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
    public static String serialize(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException ex) {
            return "{}";
        }
    }
}
