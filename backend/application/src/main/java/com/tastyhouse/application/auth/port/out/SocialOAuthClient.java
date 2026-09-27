package com.tastyhouse.application.auth.port.out;

public interface SocialOAuthClient {

    SocialProvider provider();

    SocialOAuthResult<SocialCredential> exchange(SocialAuthorization authorization);

    SocialOAuthResult<SocialProfile> fetchProfile(SocialCredential credential);
}
