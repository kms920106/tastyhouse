package com.tastyhouse.infrastructure.member.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "MEMBER_DELIVERY_ADDRESS",
    indexes = @Index(name = "idx_member_delivery_address_member_id", columnList = "member_id")
)
public class MemberDeliveryAddressJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "road_address", nullable = false, length = 500)
    private String roadAddress;

    @Column(name = "lot_address", length = 500)
    private String lotAddress;

    @Column(name = "detail_address", length = 200)
    private String detailAddress;

    @Column(name = "admin_dong_id")
    private Long adminDongId;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    protected MemberDeliveryAddressJpaEntity() {
    }

    private MemberDeliveryAddressJpaEntity(
        Long memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        this.memberId = memberId;
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
    }

    static MemberDeliveryAddressJpaEntity create(
        Long memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        return new MemberDeliveryAddressJpaEntity(
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress
        );
    }

    void applyChanges(
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
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

    public Long getAdminDongId() {
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
}
