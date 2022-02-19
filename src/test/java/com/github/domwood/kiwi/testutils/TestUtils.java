package com.github.domwood.kiwi.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.domwood.kiwi.KiwiConfig.customModules;

public class TestUtils {

    public static ObjectMapper testMapper() {
        return new ObjectMapper().registerModules(customModules());
    }
}
