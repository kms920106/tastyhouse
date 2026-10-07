package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberManagementDetailWithProfileImageResult;

public interface MemberManagementDetailQueryUseCase {

    MemberManagementDetailWithProfileImageResult getMember(Long id);
}
