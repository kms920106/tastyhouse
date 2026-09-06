package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public record ShopRequestListItemViewResult(
    Long requestId,
    ShopRequestType requestType,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    boolean contractAmending,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
