package com.tastyhouse.domain.member.follow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.follow.domain.model.MemberFollow;

/**
 * 팔로우 write 포트.
 *
 * <p>단건 로드·중복 검증·저장·삭제만 남긴다. {@code countByFollowerId}/{@code countByFollowingId}와
 * {@code findFollowingIdsByFollowerId}는 Result DTO가 아니라 원시값(long/{@code List<Long>}) 반환이고
 * 팔로우 통계·타임라인 필터 등 도메인 판정 경로에서 소비되므로 write 포트에 잔류시킨다.
 *
 * <p>팔로잉/팔로워 목록(프로필 이미지 조인 + 뷰어의 팔로우 여부 투영)은 표현 목적 read이므로
 * infrastructure-module의 {@code MemberFollowQueryDao}가 담당한다.
 */
public interface MemberFollowRepository {

    Optional<MemberFollow> findByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    boolean existsByFollowerIdAndFollowingId(MemberId followerId, MemberId followingId);

    List<Long> findFollowingIdsByFollowerId(MemberId followerId);

    long countByFollowerId(MemberId followerId);

    long countByFollowingId(MemberId followingId);

    MemberFollow save(MemberFollow memberFollow);

    void delete(MemberFollow memberFollow);
}
