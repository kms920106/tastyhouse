package com.tastyhouse.core.entity.report;

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

    @Column(name = "uploaded_file_id", nullable = false)
    private Long uploadedFileId; // UploadedFile PK

    @Column(name = "sort", nullable = false)
    private Integer sort; // 이미지 정렬 순서

    private BugReportImage(
        Long bugReportId,
        Long uploadedFileId,
        Integer sort
    ) {
        this.bugReportId = bugReportId;
        this.uploadedFileId = uploadedFileId;
        this.sort = sort;
    }

    public static BugReportImage of(
        Long bugReportId,
        Long uploadedFileId,
        Integer sort
    ) {
        return new BugReportImage(
            bugReportId,
            uploadedFileId,
            sort
        );
    }
}
