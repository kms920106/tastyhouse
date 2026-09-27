package com.tastyhouse.application.ceo.port.out.write;

import java.time.LocalDateTime;

public record CeoReplyPhraseState(
    Long id,
    Long ceoId,
    String name,
    String content,
    int sort,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
