package com.tastyhouse.batch.crawling.bbq.response;

/**
 * BBQ 상품 카테고리 응답.
 *
 * @param id        카테고리 ID
 * @param shopId    플레이스 ID
 * @param name      카테고리명
 * @param sort      정렬 순서
 * @param visible   노출 여부
 */
public record BbqProductCategoryResponse(
    Long id,
    Long shopId,
    String name,
    Integer sort,
    boolean visible
) {
    public static BbqProductCategoryResponse from(
        Long id,
        Long shopId,
        String name,
        Integer sort,
        boolean visible
    ) {
        return new BbqProductCategoryResponse(
            id,
            shopId,
            name,
            sort,
            visible
        );
    }
}
