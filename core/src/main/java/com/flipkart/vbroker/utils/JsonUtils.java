package com.flipkart.vbroker.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by hooda on 22/1/18
 */

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }


}
