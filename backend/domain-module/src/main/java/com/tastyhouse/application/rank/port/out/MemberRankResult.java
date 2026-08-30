package com.tastyhouse.application.rank.port.out;

import com.tastyhouse.domain.member.model.MemberGrade;

/**
 * 회원 랭킹 목록·단건 조회 결과 — 랭킹 행에 회원 닉네임·프로필 이미지 URL·등급을 join해 투영한다.
 *
 * <p>web(랭킹 화면·내 랭킹)과 admin(랭킹 관리 목록)이 같은 필드 셋을 쓰므로 하나로 공유한다.
 *
 * <p>조인으로 얻은 저장 경로는 DAO가 {@code FileUrlResolver}로 표시용 URL까지 변환해 담는다.
 */
public record MemberRankResult(
    Long memberId,
    String nickname,
    String profileImageUrl,
    Integer reviewCount,
    Integer rankNo,
    MemberGrade grade
) {
}
