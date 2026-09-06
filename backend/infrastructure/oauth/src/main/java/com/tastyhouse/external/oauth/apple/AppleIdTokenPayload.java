package com.tastyhouse.external.oauth.apple;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleIdTokenPayload(

    @JsonProperty("sub")
    String sub,

    @JsonProperty("email")
    String email,

    @JsonProperty("email_verified")
    Object emailVerified,

    @JsonProperty("is_private_email")
    Object isPrivateEmail
) {

}
