package com.tastyhouse.core.entity.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "PLACE_BANNER_IMAGE")
public class PlaceBannerImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 배너 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort")
    private Integer sort; // 정렬 순서
}