package com.tastyhouse.domain.member.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 회원 write 포트.
 *
 * <p>남은 메서드는 모두 도메인 모델·VO·원시값을 주고받으며 command 경로 또는 도메인 서비스의 트랜잭션
 * 안에서 소비된다 — 단건 로드({@code findById}/{@code findByUsername}/{@code findByNickname}),
 * 중복 검증({@code existsByX}), 등급 일괄 갱신, 상태 전이 저장({@code save}).
 *
 * <p>표현 목적 read(회원 목록·닉네임 검색·프로필 이미지 조인 투영, 작성자 표시명 색인)는 이 포트에
 * 두지 않고 infrastructure-module의 {@code MemberQueryDao}가 담당한다.
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

    Member save(Member member);
}
