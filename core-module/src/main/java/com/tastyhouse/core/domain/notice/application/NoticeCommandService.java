package com.tastyhouse.core.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeCreateCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.NoticeUpdateCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;

    public NoticeId createNotice(NoticeCreateCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticeRepository.save(notice);
        return saved.getNoticeId();
    }

    public void updateNotice(NoticeId noticeId, NoticeUpdateCommand command) {
        Notice notice = noticeRepository.findById(noticeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        notice.update(command.title(), command.content(), command.visible());
        noticeRepository.save(notice);
    }

    public void deleteNotice(NoticeId noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        notice.delete();
        noticeRepository.save(notice);
    }
}
