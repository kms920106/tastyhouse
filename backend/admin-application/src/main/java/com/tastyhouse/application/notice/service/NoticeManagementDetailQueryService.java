package com.tastyhouse.application.notice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notice.port.in.NoticeManagementDetailQueryUseCase;
import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class NoticeManagementDetailQueryService implements NoticeManagementDetailQueryUseCase {

    private final NoticeManagementQueryPort noticeManagementQueryPort;

    public NoticeManagementDetailQueryService(NoticeManagementQueryPort noticeManagementQueryPort) {
        this.noticeManagementQueryPort = noticeManagementQueryPort;
    }

    @Override
    public NoticeDetailResult getNotice(Long id) {
        return noticeManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.NOTICE_NOT_FOUND));
    }
}
