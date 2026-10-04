package com.tastyhouse.application.crawling.bbq;

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
