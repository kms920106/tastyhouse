package com.tastyhouse.infrastructure.member.query;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.domain.model.MemberGrade;

/**
 * 회원 요약 + 프로필 이미지 경로 read model.
 *
 * <p>프로필 카드·작성자 요약 등 여러 화면이 공유하는 최소 표현 단위다. 이미지는 파일 경로만 투영하고,
 * 표시용 URL 조립은 소비 모듈의 QueryService가 담당한다(응답 record 파일/이미지 필드 URL 규칙).
 */
public record MemberWithProfileImageResult(
    Long id,
    String nickname,
    MemberGrade memberGrade,
    String statusMessage,
    String profileImageFilePath
) {

    @QueryProjection
    public MemberWithProfileImageResult {
    }
}
