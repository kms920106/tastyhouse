package com.tastyhouse.infrastructure.bug.persistence;

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

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 버그 신고 이미지 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code BugReportImage}와 분리된 영속 전용 엔티티다. DB 매핑만 담당하고
 * 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code BugReportImageMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "BUG_REPORT_IMAGE",
    indexes = {
        @Index(name = "idx_bug_report_image_bug_report_id", columnList = "bug_report_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private BugReportImageJpaEntity(Long bugReportId, Long imageFileId, Integer sort) {
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code BugReportImageMapper#toEntity}에서만 호출한다.
     */
    static BugReportImageJpaEntity create(Long bugReportId, Long imageFileId, Integer sort) {
        return new BugReportImageJpaEntity(bugReportId, imageFileId, sort);
    }
}
