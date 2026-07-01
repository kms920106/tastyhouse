package com.tastyhouse.external.sms.solapi.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolapiMessageResponse {

    private List<FailedMessage> failedMessageList;
    private GroupInfo groupInfo;

    public boolean isSuccess() {
        return failedMessageList == null || failedMessageList.isEmpty();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailedMessage {
        private String to;
        private String from;
        private String type;
        private String statusCode;
        private String statusMessage;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupInfo {
        private Count count;
        private String status;
        private String groupId;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Count {
            private int total;
            private int sentSuccess;
            private int sentFailed;
            private int registeredSuccess;
            private int registeredFailed;
        }
    }
}
