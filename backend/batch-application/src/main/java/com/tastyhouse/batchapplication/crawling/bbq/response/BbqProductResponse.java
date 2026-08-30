package com.tastyhouse.batchapplication.crawling.bbq.response;

/**
 * BBQ 상품 응답. Product Entity 구조에 맞춘 응답이다.
 *
 * @param id             상품 ID
 * @param name           상품명
 * @param description    상품 설명
 * @param imageUrl       이미지 URL
 * @param originalPrice  원가
 * @param addPrice       추가 가격
 * @param soldOut        품절 여부
 * @param adultOnly      성인 전용 여부
 * @param canDeliver     배달 가능 여부
 * @param canTakeout     포장 가능 여부
 */
public record BbqProductResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    Integer originalPrice,
    Integer addPrice,
    boolean soldOut,
    boolean adultOnly,
    boolean canDeliver,
    boolean canTakeout
) {
    public static BbqProductResponse from(
        Long id,
        String name,
        String description,
        String imageUrl,
        Integer originalPrice,
        Integer addPrice,
        boolean soldOut,
        boolean adultOnly,
        boolean canDeliver,
        boolean canTakeout
    ) {
        return new BbqProductResponse(
            id,
            name,
            description,
            imageUrl,
            originalPrice,
            addPrice,
            soldOut,
            adultOnly,
            canDeliver,
            canTakeout
        );
    }
}
