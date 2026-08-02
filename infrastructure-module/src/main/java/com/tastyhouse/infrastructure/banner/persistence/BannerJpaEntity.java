package com.tastyhouse.infrastructure.banner.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.banner.domain.model.BannerType;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 배너 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Banner}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code BannerMapper}가 수행한다.
 */
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

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id", nullable = false)
    private UploadedFileId imageFileId;

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
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)

    protected BannerJpaEntity() {
    }

    private BannerJpaEntity(
        BannerType type,
        String title,
        UploadedFileId imageFileId,
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code BannerMapper#toEntity}에서만 호출한다.
     */
    static BannerJpaEntity create(
        BannerType type,
        String title,
        UploadedFileId imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted
    ) {
        return new BannerJpaEntity(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        BannerType type,
        String title,
        UploadedFileId imageFileId,
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

    public UploadedFileId getImageFileId() {
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
