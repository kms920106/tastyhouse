package com.tastyhouse.core.entity.banner;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "BANNER")
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private BannerType type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "uploaded_file_id", nullable = false)
    private Long uploadedFileId;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
