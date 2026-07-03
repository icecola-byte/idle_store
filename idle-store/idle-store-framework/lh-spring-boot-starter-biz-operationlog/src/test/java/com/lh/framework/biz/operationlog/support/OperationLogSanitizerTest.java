package com.lh.framework.biz.operationlog.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperationLogSanitizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OperationLogSanitizer sanitizer =
            new OperationLogSanitizer(objectMapper);

    @Test
    void shouldMaskNestedSensitiveFields() throws Exception {
        Map<String, Object> value = Map.of(
                "phone", "13812345678",
                "password", "plain-password",
                "data", Map.of(
                        "token", "login-token",
                        "content", "private message",
                        "username", "alice"
                )
        );

        JsonNode result = objectMapper.readTree(sanitizer.sanitize(value));

        assertThat(result.get("phone").asText()).isEqualTo("138****5678");
        assertThat(result.get("password").asText()).isEqualTo("******");
        assertThat(result.at("/data/token").asText()).isEqualTo("******");
        assertThat(result.at("/data/content").asText()).isEqualTo("******");
        assertThat(result.at("/data/username").asText()).isEqualTo("alice");
    }

    @Test
    void shouldMaskSensitiveFieldsIgnoringCase() throws Exception {
        Map<String, Object> value = Map.of(
                "accessKeySecret", "secret",
                "verificationCode", "123456"
        );

        JsonNode result = objectMapper.readTree(sanitizer.sanitize(value));

        assertThat(result.get("accessKeySecret").asText()).isEqualTo("******");
        assertThat(result.get("verificationCode").asText()).isEqualTo("******");
    }
}
