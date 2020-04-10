package com.github.domwood.kiwi.utilities;

import com.google.common.base.CaseFormat;

import java.util.Optional;

public class StringUtils {
    public static String convertCamelToKafkaConfigFormat(String camelCaseKafkaConfig) {
        return Optional.ofNullable(CaseFormat.LOWER_CAMEL
                .converterTo(CaseFormat.LOWER_UNDERSCORE)
                .convert(camelCaseKafkaConfig))
                .map(s -> s.replaceAll("_", "."))
                .orElse(camelCaseKafkaConfig);
    }
}
