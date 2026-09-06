package com.tastyhouse.domain.ceo.service;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;

public class CeoLoginHistoryRecorder {
    private static final int USER_AGENT_MAX_LENGTH = 500;

    private final CeoLoginHistoryRepository ceoLoginHistoryRepository;

    public CeoLoginHistoryRecorder(CeoLoginHistoryRepository ceoLoginHistoryRepository) {
        this.ceoLoginHistoryRepository = ceoLoginHistoryRepository;
    }

    public void recordSuccess(CeoId ceoId, String ipAddress, String userAgent) {
        record(ceoId, CeoLoginResult.SUCCESS, null, ipAddress, userAgent);
    }

    public void recordFailure(
        CeoId ceoId,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        record(ceoId, CeoLoginResult.FAILURE, failureReason, ipAddress, userAgent);
    }

    private void record(
        CeoId ceoId,
        CeoLoginResult result,
        CeoLoginFailureReason failureReason,
        String ipAddress,
        String userAgent
    ) {
        CeoLoginHistory history = CeoLoginHistory.of(
            ceoId,
            result,
            failureReason,
            ipAddress,
            truncateUserAgent(userAgent)
        );
        ceoLoginHistoryRepository.save(history);
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() <= USER_AGENT_MAX_LENGTH) {
            return userAgent;
        }
        return userAgent.substring(0, USER_AGENT_MAX_LENGTH);
    }
}
