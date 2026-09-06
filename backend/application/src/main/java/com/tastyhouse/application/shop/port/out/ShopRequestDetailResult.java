package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public record ShopRequestDetailResult(
    Long requestId,
    Long shopId,
    ShopRequestType requestType,
    Long sourceRequestId,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    String attachmentUrl,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
