package com.tastyhouse.batch.crawling.bbq.response;

/**
 * 서브 옵션 아이템 상세 응답.
 *
 * @param id          아이템 ID
 * @param itemTitle   아이템 제목
 * @param addPrice    추가 가격
 * @param soldOut     품절 여부
 * @param hidden      숨김 여부
 */
public record SubOptionItemDetailResponse(
    Long id,
    String itemTitle,
    Integer addPrice,
    boolean soldOut,
    boolean hidden
) {
    public static SubOptionItemDetailResponse from(
        Long id,
        String itemTitle,
        Integer addPrice,
        boolean soldOut,
        boolean hidden
    ) {
        return new SubOptionItemDetailResponse(
            id,
            itemTitle,
            addPrice,
            soldOut,
            hidden
        );
    }
}
