package com.tastyhouse.external.sms.solapi.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record SolapiMessageRequest(
    List<SolapiMessage> messages
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SolapiMessage(
        String to,
        String from,
        String text,
        String type,
        String subject
    ) {
    }
}
