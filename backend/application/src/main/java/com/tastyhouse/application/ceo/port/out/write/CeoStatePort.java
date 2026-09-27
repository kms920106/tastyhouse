package com.tastyhouse.application.ceo.port.out.write;

import java.util.Optional;

public interface CeoStatePort {
    Optional<CeoState> findById(Long id);

    Optional<CeoState> findByUsername(String username);

    boolean existsByUsername(String username);

    CeoState save(CeoState state);
}
