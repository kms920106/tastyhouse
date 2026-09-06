package com.tastyhouse.domain.ceo.repository;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;

public interface CeoLoginHistoryRepository {
    CeoLoginHistory save(CeoLoginHistory ceoLoginHistory);
}
