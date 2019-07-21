package com.github.domwood.kiwi.api.rest.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class RestUtils {
    public static String unEncodeParameter(String param){
        try {
            return URLDecoder.decode(param, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return param;
        }
    }
}
