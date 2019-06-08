package com.github.domwood.kiwi.testutils;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class HttpTestUtils {

    public static <T> HttpEntity<T> asJsonPayload(T data){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(data, httpHeaders);
    }

    public static String testPostToUrl(TestRestTemplate template, String url, String data){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        return template.exchange(url, HttpMethod.POST, new HttpEntity<>(data, headers), String.class).getBody();
    }
}
