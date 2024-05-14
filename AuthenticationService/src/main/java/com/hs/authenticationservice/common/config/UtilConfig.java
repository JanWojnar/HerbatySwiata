package com.hs.authenticationservice.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hs.authenticationservice.common.helper.JsonPrettifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    @Bean
    public JsonPrettifier jsonPrettifier(ObjectMapper objectMapper) {
        return new JsonPrettifier(objectMapper);
    }

}
