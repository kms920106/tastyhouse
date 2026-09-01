package com.tastyhouse.webapplication.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 등록·수정 응답 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ReviewDetailResult}로는 이 응답을 표현할 수 없다 —
 * {@code productId}가 <b>별도 포트 조회</b>({@code ReviewQueryPort#findProductIdByReviewId})의 결과이고,
 * 리뷰 상세 투영은 그 값을 품지 않는다.
 *
 * <p>등록·수정 명령은 식별자만 돌려주므로(CQRS 교차 주입 금지) 이 응답은 커밋 이후 재조회로 조립된다 —
 * 상세 조회 시 <b>작성자 본인을 뷰어로 넘겨야</b> 사장님만보기 리뷰에서 404 회귀가 나지 않는다.
 * 그 판정은 {@code ReviewQueryService#getReviewSubmitResult}에 있다.
 *
 * <p>컴포넌트 이름과 순서는 {@code ReviewResponse}를 그대로 승계한다 — {@code tags}는 상세 투영의
 * {@code tagNames}에서 오지만 응답 필드명이 {@code tags}라 그 이름을 따른다.
 */
public record ReviewSubmitResultView(
    Long reviewId,
    Long productId,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double totalRating,
    String content,
    List<String> imageUrls,
    List<String> tags,
    LocalDateTime createdAt
) {
}
