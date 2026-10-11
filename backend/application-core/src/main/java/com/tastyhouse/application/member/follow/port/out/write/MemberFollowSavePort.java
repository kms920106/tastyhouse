package com.tastyhouse.application.member.follow.port.out.write;

import com.tastyhouse.domain.member.follow.model.MemberFollow;

public interface MemberFollowSavePort {

    MemberFollow save(MemberFollow memberFollow);

    void delete(MemberFollow memberFollow);
}
