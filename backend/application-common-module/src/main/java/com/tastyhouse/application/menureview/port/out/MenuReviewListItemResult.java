package com.tastyhouse.application.menureview.port.out;

import java.time.LocalDateTime;

/**
 * 상품별 메뉴 평가 목록 항목(고객 공개 조회).
 *
 * <p><b>댓글·좋아요·사장님답변 필드가 없는 것은 의도적이다</b> — 소셜 기능은 매장 리뷰에만 있다.
 *
 * <p>{@code memberProfileImageUrl}은 표시용 URL이다(DAO가 {@code FileUrlResolver}로 완성).
 */
public record MenuReviewListItemResult(
    Long id,
    String memberNickname,
    String memberProfileImageUrl,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {

    /**
     * 프로필 이미지 슬롯만 교체한 사본 — 투영식에 URL 변환을 끼울 수 없어 fetch 직후 재조립한다.
     */
    public MenuReviewListItemResult withMemberProfileImageUrl(String memberProfileImageUrl) {
        return new MenuReviewListItemResult(
            this.id,
            this.memberNickname,
            memberProfileImageUrl,
            this.rating,
            this.comment,
            this.createdAt
        );
    }
}
