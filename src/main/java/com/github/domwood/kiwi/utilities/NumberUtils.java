package com.github.domwood.kiwi.utilities;

import java.util.Optional;

public class NumberUtils {

    private NumberUtils() {
    }

    public static long safeLong(final String input) {
        try {
            return Optional.ofNullable(input)
                    .map(Long::parseLong)
                    .orElse(Long.MIN_VALUE);
        } catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    public static int safeInt(final String input) {
        try {
            return Optional.ofNullable(input)
                    .map(Integer::parseInt)
                    .orElse(Integer.MIN_VALUE);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }
}
