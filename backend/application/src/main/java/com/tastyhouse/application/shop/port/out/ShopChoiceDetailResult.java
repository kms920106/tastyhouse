package com.tastyhouse.application.shop.port.out;

/**
 * 에디터 추천 단건(수정 화면) 결과.
 *
 * <p>과거 admin-api는 추천 단건을 도메인 모델({@code ShopChoice})로 읽어 네 필드만 꺼내 썼다.
 * 표현에 필요한 필드만 투영해 도메인 모델 적재를 없앤다.
 */
public record ShopChoiceDetailResult(
    Long id,
    Long shopId,
    String title,
    String content
) {
}
