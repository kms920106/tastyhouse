package com.tastyhouse.application.review.port.out;

/**
 * 리뷰 작성 화면 정보 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 주문 상품 소유권 투영({@code OrderProductOwnershipResult})과 상품 투영
 * ({@code ProductDetailResult})을 서비스가 합친 것이라 어느 한쪽 계약으로 표현되지 않는다.
 *
 * <ul>
 *   <li>{@code productPrice}는 "할인가가 있으면 할인가, 없으면 원가"를 <b>서비스가 고른</b> 결과다.</li>
 *   <li>{@code productImageUrl}은 상품 이미지 목록의 <b>첫 장</b>이다(없으면 {@code null}).</li>
 *   <li>{@code reviewed}는 (주문·상품·회원)으로 기존 리뷰 존재를 조회한 <b>판정 결과</b>다.</li>
 * </ul>
 *
 * <p>{@code orderMethod}는 소유권 투영이 이미 {@code String}으로 나르므로 강등할 것이 없다. 주문 정보를
 * 찾을 수 없으면 {@code null}이며, 화면은 그때 배달 평가 섹션을 렌더하지 않는다.
 *
 * <p>컴포넌트 이름과 순서는 {@code ReviewWriteInfoResponse}를 그대로 승계한다.
 */
public record ReviewWriteInfoView(
    Long productId,
    String productName,
    String productImageUrl,
    Integer productPrice,
    Long orderId,
    boolean reviewed,
    String orderMethod
) {
}
