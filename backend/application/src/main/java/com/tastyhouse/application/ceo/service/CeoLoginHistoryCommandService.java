package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryFailureCommand;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.domain.ceo.vo.CeoId;

@Service
@CeoApp
@Transactional
public class CeoLoginHistoryCommandService implements CeoLoginHistoryCommandUseCase {

    private final CeoLoginHistoryRecorder ceoLoginHistoryRecorder;

    public CeoLoginHistoryCommandService(CeoLoginHistoryRecorder ceoLoginHistoryRecorder) {
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
