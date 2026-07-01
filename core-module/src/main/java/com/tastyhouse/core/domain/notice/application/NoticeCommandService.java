package com.tastyhouse.core.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.notice.application.dto.command.CreateNoticeCommand;
import com.tastyhouse.core.domain.notice.application.dto.command.UpdateNoticeCommand;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeRepository noticeRepository;

    public Long createNotice(CreateNoticeCommand command) {
        Notice notice = Notice.of(command.title(), command.content(), command.visible());
        Notice saved = noticeRepository.save(notice);
        return saved.getId();
    }

    public void updateNotice(Long id, UpdateNoticeCommand command) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        notice.update(command.title(), command.content(), command.visible());
    }

    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        notice.delete();
    }
}
