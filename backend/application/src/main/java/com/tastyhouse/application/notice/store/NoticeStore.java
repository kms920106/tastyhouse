package com.tastyhouse.application.notice.store;

import java.util.Optional;

import com.tastyhouse.application.notice.port.out.write.NoticeStatePort;
import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;

public class NoticeStore implements NoticeRepository {
    private final NoticeStatePort noticeStatePort;

    public NoticeStore(NoticeStatePort noticeStatePort) {
        this.noticeStatePort = noticeStatePort;
    }

    @Override
    public Optional<Notice> findById(NoticeId noticeId) {
        if (noticeId == null) {
            return Optional.empty();
        }
        return noticeStatePort.findById(noticeId.value()).map(NoticeStateMapper::toDomain);
    }

    @Override
    public Notice save(Notice notice) {
        return NoticeStateMapper.toDomain(noticeStatePort.save(NoticeStateMapper.toState(notice)));
    }
}
