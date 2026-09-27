package com.tastyhouse.application.shop.port.out.write;

public record ProhibitedWordState(
    Long id,
    String word,
    String reason
) {
}
