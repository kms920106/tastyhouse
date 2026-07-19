package com.tastyhouse.core.domain.bug.domain.model;

import lombok.Getter;

@Getter
public class BugReportImage {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long bugReportId; // 버그 신고 ID (BugReport 참조)
    private final Long imageFileId; // 이미지 파일 ID (UploadedFile 참조)
    private final Integer sort; // 정렬 순서

    private BugReportImage(Long id, Long bugReportId, Long imageFileId, Integer sort) {
        this.id = id;
        this.bugReportId = bugReportId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    /**
     * 신규 버그 신고 이미지를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static BugReportImage of(Long bugReportId, Long imageFileId, Integer sort) {
        return new BugReportImage(null, bugReportId, imageFileId, sort);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static BugReportImage reconstitute(Long id, Long bugReportId, Long imageFileId, Integer sort) {
        return new BugReportImage(id, bugReportId, imageFileId, sort);
    }
}
