package com.tastyhouse.application.partnership.port.out.write;

import java.util.Optional;

public interface PartnershipRequestStatePort {
    Optional<PartnershipRequestState> findById(Long id);

    PartnershipRequestState save(PartnershipRequestState state);
}
