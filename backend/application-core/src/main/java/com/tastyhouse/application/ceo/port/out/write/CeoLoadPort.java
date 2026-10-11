package com.tastyhouse.application.ceo.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;

public interface CeoLoadPort {

    Optional<Ceo> findById(CeoId id);

    Optional<Ceo> findByUsername(String username);

    boolean existsByUsername(String username);
}
