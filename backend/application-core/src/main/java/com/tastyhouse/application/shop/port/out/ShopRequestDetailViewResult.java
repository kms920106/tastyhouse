package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestDetailViewResult(
    Long requestId,
    String requestType,
    String requestTypeDescription,
    String summary,
    String status,
    String statusDescription,
    String rejectReason,
    boolean contractAmending,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt,
    String attachmentLabel,
    String attachmentUrl,
    ShopRequestImageChangeDetailResult imageChange,
    ShopRequestAdjustmentDetailResult deliveryAreaAdjustment,
    ShopRequestReviewBlindDetailResult reviewBlind
) {
}
