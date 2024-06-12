package me.colrealpro.mcdiscord.utils;

public class StringUtils {

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String generateSalt(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < length) {
            int index = (int) (Math.random() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

}
