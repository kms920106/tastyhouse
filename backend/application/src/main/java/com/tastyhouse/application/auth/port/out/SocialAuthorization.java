package com.tastyhouse.application.auth.port.out;

public record SocialAuthorization(String code, String state) {

    public static SocialAuthorization of(String code) {
        return new SocialAuthorization(code, null);
    }

    public static SocialAuthorization of(String code, String state) {
        return new SocialAuthorization(code, state);
    }
}
