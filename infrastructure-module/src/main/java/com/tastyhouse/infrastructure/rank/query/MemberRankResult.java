package com.tastyhouse.infrastructure.rank.query;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 회원 랭킹 목록·단건 조회 결과 — 랭킹 행에 회원 닉네임·프로필 이미지 경로·등급을 join해 투영한다.
 *
 * <p>web(랭킹 화면·내 랭킹)과 admin(랭킹 관리 목록)이 같은 필드 셋을 쓰므로 하나로 공유한다.
 */
public record MemberRankResult(
    MemberId memberId,
    String nickname,
    String profileImageFilePath,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
    @QueryProjection
    public MemberRankResult {
    }
}
