package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public record ShopRequestListItemResult(
    Long requestId,
    ShopRequestType requestType,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
