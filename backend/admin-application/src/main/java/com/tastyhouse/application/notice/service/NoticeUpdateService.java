package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.application.notice.port.in.NoticeUpdateUseCase;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class NoticeUpdateService implements NoticeUpdateUseCase {

    private final NoticeLoadPort noticeLoadPort;
    private final NoticeSavePort noticeSavePort;

    public NoticeUpdateService(NoticeLoadPort noticeLoadPort, NoticeSavePort noticeSavePort) {
        this.noticeLoadPort = noticeLoadPort;
        this.noticeSavePort = noticeSavePort;
    }

    @Override
    public void updateNotice(NoticeUpdateCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(command.title(), command.content(), command.visible());
        noticeSavePort.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticeLoadPort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.NOTICE_NOT_FOUND));
    }
}
