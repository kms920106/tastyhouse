package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "PLACE_CHOICE")
public class PlaceChoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 선택지 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 선택지 상세 내용
}
