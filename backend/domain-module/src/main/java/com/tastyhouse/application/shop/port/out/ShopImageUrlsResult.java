package com.tastyhouse.application.shop.port.out;

/**
 * 가게 썸네일/상표 이미지 표시용 URL 조회 결과.
 *
 * <p>가게 상세 조립 시 도메인 모델({@code Shop})에서 이미지 파일 식별자만 뽑아 파일을 단건씩
 * 재조회하던 지점을 대체한다 — 두 이미지를 한 쿼리로 left join해 URL까지 변환해 담으므로,
 * 소비 서비스는 여전히 이름·평점 등 다른 필드는 애그리거트에서 얻으면서 이미지 URL만 이 결과로 받는다.
 */
public record ShopImageUrlsResult(
    Long shopId,
    String thumbnailImageUrl,
    String trademarkImageUrl
) {
}
