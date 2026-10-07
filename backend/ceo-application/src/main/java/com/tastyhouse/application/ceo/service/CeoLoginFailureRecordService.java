package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.application.ceo.port.in.CeoLoginFailureRecordUseCase;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryFailureCommand;

@Service
@Transactional
class CeoLoginFailureRecordService implements CeoLoginFailureRecordUseCase {

    private final CeoLoginHistoryRecorder ceoLoginHistoryRecorder;

    public CeoLoginFailureRecordService(CeoLoginHistoryRecorder ceoLoginHistoryRecorder) {
        this.ceoLoginHistoryRecorder = ceoLoginHistoryRecorder;
    }

    @Override
    public void recordFailure(CeoLoginHistoryFailureCommand command) {
        ceoLoginHistoryRecorder.recordFailure(
            CeoId.of(command.ceoId()),
            CeoLoginFailureReason.valueOf(command.failureReason()),
            command.ipAddress(),
            command.userAgent()
        );
    }
}
