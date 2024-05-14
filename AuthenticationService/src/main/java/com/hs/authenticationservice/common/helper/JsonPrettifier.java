package com.hs.authenticationservice.common.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPrettifier {

    final ObjectMapper objectMapper;

    public JsonPrettifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String createPrettyJson(Object o) {
        try {
            return this.objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
