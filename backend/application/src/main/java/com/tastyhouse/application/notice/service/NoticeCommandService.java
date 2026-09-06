package com.tastyhouse.application.notice.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notice.port.in.NoticeCommandUseCase;
import com.tastyhouse.application.notice.port.in.NoticeCreateCommand;
import com.tastyhouse.application.notice.port.in.NoticeDeleteCommand;
import com.tastyhouse.application.notice.port.in.NoticeUpdateCommand;
import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.repository.NoticeRepository;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;

    public NoticeCommandService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public Long createNotice(NoticeCreateCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticeRepository.save(notice);
        return saved.getNoticeId().value();
    }

    @Override
    public void updateNotice(NoticeUpdateCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.update(command.title(), command.content(), command.visible());
        noticeRepository.save(notice);
    }

    @Override
    public void deleteNotice(NoticeDeleteCommand command) {
        NoticeId noticeId = NoticeId.of(command.noticeId());
        Notice notice = findNoticeOrThrow(noticeId);

        notice.delete();
        noticeRepository.save(notice);
    }

    private Notice findNoticeOrThrow(NoticeId noticeId) {
        return noticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
