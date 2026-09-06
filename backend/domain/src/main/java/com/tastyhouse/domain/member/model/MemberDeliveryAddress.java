package com.tastyhouse.domain.member.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

public class MemberDeliveryAddress {
    private final Long id;
    private final MemberId memberId;
    private String alias;
    private String roadAddress;
    private String lotAddress;
    private String detailAddress;
    private AdminDongId adminDongId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean defaultAddress;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private MemberDeliveryAddress(
        Long id,
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberDeliveryAddress of(
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        validateAddress(roadAddress, latitude, longitude);

        return new MemberDeliveryAddress(
            null,
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress,
            null,
            null
        );
    }

    public static MemberDeliveryAddress reconstitute(
        Long id,
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MemberDeliveryAddress(
            id,
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress,
            createdAt,
            updatedAt
        );
    }

    public void update(
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        validateAddress(roadAddress, latitude, longitude);

        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void markAsDefault() {
        this.defaultAddress = true;
    }

    public void unmarkDefault() {
        this.defaultAddress = false;
    }

    public boolean isOwnedBy(MemberId memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }

    private static void validateAddress(String roadAddress, BigDecimal latitude, BigDecimal longitude) {
        if (roadAddress == null || roadAddress.isBlank()) {
            throw new IllegalArgumentException("도로명 주소는 필수입니다.");
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("배달 주소의 좌표(위도·경도)는 필수입니다: latitude=" + latitude + ", longitude=" + longitude);
        }
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getRoadAddress() {
        return this.roadAddress;
    }

    public String getLotAddress() {
        return this.lotAddress;
    }

    public String getDetailAddress() {
        return this.detailAddress;
    }

    public AdminDongId getAdminDongId() {
        return this.adminDongId;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public boolean isDefaultAddress() {
        return this.defaultAddress;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
