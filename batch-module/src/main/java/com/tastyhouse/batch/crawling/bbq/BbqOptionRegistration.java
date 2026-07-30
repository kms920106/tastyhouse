package com.tastyhouse.batch.crawling.bbq;

/**
 * BBQ 크롤링 옵션 등록 입력(batch 전용). 정렬 순서는 그룹 내 목록 순서로 결정된다.
 */
public record BbqOptionRegistration(
    String name,
    Integer additionalPrice,
    boolean soldOut,
    boolean visible
) {

    public static BbqOptionRegistration of(String name, Integer additionalPrice, boolean soldOut, boolean visible) {
        return new BbqOptionRegistration(name, additionalPrice, soldOut, visible);
    }
}
