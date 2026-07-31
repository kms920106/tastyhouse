package com.tastyhouse.domain.partnership.domain.vo;

public record PartnershipRequestId(Long value) {

    public PartnershipRequestId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("PartnershipRequestId는 양수여야 합니다: " + value);
        }
    }

    public static PartnershipRequestId of(Long value) {
        return new PartnershipRequestId(value);
    }
}
