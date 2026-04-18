package com.tastyhouse.core.entity.place;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "PLACE")
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // 상호명

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "road_address")
    private String roadAddress; // 도로명 주소

    @Column(name = "lot_address")
    private String lotAddress; // 지번 주소

    @Column(name = "phone_number")
    private String phoneNumber; // 전화번호

    @Column(name = "closed_days")
    private String closedDays; // 휴무일 (예: "연중무휴", "매주 월요일")

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl; // 목록용 썸네일 이미지 URL
}
