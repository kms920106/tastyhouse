package com.tastyhouse.application.member.port.out;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 회원 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>회원이 자기 정보를 보거나 다른 회원을 닉네임으로 찾는 조회를 담당한다. 관리 화면 조회는
 * {@code MemberManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findMemberWithProfileImageById}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에
 * 선언만 중복한다. 구현은 {@code MemberQueryDao} 하나가 담당하므로 투영 코드는 복제되지 않는다.
 */
public interface MemberQueryPort {

    PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery);

    /** 공유 메서드 — {@code MemberManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Optional<MemberPersonalInfoResult> findPersonalInfoById(MemberId memberId);

    boolean existsByNickname(String nickname);

    boolean existsByActivePhoneNumber(String phoneNumber);
}
