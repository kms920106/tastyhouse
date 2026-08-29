package com.tastyhouse.application.member.follow.port.out;

import java.util.List;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * member/follow 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code MemberFollowQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface MemberFollowQueryPort {

    PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    /**
     * 뷰어가 대상 회원을 팔로우 중인지. 표현용 단건 판정이므로 write 포트가 아니라 이 포트가 답한다.
     */
    boolean existsFollow(MemberId followerId, MemberId followingId);

    /**
     * 이 회원이 팔로우하는 수(팔로잉 카운트). 프로필 화면 표시용 집계다.
     */
    long countFollowing(MemberId memberId);

    /**
     * 이 회원을 팔로우하는 수(팔로워 카운트). 프로필 화면 표시용 집계다.
     */
    long countFollower(MemberId memberId);

    /**
     * 이 회원이 팔로우하는 회원 ID 목록. 팔로잉 타임라인 조회의 선행 입력이라 표현 목적 조회다.
     */
    List<Long> findFollowingIds(MemberId followerId);

}
