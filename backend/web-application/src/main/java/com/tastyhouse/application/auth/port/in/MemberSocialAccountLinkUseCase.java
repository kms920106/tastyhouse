package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.SocialLinkResult;

public interface MemberSocialAccountLinkUseCase {

    SocialLinkResult linkAccount(String provider, String tempToken, String smsVerifyToken);
}
