package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.application.ceo.port.in.CeoLoginSuccessRecordUseCase;

@Service
@Transactional
class CeoLoginSuccessRecordService implements CeoLoginSuccessRecordUseCase {

    private final CeoLoginHistoryRecorder ceoLoginHistoryRecorder;

    public CeoLoginSuccessRecordService(CeoLoginHistoryRecorder ceoLoginHistoryRecorder) {
        this.ceoLoginHistoryRecorder = ceoLoginHistoryRecorder;
    }

    @Override
    public void recordSuccess(CeoLoginHistorySuccessCommand command) {
        ceoLoginHistoryRecorder.recordSuccess(
            CeoId.of(command.ceoId()),
            command.ipAddress(),
            command.userAgent()
        );
    }
}
