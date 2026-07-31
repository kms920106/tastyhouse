package com.tastyhouse.domain.notice.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.notice.domain.model.Notice;
import com.tastyhouse.domain.notice.domain.vo.NoticeId;

/**
 * 공지사항 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD만 노출한다. 목록·검색·페이징 등 표현 목적 read는 이 포트가 아니라
 * infrastructure-module의 {@code notice/query/NoticeQueryDao}가 담당한다(CQRS 분리).
 */
public interface NoticeRepository {

    Optional<Notice> findById(NoticeId noticeId);

    Notice save(Notice notice);
}
