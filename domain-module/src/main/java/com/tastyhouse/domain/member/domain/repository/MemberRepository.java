package com.tastyhouse.domain.member.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.vo.MemberId;

/**
 * 회원 write 포트.
 *
 * <p>남은 메서드는 모두 도메인 모델·VO·원시값을 주고받으며 command 경로 또는 도메인 서비스의 트랜잭션
 * 안에서 소비된다 — 단건 로드({@code findById}/{@code findByUsername}/{@code findByNickname}),
 * 중복 검증({@code existsByX}), 등급 일괄 갱신, 상태 전이 저장({@code save}).
 *
 * <p>표현 목적 read(회원 목록·닉네임 검색·프로필 이미지 조인 투영)는 이 포트에 두지 않고
 * infrastructure-module의 {@code MemberQueryDao}가 담당한다.
 */
public interface MemberRepository {

    Optional<Member> findById(MemberId memberId);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade);

    /**
     * 회원 식별자 → 닉네임 색인. 리뷰 댓글·답글처럼 다른 컨텍스트의 도메인 조회가 작성자 표시명만
     * 필요할 때 쓰는 최소 조회다 — 프로필 카드 전체(등급·상태메시지·이미지)가 필요한 화면 조립은
     * infrastructure-module의 {@code MemberQueryDao}를 쓴다.
     */
    Map<Long, String> findNicknamesByIds(Collection<Long> memberIds);

    Member save(Member member);
}
