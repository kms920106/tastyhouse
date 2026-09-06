package com.tastyhouse.application.member.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface MemberManagementQueryPort {

    PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Optional<String> findProfileImageUrl(MemberId memberId);

    Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds);

    Optional<MemberManagementDetailResult> findManagementDetailById(MemberId memberId);
}
