package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopImageChangeRequestState(
    Long id,
    Long shopId,
    String imageType,
    Long imageFileId,
    String status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
