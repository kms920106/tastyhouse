package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.application.notice.port.in.NoticeUpdateUseCase;
import com.tastyhouse.application.notice.port.out.write.NoticePersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class NoticeUpdateService implements NoticeUpdateUseCase {

    private final NoticePersistencePort noticePersistencePort;

    public NoticeUpdateService(NoticePersistencePort noticePersistencePort) {
        this.noticePersistencePort = noticePersistencePort;
    }

    @Override
    public void updateNotice(NoticeUpdateCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(command.title(), command.content(), command.visible());
        noticePersistencePort.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticePersistencePort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.NOTICE_NOT_FOUND));
    }
}
