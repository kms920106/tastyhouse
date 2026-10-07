package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventManagementDetailQueryUseCase;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class EventManagementDetailQueryService implements EventManagementDetailQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementDetailQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public EventManagementDetailResult getEvent(Long id) {
        EventManagementDetailResult detail = eventManagementQueryPort.findEventDetailById(EventId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
        requireResolvedFileUrl(detail.thumbnailImageFileId(), detail.thumbnailUrl());
        requireResolvedFileUrl(detail.bannerImageFileId(), detail.bannerUrl());
        return detail;
    }

    private void requireResolvedFileUrl(Long fileId, String fileUrl) {
        if (fileId != null && fileUrl == null) {
            throw new ResourceNotFoundException(AdminErrorCode.FILE_NOT_FOUND);
        }
    }
}
