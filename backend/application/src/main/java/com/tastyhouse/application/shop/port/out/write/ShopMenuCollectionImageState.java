package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopMenuCollectionImageState(
    Long id,
    Long shopId,
    Long imageFileId,
    int sort,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
