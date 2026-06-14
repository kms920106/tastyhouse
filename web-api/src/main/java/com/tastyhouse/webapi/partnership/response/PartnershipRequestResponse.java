package com.tastyhouse.webapi.partnership.response;

import java.time.LocalDateTime;

public record PartnershipRequestResponse(
    Long id,
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    LocalDateTime consultationRequestedAt,
    LocalDateTime createdAt
) {
    public static PartnershipRequestResponse from(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        LocalDateTime createdAt
    ) {
        return new PartnershipRequestResponse(
            id,
            businessName,
            address,
            addressDetail,
            contactName,
            contactPhone,
            consultationRequestedAt,
            createdAt
        );
    }
}
