package com.tastyhouse.external.oauth.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

// Facebook Graph API 토큰 디버그 응답 (GET https://graph.facebook.com/debug_token)
public record FacebookTokenDebugResponse(
    @JsonProperty("data") TokenData data
) {
    public record TokenData(
        @JsonProperty("app_id") String appId,
        @JsonProperty("type") String type,
        @JsonProperty("application") String application,
        @JsonProperty("user_id") String userId,
        @JsonProperty("is_valid") Boolean isValid,
        @JsonProperty("expires_at") Long expiresAt
    ) {}

    public boolean isValid() {
        return data != null && Boolean.TRUE.equals(data.isValid());
    }

    public String getAppId() {
        if (data == null) return null;
        return data.appId();
    }

    public String getUserId() {
        if (data == null) return null;
        return data.userId();
    }
}
