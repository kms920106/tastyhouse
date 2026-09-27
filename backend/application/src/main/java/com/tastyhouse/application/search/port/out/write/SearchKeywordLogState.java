package com.tastyhouse.application.search.port.out.write;

import java.time.LocalDateTime;

public record SearchKeywordLogState(
    Long id,
    String keyword,
    LocalDateTime searchedAt
) {
}
