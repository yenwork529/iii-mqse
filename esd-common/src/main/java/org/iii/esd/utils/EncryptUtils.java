package org.iii.esd.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EncryptUtils {

    public static String md5Encode(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String base64Encode(String text) {
        try {
            return Base64.getEncoder().encodeToString(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String base64Decode(String text) {
        try {
            return new String(Base64.getDecoder().decode(text), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String genThinClientToken(Long fieldId, String tcip) {
        return md5Encode(fieldId + "|" + tcip);
    }

}