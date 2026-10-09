package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.application.notice.port.in.NoticeDeleteUseCase;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class NoticeDeleteService implements NoticeDeleteUseCase {

    private final NoticeLoadPort noticeLoadPort;
    private final NoticeSavePort noticeSavePort;

    public NoticeDeleteService(NoticeLoadPort noticeLoadPort, NoticeSavePort noticeSavePort) {
        this.noticeLoadPort = noticeLoadPort;
        this.noticeSavePort = noticeSavePort;
    }

    @Override
    public void deleteNotice(NoticeDeleteCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.delete();
        noticeSavePort.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticeLoadPort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.NOTICE_NOT_FOUND));
    }
}
