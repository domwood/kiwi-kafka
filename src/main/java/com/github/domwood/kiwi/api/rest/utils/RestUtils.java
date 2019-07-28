package com.github.domwood.kiwi.api.rest.utils;

import com.github.domwood.kiwi.data.input.ConsumerToFileRequest;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RestUtils {
    public static String unEncodeParameter(String param){
        try {
            return URLDecoder.decode(param, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return param;
        }
    }

    public static String getContentDisposition(ConsumerToFileRequest request){
        String suffix = request.fileType().toString().toLowerCase();
        String fileName = request.topics().get(0);
        return String.format("attachment; filename=\"%s.%s\"", fileName, suffix);
    }

    public static String base64Decoded(String dataAsBase64){
        byte[] asBytes = Base64.decodeBase64(dataAsBase64.getBytes());
        return new String(asBytes);
    }
}
