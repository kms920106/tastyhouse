package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDateTime;

public record CeoReplyPhraseResult(
    Long id,
    String name,
    String content,
    Integer sort,
    LocalDateTime createdAt
) {
}
