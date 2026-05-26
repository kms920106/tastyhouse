package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PLACE_PHOTO_CATEGORY")
public class PlacePhotoCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 사진 카테고리명 (예: 가게 외관, 메뉴, 내부 인테리어)
}
