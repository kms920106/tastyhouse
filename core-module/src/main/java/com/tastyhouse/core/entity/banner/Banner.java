package com.tastyhouse.core.entity.banner;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "BANNER")
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private BannerType type; // 배너 유형 (MAIN, EVENT 등)

    @Column(name = "title", length = 100)
    private String title; // 배너 제목

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (UPLOADED_FILE.id 참조)

    @Column(name = "link_url", length = 500)
    private String linkUrl; // 클릭 시 이동 URL

    @Column(name = "start_date")
    private LocalDateTime startDate; // 노출 시작 일시

    @Column(name = "end_date")
    private LocalDateTime endDate; // 노출 종료 일시

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_active", nullable = false)
    private Boolean active = true; // 활성화 여부 (true: 활성)
}
