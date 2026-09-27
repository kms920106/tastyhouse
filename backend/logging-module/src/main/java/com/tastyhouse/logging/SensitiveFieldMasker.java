package com.tastyhouse.logging;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SensitiveFieldMasker {

    private static final Logger log = LoggerFactory.getLogger(SensitiveFieldMasker.class);
    private static final String MASKED = "***";

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "newPassword", "newPasswordConfirm", "currentPassword",
            "accessToken", "refreshToken", "tempToken", "passwordResetToken",
            "verificationCode", "smsVerifyToken", "mailVerifyToken",
            "cardNumber", "cvv", "privateKey", "secret"
    );

    private final ObjectMapper objectMapper;

    public SensitiveFieldMasker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String mask(Object target) {
        if (target == null) {
            return "null";
        }
        try {
            JsonNode node = objectMapper.valueToTree(target);
            maskNode(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            log.debug("로그 마스킹 처리 실패: {}", e.getMessage());
            return "[직렬화 실패]";
        }
    }

    private void maskNode(JsonNode node) {
        if (node.isArray()) {
            node.forEach(this::maskNode);
            return;
        }
        if (!node.isObject()) {
            return;
        }
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.properties().forEach(entry -> {
            if (SENSITIVE_FIELDS.contains(entry.getKey())) {
                objectNode.put(entry.getKey(), MASKED);
            } else {
                maskNode(entry.getValue());
            }
        });
    }
}
