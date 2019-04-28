package com.github.domwood.kiwi.testutils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpTestUtils {

    public static <T> HttpEntity<T> asJsonPayload(T data){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(data, httpHeaders);
    }
}
