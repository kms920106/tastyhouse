package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 리뷰 댓글 read model(web 노출용).
 *
 * <p>관리 화면용 {@link ReviewCommentListItemResult}와 달리 숨김 여부 대신 <b>작성자 프로필 이미지
 * 경로</b>를 함께 투영한다 — web 응답이 작성자 프로필 이미지 URL을 포함하기 때문이다. 파일 경로만
 * 내려주고 표시용 URL 조립은 소비 모듈 QueryService가 담당한다(응답 record 파일/이미지 필드 URL 규칙).
 */
public record ReviewCommentItemResult(
    Long id,
    Long reviewId,
    MemberId memberId,
    String memberNickname,
    String memberProfileImageFilePath,
    String content,
    LocalDateTime createdAt
) {

    @QueryProjection
    public ReviewCommentItemResult {
    }
}
