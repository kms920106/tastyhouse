package com.tastyhouse.webapplication.review.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세 조회 결과 — 배달 평가 3필드의 <b>노출 판정이 이미 끝난</b> 상태.
 *
 * <p><b>챕터 10</b>에서 신설. 공용 읽기 계약 {@code ReviewDetailResult}로는 이 응답을 표현할 수 없다.
 * 이유가 둘이다.
 *
 * <ul>
 *   <li>{@code orderMethod}가 도메인 enum {@code OrderMethod}라 강등이 필요하다 — 강등 자체는 api
 *       모듈에서도 가능하지만({@code name()}은 허용 accessor), 아래 판정과 얽혀 있어 분리할 수 없다.</li>
 *   <li>{@code orderMethod}·{@code deliveryRating}·{@code deliveryComment} 세 필드는 <b>뷰어가 작성자
 *       본인일 때만</b> 채워진다. 「배민 앱 미노출」 규격에 따른 <b>서버 판정</b>이라 프론트는 물론
 *       표현 계층에도 맡길 수 없다 — 판정이 컨트롤러로 새면 다른 호출부가 그 가림을 빠뜨릴 수 있다.</li>
 * </ul>
 *
 * <p>따라서 {@code orderMethod}는 {@code String}이며, 세 필드는 이 계약에 담긴 시점에 이미
 * "보여도 되는 값"이다({@code null}이면 가려진 것이다).
 *
 * <p>컴포넌트 이름과 순서는 {@code ReviewDetailResponse}를 그대로 승계한다.
 */
public record ReviewDetailView(
    Long id,
    Long shopId,
    String shopName,
    String stationName,
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
    List<String> tagNames,
    boolean ownerOnly,
    String ownerReplyContent,
    LocalDateTime ownerReplyCreatedAt,
    String orderMethod,
    Integer deliveryRating,
    String deliveryComment
) {
}
