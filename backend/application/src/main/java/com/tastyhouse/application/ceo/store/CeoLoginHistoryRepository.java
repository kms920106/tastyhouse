package com.tastyhouse.application.ceo.store;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;

public interface CeoLoginHistoryRepository {
    CeoLoginHistory save(CeoLoginHistory ceoLoginHistory);
}
