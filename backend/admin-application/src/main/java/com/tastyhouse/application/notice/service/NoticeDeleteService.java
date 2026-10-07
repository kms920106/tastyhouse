package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.application.notice.port.in.NoticeDeleteUseCase;
import com.tastyhouse.application.notice.port.out.write.NoticePersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class NoticeDeleteService implements NoticeDeleteUseCase {

    private final NoticePersistencePort noticePersistencePort;

    public NoticeDeleteService(NoticePersistencePort noticePersistencePort) {
        this.noticePersistencePort = noticePersistencePort;
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
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.NOTICE_NOT_FOUND));
    }
}
