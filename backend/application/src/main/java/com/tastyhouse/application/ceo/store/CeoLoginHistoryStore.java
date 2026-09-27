package com.tastyhouse.application.ceo.store;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryStatePort;

public class CeoLoginHistoryStore implements CeoLoginHistoryRepository {
    private final CeoLoginHistoryStatePort ceoLoginHistoryStatePort;

    public CeoLoginHistoryStore(CeoLoginHistoryStatePort ceoLoginHistoryStatePort) {
        this.ceoLoginHistoryStatePort = ceoLoginHistoryStatePort;
    }

    @Override
    public CeoLoginHistory save(CeoLoginHistory ceoLoginHistory) {
        return CeoLoginHistoryStateMapper.toDomain(
            ceoLoginHistoryStatePort.save(CeoLoginHistoryStateMapper.toState(ceoLoginHistory)));
    }
}
