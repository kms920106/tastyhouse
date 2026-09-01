package com.tastyhouse.webapplication.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세 + 연결 상품 정보 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 리뷰 투영({@code ReviewDetailResult})과 상품 투영
 * ({@code ProductDetailResult})을 서비스가 합친 것이라 어느 한쪽 계약으로 표현되지 않고, 두 파생값이
 * 서비스에 남아야 한다.
 *
 * <ul>
 *   <li>{@code productPrice}는 "할인가가 있으면 할인가, 없으면 원가"를 <b>서비스가 고른</b> 결과다 —
 *       금액 판정이라 표현 계층으로 내리지 않는다.</li>
 *   <li>{@code productImageUrl}은 상품 이미지 목록의 <b>첫 장</b>이다(없으면 {@code null}) — 별도
 *       포트 조회를 거친다.</li>
 * </ul>
 *
 * <p>상품을 찾지 못하면 상품 4필드가 모두 {@code null}이고 리뷰 필드만 채워진다 — 리뷰 자체는 상품이
 * 사라져도 남아야 하므로 응답을 비우지 않는다.
 *
 * <p>컴포넌트 이름과 순서는 {@code ReviewProductResponse}를 그대로 승계한다.
 */
public record ReviewProductView(
    Long productId,
    String productName,
    String productImageUrl,
    Integer productPrice,
    Long reviewId,
    String content,
    Double totalRating,
    Double tasteRating,
    Double amountRating,
    Double priceRating,
    Double atmosphereRating,
    Double kindnessRating,
    Double hygieneRating,
    boolean willRevisit,
    Long memberId,
    String memberNickname,
    String memberProfileImageUrl,
    LocalDateTime createdAt,
    List<String> imageUrls,
    List<String> tagNames
) {
}
