package com.tastyhouse.application.member.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MemberManagementQueryPort {

    PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(Long memberId);

    Optional<String> findProfileImageUrl(Long memberId);

    Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds);

    Optional<MemberManagementDetailResult> findManagementDetailById(Long memberId);
}
