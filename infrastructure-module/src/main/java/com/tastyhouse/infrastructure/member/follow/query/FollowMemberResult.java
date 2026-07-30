package com.tastyhouse.infrastructure.member.follow.query;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;

/**
 * 팔로잉/팔로워 목록 항목 read model.
 *
 * <p>{@code following}은 목록을 보고 있는 뷰어가 그 회원을 팔로우 중인지를 뜻하며, DAO가 뷰어 기준
 * exists 서브쿼리로 함께 투영한다(뷰어가 없으면 항상 false).
 */
public record FollowMemberResult(
    Long memberId,
    String nickname,
    MemberGrade memberGrade,
    String profileImageFilePath,
    boolean following
) {

    @QueryProjection
    public FollowMemberResult {
    }
}
