package com.tastyhouse.application.auth.port.out;

public record SocialCredential(String value) {

    public static SocialCredential of(String value) {
        return new SocialCredential(value);
    }
}
