package com.poetry.mr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CSVUtils {

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff]");

    public static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields;
    }

    public static String getContent(List<String> fields) {
        return fields.size() > 4 ? fields.get(4) : "";
    }

    public static String getDynasty(List<String> fields) {
        return fields.size() > 3 ? fields.get(3) : "";
    }

    public static String getContentLen(List<String> fields) {
        return fields.size() > 7 ? fields.get(7) : "0";
    }

    public static String removePunctuation(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fff';
    }
}