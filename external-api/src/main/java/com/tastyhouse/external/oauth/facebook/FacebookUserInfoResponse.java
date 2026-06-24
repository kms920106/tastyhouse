package com.tastyhouse.external.oauth.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

// Facebook Graph API 사용자 정보 응답 (GET https://graph.facebook.com/me)
public record FacebookUserInfoResponse(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("picture") Picture picture
) {
    public record Picture(
    @JsonProperty("data") PictureData data
    ) {}

    public record PictureData(
    @JsonProperty("url") String url,
    @JsonProperty("is_silhouette") boolean isSilhouette
    ) {}

    public String getProfileImageUrl() {
    if (picture == null || picture.data() == null) return null;
    if (picture.data().isSilhouette()) return null;
    return picture.data().url();
    }
}
