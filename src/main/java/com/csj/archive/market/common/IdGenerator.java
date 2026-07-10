package com.csj.archive.market.common;

import java.util.Locale;
import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String prefixed(String prefix) {
        return prefix.toUpperCase(Locale.ROOT) + "-" + UUID.randomUUID();
    }
}
