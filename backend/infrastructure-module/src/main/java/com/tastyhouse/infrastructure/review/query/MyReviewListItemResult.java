package com.tastyhouse.infrastructure.review.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 마이페이지 '내 리뷰' 목록 항목.
 *
 * <p>{@code ownerOnly}는 뱃지 표시용이다 — 마이페이지는 본인 한정 조회라 사장님만보기 리뷰를
 * <b>포함</b>하므로(타인 프로필과 정반대), 어느 것이 비공개인지 화면에서 구분할 수단이 필요하다.
 * 같은 {@code boolean}이 인접하지 않도록 맨 뒤에 둔다.
 */
public record MyReviewListItemResult(
    Long id,
    String imageUrl,
    boolean ownerOnly
) {
    @QueryProjection
    public MyReviewListItemResult {
    }
}
