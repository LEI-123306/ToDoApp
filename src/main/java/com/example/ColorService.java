package com.example;

import org.apache.commons.lang3.RandomUtils;

public class ColorService {
    public static String generateRandomColor() {
        int r = RandomUtils.nextInt(0, 256);
        int g = RandomUtils.nextInt(0, 256);
        int b = RandomUtils.nextInt(0, 256);
        return String.format("#%02x%02x%02x", r, g, b); // ex: #a3c9f2
    }
}