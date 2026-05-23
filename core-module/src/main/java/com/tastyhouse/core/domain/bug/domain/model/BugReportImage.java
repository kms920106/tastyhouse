package com.tastyhouse.core.domain.bug.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "BUG_REPORT_IMAGE",
    indexes = {
        @Index(name = "idx_bug_report_image_bug_report_id", columnList = "bug_report_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BugReportImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bug_report_id", nullable = false)
    private Long bugReportId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    private BugReportImage(Long bugReportId, Long imageFileId, Integer sort) {
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static BugReportImage create(Long bugReportId, Long imageFileId, Integer sort) {
        return new BugReportImage(bugReportId, imageFileId, sort);
    }
}
