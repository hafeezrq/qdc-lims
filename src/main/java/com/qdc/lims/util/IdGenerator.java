package com.qdc.lims.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.util.Random;

public class IdGenerator {

    // ONLY Numbers (0-9)
    private static final char[] NUMBERS = "0123456789".toCharArray();
    private static final Random RANDOM = new Random();

    public static String generateMrn() {
        // Generate 6 random numbers
        String rawId = NanoIdUtils.randomNanoId(RANDOM, NUMBERS, 6);

        // Format as XXX-XXX (e.g., 852-304)
        return rawId.substring(0, 3) + "-" + rawId.substring(3, 6);
    }
}