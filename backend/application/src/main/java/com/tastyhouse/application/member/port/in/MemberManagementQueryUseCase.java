package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.member.port.out.MemberManagementDetailWithProfileImageResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface MemberManagementQueryUseCase {

    PageResult<MemberListItemResult> getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    );

    MemberManagementDetailWithProfileImageResult getMember(Long id);
}
