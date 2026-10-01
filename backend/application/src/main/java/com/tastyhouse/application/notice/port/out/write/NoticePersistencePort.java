package com.tastyhouse.application.notice.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;

public interface NoticePersistencePort {

    Optional<Notice> findById(NoticeId noticeId);

    Notice save(Notice notice);
}
