package com.tastyhouse.infrastructure.banner.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "BANNER")
public class BannerJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private BannerType type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected BannerJpaEntity() {
    }

    private BannerJpaEntity(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted
    ) {
        this.type = type;
        this.title = title;
        this.imageFileId = imageFileId;
        this.linkUrl = linkUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    static BannerJpaEntity create(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted
    ) {
        return new BannerJpaEntity(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible, deleted);
    }

    void applyChanges(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted
    ) {
        this.type = type;
        this.title = title;
        this.imageFileId = imageFileId;
        this.linkUrl = linkUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public BannerType getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public String getLinkUrl() {
        return this.linkUrl;
    }

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
