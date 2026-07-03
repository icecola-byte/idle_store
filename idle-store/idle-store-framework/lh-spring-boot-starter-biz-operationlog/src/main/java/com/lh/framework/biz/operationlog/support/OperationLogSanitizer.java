package com.lh.framework.biz.operationlog.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;
import java.util.Set;

/**
 * 对操作日志中的敏感字段进行递归脱敏，避免敏感数据写入日志文件。
 */
public class OperationLogSanitizer {

    private static final Set<String> SECRET_FIELDS = Set.of(
            "password",
            "passwordhash",
            "code",
            "verificationcode",
            "token",
            "accesstoken",
            "refreshtoken",
            "authorization",
            "accesskey",
            "accesskeyid",
            "accesskeysecret",
            "secretkey",
            "clientsecret",
            "credential",
            "content",
            "realname",
            "addressdetail"
    );

    private final ObjectMapper objectMapper;

    public OperationLogSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String sanitize(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            JsonNode node = objectMapper.valueToTree(value);
            mask(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            // 日志组件本身不能影响正常业务。
            return "\"<serialization-failed>\"";
        }
    }

    private void mask(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.properties().forEach(entry -> {
                if (isSecretField(entry.getKey())) {
                    objectNode.put(entry.getKey(), "******");
                } else if ("phone".equalsIgnoreCase(entry.getKey())) {
                    objectNode.put(
                            entry.getKey(),
                            maskPhone(entry.getValue().asText())
                    );
                } else {
                    mask(entry.getValue());
                }
            });
            return;
        }

        if (node.isArray()) {
            node.forEach(this::mask);
        }
    }

    private boolean isSecretField(String fieldName) {
        return SECRET_FIELDS.contains(fieldName.toLowerCase(Locale.ROOT));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "******";
        }
        return phone.substring(0, 3)
                + "****"
                + phone.substring(phone.length() - 4);
    }
}
