package com.tastyhouse.application.member.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 회원 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>관리 목록·상세와 그에 딸린 프로필 이미지 일괄 조회를 담당한다. 회원 화면 조회는
 * {@code MemberQueryPort}가 소유한다.
 *
 * <p>{@link #findMemberWithProfileImageById}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에
 * 선언만 중복한다.
 */
public interface MemberManagementQueryPort {

    PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery);

    /** 공유 메서드 — {@code MemberQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Optional<String> findProfileImageUrl(MemberId memberId);

    Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds);

    Optional<MemberManagementDetailResult> findManagementDetailById(MemberId memberId);
}
