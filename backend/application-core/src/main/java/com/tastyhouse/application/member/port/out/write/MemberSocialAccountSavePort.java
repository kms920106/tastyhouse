package com.tastyhouse.application.member.port.out.write;

import com.tastyhouse.domain.member.model.MemberSocialAccount;

public interface MemberSocialAccountSavePort {

    MemberSocialAccount save(MemberSocialAccount socialAccount);
}
