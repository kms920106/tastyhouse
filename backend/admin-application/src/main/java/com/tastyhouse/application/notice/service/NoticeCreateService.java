package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.application.notice.port.in.NoticeCreateCommand;
import com.tastyhouse.application.notice.port.in.NoticeCreateUseCase;
import com.tastyhouse.application.notice.port.out.write.NoticePersistencePort;

@Service
@Transactional
class NoticeCreateService implements NoticeCreateUseCase {

    private final NoticePersistencePort noticePersistencePort;

    public NoticeCreateService(NoticePersistencePort noticePersistencePort) {
        this.noticePersistencePort = noticePersistencePort;
    }

    @Override
    public Long createNotice(NoticeCreateCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticePersistencePort.save(notice);
        return saved.getNoticeId().value();
    }
}
