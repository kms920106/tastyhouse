package com.tastyhouse.domain.notice.repository;

import java.util.Optional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;

public interface NoticeRepository {
    Optional<Notice> findById(NoticeId noticeId);

    Notice save(Notice notice);
}
