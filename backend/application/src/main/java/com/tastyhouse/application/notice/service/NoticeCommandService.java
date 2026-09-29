package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.in.NoticeCommandUseCase;
import com.tastyhouse.application.notice.port.in.NoticeCreateCommand;
import com.tastyhouse.application.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.application.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.application.notice.port.out.write.NoticePersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;

@Service
@AdminApp
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticePersistencePort noticePersistencePort;

    public NoticeCommandService(NoticePersistencePort noticePersistencePort) {
        this.noticePersistencePort = noticePersistencePort;
    }

    @Override
    public Long createNotice(NoticeCreateCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticePersistencePort.save(notice);
        return saved.getNoticeId().value();
    }

    @Override
    public void updateNotice(NoticeUpdateCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(command.title(), command.content(), command.visible());
        noticePersistencePort.save(notice);
    }

    @Override
    public void deleteNotice(NoticeDeleteCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.delete();
        noticePersistencePort.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticePersistencePort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
