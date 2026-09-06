package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

@Entity
@Table(
    name = "ADMIN_DONG",
    uniqueConstraints = @UniqueConstraint(name = "uk_admin_dong_code", columnNames = "code"),
    indexes = {
        @Index(name = "idx_admin_dong_name", columnList = "sido_name, sigungu_name, dong_name"),
        @Index(name = "idx_admin_dong_center", columnList = "center_latitude, center_longitude")
    }
)
public class AdminDongJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    @Column(name = "dong_name", nullable = false, length = 50)
    private String dongName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "center_latitude", precision = 9, scale = 6)
    private BigDecimal centerLatitude;

    @Column(name = "center_longitude", precision = 9, scale = 6)
    private BigDecimal centerLongitude;

    @SuppressWarnings("unused")
    @Column(name = "boundary_min_latitude", precision = 9, scale = 6)
    private BigDecimal boundaryMinLatitude;

    @SuppressWarnings("unused")
    @Column(name = "boundary_max_latitude", precision = 9, scale = 6)
    private BigDecimal boundaryMaxLatitude;

    @SuppressWarnings("unused")
    @Column(name = "boundary_min_longitude", precision = 9, scale = 6)
    private BigDecimal boundaryMinLongitude;

    @SuppressWarnings("unused")
    @Column(name = "boundary_max_longitude", precision = 9, scale = 6)
    private BigDecimal boundaryMaxLongitude;

    @Column(name = "boundary", columnDefinition = "LONGTEXT")
    private String boundary;

    protected AdminDongJpaEntity() {
    }

    static AdminDongJpaEntity create(
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        GeoBoundingBox boundingBox,
        String boundary
    ) {
        AdminDongJpaEntity entity = new AdminDongJpaEntity();
        entity.code = code;
        entity.sidoName = sidoName;
        entity.sigunguName = sigunguName;
        entity.dongName = dongName;
        entity.active = active;
        entity.centerLatitude = centerLatitude;
        entity.centerLongitude = centerLongitude;
        entity.applyBoundary(boundingBox, boundary);
        return entity;
    }

    void applyChanges(
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        GeoBoundingBox boundingBox,
        String boundary
    ) {
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.dongName = dongName;
        this.active = active;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        applyBoundary(boundingBox, boundary);
    }

    void deactivate() {
        this.active = false;
    }

    private void applyBoundary(GeoBoundingBox boundingBox, String boundary) {
        this.boundary = boundary;
        this.boundaryMinLatitude = boundingBox == null ? null : boundingBox.minLatitude();
        this.boundaryMaxLatitude = boundingBox == null ? null : boundingBox.maxLatitude();
        this.boundaryMinLongitude = boundingBox == null ? null : boundingBox.minLongitude();
        this.boundaryMaxLongitude = boundingBox == null ? null : boundingBox.maxLongitude();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getSidoName() {
        return this.sidoName;
    }

    public String getSigunguName() {
        return this.sigunguName;
    }

    public String getDongName() {
        return this.dongName;
    }

    public boolean isActive() {
        return this.active;
    }

    public BigDecimal getCenterLatitude() {
        return this.centerLatitude;
    }

    public BigDecimal getCenterLongitude() {
        return this.centerLongitude;
    }

    public String getBoundary() {
        return this.boundary;
    }
}
