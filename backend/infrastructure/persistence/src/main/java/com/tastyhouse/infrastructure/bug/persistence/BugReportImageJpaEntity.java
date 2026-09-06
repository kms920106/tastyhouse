package com.tastyhouse.infrastructure.bug.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "BUG_REPORT_IMAGE",
    indexes = {
        @Index(name = "idx_bug_report_image_bug_report_id", columnList = "bug_report_id")
    }
)
public class BugReportImageJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bug_report_id", nullable = false)
    private Long bugReportId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected BugReportImageJpaEntity() {
    }

    private BugReportImageJpaEntity(Long bugReportId, Long imageFileId, Integer sort) {
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    static BugReportImageJpaEntity create(Long bugReportId, Long imageFileId, Integer sort) {
        return new BugReportImageJpaEntity(bugReportId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public Long getBugReportId() {
        return this.bugReportId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
