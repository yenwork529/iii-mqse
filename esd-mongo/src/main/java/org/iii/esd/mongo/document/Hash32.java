package org.iii.esd.mongo.document;

public class Hash32 {
    public static Long toLong(String si) {
        Long h = 1125899906842597L; // prime
        int len = si.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + si.charAt(i);
        }
        h = h & 0xFFFFFFFFL;
        return h;
    }
}
