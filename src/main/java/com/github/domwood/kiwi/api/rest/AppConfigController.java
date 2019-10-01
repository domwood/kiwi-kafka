package com.github.domwood.kiwi.api.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.github.domwood.kiwi.utilities.Constants.API_ENDPOINT;

@CrossOrigin("*")
@RestController
@RequestMapping(API_ENDPOINT)
public class AppConfigController {

    @Value("${app.version:dev}")
    private String appVersion;

    @Value("${spring.profiles.active}")
    private List<String> activeProfiles;

    @GetMapping("/version")
    @ResponseBody
    public String version(){
        return this.appVersion;
    }

    @GetMapping("/profiles")
    @ResponseBody
    public List<String> profiles(){
        return activeProfiles;
    }


}
