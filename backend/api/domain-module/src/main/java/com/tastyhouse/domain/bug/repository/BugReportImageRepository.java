package com.tastyhouse.domain.bug.repository;

import com.tastyhouse.domain.bug.model.BugReportImage;

/**
 * 버그 제보 첨부 이미지 write 포트.
 *
 * <p>제보 등록 시 이미지를 저장하는 경로만 남긴다. 상세 화면의 첨부 목록 조회는 표현 목적 read이므로
 * infrastructure-module의 {@code BugReportQueryDao}가 담당한다(공통 지침 패턴 4).
 */
public interface BugReportImageRepository {

    BugReportImage save(BugReportImage bugReportImage);
}
