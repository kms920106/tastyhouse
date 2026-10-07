package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MemberManagementListQueryUseCase {

    PageResult<MemberListItemResult> getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    );
}
