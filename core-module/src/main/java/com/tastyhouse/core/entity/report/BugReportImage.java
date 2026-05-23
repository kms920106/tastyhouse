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
    private Long id; // PK

    @Column(name = "bug_report_id", nullable = false)
    private Long bugReportId; // 버그 신고 ID (BUG_REPORT.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (UPLOADED_FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    private BugReportImage(
        Long bugReportId,
        Long imageFileId,
        Integer sort
    ) {
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static BugReportImage of(
        Long bugReportId,
        Long imageFileId,
        Integer sort
    ) {
        return new BugReportImage(
            bugReportId,
            imageFileId,
            sort
        );
    }
}
