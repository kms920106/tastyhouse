package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 리뷰 답글 read model(web 노출용).
 *
 * <p>관리 화면용 {@link ReviewReplyListItemResult}와 달리 숨김 여부 대신 <b>작성자 프로필 이미지
 * URL</b>를 함께 투영한다 — web 응답이 작성자 프로필 이미지 URL을 포함하기 때문이다. 조인으로 얻은
 * 저장 경로를 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이 값을 그대로 응답에 전달한다.
 */
public record ReviewReplyItemResult(
    Long id,
    Long commentId,
    MemberId memberId,
    String memberNickname,
    String memberProfileImageUrl,
    MemberId replyToMemberId,
    String replyToMemberNickname,
    String content,
    LocalDateTime createdAt
) {

    @QueryProjection
    public ReviewReplyItemResult {
    }
}
