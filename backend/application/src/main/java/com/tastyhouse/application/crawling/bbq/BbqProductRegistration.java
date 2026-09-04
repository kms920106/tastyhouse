package com.tastyhouse.application.crawling.bbq;

/**
 * BBQ 크롤링 상품 등록 입력. core command DTO를 batch로 격하한 batch 전용 입력 record다.
 * 이미지 업로드는 호출자가 먼저 수행하고 그 결과 파일 식별자만 담는다(없으면 null).
 */
public record BbqProductRegistration(
    Long shopId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    boolean soldOut,
    Integer sort,
    Long imageFileId,
    Long bbqMenuId,
    Long bbqCategoryId
) {

    public static BbqProductRegistration of(
        Long shopId,
        Long productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        boolean soldOut,
        Integer sort,
        Long imageFileId,
        Long bbqMenuId,
        Long bbqCategoryId
    ) {
        return new BbqProductRegistration(
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            soldOut,
            sort,
            imageFileId,
            bbqMenuId,
            bbqCategoryId
        );
    }
}
