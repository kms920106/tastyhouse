package com.tastyhouse.external.sms.solapi.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolapiMessageResponse {

    private List<FailedMessage> failedMessageList;
    private GroupInfo groupInfo;

    public boolean isSuccess() {
        return failedMessageList == null || failedMessageList.isEmpty();
    }

    public List<FailedMessage> getFailedMessageList() {
        return this.failedMessageList;
    }

    public GroupInfo getGroupInfo() {
        return this.groupInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailedMessage {
        private String to;
        private String from;
        private String type;
        private String statusCode;
        private String statusMessage;

        public String getTo() {
            return this.to;
        }

        public String getFrom() {
            return this.from;
        }

        public String getType() {
            return this.type;
        }

        public String getStatusCode() {
            return this.statusCode;
        }

        public String getStatusMessage() {
            return this.statusMessage;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupInfo {
        private Count count;
        private String status;
        private String groupId;

        public Count getCount() {
            return this.count;
        }

        public String getStatus() {
            return this.status;
        }

        public String getGroupId() {
            return this.groupId;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Count {
            private int total;
            private int sentSuccess;
            private int sentFailed;
            private int registeredSuccess;
            private int registeredFailed;

            public int getTotal() {
                return this.total;
            }

            public int getSentSuccess() {
                return this.sentSuccess;
            }

            public int getSentFailed() {
                return this.sentFailed;
            }

            public int getRegisteredSuccess() {
                return this.registeredSuccess;
            }

            public int getRegisteredFailed() {
                return this.registeredFailed;
            }
        }
    }
}
