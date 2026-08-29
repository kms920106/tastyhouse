package com.tastyhouse.application.member.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * member 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code MemberQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface MemberQueryPort {

    PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery);

    PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Optional<String> findProfileImageUrl(MemberId memberId);

    Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds);

    /**
     * 마이페이지 개인정보 조회. 표현용 투영이므로 애그리거트를 로드하지 않는다.
     */
    Optional<MemberPersonalInfoResult> findPersonalInfoById(MemberId memberId);

    /**
     * 닉네임 중복 여부(가입·프로필 수정 화면의 사용 가능 판정용).
     */
    boolean existsByNickname(String nickname);

    /**
     * 탈퇴하지 않은 회원 중 해당 휴대폰번호 사용 여부(가입 화면의 사용 가능 판정용).
     */
    boolean existsByActivePhoneNumber(String phoneNumber);

    /**
     * 회원 관리 상세 조회 — 관리 화면이 표시하는 식별자·등급·상태·가입일까지 담는다.
     */
    Optional<MemberManagementDetailResult> findManagementDetailById(MemberId memberId);

}
