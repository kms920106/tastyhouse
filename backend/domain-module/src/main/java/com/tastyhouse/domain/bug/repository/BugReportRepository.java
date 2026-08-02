package com.tastyhouse.domain.bug.repository;

import java.util.Optional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.vo.BugReportId;

/**
 * 버그 제보 write 포트.
 *
 * <p>command 경로·도메인 서비스의 트랜잭션 안에서 소비되는 CRUD만 남긴다. 목록/검색/페이징 등
 * 표현 목적 read는 infrastructure-module의 {@code BugReportQueryDao}가 담당한다(공통 지침 패턴 4).
 */
public interface BugReportRepository {

    Optional<BugReport> findById(BugReportId bugReportId);

    BugReport save(BugReport bugReport);
}
