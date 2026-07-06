package com.tastyhouse.core.domain.shop.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "SHOP")
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "station_id", nullable = false)
    private Long stationId; // 지하철역 ID (STATION.id 참조)

    @Column(name = "name", nullable = false, unique = true)
    private String name; // 상호명

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude; // 경도

    @Column(name = "rating")
    private Double rating; // 평균 평점

    @Column(name = "road_address")
    private String roadAddress; // 도로명 주소

    @Column(name = "lot_address")
    private String lotAddress; // 지번 주소

    @Column(name = "phone_number")
    private String phoneNumber; // 전화번호

    @Column(name = "thumbnail_image_file_id")
    private Long thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)

    @Column(name = "is_permanently_closed", nullable = false)
    private boolean permanentlyClosed; // 폐업 여부 (true: 폐업)

    public ShopId getShopId() {
        return ShopId.of(this.id);
    }
}
