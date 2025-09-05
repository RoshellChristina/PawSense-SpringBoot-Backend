package com.example.petapp.util;

import java.util.Base64;

public class ImageUtil {

    public static String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeFromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}

