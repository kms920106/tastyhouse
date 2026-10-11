package com.tastyhouse.application.ceo.port.out.write;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;

public interface CeoLoginHistorySavePort {

    CeoLoginHistory save(CeoLoginHistory ceoLoginHistory);
}
