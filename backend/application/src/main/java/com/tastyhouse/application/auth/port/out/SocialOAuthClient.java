package com.tastyhouse.application.auth.port.out;

public interface SocialOAuthClient {

    SocialProvider provider();

    SocialCredential exchange(SocialAuthorization authorization);

    SocialProfile fetchProfile(SocialCredential credential);
}
