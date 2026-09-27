package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopContentBoardState(
    Long id,
    Long shopId,
    String contentType,
    String topic,
    Long imageFileId,
    String youtubeUrl,
    String description,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
